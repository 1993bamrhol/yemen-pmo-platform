import Link from "next/link";
import { Footer } from "@/components/Footer";
import { Header } from "@/components/Header";

type AdminContentItem = {
  id: number;
  type: string;
  title: string;
  status: string;
  author: string;
  category: string;
  updatedAt: string;
};

type AdminContentSummary = {
  total: number;
  published: number;
  draft: number;
  archived: number;
};

async function loadAdminContent(): Promise<{ content: AdminContentItem[]; summary: AdminContentSummary }> {
  const baseUrl = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

  try {
    const [contentResponse, summaryResponse] = await Promise.all([
      fetch(`${baseUrl}/api/admin/content`, { cache: "no-store" }),
      fetch(`${baseUrl}/api/admin/content/summary`, { cache: "no-store" })
    ]);

    if (!contentResponse.ok || !summaryResponse.ok) {
      throw new Error("Unauthorized or unavailable");
    }

    return {
      content: await contentResponse.json(),
      summary: await summaryResponse.json()
    };
  } catch {
    return {
      content: [
        { id: 1, type: "news", title: "اجتماع لمناقشة أولويات الخدمات الحكومية الرقمية", status: "منشور", author: "أحمد علي", category: "الأخبار", updatedAt: "2026-08-16" },
        { id: 2, type: "announcement", title: "إعلان رسمي عن إطلاق المرحلة الأولى من البوابة الحكومية", status: "مسودة", author: "سارة محمد", category: "الإعلانات", updatedAt: "2026-08-15" },
        { id: 3, type: "decision", title: "قرار اعتماد الهوية البصرية الرسمية", status: "منشور", author: "خالد اليماني", category: "القرارات", updatedAt: "2026-08-12" },
        { id: 4, type: "document", title: "خطة النشر لمرحلة MVP", status: "مؤرشف", author: "منى المعلمي", category: "الوثائق", updatedAt: "2026-08-10" }
      ],
      summary: { total: 4, published: 2, draft: 1, archived: 1 }
    };
  }
}

export default async function AdminPage() {
  const { content, summary } = await loadAdminContent();

  return (
    <>
      <Header />
      <main className="container section">
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", gap: "16px", flexWrap: "wrap" }}>
          <div>
            <p className="card__meta">لوحة الإدارة</p>
            <h1 style={{ margin: "8px 0" }}>إدارة المحتوى الحكومي</h1>
          </div>
          <Link href="/" className="button button--secondary">
            العودة إلى البوابة
          </Link>
        </div>

        <div className="info-grid" style={{ marginTop: "24px" }}>
          <article className="info-card">
            <span className="card__meta">الإجمالي</span>
            <h3>{summary.total}</h3>
          </article>
          <article className="info-card">
            <span className="card__meta">منشور</span>
            <h3>{summary.published}</h3>
          </article>
          <article className="info-card">
            <span className="card__meta">قيد المراجعة</span>
            <h3>{summary.draft}</h3>
          </article>
          <article className="info-card">
            <span className="card__meta">مؤرشف</span>
            <h3>{summary.archived}</h3>
          </article>
        </div>

        <section className="section" style={{ paddingTop: "0" }}>
          <div className="list-card">
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "18px", gap: "12px", flexWrap: "wrap" }}>
              <h3 style={{ margin: 0 }}>قائمة المحتوى</h3>
              <button type="button" className="button button--primary button--compact">
                + إضافة جديد
              </button>
            </div>

            <div style={{ display: "grid", gap: "12px" }}>
              {content.map((item) => (
                <div key={item.id} style={{ border: "1px solid #e6e3dd", borderRadius: "12px", padding: "16px", display: "grid", gap: "8px" }}>
                  <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", gap: "12px", flexWrap: "wrap" }}>
                    <span className="card__meta">{item.category}</span>
                    <span className="pill pill--muted">{item.status}</span>
                  </div>
                  <div style={{ display: "flex", justifyContent: "space-between", gap: "12px", alignItems: "start", flexWrap: "wrap" }}>
                    <div>
                      <h4 style={{ margin: "0 0 6px" }}>{item.title}</h4>
                      <p style={{ margin: 0 }}>{item.type} · {item.author}</p>
                    </div>
                    <div style={{ textAlign: "left" }}>
                      <small>{item.updatedAt}</small>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </section>
      </main>
      <Footer />
    </>
  );
}
