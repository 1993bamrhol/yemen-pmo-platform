import Link from "next/link";
import { notFound } from "next/navigation";
import { api } from "@/lib/api";
import { officialAnnouncements } from "@/lib/site-data";

export default async function AnnouncementDetailPage({
  params
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  const itemId = Number(id);
  let announcement;

  try {
    announcement = await api.getAnnouncementById(itemId);
  } catch {
    announcement = officialAnnouncements.find((item) => item.id === itemId);
    if (!announcement) notFound();
  }

  return (
    <div className="container section">
      <Link href="/" className="button button--secondary">
        العودة للرئيسية
      </Link>
      <article className="list-card" style={{ marginTop: "24px" }}>
        <span className="card__meta">{announcement.category} · {announcement.date}</span>
        <h1 style={{ margin: "12px 0" }}>{announcement.title}</h1>
        <p>{announcement.excerpt}</p>
      </article>
    </div>
  );
}
