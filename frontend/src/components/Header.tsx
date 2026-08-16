import Link from "next/link";
import { navItems } from "@/lib/site-data";

export function Header() {
  return (
    <header className="header">
      <div className="header__topbar">
        <div className="container header__topbar-inner">
          <span>الجمهورية اليمنية</span>
          <span>بوابة حكومية رسمية</span>
          <span>آخر تحديث: 16 أغسطس 2026</span>
        </div>
      </div>

      <div className="container header__inner">
        <Link href="/" className="brand" aria-label="العودة إلى الصفحة الرئيسية">
          <div className="brand__logo" aria-hidden="true">
            PMO
          </div>
          <div>
            <p className="brand__eyebrow">رئاسة مجلس الوزراء</p>
            <h1 className="brand__title">بوابة رئاسة مجلس الوزراء اليمني</h1>
          </div>
        </Link>

        <nav className="nav" aria-label="التنقل الرئيسي">
          {navItems.map((item) => (
            <Link key={item.label} href={item.href} className="nav__link">
              {item.label}
            </Link>
          ))}
        </nav>

        <div className="header__actions">
          <button className="search-button" type="button">
            بحث
          </button>
          <Link className="button button--primary button--compact" href="/contact">
            تواصل
          </Link>
        </div>
      </div>
    </header>
  );
}
