import type { AnchorHTMLAttributes } from "react";

import { classNames } from "@/components/ui/classNames";

import { PUBLIC_MAIN_CONTENT_ID } from "./PageContainer";
import styles from "./Layout.module.css";

export interface SkipLinkProps extends AnchorHTMLAttributes<HTMLAnchorElement> {
  href?: `#${string}`;
}

export function SkipLink({
  children = "تجاوز إلى المحتوى الرئيسي",
  className,
  href = `#${PUBLIC_MAIN_CONTENT_ID}`,
  ...props
}: SkipLinkProps) {
  return (
    <a {...props} className={classNames(styles.skipLink, className)} href={href}>
      {children}
    </a>
  );
}
