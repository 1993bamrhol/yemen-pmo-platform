import Link, { type LinkProps } from "next/link";
import type { AnchorHTMLAttributes, ReactNode } from "react";

import styles from "./Action.module.css";
import { classNames } from "./classNames";

export interface TextLinkProps
  extends LinkProps,
    Omit<AnchorHTMLAttributes<HTMLAnchorElement>, keyof LinkProps | "href"> {
  children: ReactNode;
  icon?: ReactNode;
  iconPosition?: "start" | "end";
  tone?: "default" | "muted" | "inverse";
}

const toneClasses = {
  default: undefined,
  muted: styles.mutedLink,
  inverse: styles.inverseLink,
};

export function TextLink({
  children,
  className,
  icon,
  iconPosition = "end",
  tone = "default",
  ...props
}: TextLinkProps) {
  return (
    <Link
      {...props}
      className={classNames(styles.textLink, toneClasses[tone], className)}
    >
      {iconPosition === "start" && icon && (
        <span aria-hidden="true" className={styles.icon}>
          {icon}
        </span>
      )}
      <span className={styles.content}>{children}</span>
      {iconPosition === "end" && icon && (
        <span aria-hidden="true" className={styles.icon}>
          {icon}
        </span>
      )}
    </Link>
  );
}
