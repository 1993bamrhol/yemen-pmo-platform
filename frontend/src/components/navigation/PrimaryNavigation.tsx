"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

import { classNames } from "@/components/ui/classNames";

import {
  isCurrentNavigationItem,
  PUBLIC_NAVIGATION_ITEMS,
  type PublicNavigationItem,
} from "./navigation";
import styles from "./PublicNavigation.module.css";

export interface PrimaryNavigationProps {
  className?: string;
  id?: string;
  items?: readonly PublicNavigationItem[];
  label?: string;
  onNavigate?: () => void;
  variant?: "desktop" | "drawer";
}
export function PrimaryNavigation({
  className,
  id,
  items = PUBLIC_NAVIGATION_ITEMS,
  label = "التنقل الرئيسي",
  onNavigate,
  variant = "desktop",
}: PrimaryNavigationProps) {
  const pathname = usePathname();

  return (
    <nav className={classNames(styles.navigation, className)} id={id} aria-label={label}>
      <ul className={styles.list} data-variant={variant}>
        {items.map((item) => {
          const current = isCurrentNavigationItem(pathname, item.href);

          return (
            <li key={item.href}>
              <Link
                aria-current={current ? "page" : undefined}
                className={styles.link}
                href={item.href}
                onClick={onNavigate}
              >
                {item.label}
              </Link>
            </li>
          );
        })}
      </ul>
    </nav>
  );
}
