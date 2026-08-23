package ye.gov.pmo.bootstrap.backfill;

import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class BackfillCategoryCatalog {
    private static final Map<String, String> SLUGS = Map.ofEntries(
            Map.entry("الأخبار", "news"),
            Map.entry("الإعلانات", "announcements"),
            Map.entry("القرارات", "decisions"),
            Map.entry("الوثائق", "documents"),
            Map.entry("البيانات", "statements"),
            Map.entry("إعلان رسمي", "official-announcement"),
            Map.entry("خدمة عامة", "public-service"),
            Map.entry("إرشاد", "guidance"),
            Map.entry("قرار", "decision"),
            Map.entry("تعميم", "circular"),
            Map.entry("توجيه", "directive"),
            Map.entry("وثيقة", "document"),
            Map.entry("خطة", "plan"),
            Map.entry("دليل", "guide"));

    public Optional<String> slugFor(String label) {
        return Optional.ofNullable(SLUGS.get(label));
    }
}
