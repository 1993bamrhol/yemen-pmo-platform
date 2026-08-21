"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { Footer } from "@/components/Footer";
import { Header } from "@/components/Header";
import { api, ApiError, type AdminContentInput, type AdminContentItem, type AdminContentSummary, type SupportInboxItem } from "@/lib/api";
import { clearStoredAuthToken, getStoredAuthToken } from "@/lib/auth";

const emptySummary: AdminContentSummary = { total: 0, published: 0, draft: 0, archived: 0 };

const supportStatusLabels: Record<string, string> = {
  new: "جديدة",
  in_review: "قيد المراجعة",
  replied: "تم الرد",
  resolved: "مغلقة"
};

const emptyForm: AdminContentInput = {
  type: "news",
  title: "",
  status: "مسودة",
  author: "",
  category: "الأخبار"
};

export default function AdminPage() {
  const [token, setToken] = useState<string | null>(null);
  const [content, setContent] = useState<AdminContentItem[]>([]);
  const [supportRequests, setSupportRequests] = useState<SupportInboxItem[]>([]);
  const [summary, setSummary] = useState<AdminContentSummary>(emptySummary);
  const [isLoading, setIsLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState("all");
  const [error, setError] = useState("");
  const [formOpen, setFormOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [formValue, setFormValue] = useState<AdminContentInput>(emptyForm);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    const savedToken = getStoredAuthToken();
    if (!savedToken) {
      queueMicrotask(() => setIsLoading(false));
      return;
    }

    Promise.all([
      api.getAdminContent(savedToken),
      api.getAdminSummary(savedToken),
      api.getSupportRequests(savedToken)
    ])
      .then(([nextContent, nextSummary, nextRequests]) => {
        setToken(savedToken);
        setContent(nextContent);
        setSummary(nextSummary);
        setSupportRequests(nextRequests);
        setError("");
      })
      .catch((refreshError: unknown) => {
        if (refreshError instanceof ApiError && [401, 403].includes(refreshError.status)) {
          clearStoredAuthToken();
          setToken(null);
        }
        setError("جلسة الإدارة منتهية أو غير صالحة. يُرجى تسجيل الدخول مجددًا.");
      })
      .finally(() => setIsLoading(false));
  }, []);

  const handleOpenCreate = () => {
    setEditingId(null);
    setFormValue({ ...emptyForm });
    setFormOpen(true);
  };

  const handleOpenEdit = (item: AdminContentItem) => {
    setEditingId(item.id);
    setFormValue({
      type: item.type,
      title: item.title,
      status: item.status,
      author: item.author,
      category: item.category
    });
    setFormOpen(true);
  };

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!token) {
      setError("تحتاج إلى تسجيل الدخول قبل حفظ المحتوى.");
      return;
    }

    const trimmedTitle = formValue.title.trim();
    const trimmedAuthor = formValue.author.trim();
    if (!trimmedTitle || !trimmedAuthor) {
      setError("يجب إدخال عنوان ونص المؤلف قبل الحفظ.");
      return;
    }

    setIsSubmitting(true);
    try {
      if (editingId !== null) {
        const updated = await api.updateAdminContent(token, editingId, { ...formValue, title: trimmedTitle, author: trimmedAuthor });
        setContent((current) => current.map((item) => item.id === updated.id ? updated : item));
      } else {
        const created = await api.createAdminContent(token, { ...formValue, title: trimmedTitle, author: trimmedAuthor });
        setContent((current) => [created, ...current]);
      }

      const nextSummary = await api.getAdminSummary(token);
      setSummary(nextSummary);
      setFormOpen(false);
      setError("");
      setFormValue(emptyForm);
      setEditingId(null);
    } catch {
      setError("فشل حفظ المحتوى. تأكد من صلاحية الجلسة والمحاولة مرة أخرى.");
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (!token) {
      return;
    }

    if (!window.confirm("هل أنت متأكد من حذف هذا المحتوى؟")) {
      return;
    }

    const deletedItem = content.find((item) => item.id === id);

    try {
      await api.deleteAdminContent(token, id);
      setContent((current) => current.filter((item) => item.id !== id));
      setSummary((current) => ({
        ...current,
        total: Math.max(0, current.total - 1),
        published: Math.max(0, current.published - (deletedItem?.status === "منشور" ? 1 : 0)),
        draft: Math.max(0, current.draft - (deletedItem && ["مسودة", "قيد المراجعة"].includes(deletedItem.status) ? 1 : 0)),
        archived: Math.max(0, current.archived - (deletedItem?.status === "مؤرشف" ? 1 : 0))
      }));
      setError("");
      const nextSummary = await api.getAdminSummary(token);
      setSummary(nextSummary);
    } catch {
      setError("تعذر حذف المحتوى. حاول مرة أخرى.");
    }
  };

  const handleStatusUpdate = async (id: number, status: string) => {
    if (!token) {
      return;
    }

    try {
      const updated = await api.updateSupportRequestStatus(token, id, status);
      setSupportRequests((current) => current.map((item) => (item.id === id ? { ...item, status: updated.status } : item)));
      setError("");
    } catch {
      setError("تعذر تحديث حالة الطلب. حاول مرة أخرى.");
    }
  };

  const handleLogout = () => {
    clearStoredAuthToken();
    setToken(null);
    setError("تم تسجيل الخروج بنجاح.");
  };

  const visibleRequests = supportRequests.filter((request) => statusFilter === "all" || request.status === statusFilter);

  return (
    <>
      <Header />
      <main className="container section">
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", gap: "16px", flexWrap: "wrap" }}>
          <div>
            <p className="card__meta">لوحة الإدارة</p>
            <h1 style={{ margin: "8px 0" }}>إدارة المحتوى الحكومي</h1>
          </div>
          <div style={{ display: "flex", gap: "12px", alignItems: "center", flexWrap: "wrap" }}>
            {token ? (
              <button type="button" className="button button--secondary" onClick={handleLogout}>
                تسجيل الخروج
              </button>
            ) : (
              <Link href="/login" className="button button--primary">
                تسجيل الدخول
              </Link>
            )}
            <Link href="/" className="button button--secondary">
              العودة إلى البوابة
            </Link>
          </div>
        </div>

        {!token && !isLoading ? (
          <div className="notice notice--warning" role="alert" style={{ marginTop: "24px" }}>
            تحتاج إلى تسجيل الدخول أولًا للوصول إلى لوحة الإدارة.
          </div>
        ) : null}

        {error ? (
          <div className="notice notice--warning" role="alert" style={{ marginTop: "24px" }}>
            {error}
          </div>
        ) : null}

        {token ? (
          <>
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
              <div className="list-card" style={{ marginBottom: "20px" }}>
                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "18px", gap: "12px", flexWrap: "wrap" }}>
                  <h3 style={{ margin: 0 }}>صندوق المراسلات</h3>
                  <div style={{ display: "flex", alignItems: "center", gap: "12px", flexWrap: "wrap" }}>
                    <label style={{ display: "grid", gap: "6px" }}>
                      <span className="card__meta">تصفية الحالة</span>
                      <select
                        value={statusFilter}
                        onChange={(event) => setStatusFilter(event.target.value)}
                        style={{ padding: "8px 10px", borderRadius: "10px", border: "1px solid #d0d5dd" }}
                      >
                        <option value="all">الكل</option>
                        <option value="new">جديدة</option>
                        <option value="in_review">قيد المراجعة</option>
                        <option value="replied">تم الرد</option>
                        <option value="resolved">مغلقة</option>
                      </select>
                    </label>
                    <span className="pill pill--muted">{visibleRequests.length} طلب</span>
                  </div>
                </div>

                {visibleRequests.length === 0 ? (
                  <p>لا توجد طلبات جديدة حاليًا.</p>
                ) : (
                  <div style={{ display: "grid", gap: "12px" }}>
                    {visibleRequests.map((request) => (
                      <div key={request.id} style={{ border: "1px solid #e6e3dd", borderRadius: "12px", padding: "16px", display: "grid", gap: "8px" }}>
                        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", gap: "12px", flexWrap: "wrap" }}>
                          <span className="card__meta">{request.category}</span>
                          <div style={{ display: "flex", alignItems: "center", gap: "8px", flexWrap: "wrap" }}>
                            <span className="pill pill--muted">{supportStatusLabels[request.status] ?? request.status}</span>
                            <select
                              value={request.status}
                              onChange={(event) => handleStatusUpdate(request.id, event.target.value)}
                              style={{ padding: "8px 10px", borderRadius: "10px", border: "1px solid #d0d5dd" }}
                            >
                              <option value="new">جديدة</option>
                              <option value="in_review">قيد المراجعة</option>
                              <option value="replied">تم الرد</option>
                              <option value="resolved">مغلقة</option>
                            </select>
                          </div>
                        </div>
                        <div>
                          <h4 style={{ margin: "0 0 6px" }}>{request.subject}</h4>
                          <p style={{ margin: 0 }}>{request.fullName} · {request.email} · {request.phone || "لا يوجد هاتف"}</p>
                        </div>
                        <p style={{ margin: 0, lineHeight: 1.8, color: "#3f3a36" }}>{request.message}</p>
                        <small>{new Date(request.createdAt).toLocaleDateString("ar-YE")}</small>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              <div className="list-card">
                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "18px", gap: "12px", flexWrap: "wrap" }}>
                  <h3 style={{ margin: 0 }}>قائمة المحتوى</h3>
                  <button type="button" className="button button--primary button--compact" onClick={handleOpenCreate}>
                    + إضافة جديد
                  </button>
                </div>

                {isLoading ? (
                  <p>جاري تحميل المحتوى...</p>
                ) : (
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
                          <div style={{ textAlign: "left", display: "flex", flexDirection: "column", gap: "8px" }}>
                            <small>{item.updatedAt}</small>
                            <div style={{ display: "flex", gap: "8px", justifyContent: "flex-end" }}>
                              <button type="button" className="button button--secondary button--compact" onClick={() => handleOpenEdit(item)}>
                                تعديل
                              </button>
                              <button type="button" className="button button--secondary button--compact" onClick={() => handleDelete(item.id)}>
                                حذف
                              </button>
                            </div>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </section>
          </>
        ) : null}
      </main>

      {formOpen ? (
        <div style={{ position: "fixed", inset: 0, background: "rgba(15, 23, 42, 0.4)", display: "grid", placeItems: "center", padding: "24px", zIndex: 30 }}>
          <div className="list-card" style={{ width: "min(700px, 100%)", maxHeight: "90vh", overflowY: "auto" }}>
            <div style={{ display: "flex", gap: "12px", justifyContent: "space-between", alignItems: "center", marginBottom: "18px" }}>
              <h3 style={{ margin: 0 }}>{editingId !== null ? "تعديل المحتوى" : "إضافة محتوى جديد"}</h3>
              <button type="button" className="button button--secondary button--compact" onClick={() => setFormOpen(false)}>
                إغلاق
              </button>
            </div>

            <form onSubmit={handleSubmit} style={{ display: "grid", gap: "16px" }}>
              <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(220px, 1fr))", gap: "16px" }}>
                <label style={{ display: "grid", gap: "8px" }}>
                  <span>نوع المحتوى</span>
                  <select
                    value={formValue.type}
                    onChange={(event) => setFormValue((current) => ({ ...current, type: event.target.value }))}
                    style={{ padding: "12px 14px", borderRadius: "10px", border: "1px solid #d0d5dd" }}
                  >
                    <option value="news">الأخبار</option>
                    <option value="announcement">الإعلانات</option>
                    <option value="decision">القرارات</option>
                    <option value="document">الوثائق</option>
                  </select>
                </label>

                <label style={{ display: "grid", gap: "8px" }}>
                  <span>الحالة</span>
                  <select
                    value={formValue.status}
                    onChange={(event) => setFormValue((current) => ({ ...current, status: event.target.value }))}
                    style={{ padding: "12px 14px", borderRadius: "10px", border: "1px solid #d0d5dd" }}
                  >
                    <option value="منشور">منشور</option>
                    <option value="مسودة">مسودة</option>
                    <option value="قيد المراجعة">قيد المراجعة</option>
                    <option value="مؤرشف">مؤرشف</option>
                  </select>
                </label>
              </div>

              <label style={{ display: "grid", gap: "8px" }}>
                <span>العنوان</span>
                <input
                  value={formValue.title}
                  onChange={(event) => setFormValue((current) => ({ ...current, title: event.target.value }))}
                  style={{ padding: "12px 14px", borderRadius: "10px", border: "1px solid #d0d5dd" }}
                />
              </label>

              <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(220px, 1fr))", gap: "16px" }}>
                <label style={{ display: "grid", gap: "8px" }}>
                  <span>المؤلف</span>
                  <input
                    value={formValue.author}
                    onChange={(event) => setFormValue((current) => ({ ...current, author: event.target.value }))}
                    style={{ padding: "12px 14px", borderRadius: "10px", border: "1px solid #d0d5dd" }}
                  />
                </label>

                <label style={{ display: "grid", gap: "8px" }}>
                  <span>الفئة</span>
                  <input
                    value={formValue.category}
                    onChange={(event) => setFormValue((current) => ({ ...current, category: event.target.value }))}
                    style={{ padding: "12px 14px", borderRadius: "10px", border: "1px solid #d0d5dd" }}
                  />
                </label>
              </div>

              <div style={{ display: "flex", justifyContent: "flex-end", gap: "12px", flexWrap: "wrap" }}>
                <button type="button" className="button button--secondary" onClick={() => setFormOpen(false)}>
                  إلغاء
                </button>
                <button type="submit" className="button button--primary" disabled={isSubmitting}>
                  {isSubmitting ? "جاري الحفظ..." : editingId !== null ? "حفظ التعديلات" : "حفظ المحتوى"}
                </button>
              </div>
            </form>
          </div>
        </div>
      ) : null}

      <Footer />
    </>
  );
}
