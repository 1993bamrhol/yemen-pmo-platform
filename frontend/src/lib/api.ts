import type { PortalHomeContent } from "@/lib/site-data";

function getBaseUrl(): string {
  if (typeof window === "undefined") {
    return process.env.API_BASE_URL ?? process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";
  }

  return process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";
}

export class ApiError extends Error {
  constructor(public readonly status: number) {
    super(`Request failed: ${status}`);
    this.name = "ApiError";
  }
}

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  // Keep public pages responsive when the API is unavailable. Each data-fed
  // section owns its explicit loading, empty, and error presentation.
  const timeoutMs = typeof window === "undefined" ? 1_500 : 10_000;
  const response = await fetch(`${getBaseUrl()}${path}`, {
    cache: "no-store",
    ...init,
    signal: init.signal ?? AbortSignal.timeout(timeoutMs)
  });

  if (!response.ok) {
    throw new ApiError(response.status);
  }

  const text = await response.text();
  if (!text) {
    return undefined as T;
  }

  return JSON.parse(text) as T;
}

export type AuthLoginRequest = {
  username: string;
  password: string;
};

export type AuthSession = {
  token: string;
  tokenType: string;
  username: string;
  roles: string[];
};

export type SupportSubmission = {
  fullName: string;
  email: string;
  phone?: string;
  category: string;
  subject: string;
  message: string;
};

export type SupportResponse = SupportSubmission & {
  id?: number;
  createdAt?: string;
};

export type SupportInboxItem = {
  id: number;
  fullName: string;
  email: string;
  phone?: string;
  category: string;
  subject: string;
  message: string;
  status: string;
  createdAt: string;
};

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

export type EntityTypeItem = {
  id: number;
  code: string;
  name: string;
  pathSegment: string;
};

export type GovernmentEntityItem = {
  canonicalPath: string;
  description?: string | null;
  id: string;
  officialName: string;
  parent?: {
    canonicalPath: string;
    id: string;
    officialName: string;
  } | null;
  shortName?: string | null;
  slug: string;
  status: string;
  type?: EntityTypeItem | null;
  websiteUrl?: string | null;
};

export type GovernmentServiceDetailItem = {
  description?: string | null;
  order: number;
  title: string;
};

export type GovernmentServiceChannel = {
  actionUrl?: string | null;
  instructions?: string | null;
  label?: string | null;
  order: number;
  type: "ONLINE" | "IN_PERSON" | "PHONE";
};

export type GovernmentServiceOwner = {
  canonicalPath: string;
  id: string;
  officialName: string;
};

export type GovernmentServiceSource = {
  reference: string;
  type:
    | "OFFICIAL_MANUAL_ENTRY"
    | "OFFICIAL_SOURCE_REFERENCE"
    | "APPROVED_IMPORT";
  verifiedAt: string;
};

export type GovernmentServiceDetail = {
  canonicalPath: string;
  channels: GovernmentServiceChannel[];
  description?: string | null;
  eligibility: GovernmentServiceDetailItem[];
  fees?: string | null;
  id: string;
  locale: string;
  officialName: string;
  officialNameEn?: string | null;
  ownerEntity: GovernmentServiceOwner;
  processingTime?: string | null;
  publishedAt: string;
  requirements: GovernmentServiceDetailItem[];
  slug: string;
  source: GovernmentServiceSource;
  steps: GovernmentServiceDetailItem[];
  summary: string;
  updatedAt: string;
};

export const api = {
  login: (payload: AuthLoginRequest) =>
    request<AuthSession>("/api/auth/login", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(payload)
    }),
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
  getGovernmentEntities: () =>
    request<GovernmentEntityItem[]>("/api/v1/entities"),
  getGovernmentServiceBySlug: (slug: string) =>
    request<GovernmentServiceDetail>(
      `/api/v1/services/by-slug/${encodeURIComponent(slug)}`,
    ),
  getPortalHome: () => request<PortalHomeContent>("/api/portal/home"),
  submitSupportRequest: (payload: SupportSubmission) =>
    request<SupportResponse>("/api/support/requests", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(payload)
    }),
  getSupportRequests: (token: string) =>
    request<SupportInboxItem[]>("/api/support/requests", {
      headers: {
        Authorization: `Bearer ${token}`
      }
    }),
  updateSupportRequestStatus: (token: string, id: number, status: string) =>
    request<SupportInboxItem>(`/api/support/requests/${id}/status`, {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`
      },
      body: JSON.stringify({ status })
    }),
  getAdminContent: (token: string) =>
    request<AdminContentItem[]>("/api/admin/content", {
      headers: {
        Authorization: `Bearer ${token}`
      }
    }),
  getAdminSummary: (token: string) =>
    request<AdminContentSummary>("/api/admin/content/summary", {
      headers: {
        Authorization: `Bearer ${token}`
      }
    }),
  createAdminContent: (token: string, payload: AdminContentInput) =>
    request<AdminContentItem>("/api/admin/content", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`
      },
      body: JSON.stringify(payload)
    }),
  updateAdminContent: (token: string, id: number, payload: AdminContentInput) =>
    request<AdminContentItem>(`/api/admin/content/${id}`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`
      },
      body: JSON.stringify(payload)
    }),
  deleteAdminContent: (token: string, id: number) =>
    request<void>(`/api/admin/content/${id}`, {
      method: "DELETE",
      headers: {
        Authorization: `Bearer ${token}`
      }
    })
};

export type AdminContentInput = {
  type: string;
  title: string;
  status: string;
  author: string;
  category: string;
};

export type AdminContentItem = {
  id: number;
  type: string;
  title: string;
  status: string;
  author: string;
  category: string;
  updatedAt: string;
};

export type AdminContentSummary = {
  total: number;
  published: number;
  draft: number;
  archived: number;
};
