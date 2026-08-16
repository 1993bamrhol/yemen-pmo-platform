import Link from "next/link";

export function Footer() {
  return (
    <footer className="footer">
      <div className="container footer__grid">
        <div>
          <h4>بوابة رئاسة مجلس الوزراء اليمني</h4>
          <p>منصة رسمية حديثة تعكس الهوية اليمنية وتخدم المواطن والمؤسسات.</p>
        </div>
        <div>
          <h4>روابط سريعة</h4>
          <ul>
            <li><Link href="/">الرئيسية</Link></li>
            <li><Link href="/services">الخدمات</Link></li>
            <li><Link href="/contact">تواصل معنا</Link></li>
          </ul>
        </div>
        <div id="contact">
          <h4>تواصل معنا</h4>
          <p>الإدارة العامة للبوابة الرقمية</p>
          <p>البريد: info@example.gov.ye</p>
          <p>الهاتف: +967 1 000 000</p>
        </div>
      </div>
    </footer>
  );
}
