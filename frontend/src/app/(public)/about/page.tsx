import Link from "next/link";
import { SectionHeading } from "@/components/SectionHeading";

const leadershipAreas = [
  {
    title: "القيادة والسياسة",
    description: "تحديد الأولويات الحكومية، ومتابعة الخطة الوطنية، وتنسيق أطر العمل بين الجهات التنفيذية والقطاع العام."
  },
  {
    title: "التخطيط والبرامج",
    description: "إعداد الاستراتيجيات ومؤشرات الأداء وتوجيه الجهود نحو تحقيق الأهداف الوطنية والاستجابة لاحتياجات المواطنين."
  },
  {
    title: "الحوكمة الرقمية",
    description: "تحديث المنصات الرسمية، وتوحيد قنوات التواصل، وتحسين المعلومة الحكومية عبر أدوات رقمية موثوقة ومنظمة."
  },
  {
    title: "الشفافية والمساءلة",
    description: "تعزيز التفاعل مع المواطنين، وتوفير المعلومة في الوقت المناسب، وضمان الوصول العادل إلى الخدمات والبيانات الرسمية."
  }
];

const organizationChart = [
  "رئاسة مجلس الوزراء",
  "الأمانة العامة",
  "الهيئة العامة للمعلومات",
  "الإدارة التنفيذية",
  "مراكز الخدمات الرقمية",
  "الجهات الحكومية ذات العلاقة"
];

export default function AboutPage() {
  return (
    <div className="container section">
        <SectionHeading
          title="من نحن"
          description="رئاسة مجلس الوزراء اليمني هي الجهة التنفيذية المسؤولة عن تنسيق السياسات الحكومية، متابعة أولويات الحكومة، وتطوير البنية المؤسسية الرقمية للخدمات العامة."
        />

        <div className="list-card" style={{ marginTop: "24px" }}>
          <p style={{ marginTop: 0 }}>
            تسهم الرئاسة في ترجمة السياسات العامة إلى برامج تنفيذية، وتنسيق العمل الحكومي بين الجهات التنفيذية، مع الالتزام بمبادئ الشفافية، الفعالية، والحوكمة الرشيدة، وصولًا إلى خدمة موثوقة لمواطني الدولة ومؤسساتها.
          </p>
        </div>

        <section className="section" style={{ paddingTop: "0" }}>
          <SectionHeading
            title="مجالات العمل"
            description="تتسع مهام الرئاسة في مجالات القيادة، التخطيط، الحوكمة، والارتقاء نحو منظومة خدمات عامة أكثر كفاءة."
          />
          <div className="info-grid">
            {leadershipAreas.map((item) => (
              <article key={item.title} className="info-card">
                <span className="card__meta">قطاع</span>
                <h3>{item.title}</h3>
                <p>{item.description}</p>
              </article>
            ))}
          </div>
        </section>

        <section className="section" style={{ paddingTop: "0" }}>
          <SectionHeading
            title="الهيكل المؤسسي"
            description="تعمل الرئاسة في إطار هيكل تنظيمي يضمن التنسيق بين القيادة، الأمانة العامة، والجهات ذات العلاقة في تنفيذ أولويات الدولة."
          />
          <div className="pill-grid">
            {organizationChart.map((item) => (
              <span key={item} className="pill pill--muted">
                {item}
              </span>
            ))}
          </div>
        </section>

        <div style={{ display: "flex", justifyContent: "center", marginTop: "12px" }}>
          <Link href="/contact" className="button button--primary">
            التواصل مع الدولة
          </Link>
        </div>
    </div>
  );
}
