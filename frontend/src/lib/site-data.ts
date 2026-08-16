export type NavItem = {
  label: string;
  href: string;
};

export type HighlightCard = {
  id?: number;
  title: string;
  date: string;
  category: string;
  excerpt: string;
};

export type ListItem = {
  title: string;
  meta: string;
  description: string;
};

export type HeroStory = {
  title: string;
  description: string;
  ctaLabel: string;
  secondaryCtaLabel: string;
};

export type Metric = {
  label: string;
  value: string;
};

export type Channel = {
  label: string;
  value: string;
};

export type ServiceCard = {
  title: string;
  description: string;
};

export type PortalHomeContent = {
  hero: HeroStory;
  stats: Metric[];
  portalHighlights: string[];
  officialChannels: Channel[];
  latestNews: HighlightCard[];
  officialAnnouncements?: HighlightCard[];
  officialStatements: ListItem[];
  decisions: ListItem[];
  serviceCards: ServiceCard[];
  services: string[];
  documents: string[];
  mediaItems: string[];
  governancePrinciples: string[];
};

export const navItems: NavItem[] = [
  { label: "الرئيسية", href: "/" },
  { label: "الأخبار", href: "#news" },
  { label: "الإعلانات", href: "#announcements" },
  { label: "البيانات", href: "#statements" },
  { label: "القرارات", href: "#decisions" },
  { label: "الخدمات", href: "#services" },
  { label: "الوثائق", href: "#documents" },
  { label: "الإدارة", href: "/admin" },
  { label: "تواصل معنا", href: "#contact" }
];

export const heroStory: HeroStory = {
  title: "بوابة رئاسة مجلس الوزراء اليمني: المصدر الرسمي للمعلومة الحكومية",
  description:
    "منصة سيادية حديثة تجمع الأخبار والبيانات والقرارات والخدمات في واجهة عربية واضحة تعكس الهوية اليمنية الرسمية.",
  ctaLabel: "استعراض المحتوى الرسمي",
  secondaryCtaLabel: "الانتقال إلى الأخبار"
};

export const stats: Metric[] = [
  { label: "أخبار حديثة", value: "24" },
  { label: "بيانات رسمية", value: "11" },
  { label: "قرارات", value: "8" },
  { label: "وثائق", value: "35" }
];

export const portalHighlights = [
  "المصدر الرسمي للمعلومة الحكومية",
  "واجهة عربية أولًا",
  "أرشفة منظمة وقابلة للبحث",
  "خدمات عامة موثوقة"
];

export const officialChannels: Channel[] = [
  { label: "البريد الرسمي", value: "info@example.gov.ye" },
  { label: "مركز الاتصال", value: "+967 1 000 000" },
  { label: "ساعات الخدمة", value: "الأحد - الخميس" }
];

export const latestNews: HighlightCard[] = [
  {
    title: "اجتماع لمناقشة أولويات الخدمات الحكومية الرقمية",
    date: "16 أغسطس 2026",
    category: "الأخبار",
    excerpt: "رئاسة الوزراء تتابع خطوات تنفيذ المنصة الرقمية الموحدة."
  },
  {
    title: "اعتماد الإطار المؤسسي للبوابة الرسمية",
    date: "15 أغسطس 2026",
    category: "البيانات",
    excerpt: "الوثيقة تحدد الأهداف والفئات المستهدفة والهيكل التنظيمي."
  },
  {
    title: "إطلاق المرحلة الأولى من المحتوى الرسمي",
    date: "14 أغسطس 2026",
    category: "القرارات",
    excerpt: "بدء نشر الأخبار والقرارات والتعاميم عبر البوابة الجديدة."
  }
];

export const officialAnnouncements: HighlightCard[] = [
  {
    title: "إعلان رسمي عن إطلاق المرحلة الأولى من البوابة الحكومية",
    date: "18 أغسطس 2026",
    category: "إعلان رسمي",
    excerpt: "تبدأ الرئاسة في نشر البيانات الرسمية والخدمات الأساسية عبر البوابة الموحدة."
  },
  {
    title: "تحديث نظام الاستقبال الإلكتروني للملاحظات والاقتراحات",
    date: "17 أغسطس 2026",
    category: "خدمة عامة",
    excerpt: "يتم توحيد قنوات الاستقبال ومراجعة الطلبات بطريقة موحدة وشفافة."
  },
  {
    title: "إعلان حول آلية نشر البيانات والوثائق الرسمية",
    date: "16 أغسطس 2026",
    category: "إرشاد",
    excerpt: "يحدد الإعلان أوقات النشر ومراجعة المحتوى وتحديث الوثائق الرسمية."
  }
];

export const officialStatements: ListItem[] = [
  {
    title: "بيان رسمي حول تقدم أعمال البوابة",
    meta: "بيان رسمي",
    description: "التأكيد على أن البوابة ستكون المصدر الرسمي للمعلومة الحكومية."
  },
  {
    title: "تحديثات تنظيمية على مسار النشر",
    meta: "أمانة عامة",
    description: "ضبط إجراءات النشر والمراجعة والصلاحيات التحريرية."
  }
];

export const decisions: ListItem[] = [
  {
    title: "قرار اعتماد الهوية البصرية الرسمية",
    meta: "قرار",
    description: "اعتماد الألوان والخطوط والطابع الرسمي للبوابة."
  },
  {
    title: "تعميم تنظيم المحتوى الحكومي",
    meta: "تعميم",
    description: "تحديد أسلوب النشر والتصنيف والأرشفة."
  }
];

export const services = [
  "إرسال استفسار",
  "تقديم اقتراح",
  "تحميل الوثائق",
  "الأسئلة الشائعة"
];

export const documents = [
  "وثيقة التحليل المؤسسي",
  "خطة MVP",
  "دليل الهوية البصرية",
  "خارطة الموقع"
];

export const mediaItems = [
  "صور رسمية",
  "فيديوهات",
  "تصريحات",
  "تغطيات"
];

export const serviceCards: ServiceCard[] = [
  {
    title: "تواصل رسمي",
    description: "قنوات واضحة لتلقي الاستفسارات والملاحظات والاقتراحات."
  },
  {
    title: "الوثائق والتحميلات",
    description: "وصول مباشر إلى الملفات الرسمية والقرارات والأدلة."
  },
  {
    title: "الأسئلة الشائعة",
    description: "إجابات سريعة حول الخدمات والمحتوى وآلية الاستخدام."
  }
];

export const governancePrinciples = [
  "الشفافية",
  "السرعة",
  "الدقة",
  "الموثوقية",
  "إمكانية الوصول"
];

export const portalHomeFallback: PortalHomeContent = {
  hero: heroStory,
  stats,
  portalHighlights,
  officialChannels,
  latestNews,
  officialAnnouncements,
  officialStatements,
  decisions,
  serviceCards,
  services,
  documents,
  mediaItems,
  governancePrinciples
};
