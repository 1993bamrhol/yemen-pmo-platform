import type { LinkProps } from "next/link";

import { LinkedContentCard } from "./LinkedContentCard";
import { MetadataList, type MetadataItem } from "./MetadataList";

export interface ServiceCardProps {
  actionLabel?: string;
  category?: string;
  className?: string;
  description?: string;
  href?: LinkProps["href"];
  providerName?: string;
  title: string;
}

export function ServiceCard({
  actionLabel = "بدء الخدمة",
  category,
  className,
  description,
  href,
  providerName,
  title,
}: ServiceCardProps) {
  const metadata: MetadataItem[] = [
    { label: category },
    { label: providerName },
  ];

  return (
    <LinkedContentCard
      actionLabel={actionLabel}
      className={className}
      description={description}
      href={href}
      icon="service"
      metadata={<MetadataList ariaLabel="بيانات الخدمة" items={metadata} />}
      title={title}
    />
  );
}
