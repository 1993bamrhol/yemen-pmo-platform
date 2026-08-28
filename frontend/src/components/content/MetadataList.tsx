import type { ReactNode } from "react";

import styles from "./ContentComposition.module.css";

export interface MetadataItem {
  dateTime?: string;
  label: ReactNode;
}

export interface MetadataListProps {
  ariaLabel?: string;
  items: readonly MetadataItem[];
}

export function MetadataList({
  ariaLabel = "بيانات العنصر",
  items,
}: MetadataListProps) {
  const visibleItems = items.filter((item) => item.label !== null && item.label !== undefined);

  if (visibleItems.length === 0) {
    return null;
  }

  return (
    <ul aria-label={ariaLabel} className={styles.metadata}>
      {visibleItems.map((item, index) => (
        <li className={styles.metadataItem} key={`${item.dateTime ?? "meta"}-${index}`}>
          {item.dateTime ? (
            <time dateTime={item.dateTime}>{item.label}</time>
          ) : (
            item.label
          )}
        </li>
      ))}
    </ul>
  );
}
