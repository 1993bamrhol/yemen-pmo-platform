import type { ButtonHTMLAttributes, ReactNode } from "react";

import styles from "./Action.module.css";
import type { ButtonVariant } from "./Button";
import { classNames } from "./classNames";

export interface IconButtonProps
  extends Omit<ButtonHTMLAttributes<HTMLButtonElement>, "aria-label"> {
  "aria-label": string;
  children: ReactNode;
  loading?: boolean;
  variant?: ButtonVariant;
}

export function IconButton({
  "aria-label": ariaLabel,
  children,
  className,
  disabled,
  loading = false,
  type = "button",
  variant = "secondary",
  ...props
}: IconButtonProps) {
  return (
    <button
      {...props}
      aria-busy={loading || undefined}
      aria-label={ariaLabel}
      className={classNames(styles.iconButton, styles[variant], className)}
      data-loading={loading || undefined}
      disabled={disabled || loading}
      type={type}
    >
      {loading ? (
        <span aria-hidden="true" className={styles.spinner} />
      ) : (
        <span aria-hidden="true" className={styles.icon}>
          {children}
        </span>
      )}
    </button>
  );
}
