import type { Metadata } from "next";
import type { Viewport } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "بوابة رئاسة مجلس الوزراء اليمني",
  description: "بوابة حكومية رسمية حديثة تعكس الهوية اليمنية وتجمع المحتوى والخدمات الرسمية."
};

export const viewport: Viewport = {
  themeColor: "#B21F2D"
};

export default function RootLayout({
  children
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ar" dir="rtl">
      <body>{children}</body>
    </html>
  );
}
