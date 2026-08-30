import type { Metadata } from "next";

import EntityProfilePage, {
  generateMetadata as generateEntityMetadata,
} from "../[publicPathSegment]/[slug]/page";

const PUBLIC_PATH_SEGMENT = "prime-ministers-office";

function entityParams() {
  return Promise.resolve({
    publicPathSegment: PUBLIC_PATH_SEGMENT,
    requestedPath: `/${PUBLIC_PATH_SEGMENT}`,
    slug: PUBLIC_PATH_SEGMENT,
  });
}

export function generateMetadata(): Promise<Metadata> {
  return generateEntityMetadata({ params: entityParams() });
}

export default function PrimeMinistersOfficePage() {
  return <EntityProfilePage params={entityParams()} />;
}
