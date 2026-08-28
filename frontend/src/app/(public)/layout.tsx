import type { ReactNode } from "react";

import { PUBLIC_MAIN_CONTENT_ID, SkipLink } from "@/components/layout";
import { GovernmentFooter, GovernmentHeader } from "@/components/shell";

export default function PublicLayout({ children }: Readonly<{ children: ReactNode }>) {
  return (
    <>
      <SkipLink />
      <GovernmentHeader />
      <main id={PUBLIC_MAIN_CONTENT_ID} tabIndex={-1}>
        {children}
      </main>
      <GovernmentFooter />
    </>
  );
}
