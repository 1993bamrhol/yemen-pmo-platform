import type { Metadata } from "next";
import { notFound, permanentRedirect } from "next/navigation";
import { cache, Suspense } from "react";

import {
  CardGrid,
  ContentState,
  MetadataList,
  ServiceCard,
} from "@/components/content";
import { PageContainer, Section, SectionHeader } from "@/components/layout";
import { Badge, Breadcrumbs, TextLink } from "@/components/ui";
import {
  api,
  ApiError,
  type GovernmentEntityDetail,
  type GovernmentServiceChannel,
} from "@/lib/api";

import styles from "./EntityProfile.module.css";

type EntityRouteParams = {
  publicPathSegment: string;
  requestedPath?: string;
  slug: string;
};

const CHANNEL_LABELS: Record<GovernmentServiceChannel["type"], string> = {
  ONLINE: "إلكترونية",
  IN_PERSON: "حضورية",
  PHONE: "هاتفية",
};

const SAFE_PATH_SEGMENT = /^[a-z0-9]+(?:-[a-z0-9]+)*$/;

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

function formatDate(value: string | null | undefined): string | undefined {
  if (!hasText(value)) return undefined;

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return undefined;

  return new Intl.DateTimeFormat("ar-YE", { dateStyle: "long" }).format(date);
}

function requestedPath({ publicPathSegment, slug }: EntityRouteParams): string {
  return `/${publicPathSegment}/${slug}`;
}

function currentRequestPath(params: EntityRouteParams): string {
  return params.requestedPath ?? requestedPath(params);
}

function retryPath({
  publicPathSegment,
  requestedPath,
  slug,
}: EntityRouteParams): string {
  return requestedPath
    ? `/${encodeURIComponent(publicPathSegment)}`
    : `/${encodeURIComponent(publicPathSegment)}/${encodeURIComponent(slug)}`;
}

function trustedCanonicalPath(entity: GovernmentEntityDetail): string | undefined {
  const publicPathSegment = entity.type?.pathSegment;

  if (
    !hasText(publicPathSegment) ||
    !SAFE_PATH_SEGMENT.test(publicPathSegment) ||
    !SAFE_PATH_SEGMENT.test(entity.slug)
  ) {
    return undefined;
  }

  const expectedPath =
    entity.type?.code === "PRIME_MINISTERS_OFFICE"
      ? `/${publicPathSegment}`
      : `/${publicPathSegment}/${entity.slug}`;
  return entity.canonicalPath === expectedPath ? expectedPath : undefined;
}

const loadEntity = cache((publicPathSegment: string, slug: string) =>
  api.getGovernmentEntityBySlug(publicPathSegment, slug),
);

export async function generateMetadata({
  params,
}: {
  params: Promise<EntityRouteParams>;
}): Promise<Metadata> {
  const routeParams = await params;

  try {
    const entity = await loadEntity(
      routeParams.publicPathSegment,
      routeParams.slug,
    );
    const canonicalPath = trustedCanonicalPath(entity);

    return {
      alternates: canonicalPath ? { canonical: canonicalPath } : undefined,
      description: hasText(entity.description) ? entity.description : undefined,
      title: entity.officialName,
    };
  } catch {
    return {
      title: "الجهة الحكومية",
    };
  }
}

function EntityApiErrorState({ params }: { params: EntityRouteParams }) {
  return (
    <Section aria-labelledby="entity-error-title" spacing="roomy">
      <h1 className={styles.stateTitle} id="entity-error-title">
        تعذر عرض بيانات الجهة
      </h1>
      <ContentState
        action={<TextLink href={retryPath(params)}>إعادة المحاولة</TextLink>}
        description="تعذر الاتصال بدليل الجهات الحكومية. لم تُعرض أي بيانات بديلة غير موثقة."
        state="error"
        title="بيانات الجهة غير متاحة مؤقتًا"
      />
    </Section>
  );
}

function EntityServicesLoading() {
  return (
    <ContentState
      itemCount={3}
      label="جارٍ تحميل خدمات الجهة الحكومية"
      state="loading"
    />
  );
}

async function EntityServices({
  entityId,
  entityName,
}: {
  entityId: string;
  entityName: string;
}) {
  let directory;

  try {
    directory = await api.getGovernmentServicesForEntity(entityId);
  } catch {
    return (
      <ContentState
        description="تعذر تحميل خدمات هذه الجهة، بينما بقيت معلومات الجهة متاحة بصورة مستقلة."
        state="error"
        title="خدمات الجهة غير متاحة مؤقتًا"
      />
    );
  }

  const services = (directory.items ?? []).filter(
    (service) => hasText(service.officialName) && hasText(service.slug),
  );

  if (!services.length) {
    return (
      <ContentState
        description="لم يُرجع دليل الخدمات أي خدمة عامة منشورة ومعتمدة لهذه الجهة."
        state="empty"
        title="لا توجد خدمات متاحة حاليًا"
      />
    );
  }

  return (
    <CardGrid>
      {services.map((service) => {
        const channelNames = [
          ...new Set(
            (service.channels ?? []).map((channel) => CHANNEL_LABELS[channel]),
          ),
        ];

        return (
          <ServiceCard
            actionLabel="عرض تفاصيل الخدمة"
            category={channelNames.length ? channelNames.join("، ") : undefined}
            description={hasText(service.summary) ? service.summary : undefined}
            href={`/services/${encodeURIComponent(service.slug)}`}
            key={service.id}
            providerName={service.ownerEntity?.officialName || entityName}
            title={service.officialName}
          />
        );
      })}
    </CardGrid>
  );
}

export default async function EntityProfilePage({
  params,
}: {
  params: Promise<EntityRouteParams>;
}) {
  const routeParams = await params;
  let entity: GovernmentEntityDetail;

  try {
    entity = await loadEntity(
      routeParams.publicPathSegment,
      routeParams.slug,
    );
  } catch (error) {
    if (error instanceof ApiError && error.status === 404) notFound();
    return <EntityApiErrorState params={routeParams} />;
  }

  const canonicalPath = trustedCanonicalPath(entity);
  if (canonicalPath && currentRequestPath(routeParams) !== canonicalPath) {
    permanentRedirect(canonicalPath);
  }

  const websiteUrl = safeExternalUrl(entity.websiteUrl);
  const description = hasText(entity.description) ? entity.description : undefined;
  const mandate = hasText(entity.mandate) ? entity.mandate : undefined;
  const officialNameEn = hasText(entity.officialNameEn)
    ? entity.officialNameEn
    : undefined;
  const sourceReference = hasText(entity.officialSourceReference)
    ? entity.officialSourceReference
    : undefined;
  const contact = entity.contact;
  const email = hasText(contact?.email) ? contact.email : undefined;
  const phone = hasText(contact?.phone) ? contact.phone : undefined;
  const address = hasText(contact?.address) ? contact.address : undefined;
  const hasContactInformation = Boolean(websiteUrl || email || phone || address);
  const updatedDate = formatDate(entity.updatedAt);

  return (
    <article className={styles.page}>
      <section aria-labelledby="entity-title" className={styles.hero}>
        <PageContainer className={styles.heroContainer}>
          <Breadcrumbs
            items={[
              { href: "/", label: "الرئيسية" },
              { disabled: true, label: entity.type?.name || "جهة حكومية" },
              { current: true, label: entity.officialName },
            ]}
          />

          <div className={styles.identityCard}>
            <div className={styles.badges}>
              <Badge emphasis="outline">{entity.type?.name || "جهة حكومية"}</Badge>
              <Badge emphasis="outline" tone="success">
                جهة حكومية نشطة
              </Badge>
            </div>
            <h1 className={styles.heroTitle} id="entity-title">
              {entity.officialName}
            </h1>
            {officialNameEn ? (
              <p className={styles.englishName} dir="ltr" lang="en">
                {officialNameEn}
              </p>
            ) : null}
            {description ? <p className={styles.heroDescription}>{description}</p> : null}
            {websiteUrl ? (
              <TextLink
                className={styles.websiteLink}
                href={websiteUrl}
                rel="noopener noreferrer"
                target="_blank"
              >
                زيارة الموقع الرسمي (يفتح في نافذة جديدة)
              </TextLink>
            ) : null}
          </div>

          <span aria-hidden="true" className={styles.heroMotif} />
        </PageContainer>
      </section>

      {mandate ? (
        <Section aria-labelledby="entity-mandate-title" spacing="roomy">
          <SectionHeader headingId="entity-mandate-title" title="اختصاص الجهة" />
          <p className={styles.profileText}>{mandate}</p>
        </Section>
      ) : null}

      {hasContactInformation ? (
        <Section
          aria-labelledby="entity-contact-title"
          spacing="roomy"
          tone="subtle"
        >
          <SectionHeader
            description="تظهر فقط بيانات التواصل التي يعيدها السجل العام للجهة."
            headingId="entity-contact-title"
            title="معلومات التواصل"
          />
          <dl className={styles.contactGrid}>
            {email ? (
              <div className={styles.contactItem}>
                <dt>البريد الإلكتروني</dt>
                <dd>
                  <bdi>{email}</bdi>
                </dd>
              </div>
            ) : null}
            {phone ? (
              <div className={styles.contactItem}>
                <dt>الهاتف</dt>
                <dd>
                  <bdi>{phone}</bdi>
                </dd>
              </div>
            ) : null}
            {address ? (
              <div className={styles.contactItem}>
                <dt>العنوان</dt>
                <dd>{address}</dd>
              </div>
            ) : null}
            {websiteUrl ? (
              <div className={styles.contactItem}>
                <dt>الموقع الرسمي</dt>
                <dd>
                  <TextLink
                    href={websiteUrl}
                    rel="noopener noreferrer"
                    target="_blank"
                  >
                    فتح الموقع الرسمي (يفتح في نافذة جديدة)
                  </TextLink>
                </dd>
              </div>
            ) : null}
          </dl>
        </Section>
      ) : null}

      <Section aria-labelledby="entity-services-title" spacing="roomy">
        <SectionHeader
          description="الخدمات العامة المنشورة والمعتمدة والمرتبطة بهذه الجهة فقط."
          headingId="entity-services-title"
          title="خدمات الجهة"
        />
        <Suspense fallback={<EntityServicesLoading />}>
          <EntityServices entityId={entity.id} entityName={entity.officialName} />
        </Suspense>
      </Section>

      <Section
        aria-labelledby="entity-source-title"
        spacing="roomy"
        tone="subtle"
      >
        <SectionHeader
          description="بيانات تعريفية من سجل الجهات الحكومي العام."
          headingId="entity-source-title"
          title="المصدر وحالة التحديث"
        />
        <div className={styles.sourcePanel}>
          {sourceReference ? (
            <dl className={styles.sourceDetails}>
              <div>
                <dt>مرجع المصدر الرسمي</dt>
                <dd>
                  <bdi>{sourceReference}</bdi>
                </dd>
              </div>
            </dl>
          ) : null}
          <MetadataList
            ariaLabel="بيانات تحديث الجهة"
            items={[
              {
                dateTime: entity.updatedAt,
                label: updatedDate && `آخر تحديث ${updatedDate}`,
              },
              { label: entity.parent?.officialName && `الجهة الأم: ${entity.parent.officialName}` },
            ]}
          />
        </div>
      </Section>
    </article>
  );
}
