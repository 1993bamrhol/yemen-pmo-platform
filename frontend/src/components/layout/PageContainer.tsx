import type { HTMLAttributes, ReactNode } from "react";

import { classNames } from "@/components/ui/classNames";

import styles from "./Layout.module.css";

export const PUBLIC_MAIN_CONTENT_ID = "main-content";

export interface PageContainerProps extends HTMLAttributes<HTMLElement> {
  as?: "div" | "main";
  children: ReactNode;
}

export function PageContainer({
  as: Component = "div",
  children,
  className,
  id,
  tabIndex,
  ...props
}: PageContainerProps) {
  const isMain = Component === "main";

  return (
    <Component
      {...props}
      className={classNames(styles.container, className)}
      id={id ?? (isMain ? PUBLIC_MAIN_CONTENT_ID : undefined)}
      tabIndex={tabIndex ?? (isMain ? -1 : undefined)}
    >
      {children}
    </Component>
  );
}
