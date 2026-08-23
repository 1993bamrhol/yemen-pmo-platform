import Link from "next/link";
import { notFound } from "next/navigation";
import { api } from "@/lib/api";
import { latestNews } from "@/lib/site-data";

export default async function NewsDetailPage({
  params
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  const articleId = Number(id);
  let article;

  try {
    article = await api.getNewsById(articleId);
  } catch {
    article = latestNews.find((item) => item.id === articleId);
    if (!article) notFound();
  }

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
}
