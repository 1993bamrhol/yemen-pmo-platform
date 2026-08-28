import type { ReactNode } from "react";

import { classNames } from "@/components/ui/classNames";

import styles from "./Layout.module.css";

export interface SectionHeaderProps {
  action?: ReactNode;
  className?: string;
  description?: ReactNode;
  eyebrow?: ReactNode;
  headingId?: string;
  headingLevel?: 2 | 3 | 4;
  title: ReactNode;
}
export function SectionHeader({
  action,
  className,
  description,
  eyebrow,
  headingId,
  headingLevel = 2,
  title,
}: SectionHeaderProps) {
  const Heading = `h${headingLevel}` as "h2" | "h3" | "h4";

  return (
    <header className={classNames(styles.sectionHeader, className)}>
      <div className={styles.sectionHeaderCopy}>
        {eyebrow ? <p className={styles.eyebrow}>{eyebrow}</p> : null}
        <Heading className={styles.heading} id={headingId}>
          {title}
        </Heading>
        {description ? <p className={styles.description}>{description}</p> : null}
      </div>
      {action ? <div className={styles.action}>{action}</div> : null}
    </header>
  );
}
