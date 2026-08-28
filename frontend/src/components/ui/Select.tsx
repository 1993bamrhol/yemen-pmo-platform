import type { ReactNode, SelectHTMLAttributes } from "react";

import styles from "./Field.module.css";
import { classNames } from "./classNames";
import { FieldMessage, getFieldDescriptionId } from "./fieldA11y";

export interface SelectProps
  extends Omit<SelectHTMLAttributes<HTMLSelectElement>, "id"> {
  containerClassName?: string;
  error?: string;
  helperText?: ReactNode;
  id: string;
  label: ReactNode;
  loading?: boolean;
}

export function Select({
  "aria-describedby": ariaDescribedBy,
  children,
  className,
  containerClassName,
  disabled,
  error,
  helperText,
  id,
  label,
  loading = false,
  ...props
}: SelectProps) {
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
        <select
          {...props}
          aria-busy={loading || undefined}
          aria-describedby={descriptionId}
          aria-invalid={error ? true : undefined}
          className={classNames(
            styles.select,
            styles.withAdornment,
            className,
          )}
          data-loading={loading || undefined}
          disabled={disabled || loading}
          id={id}
        >
          {children}
        </select>
        <span aria-hidden="true" className={styles.adornment}>
          <span className={loading ? styles.spinner : styles.chevronIcon} />
        </span>
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
