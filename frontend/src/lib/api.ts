import type { PortalHomeContent } from "@/lib/site-data";

const baseUrl = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

async function request<T>(path: string): Promise<T> {
  const response = await fetch(`${baseUrl}${path}`, {
    cache: "no-store"
  });

  if (!response.ok) {
    throw new Error(`Request failed: ${response.status}`);
  }

  return response.json() as Promise<T>;
}

export type NewsItem = {
  id?: number;
  title: string;
  date: string;
  category: string;
  excerpt: string;
};

export type AnnouncementItem = {
  id?: number;
  title: string;
  date: string;
  category: string;
  excerpt: string;
};

export type DecisionItem = {
  id?: number;
  title: string;
  category: string;
  date: string;
  description: string;
};

export type DocumentItem = {
  id?: number;
  title: string;
  category: string;
  updatedAt: string;
  description: string;
};

export const api = {
  getUsers: () => request<unknown[]>("/api/users"),
  getRoles: () => request<unknown[]>("/api/roles"),
  getPermissions: () => request<unknown[]>("/api/permissions"),
  getNews: () => request<NewsItem[]>("/api/news"),
  getNewsById: (id: number) => request<NewsItem>(`/api/news/${id}`),
  getAnnouncements: () => request<AnnouncementItem[]>("/api/announcements"),
  getAnnouncementById: (id: number) => request<AnnouncementItem>(`/api/announcements/${id}`),
  getDecisions: () => request<DecisionItem[]>("/api/decisions"),
  getDecisionById: (id: number) => request<DecisionItem>(`/api/decisions/${id}`),
  getDocuments: () => request<DocumentItem[]>("/api/documents"),
  getDocumentById: (id: number) => request<DocumentItem>(`/api/documents/${id}`),
  getPortalHome: () => request<PortalHomeContent>("/api/portal/home")
};
