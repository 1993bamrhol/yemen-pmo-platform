"use client";

import Link from "next/link";
import { useState } from "react";
import { SectionHeading } from "@/components/SectionHeading";
import { api } from "@/lib/api";

const defaultForm = {
  fullName: "",
  email: "",
  phone: "",
  category: "استفسار",
  subject: "",
  message: ""
};

export default function ComplaintsPage() {
  const [form, setForm] = useState(defaultForm);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isSubmitted, setIsSubmitted] = useState(false);
  const [error, setError] = useState("");

  const handleChange = (field: keyof typeof defaultForm, value: string) => {
    setForm((current) => ({ ...current, [field]: value }));
  };

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (!form.fullName.trim() || !form.email.trim() || !form.subject.trim() || !form.message.trim()) {
      setError("يجب إدخال الاسم، البريد الإلكتروني، عنوان الرسالة، ونص الرسالة.");
      return;
    }

    setError("");
    setIsSubmitting(true);

    try {
      await api.submitSupportRequest(form);
      setIsSubmitted(true);
      setForm(defaultForm);
    } catch {
      setError("تعذر إرسال الرسالة. حاول مرة أخرى أو تواصل معنا مباشرة.");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="container section">
        <SectionHeading
          title="الاستفسارات والشكاوى"
          description="قناة رسمية لتلقي الاستفسارات، المقترحات، والشكاوى المتعلقة بالخدمات والقرارات والبيانات الحكومية."
        />

        <div className="info-grid" style={{ marginTop: "24px" }}>
          <article className="info-card">
            <span className="card__meta">الاستجابة</span>
            <h3>خلال 3-5 أيام عمل</h3>
            <p>يتم مراجعة الرسائل الرسمية وتوجيهها إلى الجهة المختصة في أقرب وقت ممكن.</p>
          </article>
          <article className="info-card">
            <span className="card__meta">الخصوصية</span>
            <h3>معلومات آمنة</h3>
            <p>يتم استخدام البيانات فقط لأغراض معالجة الاستفسار أو الشكوى وفق المعايير المتبعة.</p>
          </article>
          <article className="info-card">
            <span className="card__meta">الاستعلامات</span>
            <h3>مؤسسية</h3>
            <p>يمكنكم تقديم مقترحاتكم، ملاحظاتكم، أو طلبات الوثائق وطلبات الاستفسار الرسمية.</p>
          </article>
        </div>

        <section className="section" style={{ paddingTop: "0" }}>
          <div className="list-card">
            <h3 style={{ marginTop: 0 }}>نموذج إرسال الرسالة</h3>

            {isSubmitted ? (
              <div className="notice notice--success" role="status" style={{ marginBottom: "16px" }}>
                تم استلام رسالتك بنجاح، وسيتم متابعتها من قبل الإدارة المختصة.
              </div>
            ) : null}

            {error ? (
              <div className="notice notice--warning" role="alert" style={{ marginBottom: "16px" }}>
                {error}
              </div>
            ) : null}

            <form onSubmit={handleSubmit} style={{ display: "grid", gap: "16px" }}>
              <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(220px, 1fr))", gap: "16px" }}>
                <label style={{ display: "grid", gap: "8px" }}>
                  <span>الاسم الكامل</span>
                  <input
                    value={form.fullName}
                    onChange={(event) => handleChange("fullName", event.target.value)}
                    style={{ padding: "12px 14px", borderRadius: "10px", border: "1px solid #d0d5dd" }}
                  />
                </label>

                <label style={{ display: "grid", gap: "8px" }}>
                  <span>البريد الإلكتروني</span>
                  <input
                    type="email"
                    value={form.email}
                    onChange={(event) => handleChange("email", event.target.value)}
                    style={{ padding: "12px 14px", borderRadius: "10px", border: "1px solid #d0d5dd" }}
                  />
                </label>
              </div>

              <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(220px, 1fr))", gap: "16px" }}>
                <label style={{ display: "grid", gap: "8px" }}>
                  <span>رقم الهاتف</span>
                  <input
                    value={form.phone}
                    onChange={(event) => handleChange("phone", event.target.value)}
                    style={{ padding: "12px 14px", borderRadius: "10px", border: "1px solid #d0d5dd" }}
                  />
                </label>

                <label style={{ display: "grid", gap: "8px" }}>
                  <span>نوع الرسالة</span>
                  <select
                    value={form.category}
                    onChange={(event) => handleChange("category", event.target.value)}
                    style={{ padding: "12px 14px", borderRadius: "10px", border: "1px solid #d0d5dd" }}
                  >
                    <option value="استفسار">استفسار</option>
                    <option value="اقتراح">اقتراح</option>
                    <option value="شكوى">شكوى</option>
                    <option value="طلب وثيقة">طلب وثيقة</option>
                    <option value="خدمة">خدمة</option>
                  </select>
                </label>
              </div>

              <label style={{ display: "grid", gap: "8px" }}>
                <span>عنوان الرسالة</span>
                <input
                  value={form.subject}
                  onChange={(event) => handleChange("subject", event.target.value)}
                  style={{ padding: "12px 14px", borderRadius: "10px", border: "1px solid #d0d5dd" }}
                />
              </label>

              <label style={{ display: "grid", gap: "8px" }}>
                <span>نص الرسالة</span>
                <textarea
                  rows={6}
                  value={form.message}
                  onChange={(event) => handleChange("message", event.target.value)}
                  style={{ padding: "12px 14px", borderRadius: "10px", border: "1px solid #d0d5dd" }}
                />
              </label>

              <div style={{ display: "flex", gap: "12px", justifyContent: "flex-end", flexWrap: "wrap" }}>
                <Link href="/contact" className="button button--secondary">
                  العودة إلى التواصل
                </Link>
                <button type="submit" className="button button--primary" disabled={isSubmitting}>
                  {isSubmitting ? "جاري الإرسال..." : "إرسال الرسالة"}
                </button>
              </div>
            </form>
          </div>
        </section>
    </div>
  );
}
