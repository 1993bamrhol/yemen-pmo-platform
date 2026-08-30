import { notFound } from "next/navigation";

import { ContentState, MetadataList } from "@/components/content";
import { PageContainer, Section, SectionHeader } from "@/components/layout";
import { Badge, Breadcrumbs, TextLink } from "@/components/ui";
import {
  api,
  ApiError,
  type GovernmentServiceChannel,
  type GovernmentServiceDetail,
  type GovernmentServiceDetailItem,
} from "@/lib/api";

import styles from "./ServiceDetail.module.css";

const CHANNEL_LABELS: Record<GovernmentServiceChannel["type"], string> = {
  ONLINE: "إلكترونية",
  IN_PERSON: "حضورية",
  PHONE: "هاتفية",
};

const SOURCE_LABELS: Record<GovernmentServiceDetail["source"]["type"], string> = {
  OFFICIAL_MANUAL_ENTRY: "إدخال إداري رسمي",
  OFFICIAL_SOURCE_REFERENCE: "مرجع رسمي",
  APPROVED_IMPORT: "استيراد معتمد",
};

function hasText(value: string | null | undefined): value is string {
  return typeof value === "string" && value.trim().length > 0;
}

function safeExternalUrl(value: string | null | undefined): string | undefined {
  if (!hasText(value)) return undefined;

  try {
    const url = new URL(value);
    return url.protocol === "https:" ? url.toString() : undefined;
  } catch {
    return undefined;
  }
}

function sortItems<T extends { order: number }>(items: readonly T[]): T[] {
  return [...items].sort((left, right) => left.order - right.order);
}

function formatDate(value: string | null | undefined): string | undefined {
  if (!hasText(value)) return undefined;

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return undefined;

  return new Intl.DateTimeFormat("ar-YE", { dateStyle: "long" }).format(date);
}

function DetailItems({ items }: { items: readonly GovernmentServiceDetailItem[] }) {
  return (
    <ul className={styles.detailList}>
      {sortItems(items).map((item) => (
        <li key={`${item.order}-${item.title}`}>
          <strong>{item.title}</strong>
          {hasText(item.description) ? <p>{item.description}</p> : null}
        </li>
      ))}
    </ul>
  );
}

function ApiErrorState({ slug }: { slug: string }) {
  return (
    <Section aria-labelledby="service-error-title" spacing="roomy">
      <h1 className={styles.stateTitle} id="service-error-title">
        تعذر عرض تفاصيل الخدمة
      </h1>
      <ContentState
        action={
          <TextLink href={`/services/${encodeURIComponent(slug)}`}>
            إعادة المحاولة
          </TextLink>
        }
        description="تعذر الاتصال بدليل الخدمات الحكومي. لم تُعرض أي بيانات بديلة غير موثقة."
        state="error"
        title="الخدمة غير متاحة مؤقتًا"
      />
    </Section>
  );
}

export default async function ServiceDetailPage({
  params,
}: {
  params: Promise<{ slug: string }>;
}) {
  const { slug } = await params;
  let service: GovernmentServiceDetail;

  try {
    service = await api.getGovernmentServiceBySlug(slug);
  } catch (error) {
    if (error instanceof ApiError && error.status === 404) notFound();
    return <ApiErrorState slug={slug} />;
  }

  const eligibility = sortItems(service.eligibility ?? []);
  const requirements = sortItems(service.requirements ?? []);
  const steps = sortItems(service.steps ?? []);
  const channels = sortItems(service.channels ?? []);
  const actionableChannel = channels.find((channel) => safeExternalUrl(channel.actionUrl));
  const actionUrl = safeExternalUrl(actionableChannel?.actionUrl);
  const channelNames = [...new Set(channels.map((channel) => CHANNEL_LABELS[channel.type]))];
  const facts = [
    hasText(service.processingTime)
      ? { label: "مدة الإنجاز", value: service.processingTime }
      : undefined,
    hasText(service.fees) ? { label: "الرسوم", value: service.fees } : undefined,
    channelNames.length
      ? { label: "قنوات التقديم", value: channelNames.join("، ") }
      : undefined,
  ].filter((fact): fact is { label: string; value: string } => Boolean(fact));
  const showEligibilityAndRequirements = eligibility.length > 0 || requirements.length > 0;
  const showDescription = hasText(service.description) && service.description !== service.summary;
  const publishedDate = formatDate(service.publishedAt);
  const updatedDate = formatDate(service.updatedAt);
  const verifiedDate = formatDate(service.source.verifiedAt);

  return (
    <article className={styles.page}>
      <section aria-labelledby="service-title" className={styles.hero}>
        <PageContainer className={styles.heroContainer}>
          <Breadcrumbs
            items={[
              { href: "/", label: "الرئيسية" },
              { href: "/services", label: "الخدمات" },
              { current: true, label: service.officialName },
            ]}
          />

          <div className={styles.heroGrid} data-has-action={Boolean(actionUrl)}>
            <div className={styles.heroCopy}>
              <Badge emphasis="outline" tone="success">
                خدمة حكومية معتمدة
              </Badge>
              <h1 className={styles.heroTitle} id="service-title">
                {service.officialName}
              </h1>
              <p className={styles.ownerLabel}>الجهة المقدمة</p>
              <p className={styles.ownerName}>{service.ownerEntity.officialName}</p>
              <p className={styles.summary}>{service.summary}</p>
            </div>

            {actionUrl && actionableChannel ? (
              <aside aria-label="بدء الخدمة" className={styles.actionCard}>
                <p className={styles.actionEyebrow}>القناة الرسمية</p>
                <h2 className={styles.actionTitle}>
                  {hasText(actionableChannel.label)
                    ? actionableChannel.label
                    : CHANNEL_LABELS[actionableChannel.type]}
                </h2>
                {hasText(actionableChannel.instructions) ? (
                  <p className={styles.actionDescription}>{actionableChannel.instructions}</p>
                ) : null}
                <TextLink
                  href={actionUrl}
                  rel="noopener noreferrer"
                  target="_blank"
                >
                  الانتقال إلى القناة الرسمية (تفتح في نافذة جديدة)
                </TextLink>
              </aside>
            ) : null}
          </div>
          <span aria-hidden="true" className={styles.heroMotif} />
        </PageContainer>
      </section>

      {facts.length ? (
        <Section aria-labelledby="service-facts-title" spacing="roomy" tone="subtle">
          <SectionHeader headingId="service-facts-title" title="معلومات سريعة قبل البدء" />
          <dl className={styles.facts}>
            {facts.map((fact) => (
              <div className={styles.fact} key={fact.label}>
                <dt>{fact.label}</dt>
                <dd>{fact.value}</dd>
              </div>
            ))}
          </dl>
        </Section>
      ) : null}

      {showDescription ? (
        <Section aria-labelledby="service-description-title" spacing="roomy">
          <SectionHeader headingId="service-description-title" title="معلومات الخدمة" />
          <p className={styles.description}>{service.description}</p>
        </Section>
      ) : null}

      {showEligibilityAndRequirements ? (
        <Section aria-labelledby="eligibility-title" spacing="roomy">
          <SectionHeader headingId="eligibility-title" title="الأهلية والمتطلبات" />
          <div className={styles.requirementsGrid}>
            {eligibility.length ? (
              <section aria-labelledby="eligibility-subtitle" className={styles.detailPanel}>
                <h3 id="eligibility-subtitle">من يمكنه التقديم؟</h3>
                <DetailItems items={eligibility} />
              </section>
            ) : null}
            {requirements.length ? (
              <section aria-labelledby="requirements-subtitle" className={styles.detailPanel}>
                <h3 id="requirements-subtitle">المتطلبات</h3>
                <DetailItems items={requirements} />
              </section>
            ) : null}
          </div>
        </Section>
      ) : null}

      {steps.length ? (
        <Section aria-labelledby="service-steps-title" spacing="roomy" tone="subtle">
          <SectionHeader headingId="service-steps-title" title="خطوات التقديم" />
          <ol className={styles.steps}>
            {steps.map((step) => (
              <li key={`${step.order}-${step.title}`}>
                <div>
                  <h3>{step.title}</h3>
                  {hasText(step.description) ? <p>{step.description}</p> : null}
                </div>
              </li>
            ))}
          </ol>
        </Section>
      ) : null}

      {channels.length ? (
        <Section aria-labelledby="service-channels-title" spacing="roomy">
          <SectionHeader headingId="service-channels-title" title="طرق تقديم الخدمة" />
          <ul className={styles.channels}>
            {channels.map((channel) => {
              const externalUrl = safeExternalUrl(channel.actionUrl);
              return (
                <li key={`${channel.type}-${channel.order}`}>
                  <Badge emphasis="outline">{CHANNEL_LABELS[channel.type]}</Badge>
                  <h3>
                    {hasText(channel.label) ? channel.label : CHANNEL_LABELS[channel.type]}
                  </h3>
                  {hasText(channel.instructions) ? <p>{channel.instructions}</p> : null}
                  {externalUrl ? (
                    <TextLink
                      href={externalUrl}
                      rel="noopener noreferrer"
                      target="_blank"
                    >
                      فتح القناة الرسمية (تفتح في نافذة جديدة)
                    </TextLink>
                  ) : null}
                </li>
              );
            })}
          </ul>
        </Section>
      ) : null}

      <Section aria-labelledby="service-source-title" spacing="roomy" tone="subtle">
        <SectionHeader
          description="تُعرض هذه البيانات كما وردت من سجل الخدمات العام المعتمد."
          headingId="service-source-title"
          title="المصدر وحالة التحديث"
        />
        <div className={styles.sourcePanel}>
          <dl className={styles.sourceDetails}>
            <div>
              <dt>نوع المصدر</dt>
              <dd>{SOURCE_LABELS[service.source.type]}</dd>
            </div>
            <div>
              <dt>مرجع المصدر</dt>
              <dd>
                <bdi>{service.source.reference}</bdi>
              </dd>
            </div>
          </dl>
          <MetadataList
            ariaLabel="تواريخ نشر واعتماد الخدمة"
            items={[
              { dateTime: service.publishedAt, label: publishedDate && `نُشرت في ${publishedDate}` },
              { dateTime: service.updatedAt, label: updatedDate && `آخر تحديث ${updatedDate}` },
              { dateTime: service.source.verifiedAt, label: verifiedDate && `اعتمد المصدر في ${verifiedDate}` },
            ]}
          />
        </div>
      </Section>
    </article>
  );
}
