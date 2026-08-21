import Link from "next/link";
import { notFound } from "next/navigation";
import { api } from "@/lib/api";

export default async function DecisionDetailPage({
  params
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  const decisionId = Number(id);
  let decision;

  try {
    decision = await api.getDecisionById(decisionId);
  } catch {
    notFound();
  }

  return (
    <main className="container section">
      <Link href="/" className="button button--secondary">
        العودة للرئيسية
      </Link>
      <article className="list-card" style={{ marginTop: "24px" }}>
        <span className="card__meta">{decision.category} · {decision.date}</span>
        <h1 style={{ margin: "12px 0" }}>{decision.title}</h1>
        <p>{decision.description}</p>
      </article>
    </main>
  );
}
