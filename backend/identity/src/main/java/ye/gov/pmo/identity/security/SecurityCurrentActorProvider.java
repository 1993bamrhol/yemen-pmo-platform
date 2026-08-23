package ye.gov.pmo.identity.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ye.gov.pmo.identity.repository.UserRepository;
import ye.gov.pmo.shared.security.CurrentActorProvider;

@Component
public class SecurityCurrentActorProvider implements CurrentActorProvider {

    private final UserRepository userRepository;

    public SecurityCurrentActorProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Long currentUserId() {
        String username = currentUsername();
        return username == null
                ? null
                : userRepository.findByUsername(username).map(user -> user.getId()).orElse(null);
    }

    @Override
    public String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return null;
        }
        return authentication.getName();
    }
}
