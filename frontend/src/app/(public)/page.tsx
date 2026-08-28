import Link from "next/link";
import { Hero } from "@/components/Hero";
import { SectionHeading } from "@/components/SectionHeading";
import { api } from "@/lib/api";
import {
  decisions,
  documents,
  governancePrinciples,
  latestNews,
  mediaItems,
  officialAnnouncements,
  officialStatements,
  portalHomeFallback,
  serviceCards,
  services
} from "@/lib/site-data";

type SearchResult = {
  title: string;
  description: string;
  href: string;
  category: string;
};

export default async function HomePage({
  searchParams
}: {
  searchParams: Promise<{ q?: string | string[] }>;
}) {
  const resolvedSearchParams = await searchParams;
  const query = (Array.isArray(resolvedSearchParams.q) ? resolvedSearchParams.q[0] : resolvedSearchParams.q)?.trim() ?? "";
  const [portalHomeResult, newsResult, announcementsResult, decisionsResult, documentsResult] = await Promise.all([
    api.getPortalHome().then(
      (data) => ({ ok: true as const, data }),
      (error: unknown) => ({
        ok: false as const,
        error: error instanceof Error ? error.message : "تعذر تحميل البيانات الحية"
      })
    ),
    api.getNews().then(
      (data) => ({ ok: true as const, data }),
      () => ({ ok: false as const, data: latestNews })
    ),
    api.getAnnouncements().then(
      (data) => ({ ok: true as const, data }),
      () => ({ ok: false as const, data: officialAnnouncements })
    ),
    api.getDecisions().then(
      (data) => ({ ok: true as const, data }),
      () => ({ ok: false as const, data: decisions })
    ),
    api.getDocuments().then(
      (data) => ({ ok: true as const, data }),
      () => ({ ok: false as const, data: documents })
    )
  ]);

  const home = portalHomeResult.ok ? portalHomeResult.data : portalHomeFallback;
  const newsItems = newsResult.ok ? newsResult.data : home.latestNews ?? latestNews;
  const announcementItems = (announcementsResult.ok ? announcementsResult.data : home.officialAnnouncements ?? officialAnnouncements) as Array<{
    id?: number;
    title: string;
    category?: string;
    date: string;
    excerpt: string;
  }>;
  const decisionItems = (decisionsResult.ok ? decisionsResult.data : decisions) as Array<{
    id?: number;
    title: string;
    category?: string;
    meta?: string;
    description: string;
  }>;
  const documentItems = documentsResult.ok ? documentsResult.data : home.documents ?? documents;
  const normalizedQuery = query.toLocaleLowerCase("ar");
  const searchResults: SearchResult[] = normalizedQuery
    ? [
        ...announcementItems.flatMap((item) =>
          item.id
            ? [{ title: item.title, description: item.excerpt, href: `/announcements/${item.id}`, category: item.category ?? "إعلان" }]
            : []
        ),
        ...newsItems.flatMap((item) =>
          item.id
            ? [{ title: item.title, description: item.excerpt, href: `/news/${item.id}`, category: item.category ?? "خبر" }]
            : []
        ),
        ...decisionItems.flatMap((item) =>
          item.id
            ? [{ title: item.title, description: item.description, href: `/decisions/${item.id}`, category: item.category ?? item.meta ?? "قرار" }]
            : []
        ),
        ...documentItems.flatMap((document) =>
          typeof document === "string" || !document.id
            ? []
            : [{ title: document.title, description: "وثيقة منشورة في مكتبة البوابة", href: `/documents/${document.id}`, category: "وثيقة" }]
        )
      ].filter((item) => `${item.title} ${item.description} ${item.category}`.toLocaleLowerCase("ar").includes(normalizedQuery))
    : [];

  return (
    <>
      {!portalHomeResult.ok ? (
          <section className="container section">
            <div className="notice notice--warning" role="status">
              نعرض نسخة محفوظة مؤقتًا من محتوى البوابة حتى استعادة الاتصال بالخدمة.
            </div>
          </section>
        ) : null}

        {query ? (
          <section className="container section search-results" id="search-results" aria-labelledby="search-results-title">
            <div className="section-heading">
              <span className="eyebrow">نتائج البحث</span>
              <h2 id="search-results-title">نتائج البحث عن «{query}»</h2>
              <p>{searchResults.length ? `تم العثور على ${searchResults.length} نتيجة.` : "لم نعثر على نتائج مطابقة. جرّب كلمات أقصر أو أكثر عمومية."}</p>
            </div>
            {searchResults.length ? (
              <div className="list-grid">
                {searchResults.map((item) => (
                  <article key={`${item.href}-${item.title}`} className="list-card">
                    <span className="card__meta">{item.category}</span>
                    <h3><Link href={item.href}>{item.title}</Link></h3>
                    <p>{item.description}</p>
                  </article>
                ))}
              </div>
            ) : null}
          </section>
        ) : null}

        <Hero
          hero={home.hero}
          stats={home.stats}
          portalHighlights={home.portalHighlights}
          officialChannels={home.officialChannels}
        />

        <section className="container section" id="announcements">
          <SectionHeading
            title="الإعلانات الرسمية"
            description="إعلانات الرئاسة والجهات الحكومية الأساسية المتعلقة بالخدمات والتحديثات المؤسسية."
          />
          <div className="card-grid">
            {announcementItems.map((item) => (
              <article key={item.id ?? item.title} className="card">
                <span className="card__meta">
                  {item.category} · {item.date}
                </span>
                <h4>
                  {item.id ? <Link href={`/announcements/${item.id}`}>{item.title}</Link> : item.title}
                </h4>
                <p>{item.excerpt}</p>
              </article>
            ))}
          </div>
        </section>

        <section className="container section" id="news">
          <SectionHeading
            title="آخر الأخبار"
            description="المستجدات الرسمية والأخبار الحكومية المنشورة عبر البوابة."
          />
          <div className="card-grid">
            {newsItems.map((item) => (
              <article key={item.id ?? item.title} className="card">
                <span className="card__meta">
                  {item.category} · {item.date}
                </span>
                <h4>
                  {item.id ? <Link href={`/news/${item.id}`}>{item.title}</Link> : item.title}
                </h4>
                <p>{item.excerpt}</p>
              </article>
            ))}
          </div>
        </section>

        <section className="container section">
          <SectionHeading
            title="مبادئ البوابة"
            description="الأسس التي تحكم الأسلوب التحريري والتجربة الرقمية للبوابة الرسمية."
          />
          <div className="pill-grid">
            {(home.governancePrinciples ?? governancePrinciples).map((item) => (
              <span key={item} className="pill pill--static">
                {item}
              </span>
            ))}
          </div>
        </section>

        <section className="container section" id="statements">
          <SectionHeading
            title="البيانات الرسمية"
            description="آخر البيانات الصادرة عن رئاسة مجلس الوزراء والأمانة العامة."
          />
          <div className="list-grid">
            {(home.officialStatements ?? officialStatements).map((item) => (
              <article key={item.title} className="list-card">
                <span className="card__meta">{item.meta}</span>
                <h4>{item.title}</h4>
                <p>{item.description}</p>
              </article>
            ))}
          </div>
        </section>

        <section className="container section" id="decisions">
          <SectionHeading
            title="القرارات والتعاميم"
            description="الوصول السريع إلى القرارات والتعاميم الرسمية القابلة للأرشفة والتحميل."
          />
          <div className="list-grid">
            {decisionItems.map((item) => (
              <article key={item.id ?? item.title} className="list-card">
                <span className="card__meta">{item.category ?? item.meta ?? "قرار"}</span>
                <h4>
                  {item.id ? <Link href={`/decisions/${item.id}`}>{item.title}</Link> : item.title}
                </h4>
                <p>{item.description}</p>
              </article>
            ))}
          </div>
        </section>

        <section className="container section" id="services">
          <SectionHeading
            title="الخدمات الإلكترونية"
            description="مسارات خدمية أولية تفتح باب التفاعل مع المواطن والجهات المختلفة."
          />
          <div className="info-grid">
            {(home.serviceCards ?? serviceCards).map((item) => (
              <Link key={item.title} className="info-card info-card--link" href="/services">
                <span className="card__meta">خدمة رقمية</span>
                <h4>{item.title}</h4>
                <p>{item.description}</p>
              </Link>
            ))}
          </div>
          <div className="pill-grid">
            {(home.services ?? services).map((service) => (
              <Link key={service} className="pill" href="/services">
                {service}
              </Link>
            ))}
          </div>
        </section>

        <section className="container section" id="documents">
          <SectionHeading
            title="مكتبة الوثائق"
            description="الوثائق والأدلة والخطط الرسمية المنشورة للجمهور والجهات الحكومية."
          />
          <div className="pill-grid">
            {documentItems.map((document) => (
              <Link
                key={typeof document === "string" ? document : document.title}
                href={typeof document === "string" ? "/#documents" : `/documents/${document.id}`}
                className="pill"
              >
                {typeof document === "string" ? document : document.title}
              </Link>
            ))}
          </div>
        </section>

        <section className="container section" id="media">
          <SectionHeading
            title="المركز الإعلامي"
            description="صور وفيديوهات وتصريحات وتغطيات رسمية منظمة."
          />
          <div className="pill-grid">
            {(home.mediaItems ?? mediaItems).map((item) => (
              <span key={item} className="pill pill--static">
                {item}
              </span>
            ))}
          </div>
        </section>
    </>
  );
}
