import Link, { type LinkProps } from "next/link";
import type { ReactNode } from "react";

import styles from "./Navigation.module.css";

export interface PaginationItem {
  "aria-label"?: string;
  current?: boolean;
  disabled?: boolean;
  href?: LinkProps["href"];
  label: ReactNode;
}

export interface PaginationProps {
  "aria-label"?: string;
  items: PaginationItem[];
}

export function Pagination({
  "aria-label": ariaLabel = "ترقيم الصفحات",
  items,
}: PaginationProps) {
  return (
    <nav aria-label={ariaLabel} className={styles.pagination}>
      <ol className={styles.paginationList}>
        {items.map((item, index) => {
          const key = `${index}-${typeof item.label === "string" || typeof item.label === "number" ? item.label : "page"}`;

          return (
            <li key={key}>
              {item.current ? (
                <span
                  aria-current="page"
                  aria-label={item["aria-label"]}
                  className={styles.pageCurrent}
                >
                  {item.label}
                </span>
              ) : item.disabled ? (
                <span
                  aria-disabled="true"
                  aria-label={item["aria-label"]}
                  className={styles.pageDisabled}
                >
                  {item.label}
                </span>
              ) : item.href ? (
                <Link
                  aria-label={item["aria-label"]}
                  className={styles.pageLink}
                  href={item.href}
                >
                  {item.label}
                </Link>
              ) : (
                <span aria-label={item["aria-label"]} className={styles.pageStatic}>
                  {item.label}
                </span>
              )}
            </li>
          );
        })}
      </ol>
    </nav>
  );
}
