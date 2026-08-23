package ye.gov.pmo.content.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ye.gov.pmo.content.entity.TaxonomyTerm;

public interface TaxonomyTermRepository extends JpaRepository<TaxonomyTerm, UUID> {
    List<TaxonomyTerm> findAllByTaxonomyCodeAndSlugInAndActiveTrue(
            String taxonomyCode, Collection<String> slugs);
}
