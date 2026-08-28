import type { LinkProps } from "next/link";

import { LinkedContentCard } from "./LinkedContentCard";
import { MetadataList, type MetadataItem } from "./MetadataList";

export interface GovernmentEntityCardProps {
  actionLabel?: string;
  className?: string;
  entityType?: string;
  href?: LinkProps["href"];
  name: string;
  summary?: string;
}

export function GovernmentEntityCard({
  actionLabel = "عرض الجهة",
  className,
  entityType,
  href,
  name,
  summary,
}: GovernmentEntityCardProps) {
  const metadata: MetadataItem[] = [{ label: entityType }];

  return (
    <LinkedContentCard
      actionLabel={actionLabel}
      className={className}
      description={summary}
      href={href}
      icon="entity"
      metadata={<MetadataList ariaLabel="بيانات الجهة" items={metadata} />}
      title={name}
    />
  );
}
