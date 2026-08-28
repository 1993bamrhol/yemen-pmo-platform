import type { ButtonHTMLAttributes, ReactNode } from "react";

import styles from "./Action.module.css";
import { classNames } from "./classNames";

export type ButtonVariant = "primary" | "secondary" | "ghost";

export interface ButtonProps
  extends ButtonHTMLAttributes<HTMLButtonElement> {
  icon?: ReactNode;
  iconPosition?: "start" | "end";
  loading?: boolean;
  loadingLabel?: string;
  variant?: ButtonVariant;
}

export function Button({
  children,
  className,
  disabled,
  icon,
  iconPosition = "end",
  loading = false,
  loadingLabel = "جارٍ التنفيذ",
  type = "button",
  variant = "primary",
  ...props
}: ButtonProps) {
  const content = loading ? loadingLabel : children;

  return (
    <button
      {...props}
      aria-busy={loading || undefined}
      className={classNames(styles.button, styles[variant], className)}
      data-loading={loading || undefined}
      disabled={disabled || loading}
      type={type}
    >
      {loading ? (
        <span aria-hidden="true" className={styles.spinner} />
      ) : (
        iconPosition === "start" && icon && (
          <span aria-hidden="true" className={styles.icon}>
            {icon}
          </span>
        )
      )}
      <span className={styles.content}>{content}</span>
      {!loading && iconPosition === "end" && icon && (
        <span aria-hidden="true" className={styles.icon}>
          {icon}
        </span>
      )}
    </button>
  );
}
