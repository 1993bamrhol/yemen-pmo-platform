import type { HTMLAttributes, ReactNode } from "react";

import styles from "./Feedback.module.css";
import { classNames } from "./classNames";

export type FeedbackTone = "neutral" | "info" | "success" | "warning" | "error";

export interface BadgeProps extends HTMLAttributes<HTMLSpanElement> {
  children: ReactNode;
  emphasis?: "subtle" | "outline";
  tone?: FeedbackTone;
}

export function Badge({
  children,
  className,
  emphasis = "subtle",
  tone = "neutral",
  ...props
}: BadgeProps) {
  return (
    <span
      {...props}
      className={classNames(
        styles.badge,
        emphasis === "outline" && styles.outline,
        tone !== "neutral" && styles[tone],
        className,
      )}
    >
      {children}
    </span>
  );
}
