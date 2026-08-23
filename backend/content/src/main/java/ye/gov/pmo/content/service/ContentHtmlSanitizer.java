package ye.gov.pmo.content.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

@Component
public class ContentHtmlSanitizer {
    private static final Safelist ALLOWED = Safelist.basic()
            .addTags("h2", "h3", "h4", "figure", "figcaption")
            .addAttributes("a", "target")
            .addProtocols("a", "href", "http", "https", "mailto");

    public String sanitize(String html) {
        Document.OutputSettings settings = new Document.OutputSettings().prettyPrint(false);
        return Jsoup.clean(html, "", ALLOWED, settings);
    }
}
