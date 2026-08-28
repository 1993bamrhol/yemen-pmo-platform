export type PublicNavigationItem = {
  href: "/" | "/about" | "/complaints" | "/contact" | "/services";
  label: string;
};

export const PUBLIC_NAVIGATION_ITEMS: readonly PublicNavigationItem[] = [
  { href: "/", label: "الرئيسية" },
  { href: "/services", label: "الخدمات" },
  { href: "/about", label: "عن المنصة" },
  { href: "/complaints", label: "الاستفسارات والملاحظات" },
  { href: "/contact", label: "تواصل معنا" },
];

export function isCurrentNavigationItem(pathname: string, href: string): boolean {
  return href === "/"
    ? pathname === href
    : pathname === href || pathname.startsWith(`${href}/`);
}
