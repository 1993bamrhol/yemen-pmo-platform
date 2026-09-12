"use client";

import { ContentState } from "@/components/content";
import { Section } from "@/components/layout";
import { Button } from "@/components/ui";

import styles from "./NewsDetail.module.css";

export default function NewsDetailError({ reset }: { reset: () => void }) {
  return (
    <Section aria-labelledby="news-runtime-error-title" spacing="roomy">
      <h1 className={styles.stateTitle} id="news-runtime-error-title">
        تعذر عرض الخبر
      </h1>
      <ContentState
        action={<Button onClick={reset}>إعادة المحاولة</Button>}
        description="حدث خطأ غير متوقع أثناء عرض الصفحة. لم تُعرض أي رسالة داخلية أو بيانات بديلة."
        state="error"
        title="الخبر غير متاح مؤقتًا"
      />
    </Section>
  );
}
