import type { ReactNode, TextareaHTMLAttributes } from "react";

import styles from "./Field.module.css";
import { classNames } from "./classNames";
import { FieldMessage, getFieldDescriptionId } from "./fieldA11y";

export interface TextareaProps
  extends Omit<TextareaHTMLAttributes<HTMLTextAreaElement>, "id"> {
  containerClassName?: string;
  error?: string;
  helperText?: ReactNode;
  id: string;
  label: ReactNode;
  loading?: boolean;
}

export function Textarea({
  "aria-describedby": ariaDescribedBy,
  className,
  containerClassName,
  error,
  helperText,
  id,
  label,
  loading = false,
  readOnly,
  ...props
}: TextareaProps) {
  const descriptionId = getFieldDescriptionId(
    id,
    helperText,
    error,
    ariaDescribedBy,
  );

  return (
    <div
      className={classNames(styles.field, containerClassName)}
      data-invalid={Boolean(error) || undefined}
    >
      <label className={styles.label} htmlFor={id}>
        {label}
      </label>
      <div className={styles.controlWrap}>
        <textarea
          {...props}
          aria-busy={loading || undefined}
          aria-describedby={descriptionId}
          aria-invalid={error ? true : undefined}
          className={classNames(
            styles.textarea,
            loading && styles.withAdornment,
            className,
          )}
          id={id}
          readOnly={loading || readOnly}
        />
        {loading && (
          <span aria-hidden="true" className={styles.adornment}>
            <span className={styles.spinner} />
          </span>
        )}
      </div>
      <FieldMessage
        className={styles.message}
        error={error}
        helperText={helperText}
        id={id}
      />
    </div>
  );
}
