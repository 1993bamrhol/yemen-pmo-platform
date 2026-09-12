package ye.gov.pmo.content.config;

import java.util.EnumSet;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import ye.gov.pmo.content.domain.ContentType;

@Component
@ConfigurationProperties(prefix = "features.unified-content-read")
public class PublicContentReadPolicy {

    private boolean enabled;
    private Set<ContentType> allowedTypes = EnumSet.noneOf(ContentType.class);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Set<ContentType> getAllowedTypes() {
        return Set.copyOf(allowedTypes);
    }

    public void setAllowedTypes(Set<ContentType> allowedTypes) {
        this.allowedTypes = allowedTypes == null || allowedTypes.isEmpty()
                ? EnumSet.noneOf(ContentType.class)
                : EnumSet.copyOf(allowedTypes);
    }

    public Set<ContentType> publicTypes() {
        return enabled ? Set.copyOf(allowedTypes) : Set.of();
    }

    public boolean allows(ContentType contentType) {
        return enabled && allowedTypes.contains(contentType);
    }
}
