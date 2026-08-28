import Link from "next/link";
import { SectionHeading } from "@/components/SectionHeading";

const serviceDirectory = [
  {
    title: "تقديم استفسار",
    description: "إرسال استفسارات المواطنين حول الإجراءات والخدمات والقرارات الرسمية مباشرة إلى الجهة المختصة.",
    meta: "استفسار"
  },
  {
    title: "تقديم اقتراح",
    description: "استقبال المقترحات التحسينية والآراء المتعلقة بتحسين الخدمات العامة ورفع كفاءة الأداء المؤسسي.",
    meta: "مقترح"
  },
  {
    title: "طلب وثيقة أو نسخة رسمية",
    description: "الاستعلام عن الوثائق الرسمية والملفات المعتمدة وتقديم طلب الحصول عليها عبر القنوات الرقمية الملتزم بها.",
    meta: "وثيقة"
  },
  {
    title: "استفسار عن الإجراءات الإدارية",
    description: "توضيح مسار المعاملات الإدارية والمهلة الزمنية ومتطلبات الإفصاح والوثائق المطلوبة.",
    meta: "إجراء"
  },
  {
    title: "مراجعة الخدمة الرقمية",
    description: "تقييم تجربة المستخدم للخدمة وتسجيل الملاحظات التشغيلية أو التقارير الفنية المتعلقة بالتفاعل الإلكتروني.",
    meta: "تقييم"
  },
  {
    title: "التواصل مع مركز الخدمة",
    description: "الوصول إلى المركز الرسمي للاستفسار والرد على الاستفسارات المتعلقة بالبوابة أو الإجراءات الحكومية المتاحة.",
    meta: "مركز خدمة"
  }
];

export default function ServicesPage() {
  return (
    <div className="container section">
        <SectionHeading
          title="الخدمات الإلكترونية"
          description="خدمات أساسية وموثوقة تستجيب لاحتياجات المواطنين والجهات الحكومية في إطار من الشفافية والسرعة والموثوقية."
        />

        <div className="info-grid" style={{ marginTop: "24px" }}>
          {serviceDirectory.map((item) => (
            <article key={item.title} className="info-card">
              <span className="card__meta">{item.meta}</span>
              <h3>{item.title}</h3>
              <p>{item.description}</p>
            </article>
          ))}
        </div>

        <section className="section" style={{ paddingTop: "0" }}>
          <div className="list-card">
            <h3 style={{ marginTop: 0 }}>مسارات الخدمة</h3>
            <ol style={{ margin: 0, paddingRight: "20px", display: "grid", gap: "10px" }}>
              <li>تقديم الطلب أو الاستفسار عبر البوابة الرسمية.</li>
              <li>تدقيق الطلب من قبل الجهة المختصة وتحديد نوع الخدمة.</li>
              <li>تحديث الحالة للمستخدم عبر القنوات الرسمية المعتمدة.</li>
              <li>تسليم النتيجة أو الوثيقة أو الرد الرسمي في الوقت المحدد.</li>
            </ol>
          </div>
        </section>

        <div style={{ display: "flex", justifyContent: "center", marginTop: "12px" }}>
          <Link href="/contact" className="button button--primary">
            التواصل مع الإدارة
          </Link>
        </div>
    </div>
  );
}
