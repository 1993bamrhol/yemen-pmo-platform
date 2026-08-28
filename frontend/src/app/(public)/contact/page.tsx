import Link from "next/link";
import { SectionHeading } from "@/components/SectionHeading";

const contactChannels = [
  { label: "البريد الإلكتروني", value: "info@example.gov.ye" },
  { label: "الهاتف", value: "+967 1 000 000" },
  { label: "العنوان", value: "صنعاء - الجمهورية اليمنية - رئاسة مجلس الوزراء" },
  { label: "ساعات الاستقبال", value: "الأحد - الخميس | 08:00 - 15:00" }
];

export default function ContactPage() {
  return (
    <div className="container section">
        <SectionHeading
          title="تواصل معنا"
          description="قنوات الاتصال الرسمية للبوابة، واستقبال الاستفسارات والملاحظات والاقتراحات على مستوى المؤسسة."
        />

        <div className="info-grid" style={{ marginTop: "24px" }}>
          {contactChannels.map((channel) => (
            <article key={channel.label} className="info-card">
              <span className="card__meta">{channel.label}</span>
              <h3>{channel.value}</h3>
            </article>
          ))}
        </div>

        <section className="section" style={{ paddingTop: "0" }}>
          <div className="list-card">
            <h3 style={{ marginTop: 0 }}>نموذج التواصل</h3>
            <form style={{ display: "grid", gap: "16px" }}>
              <label style={{ display: "grid", gap: "8px" }}>
                <span>الاسم الكامل</span>
                <input style={{ padding: "12px 14px", borderRadius: "10px", border: "1px solid #d0d5dd" }} placeholder="أدخل الاسم" />
              </label>
              <label style={{ display: "grid", gap: "8px" }}>
                <span>البريد الإلكتروني</span>
                <input type="email" style={{ padding: "12px 14px", borderRadius: "10px", border: "1px solid #d0d5dd" }} placeholder="name@example.com" />
              </label>
              <label style={{ display: "grid", gap: "8px" }}>
                <span>نوع التواصل</span>
                <select style={{ padding: "12px 14px", borderRadius: "10px", border: "1px solid #d0d5dd" }}>
                  <option>استفسار</option>
                  <option>اقتراح</option>
                  <option>شكوى</option>
                  <option>طلب وثيقة</option>
                </select>
              </label>
              <label style={{ display: "grid", gap: "8px" }}>
                <span>الرسالة</span>
                <textarea rows={5} style={{ padding: "12px 14px", borderRadius: "10px", border: "1px solid #d0d5dd" }} placeholder="اكتب رسالتك هنا" />
              </label>
              <div style={{ display: "flex", justifyContent: "flex-end", gap: "12px", flexWrap: "wrap" }}>
                <Link href="/complaints" className="button button--secondary">
                  نموذج الاستفسارات
                </Link>
                <button type="button" className="button button--primary">
                  إرسال الرسالة
                </button>
              </div>
            </form>
          </div>
        </section>

        <div style={{ display: "flex", justifyContent: "center", marginTop: "12px" }}>
          <Link href="/services" className="button button--secondary">
            عرض الخدمات
          </Link>
        </div>
    </div>
  );
}
