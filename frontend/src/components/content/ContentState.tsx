import type { ReactNode } from "react";

import { Alert } from "@/components/ui";
import { classNames } from "@/components/ui/classNames";

import { CardGrid } from "./CardGrid";
import { CardIcon } from "./CardIcon";
import styles from "./ContentComposition.module.css";

interface ContentLoadingStateProps {
  className?: string;
  itemCount?: number;
  label?: string;
  state: "loading";
}

interface ContentMessageStateProps {
  action?: ReactNode;
  className?: string;
  description: ReactNode;
  live?: "off" | "polite" | "assertive";
  state: "empty" | "error";
  title: ReactNode;
}

export type ContentStateProps = ContentLoadingStateProps | ContentMessageStateProps;

function SkeletonCard() {
  return (
    <div className={classNames(styles.card, styles.skeletonCard)}>
      <div className={styles.skeletonMeta}>
        <span className={styles.skeletonIcon} />
        <span className={classNames(styles.skeletonLine, styles.skeletonLineShort)} />
      </div>
      <span className={classNames(styles.skeletonLine, styles.skeletonLineMedium)} />
      <span className={styles.skeletonLine} />
      <span className={classNames(styles.skeletonLine, styles.skeletonLineShort)} />
    </div>
  );
}

export function ContentState(props: ContentStateProps) {
  if (props.state === "loading") {
    const itemCount = Math.min(Math.max(props.itemCount ?? 3, 1), 6);

    return (
      <div
        aria-atomic="true"
        aria-busy="true"
        aria-live="polite"
        className={classNames(styles.state, styles.loadingState, props.className)}
        role="status"
      >
        <span className={styles.visuallyHidden}>{props.label ?? "جارٍ تحميل المحتوى"}</span>
        <CardGrid aria-hidden="true">
          {Array.from({ length: itemCount }, (_, index) => (
            <SkeletonCard key={index} />
          ))}
        </CardGrid>
      </div>
    );
  }

  const isError = props.state === "error";

  return (
    <div className={classNames(styles.state, props.className)}>
      <Alert
        action={props.action}
        icon={<CardIcon name={isError ? "error" : "info"} status />}
        live={props.live ?? (isError ? "assertive" : "polite")}
        title={props.title}
        tone={isError ? "error" : "info"}
      >
        {props.description}
      </Alert>
    </div>
  );
}
