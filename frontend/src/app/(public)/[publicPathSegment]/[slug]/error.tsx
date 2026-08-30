"use client";

import { ContentState } from "@/components/content";
import { Section } from "@/components/layout";
import { Button } from "@/components/ui";

import styles from "./EntityProfile.module.css";

export default function EntityProfileError({
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  return (
    <Section aria-labelledby="entity-runtime-error-title" spacing="roomy">
      <h1 className={styles.stateTitle} id="entity-runtime-error-title">
        تعذر عرض بيانات الجهة
      </h1>
      <ContentState
        action={<Button onClick={reset}>إعادة المحاولة</Button>}
        description="حدث خطأ غير متوقع أثناء عرض الصفحة. لم تُعرض أي رسالة داخلية أو بيانات بديلة."
        state="error"
        title="بيانات الجهة غير متاحة مؤقتًا"
      />
    </Section>
  );
}
