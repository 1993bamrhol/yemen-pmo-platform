import Link from "next/link";
import { notFound } from "next/navigation";
import { api } from "@/lib/api";

export default async function NewsDetailPage({
  params
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  const articleId = Number(id);

  try {
    const article = await api.getNewsById(articleId);

    return (
      <main className="container section">
        <Link href="/" className="button button--secondary">
          العودة للرئيسية
        </Link>
        <article className="list-card" style={{ marginTop: "24px" }}>
          <span className="card__meta">{article.category} · {article.date}</span>
          <h1 style={{ margin: "12px 0" }}>{article.title}</h1>
          <p>{article.excerpt}</p>
        </article>
      </main>
    );
  } catch {
    notFound();
  }
}
