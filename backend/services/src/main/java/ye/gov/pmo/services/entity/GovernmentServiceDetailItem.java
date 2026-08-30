package ye.gov.pmo.services.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import ye.gov.pmo.services.domain.ServiceDetailSection;

@Entity
@Table(name = "government_service_detail_items")
public class GovernmentServiceDetailItem {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "government_service_id", nullable = false)
    private GovernmentService service;

    @Enumerated(EnumType.STRING)
    @Column(name = "section_type", nullable = false, length = 20)
    private ServiceDetailSection sectionType;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "title_ar", nullable = false, length = 500)
    private String titleAr;

    @Column(name = "description_ar", columnDefinition = "TEXT")
    private String descriptionAr;

    protected GovernmentServiceDetailItem() {
    }

    public GovernmentServiceDetailItem(GovernmentService service, ServiceDetailSection sectionType,
                                       int displayOrder, String titleAr, String descriptionAr) {
        this.id = UUID.randomUUID();
        this.service = service;
        this.sectionType = sectionType;
        this.displayOrder = displayOrder;
        this.titleAr = titleAr;
        this.descriptionAr = descriptionAr;
    }

    public ServiceDetailSection getSectionType() { return sectionType; }
    public int getDisplayOrder() { return displayOrder; }
    public String getTitleAr() { return titleAr; }
    public String getDescriptionAr() { return descriptionAr; }
}
