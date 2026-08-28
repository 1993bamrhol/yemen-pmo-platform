import type { LinkProps } from "next/link";

import { LinkedContentCard } from "./LinkedContentCard";
import { MetadataList, type MetadataItem } from "./MetadataList";

export type PublicContentType = "ANNOUNCEMENT" | "DECISION" | "DOCUMENT" | "NEWS";

const contentTypeLabels: Record<PublicContentType, string> = {
  ANNOUNCEMENT: "إعلان",
  DECISION: "قرار",
  DOCUMENT: "وثيقة",
  NEWS: "خبر",
};

export interface ContentCardProps {
  actionLabel?: string;
  className?: string;
  contentType: PublicContentType;
  contentTypeLabel?: string;
  href?: LinkProps["href"];
  publishedAt?: {
    dateTime: string;
    label: string;
  };
  source?: string;
  summary?: string;
  title: string;
}

export function ContentCard({
  actionLabel = "قراءة المحتوى",
  className,
  contentType,
  contentTypeLabel,
  href,
  publishedAt,
  source,
  summary,
  title,
}: ContentCardProps) {
  const metadata: MetadataItem[] = [
    { label: contentTypeLabel ?? contentTypeLabels[contentType] },
    ...(publishedAt
      ? [{ dateTime: publishedAt.dateTime, label: publishedAt.label }]
      : []),
    { label: source },
  ];

  return (
    <LinkedContentCard
      actionLabel={actionLabel}
      className={className}
      description={summary}
      href={href}
      icon="content"
      metadata={<MetadataList ariaLabel="بيانات المحتوى" items={metadata} />}
      title={title}
    />
  );
}
