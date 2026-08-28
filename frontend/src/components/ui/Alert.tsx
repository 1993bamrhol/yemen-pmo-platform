import type { HTMLAttributes, ReactNode } from "react";

import styles from "./Feedback.module.css";
import type { FeedbackTone } from "./Badge";
import { classNames } from "./classNames";

export interface AlertProps
  extends Omit<HTMLAttributes<HTMLDivElement>, "role" | "title"> {
  action?: ReactNode;
  children: ReactNode;
  icon: ReactNode;
  live?: "off" | "polite" | "assertive";
  title: ReactNode;
  tone?: FeedbackTone;
}

export function Alert({
  action,
  children,
  className,
  icon,
  live = "off",
  title,
  tone = "info",
  ...props
}: AlertProps) {
  const role = live === "assertive" ? "alert" : live === "polite" ? "status" : undefined;

  return (
    <div
      {...props}
      aria-atomic={live === "off" ? undefined : true}
      aria-live={live === "off" ? undefined : live}
      className={classNames(
        styles.alert,
        tone !== "neutral" && styles[tone],
        className,
      )}
      role={role}
    >
      <span aria-hidden="true" className={styles.iconSlot}>
        {icon}
      </span>
      <div className={styles.alertContent}>
        <p className={styles.alertTitle}>{title}</p>
        <div className={styles.alertBody}>{children}</div>
      </div>
      {action && <div className={styles.alertAction}>{action}</div>}
    </div>
  );
}
