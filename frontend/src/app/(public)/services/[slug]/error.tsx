"use client";

import { ContentState } from "@/components/content";
import { Section } from "@/components/layout";
import { Button } from "@/components/ui";

import styles from "./ServiceDetail.module.css";

export default function ServiceDetailError({
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  return (
    <Section aria-labelledby="service-runtime-error-title" spacing="roomy">
      <h1 className={styles.stateTitle} id="service-runtime-error-title">
        تعذر عرض تفاصيل الخدمة
      </h1>
      <ContentState
        action={<Button onClick={reset}>إعادة المحاولة</Button>}
        description="حدث خطأ غير متوقع أثناء عرض الصفحة. لم تُعرض أي رسالة داخلية أو بيانات بديلة."
        state="error"
        title="الخدمة غير متاحة مؤقتًا"
      />
    </Section>
  );
}
