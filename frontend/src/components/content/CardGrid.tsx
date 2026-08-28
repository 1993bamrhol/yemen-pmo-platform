import { Children, type HTMLAttributes, type ReactNode } from "react";

import { classNames } from "@/components/ui/classNames";

import styles from "./ContentComposition.module.css";

export interface CardGridProps
  extends Omit<HTMLAttributes<HTMLUListElement>, "children"> {
  children: ReactNode;
}

export function CardGrid({ children, className, ...props }: CardGridProps) {
  return (
    <ul {...props} className={classNames(styles.cardGrid, className)}>
      {Children.map(children, (child) => (
        <li className={styles.cardGridItem}>{child}</li>
      ))}
    </ul>
  );
}
