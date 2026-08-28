import Link from "next/link";

import {
  MobileNavigation,
  PrimaryNavigation,
} from "@/components/navigation";
import { TextLink } from "@/components/ui";

import styles from "./Shell.module.css";

function GovernmentBrand({ compact = false }: { compact?: boolean }) {
  return (
    <Link
      aria-label="الانتقال إلى الصفحة الرئيسية"
      className={styles.brand}
      data-size={compact ? "compact" : "desktop"}
      href="/"
    >
      <span aria-hidden="true" className={styles.brandMark}>
        ي
      </span>
      <span className={styles.brandCopy}>
        <span className={styles.brandTitle}>المنصة الحكومية اليمنية</span>
        <span className={styles.brandDescriptor}>بوابة حكومية رسمية</span>
      </span>
    </Link>
  );
}

export function GovernmentHeader() {
  return (
    <header className={styles.governmentHeader}>
      <div className={styles.headerFrame}>
        <div className={styles.utilityBar}>
          <div className={styles.desktopBrand}>
            <GovernmentBrand />
          </div>
          <div className={styles.utilityMeta}>
            <span>الجمهورية اليمنية</span>
            <span>بوابة حكومية رقمية</span>
          </div>
        </div>

        <div className={styles.headerMain}>
          <div className={styles.mobileBrand}>
            <GovernmentBrand compact />
          </div>

          <PrimaryNavigation className={styles.desktopNavigation} />

          <div className={styles.headerActions}>
            <TextLink className={styles.headerAction} href="/contact">
              تواصل معنا
            </TextLink>
          </div>

          <MobileNavigation className={styles.mobileNavigation} />
        </div>
      </div>
    </header>
  );
}
