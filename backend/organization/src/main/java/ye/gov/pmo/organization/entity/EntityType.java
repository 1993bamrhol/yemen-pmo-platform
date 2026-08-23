package ye.gov.pmo.organization.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "entity_types")
public class EntityType {

    @Id
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name_ar", nullable = false, length = 150)
    private String nameAr;

    @Column(name = "public_path_segment", nullable = false, unique = true, length = 80)
    private String publicPathSegment;

    @Column(nullable = false)
    private boolean active;

    protected EntityType() {
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getNameAr() {
        return nameAr;
    }

    public String getPublicPathSegment() {
        return publicPathSegment;
    }

    public boolean isActive() {
        return active;
    }
}
