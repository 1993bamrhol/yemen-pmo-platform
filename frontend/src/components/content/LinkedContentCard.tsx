import Link, { type LinkProps } from "next/link";
import type { ReactNode } from "react";

import { classNames } from "@/components/ui/classNames";

import { CardIcon, type ContentIconName } from "./CardIcon";
import styles from "./ContentComposition.module.css";

interface LinkedContentCardProps {
  actionLabel: string;
  className?: string;
  description?: ReactNode;
  href?: LinkProps["href"];
  icon: ContentIconName;
  metadata?: ReactNode;
  title: ReactNode;
}

function CardBody({
  actionLabel,
  description,
  href,
  icon,
  metadata,
  title,
}: Omit<LinkedContentCardProps, "className">) {
  return (
    <>
      <div className={styles.cardMetaRow}>
        <CardIcon name={icon} />
        {metadata}
      </div>
      <div className={styles.cardCopy}>
        <h3 className={styles.cardTitle}>{title}</h3>
        {description ? <p className={styles.cardDescription}>{description}</p> : null}
      </div>
      {href ? (
        <span className={styles.cardAction}>
          <span>{actionLabel}</span>
          <span aria-hidden="true" className={styles.actionIcon} />
        </span>
      ) : null}
    </>
  );
}

export function LinkedContentCard(props: LinkedContentCardProps) {
  const { className, href } = props;

  if (href) {
    return (
      <Link className={classNames(styles.card, styles.cardLink, className)} href={href}>
        <CardBody {...props} />
      </Link>
    );
  }

  return (
    <article className={classNames(styles.card, className)}>
      <CardBody {...props} />
    </article>
  );
}
