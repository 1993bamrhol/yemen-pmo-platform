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
  id?: number;
  title: string;
  meta: string;
  description: string;
};

export type DocumentLink = {
  id: number;
  title: string;
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
  documents: Array<string | DocumentLink>;
  mediaItems: string[];
  governancePrinciples: string[];
};

export const navItems: NavItem[] = [
  { label: "الرئيسية", href: "/" },
  { label: "من نحن", href: "/about" },
  { label: "الأخبار", href: "/#news" },
  { label: "الإعلانات", href: "/#announcements" },
  { label: "البيانات", href: "/#statements" },
  { label: "القرارات", href: "/#decisions" },
  { label: "الخدمات", href: "/services" },
  { label: "الاستفسارات", href: "/complaints" },
  { label: "الوثائق", href: "/#documents" }
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
  { label: "المراسلات", value: "نموذج التواصل الإلكتروني" },
  { label: "الاستفسارات", value: "بوابة الطلبات الرسمية" },
  { label: "ساعات الخدمة", value: "الأحد - الخميس" }
];

export const latestNews: HighlightCard[] = [
  {
    id: 1,
    title: "اجتماع لمناقشة أولويات الخدمات الحكومية الرقمية",
    date: "16 أغسطس 2026",
    category: "الأخبار",
    excerpt: "رئاسة الوزراء تتابع خطوات تنفيذ المنصة الرقمية الموحدة."
  },
  {
    id: 2,
    title: "اعتماد الإطار المؤسسي للبوابة الرسمية",
    date: "15 أغسطس 2026",
    category: "البيانات",
    excerpt: "الوثيقة تحدد الأهداف والفئات المستهدفة والهيكل التنظيمي."
  },
  {
    id: 3,
    title: "إطلاق المرحلة الأولى من المحتوى الرسمي",
    date: "14 أغسطس 2026",
    category: "القرارات",
    excerpt: "بدء نشر الأخبار والقرارات والتعاميم عبر البوابة الجديدة."
  }
];

export const officialAnnouncements: HighlightCard[] = [
  {
    id: 1,
    title: "إعلان رسمي عن إطلاق المرحلة الأولى من البوابة الحكومية",
    date: "18 أغسطس 2026",
    category: "إعلان رسمي",
    excerpt: "تبدأ الرئاسة في نشر البيانات الرسمية والخدمات الأساسية عبر البوابة الموحدة."
  },
  {
    id: 2,
    title: "تحديث نظام الاستقبال الإلكتروني للملاحظات والاقتراحات",
    date: "17 أغسطس 2026",
    category: "خدمة عامة",
    excerpt: "يتم توحيد قنوات الاستقبال ومراجعة الطلبات بطريقة موحدة وشفافة."
  },
  {
    id: 3,
    title: "إعلان حول آلية نشر البيانات والوثائق الرسمية",
    date: "16 أغسطس 2026",
    category: "إرشاد",
    excerpt: "يحدد الإعلان أوقات النشر ومراجعة المحتوى وتحديث الوثائق الرسمية."
  }
];

export const officialStatements: ListItem[] = [
  {
    id: 1,
    title: "بيان رسمي حول تقدم أعمال البوابة",
    meta: "بيان رسمي",
    description: "التأكيد على أن البوابة ستكون المصدر الرسمي للمعلومة الحكومية."
  },
  {
    id: 2,
    title: "تحديثات تنظيمية على مسار النشر",
    meta: "أمانة عامة",
    description: "ضبط إجراءات النشر والمراجعة والصلاحيات التحريرية."
  }
];

export const decisions: ListItem[] = [
  {
    id: 1,
    title: "قرار اعتماد الهوية البصرية الرسمية",
    meta: "قرار",
    description: "اعتماد الألوان والخطوط والطابع الرسمي للبوابة."
  },
  {
    id: 2,
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

export const documents: DocumentLink[] = [
  { id: 1, title: "وثيقة التحليل المؤسسي" },
  { id: 2, title: "خطة إطلاق البوابة" },
  { id: 3, title: "دليل الهوية البصرية" },
  { id: 4, title: "خارطة الموقع" }
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
