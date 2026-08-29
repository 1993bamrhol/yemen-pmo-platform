package ye.gov.pmo.bootstrap.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import ye.gov.pmo.identity.exception.ResourceConflictException;
import ye.gov.pmo.identity.exception.ResourceNotFoundException;
import ye.gov.pmo.shared.web.ApiErrorResponse;
import ye.gov.pmo.shared.web.ApiFieldError;
import ye.gov.pmo.shared.web.ApiV1;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(annotations = ApiV1.class)
public class ApiV1ExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiV1ExceptionHandler.class);
    private static final String VALIDATION_MESSAGE =
            "تعذر قبول الطلب. تحقق من القيم المرسلة.";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        return fieldValidationResponse(
                exception.getBindingResult().getFieldErrors(), request.getRequestURI());
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiErrorResponse> handleBind(
            BindException exception, HttpServletRequest request) {
        return fieldValidationResponse(
                exception.getBindingResult().getFieldErrors(), request.getRequestURI());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception, HttpServletRequest request) {
        List<ApiFieldError> details = exception.getConstraintViolations().stream()
                .map(violation -> new ApiFieldError(
                        fieldIdentifier(violation.getPropertyPath().toString()),
                        "INVALID",
                        "قيمة الحقل غير صالحة."))
                .sorted(Comparator.comparing(ApiFieldError::field))
                .toList();
        return validationResponse(details, request.getRequestURI());
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleHandlerMethodValidation(
            HandlerMethodValidationException exception, HttpServletRequest request) {
        List<ApiFieldError> details = exception.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(error -> new ApiFieldError(
                                fieldIdentifier(result.getMethodParameter().getParameterName()),
                                reason(validationCode(error.getCodes())),
                                safeValidationMessage(validationCode(error.getCodes())))))
                .sorted(Comparator.comparing(ApiFieldError::field,
                        Comparator.nullsLast(String::compareTo)))
                .toList();
        return validationResponse(details, request.getRequestURI());
    }

    @ExceptionHandler({
        HttpMessageNotReadableException.class,
        MethodArgumentTypeMismatchException.class,
        MissingServletRequestParameterException.class
    })
    public ResponseEntity<ApiErrorResponse> handleMalformedRequest(
            Exception exception, HttpServletRequest request) {
        return validationResponse(List.of(), request.getRequestURI());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthentication(
            AuthenticationException exception, HttpServletRequest request) {
        return errorResponse(HttpStatus.UNAUTHORIZED, request.getRequestURI());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException exception, HttpServletRequest request) {
        return errorResponse(HttpStatus.FORBIDDEN, request.getRequestURI());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(
            ResponseStatusException exception, HttpServletRequest request) {
        return errorResponse(exception.getStatusCode(), request.getRequestURI());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(
            ResourceNotFoundException exception, HttpServletRequest request) {
        return errorResponse(HttpStatus.NOT_FOUND, request.getRequestURI());
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceConflict(
            ResourceConflictException exception, HttpServletRequest request) {
        return errorResponse(HttpStatus.CONFLICT, request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(
            Exception exception, HttpServletRequest request) {
        ResponseStatus responseStatus = AnnotatedElementUtils.findMergedAnnotation(
                exception.getClass(), ResponseStatus.class);
        if (responseStatus != null) {
            HttpStatus status = responseStatus.code();
            return errorResponse(status, request.getRequestURI());
        }

        LOGGER.error("Unhandled API v1 exception for path {}", request.getRequestURI(), exception);
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, request.getRequestURI());
    }

    private ResponseEntity<ApiErrorResponse> fieldValidationResponse(
            List<FieldError> fieldErrors, String path) {
        List<ApiFieldError> details = fieldErrors.stream()
                .map(fieldError -> new ApiFieldError(
                        fieldError.getField(),
                        reason(fieldError.getCode()),
                        safeValidationMessage(fieldError.getCode())))
                .sorted(Comparator.comparing(ApiFieldError::field))
                .toList();
        return validationResponse(details, path);
    }

    private ResponseEntity<ApiErrorResponse> validationResponse(
            List<ApiFieldError> details, String path) {
        List<ApiFieldError> responseDetails = details.isEmpty() ? null : details;
        return ResponseEntity.badRequest().body(ApiErrorResponse.validation(
                VALIDATION_MESSAGE, HttpStatus.BAD_REQUEST.value(), path, responseDetails));
    }

    private ResponseEntity<ApiErrorResponse> errorResponse(HttpStatusCode status, String path) {
        ErrorDescriptor descriptor = describe(status.value());
        ApiErrorResponse response = ApiErrorResponse.of(
                descriptor.code(), descriptor.message(), status.value(), path);
        return ResponseEntity.status(status).body(response);
    }

    private ErrorDescriptor describe(int status) {
        return switch (status) {
            case 400 -> new ErrorDescriptor("VALIDATION_ERROR", VALIDATION_MESSAGE);
            case 401 -> new ErrorDescriptor(
                    "UNAUTHORIZED", "المصادقة مطلوبة للوصول إلى هذا المورد.");
            case 403 -> new ErrorDescriptor(
                    "FORBIDDEN", "لا تملك صلاحية الوصول إلى هذا المورد.");
            case 404 -> new ErrorDescriptor(
                    "RESOURCE_NOT_FOUND", "المورد المطلوب غير موجود.");
            case 409 -> new ErrorDescriptor(
                    "CONFLICT", "يتعارض الطلب مع الحالة الحالية للمورد.");
            case 422 -> new ErrorDescriptor(
                    "UNPROCESSABLE_ENTITY", "تعذر معالجة الطلب بالحالة الحالية.");
            default -> status >= 500
                    ? new ErrorDescriptor("INTERNAL_ERROR", "حدث خطأ داخلي غير متوقع.")
                    : new ErrorDescriptor("REQUEST_ERROR", "تعذر تنفيذ الطلب.");
        };
    }

    private static String reason(String validationCode) {
        if (validationCode == null) {
            return "INVALID";
        }
        return switch (validationCode) {
            case "NotBlank" -> "NOT_BLANK";
            case "NotNull" -> "NOT_NULL";
            case "Size" -> "SIZE";
            case "Pattern" -> "PATTERN";
            case "Email" -> "EMAIL";
            case "Min" -> "MIN";
            case "Max" -> "MAX";
            default -> "INVALID";
        };
    }

    private static String validationCode(String[] validationCodes) {
        return validationCodes == null || validationCodes.length == 0
                ? null
                : validationCodes[validationCodes.length - 1];
    }

    private static String fieldIdentifier(String propertyPath) {
        if (propertyPath == null || propertyPath.isBlank()) {
            return "request";
        }
        int lastSeparator = propertyPath.lastIndexOf('.');
        return lastSeparator < 0 ? propertyPath : propertyPath.substring(lastSeparator + 1);
    }

    private static String safeValidationMessage(String validationCode) {
        return switch (reason(validationCode)) {
            case "NOT_BLANK" -> "يجب ألا يكون الحقل فارغًا.";
            case "NOT_NULL" -> "الحقل مطلوب.";
            case "SIZE" -> "طول الحقل غير صالح.";
            case "PATTERN" -> "صيغة الحقل غير صالحة.";
            case "EMAIL" -> "صيغة البريد الإلكتروني غير صالحة.";
            case "MIN", "MAX" -> "قيمة الحقل خارج النطاق المسموح.";
            default -> "قيمة الحقل غير صالحة.";
        };
    }

    private record ErrorDescriptor(String code, String message) {
    }
}
