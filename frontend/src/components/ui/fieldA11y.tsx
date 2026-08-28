import type { ReactNode } from "react";

interface FieldMessageProps {
  className?: string;
  error?: string;
  helperText?: ReactNode;
  id: string;
}

export function getFieldDescriptionId(
  id: string,
  helperText: ReactNode | undefined,
  error: string | undefined,
  externalDescriptionId?: string,
): string | undefined {
  const localId = error
    ? `${id}-error`
    : helperText
      ? `${id}-description`
      : undefined;

  return [externalDescriptionId, localId].filter(Boolean).join(" ") || undefined;
}

export function FieldMessage({ className, error, helperText, id }: FieldMessageProps) {
  const message = error ?? helperText;

  if (!message) {
    return null;
  }

  return (
    <span
      className={className}
      id={error ? `${id}-error` : `${id}-description`}
      role={error ? "alert" : undefined}
    >
      {message}
    </span>
  );
}
