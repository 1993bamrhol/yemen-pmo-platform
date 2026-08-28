import Link, { type LinkProps } from "next/link";
import type { ReactNode } from "react";

import styles from "./Navigation.module.css";

export interface BreadcrumbItem {
  current?: boolean;
  disabled?: boolean;
  href?: LinkProps["href"];
  label: ReactNode;
}

export interface BreadcrumbsProps {
  "aria-label"?: string;
  items: BreadcrumbItem[];
  separator?: ReactNode;
}

export function Breadcrumbs({
  "aria-label": ariaLabel = "مسار التنقل",
  items,
  separator,
}: BreadcrumbsProps) {
  return (
    <nav aria-label={ariaLabel} className={styles.breadcrumb}>
      <ol className={styles.breadcrumbList}>
        {items.map((item, index) => {
          const isCurrent = item.current ?? index === items.length - 1;
          const key = `${index}-${typeof item.label === "string" ? item.label : "breadcrumb"}`;

          return (
            <li className={styles.breadcrumbItem} key={key}>
              {item.disabled ? (
                <span aria-disabled="true" className={styles.breadcrumbDisabled}>
                  {item.label}
                </span>
              ) : isCurrent ? (
                <span aria-current="page" className={styles.breadcrumbCurrent}>
                  {item.label}
                </span>
              ) : item.href ? (
                <Link className={styles.breadcrumbLink} href={item.href}>
                  {item.label}
                </Link>
              ) : (
                <span className={styles.breadcrumbCurrent}>{item.label}</span>
              )}
              {index < items.length - 1 && (
                <span aria-hidden="true" className={styles.separator}>
                  {separator ?? <span className={styles.separatorIcon} />}
                </span>
              )}
            </li>
          );
        })}
      </ol>
    </nav>
  );
}
