"use client";

import type { MouseEvent } from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { navItems } from "@/lib/site-data";

export function Header() {
  const pathname = usePathname();
  const closeMobileMenu = (event: MouseEvent<HTMLDivElement>) => {
    if ((event.target as HTMLElement).closest("a")) {
      event.currentTarget.closest("details")?.removeAttribute("open");
    }
  };

  const navigation = (
    <nav className="nav" aria-label="التنقل الرئيسي">
      {navItems.map((item) => (
        <Link
          key={item.label}
          href={item.href}
          className="nav__link"
          aria-current={item.href === pathname ? "page" : undefined}
        >
          {item.label}
        </Link>
      ))}
    </nav>
  );

  const renderSearchForm = (id: string) => (
    <form className="search-form" action="/#search-results" method="get" role="search">
      <label className="visually-hidden" htmlFor={id}>
        ابحث في محتوى البوابة
      </label>
      <input id={id} name="q" type="search" placeholder="ابحث في البوابة" minLength={2} />
      <button className="button button--primary button--compact" type="submit">
        بحث
      </button>
    </form>
  );

  return (
    <header className="header">
      <div className="header__topbar">
        <div className="container header__topbar-inner">
          <span>الجمهورية اليمنية</span>
          <span>بوابة حكومية رسمية</span>
          <span className="header__updated">آخر تحديث: 16 أغسطس 2026</span>
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

        <div className="header__desktop-nav">{navigation}</div>

        <div className="header__actions">
          <details className="search-menu">
            <summary className="search-button">بحث</summary>
            <div className="search-menu__panel">{renderSearchForm("desktop-site-search")}</div>
          </details>
          <Link className="button button--primary button--compact" href="/contact">
            تواصل
          </Link>
        </div>

        <details className="mobile-menu">
          <summary className="button button--secondary">القائمة</summary>
          <div className="mobile-menu__panel" onClick={closeMobileMenu}>
            {navigation}
            {renderSearchForm("mobile-site-search")}
            <Link className="button button--primary" href="/contact">
              تواصل معنا
            </Link>
          </div>
        </details>
      </div>
    </header>
  );
}
