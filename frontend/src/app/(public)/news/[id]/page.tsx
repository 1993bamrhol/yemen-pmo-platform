import { notFound } from "next/navigation";

import { ContentState, MetadataList, type MetadataItem } from "@/components/content";
import { PageContainer, Section } from "@/components/layout";
import { Badge, Breadcrumbs, TextLink } from "@/components/ui";
import { api, ApiError, type NewsItem, type PublicNewsDetail } from "@/lib/api";
import { latestNews } from "@/lib/site-data";

import styles from "./NewsDetail.module.css";

const POSITIVE_INTEGER_PATTERN = /^\d+$/;
const NEWS_SLUG_PATTERN = /^[a-z0-9]+(?:-[a-z0-9]+)*$/;

type NewsLocator =
  | { kind: "numeric"; value: number }
  | { kind: "slug"; value: string }
  | { kind: "invalid" };

function classifyLocator(value: string): NewsLocator {
  if (POSITIVE_INTEGER_PATTERN.test(value)) {
    const numericId = Number(value);
    return Number.isSafeInteger(numericId) && numericId > 0
      ? { kind: "numeric", value: numericId }
      : { kind: "invalid" };
  }

  return NEWS_SLUG_PATTERN.test(value)
    ? { kind: "slug", value }
    : { kind: "invalid" };
}

function hasText(value: string | null | undefined): value is string {
  return typeof value === "string" && value.trim().length > 0;
}

function formatPublishedDate(value: string): string | undefined {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return undefined;

  return new Intl.DateTimeFormat("ar-YE", { dateStyle: "long" }).format(date);
}

function NewsApiErrorState({ slug }: { slug: string }) {
  return (
    <Section aria-labelledby="news-api-error-title" spacing="roomy">
      <h1 className={styles.stateTitle} id="news-api-error-title">
        تعذر عرض الخبر
      </h1>
      <ContentState
        action={<TextLink href={`/news/${encodeURIComponent(slug)}`}>إعادة المحاولة</TextLink>}
        description="تعذر الاتصال بمصدر الأخبار الرسمي. لم تُعرض أي بيانات بديلة غير موثقة."
        state="error"
        title="الخبر غير متاح مؤقتًا"
      />
    </Section>
  );
}

function LegacyNewsDetail({ article }: { article: NewsItem }) {
  const metadata: MetadataItem[] = [
    { label: article.category },
    { label: article.date },
  ];

  return (
    <article className={styles.page}>
      <PageContainer className={styles.container}>
        <Breadcrumbs
          items={[
            { href: "/", label: "الرئيسية" },
            { current: true, label: "الأخبار" },
          ]}
        />
        <div className={styles.header}>
          <Badge emphasis="outline">خبر</Badge>
          <h1 className={styles.title}>{article.title}</h1>
          <MetadataList ariaLabel="بيانات الخبر" items={metadata} />
        </div>
        <div className={styles.legacyBody}>
          <p>{article.excerpt}</p>
        </div>
      </PageContainer>
    </article>
  );
}

function PublicNewsDetailView({ article }: { article: PublicNewsDetail }) {
  const publishedDate = formatPublishedDate(article.publishedAt);
  const categories = (article.categories ?? [])
    .map((category) => category.label?.trim())
    .filter(hasText);
  const metadata: MetadataItem[] = [
    { label: "خبر رسمي" },
    {
      dateTime: publishedDate ? article.publishedAt : undefined,
      label: publishedDate ? `نُشر على البوابة في ${publishedDate}` : undefined,
    },
    { label: hasText(article.byline) ? article.byline : undefined },
    { label: hasText(article.primaryEntity?.officialName) ? article.primaryEntity.officialName : undefined },
    ...categories.map((label) => ({ label })),
  ];

  return (
    <article className={styles.page}>
      <PageContainer className={styles.container}>
        <Breadcrumbs
          items={[
            { href: "/", label: "الرئيسية" },
            { current: true, label: "الأخبار" },
          ]}
        />
        <header className={styles.header}>
          <Badge emphasis="outline" tone="success">
            خبر رسمي معتمد
          </Badge>
          <h1 className={styles.title}>{article.title}</h1>
          <MetadataList ariaLabel="بيانات نشر الخبر" items={metadata} />
          {hasText(article.summary) ? <p className={styles.summary}>{article.summary}</p> : null}
        </header>
        <div
          className={styles.body}
          dangerouslySetInnerHTML={{ __html: article.body }}
        />
      </PageContainer>
    </article>
  );
}

async function loadLegacyNews(id: number): Promise<NewsItem> {
  try {
    return await api.getNewsById(id);
  } catch {
    const fallback = latestNews.find((item) => item.id === id);
    if (!fallback) notFound();
    return fallback;
  }
}

export default async function NewsDetailPage({
  params
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  const locator = classifyLocator(id);

  if (locator.kind === "invalid") notFound();

  if (locator.kind === "numeric") {
    return <LegacyNewsDetail article={await loadLegacyNews(locator.value)} />;
  }

  let article: PublicNewsDetail;
  try {
    article = await api.getPublicNewsBySlug(locator.value);
  } catch (error) {
    if (error instanceof ApiError && error.status === 404) notFound();
    return <NewsApiErrorState slug={locator.value} />;
  }

  const expectedCanonicalPath = `/news/${locator.value}`;
  if (!hasText(article.id)
      || article.slug !== locator.value
      || article.canonicalPath !== expectedCanonicalPath
      || !hasText(article.title)
      || !hasText(article.body)) {
    notFound();
  }

  return <PublicNewsDetailView article={article} />;
}
