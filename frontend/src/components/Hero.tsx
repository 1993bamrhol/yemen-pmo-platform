import type { PortalHomeContent } from "@/lib/site-data";

type HeroProps = Pick<
  PortalHomeContent,
  "hero" | "stats" | "portalHighlights" | "officialChannels"
>;

export function Hero({ hero, stats, portalHighlights, officialChannels }: HeroProps) {
  return (
    <section className="hero">
      <div className="container hero__grid">
        <div className="hero__content">
          <span className="eyebrow">الهوية الحكومية الرسمية</span>
          <h2>{hero.title}</h2>
          <p>{hero.description}</p>

          <div className="hero__actions">
            <a className="button button--primary" href="#statements">
              {hero.ctaLabel}
            </a>
            <a className="button button--secondary" href="#news">
              {hero.secondaryCtaLabel}
            </a>
          </div>

          <div className="hero__highlights" aria-label="مزايا البوابة">
            {portalHighlights.map((item) => (
              <span key={item} className="chip">
                {item}
              </span>
            ))}
          </div>
        </div>

        <aside className="hero__panel" aria-label="إحصاءات البوابة والقنوات الرسمية">
          <div className="panel-card">
            <p className="panel-card__eyebrow">مؤشرات البوابة</p>
            {stats.map((item) => (
              <div key={item.label} className="stat">
                <span className="stat__label">{item.label}</span>
                <span className="stat__value">{item.value}</span>
              </div>
            ))}
          </div>

          <div className="panel-card panel-card--accent">
            <p className="panel-card__eyebrow">قنوات رسمية</p>
            <ul className="channel-list">
              {officialChannels.map((item) => (
                <li key={item.label}>
                  <strong>{item.label}:</strong> {item.value}
                </li>
              ))}
            </ul>
          </div>
        </aside>
      </div>
    </section>
  );
}
