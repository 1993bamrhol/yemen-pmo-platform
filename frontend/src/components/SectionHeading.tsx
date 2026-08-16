type SectionHeadingProps = {
  title: string;
  description: string;
};

export function SectionHeading({ title, description }: SectionHeadingProps) {
  return (
    <div className="section-heading">
      <span className="eyebrow">بوابة رسمية</span>
      <h3>{title}</h3>
      <p>{description}</p>
    </div>
  );
}
