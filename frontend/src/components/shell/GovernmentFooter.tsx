import Image from "next/image";

import { PageContainer } from "@/components/layout";
import { TextLink } from "@/components/ui";

import styles from "./Shell.module.css";

const governmentLinks = [
  { href: "/", label: "الرئيسية" },
  { href: "/services", label: "الخدمات" },
  { href: "/complaints", label: "الاستفسارات والملاحظات" },
] as const;

const platformLinks = [
  { href: "/about", label: "عن المنصة" },
  { href: "/contact", label: "تواصل معنا" },
] as const;

export function GovernmentFooter() {
  return (
    <footer className={styles.governmentFooter}>
      <PageContainer className={styles.footerFrame}>
        <div className={styles.footerMain}>
          <div className={styles.footerBrand}>
            <Image
              alt=""
              aria-hidden="true"
              className={styles.footerMotifDesktop}
              height={60}
              src="/icons/yegov-footer-arch-desktop.svg"
              width={108}
            />
            <Image
              alt=""
              aria-hidden="true"
              className={styles.footerMotifMobile}
              height={27}
              src="/icons/yegov-footer-arch-mobile.svg"
              width={48}
            />
            <div className={styles.footerBrandRow}>
              <span aria-hidden="true" className={styles.brandMark}>
                ي
              </span>
              <p className={styles.footerTitle}>المنصة الحكومية اليمنية</p>
            </div>
            <p className={styles.footerDescription}>
              واجهة رقمية للوصول إلى المعلومات والمسارات الحكومية المنشورة عبر البوابة.
            </p>
          </div>

          <nav className={styles.footerSection} aria-labelledby="footer-government-links">
            <h2 className={styles.footerSectionTitle} id="footer-government-links">
              روابط البوابة
            </h2>
            <ul className={styles.footerLinks}>
              {governmentLinks.map((item) => (
                <li key={item.href}>
                  <TextLink href={item.href}>{item.label}</TextLink>
                </li>
              ))}
            </ul>
          </nav>

          <nav className={styles.footerSection} aria-labelledby="footer-platform-links">
            <h2 className={styles.footerSectionTitle} id="footer-platform-links">
              عن المنصة
            </h2>
            <ul className={styles.footerLinks}>
              {platformLinks.map((item) => (
                <li key={item.href}>
                  <TextLink href={item.href}>{item.label}</TextLink>
                </li>
              ))}
            </ul>
          </nav>
        </div>

        <div className={styles.footerBottom}>
          <span aria-hidden="true" className={styles.footerNationalAccent} />
          <p className={styles.footerMeta}>الجمهورية اليمنية · بوابة حكومية رقمية</p>
        </div>
      </PageContainer>
    </footer>
  );
}
