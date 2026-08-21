"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { api } from "@/lib/api";
import { setStoredAuthToken } from "@/lib/auth";

export default function LoginPage() {
  const router = useRouter();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError("");
    setIsSubmitting(true);

    try {
      const response = await api.login({ username, password });
      setStoredAuthToken(response.token);
      router.push("/admin");
    } catch {
      setError("فشل تسجيل الدخول. تحقق من اسم المستخدم وكلمة المرور.");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <main className="container section" style={{ maxWidth: "540px" }}>
      <div className="list-card">
        <p className="card__meta">تسجيل الدخول</p>
        <h1 style={{ marginTop: "8px" }}>لوحة الإدارة</h1>
        <p style={{ color: "#475467" }}>
          استخدم حساب المشرف للوصول إلى إدارة المحتوى الحكومي.
        </p>

        <form onSubmit={handleSubmit} style={{ display: "grid", gap: "18px", marginTop: "24px" }}>
          <label style={{ display: "grid", gap: "8px" }}>
            <span>اسم المستخدم</span>
            <input
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              autoComplete="username"
              required
              style={{ padding: "12px 14px", borderRadius: "10px", border: "1px solid #d0d5dd" }}
            />
          </label>

          <label style={{ display: "grid", gap: "8px" }}>
            <span>كلمة المرور</span>
            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              autoComplete="current-password"
              required
              style={{ padding: "12px 14px", borderRadius: "10px", border: "1px solid #d0d5dd" }}
            />
          </label>

          {error ? (
            <div className="notice notice--warning" role="alert">
              {error}
            </div>
          ) : null}

          <div style={{ display: "flex", gap: "12px", alignItems: "center", flexWrap: "wrap" }}>
            <button type="submit" className="button button--primary" disabled={isSubmitting}>
              {isSubmitting ? "جاري تسجيل الدخول..." : "دخول الإدارة"}
            </button>
            <Link href="/" className="button button--secondary">
              العودة للرئيسية
            </Link>
          </div>
        </form>
      </div>
    </main>
  );
}
