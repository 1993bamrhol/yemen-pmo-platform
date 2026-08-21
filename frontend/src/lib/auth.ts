export const AUTH_STORAGE_KEY = "pmo_admin_token";

export function getStoredAuthToken(): string | null {
  if (typeof window === "undefined") {
    return null;
  }

  return sessionStorage.getItem(AUTH_STORAGE_KEY);
}

export function setStoredAuthToken(token: string): void {
  if (typeof window === "undefined") {
    return;
  }

  sessionStorage.setItem(AUTH_STORAGE_KEY, token);
  localStorage.removeItem(AUTH_STORAGE_KEY);
}

export function clearStoredAuthToken(): void {
  if (typeof window === "undefined") {
    return;
  }

  sessionStorage.removeItem(AUTH_STORAGE_KEY);
  localStorage.removeItem(AUTH_STORAGE_KEY);
}
