import Link from "next/link";
import { Footer } from "@/components/Footer";
import { Header } from "@/components/Header";
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

export default async function HomePage() {
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

  return (
    <>
      <Header />
      <main>
        {!portalHomeResult.ok ? (
          <section className="container section">
            <div className="notice notice--warning" role="status">
              تعذر الاتصال بالخادم المؤسسي: {portalHomeResult.error}. وتعرض الصفحة البيانات الاحتياطية المحلية.
            </div>
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
                  <Link href={`/announcements/${item.id ?? 1}`}>{item.title}</Link>
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
                  <Link href={`/news/${item.id ?? 1}`}>{item.title}</Link>
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
              <span key={item} className="pill pill--muted">
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
                  <Link href={`/decisions/${item.id ?? 1}`}>{item.title}</Link>
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
              <article key={item.title} className="info-card">
                <span className="card__meta">خدمة رقمية</span>
                <h4>{item.title}</h4>
                <p>{item.description}</p>
              </article>
            ))}
          </div>
          <div className="pill-grid">
            {(home.services ?? services).map((service) => (
              <span key={service} className="pill">
                {service}
              </span>
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
                href={typeof document === "string" ? "/documents/1" : `/documents/${document.id ?? 1}`}
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
              <span key={item} className="pill">
                {item}
              </span>
            ))}
          </div>
        </section>
      </main>
      <Footer />
    </>
  );
}
