import type { InputHTMLAttributes, ReactNode } from "react";

import styles from "./Field.module.css";
import { classNames } from "./classNames";
import { FieldMessage, getFieldDescriptionId } from "./fieldA11y";

export interface InputProps
  extends Omit<InputHTMLAttributes<HTMLInputElement>, "id"> {
  containerClassName?: string;
  endAdornment?: ReactNode;
  error?: string;
  helperText?: ReactNode;
  id: string;
  label: ReactNode;
  loading?: boolean;
}

export function Input({
  "aria-describedby": ariaDescribedBy,
  className,
  containerClassName,
  disabled,
  endAdornment,
  error,
  helperText,
  id,
  label,
  loading = false,
  readOnly,
  ...props
}: InputProps) {
  const descriptionId = getFieldDescriptionId(
    id,
    helperText,
    error,
    ariaDescribedBy,
  );
  const hasAdornment = loading || Boolean(endAdornment);

  return (
    <div
      className={classNames(styles.field, containerClassName)}
      data-invalid={Boolean(error) || undefined}
    >
      <label className={styles.label} htmlFor={id}>
        {label}
      </label>
      <div className={styles.controlWrap}>
        <input
          {...props}
          aria-busy={loading || undefined}
          aria-describedby={descriptionId}
          aria-invalid={error ? true : undefined}
          className={classNames(
            styles.control,
            hasAdornment && styles.withAdornment,
            className,
          )}
          disabled={disabled}
          id={id}
          readOnly={loading || readOnly}
        />
        {hasAdornment && (
          <span aria-hidden="true" className={styles.adornment}>
            {loading ? <span className={styles.spinner} /> : endAdornment}
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
