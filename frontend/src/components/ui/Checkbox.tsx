import type { InputHTMLAttributes, ReactNode } from "react";

import styles from "./Choice.module.css";
import { classNames } from "./classNames";
import { FieldMessage, getFieldDescriptionId } from "./fieldA11y";

export interface CheckboxProps
  extends Omit<InputHTMLAttributes<HTMLInputElement>, "id" | "type"> {
  containerClassName?: string;
  error?: string;
  helperText?: ReactNode;
  id: string;
  label: ReactNode;
}

export function Checkbox({
  "aria-describedby": ariaDescribedBy,
  className,
  containerClassName,
  disabled,
  error,
  helperText,
  id,
  label,
  ...props
}: CheckboxProps) {
  const descriptionId = getFieldDescriptionId(id, helperText, error, ariaDescribedBy);

  return (
    <div
      className={classNames(styles.choiceGroup, containerClassName)}
      data-invalid={Boolean(error) || undefined}
    >
      <label
        className={classNames(styles.choice, className)}
        data-disabled={disabled || undefined}
        data-invalid={Boolean(error) || undefined}
        htmlFor={id}
      >
        <input
          {...props}
          aria-describedby={descriptionId}
          aria-invalid={error ? true : undefined}
          className={styles.nativeControl}
          disabled={disabled}
          id={id}
          type="checkbox"
        />
        <span className={styles.label}>{label}</span>
      </label>
      <FieldMessage
        className={styles.message}
        error={error}
        helperText={helperText}
        id={id}
      />
    </div>
  );
}
