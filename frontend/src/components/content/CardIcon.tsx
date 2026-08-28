import { classNames } from "@/components/ui/classNames";

import styles from "./ContentComposition.module.css";

export type ContentIconName = "content" | "entity" | "error" | "info" | "service";

const iconClasses: Record<ContentIconName, string> = {
  content: styles.contentIcon,
  entity: styles.entityIcon,
  error: styles.errorIcon,
  info: styles.infoIcon,
  service: styles.serviceIcon,
};

export function CardIcon({
  name,
  status = false,
}: {
  name: ContentIconName;
  status?: boolean;
}) {
  return (
    <span
      aria-hidden="true"
      className={classNames(
        status ? styles.statusIcon : styles.cardIcon,
        iconClasses[name],
      )}
    />
  );
}
