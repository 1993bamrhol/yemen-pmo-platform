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
import ye.gov.pmo.services.domain.ServiceDeliveryChannel;

@Entity
@Table(name = "government_service_channels")
public class GovernmentServiceChannel {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "government_service_id", nullable = false)
    private GovernmentService service;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel_type", nullable = false, length = 20)
    private ServiceDeliveryChannel channelType;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "label_ar", length = 255)
    private String labelAr;

    @Column(name = "action_url", length = 1000)
    private String actionUrl;

    @Column(name = "instructions_ar", length = 2000)
    private String instructionsAr;

    protected GovernmentServiceChannel() {
    }

    public GovernmentServiceChannel(GovernmentService service, ServiceDeliveryChannel channelType,
                                    int displayOrder, String labelAr, String actionUrl,
                                    String instructionsAr) {
        this.id = UUID.randomUUID();
        this.service = service;
        this.channelType = channelType;
        this.displayOrder = displayOrder;
        this.labelAr = labelAr;
        this.actionUrl = actionUrl;
        this.instructionsAr = instructionsAr;
    }

    public ServiceDeliveryChannel getChannelType() { return channelType; }
    public UUID getServiceId() { return service.getId(); }
    public int getDisplayOrder() { return displayOrder; }
    public String getLabelAr() { return labelAr; }
    public String getActionUrl() { return actionUrl; }
    public String getInstructionsAr() { return instructionsAr; }
}
