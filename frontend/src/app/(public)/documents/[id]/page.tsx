import Link from "next/link";
import { notFound } from "next/navigation";
import { api } from "@/lib/api";
import { documents } from "@/lib/site-data";

export default async function DocumentDetailPage({
  params
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  const documentId = Number(id);
  let document;

  try {
    document = await api.getDocumentById(documentId);
  } catch {
    const fallback = documents.find((item) => item.id === documentId);
    document = fallback
      ? {
          ...fallback,
          category: "وثيقة رسمية",
          updatedAt: "نسخة محفوظة",
          description: "هذه الوثيقة متاحة ضمن النسخة المحفوظة من مكتبة البوابة."
        }
      : undefined;
    if (!document) notFound();
  }

  return (
    <div className="container section">
      <Link href="/" className="button button--secondary">
        العودة للرئيسية
      </Link>
      <article className="list-card" style={{ marginTop: "24px" }}>
        <span className="card__meta">{document.category} · {document.updatedAt}</span>
        <h1 style={{ margin: "12px 0" }}>{document.title}</h1>
        <p>{document.description}</p>
      </article>
    </div>
  );
}
