import type { HTMLAttributes, ReactNode } from "react";

import { classNames } from "@/components/ui/classNames";

import { PageContainer } from "./PageContainer";
import styles from "./Layout.module.css";

export interface SectionProps extends HTMLAttributes<HTMLElement> {
  children: ReactNode;
  contained?: boolean;
  spacing?: "compact" | "default" | "roomy";
  tone?: "default" | "subtle";
}
export function Section({
  children,
  className,
  contained = true,
  spacing = "default",
  tone = "default",
  ...props
}: SectionProps) {
  const content = contained ? <PageContainer>{children}</PageContainer> : children;

  return (
    <section
      {...props}
      className={classNames(styles.section, className)}
      data-spacing={spacing}
      data-tone={tone}
    >
      {content}
    </section>
  );
}
