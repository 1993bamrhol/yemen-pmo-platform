import Link from "next/link";
import { notFound } from "next/navigation";
import { api } from "@/lib/api";

export default async function DocumentDetailPage({
  params
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  const documentId = Number(id);

  try {
    const document = await api.getDocumentById(documentId);

    return (
      <main className="container section">
        <Link href="/" className="button button--secondary">
          العودة للرئيسية
        </Link>
        <article className="list-card" style={{ marginTop: "24px" }}>
          <span className="card__meta">{document.category} · {document.updatedAt}</span>
          <h1 style={{ margin: "12px 0" }}>{document.title}</h1>
          <p>{document.description}</p>
        </article>
      </main>
    );
  } catch {
    notFound();
  }
}
