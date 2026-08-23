package ye.gov.pmo.shared.security;

public interface CurrentActorProvider {

    Long currentUserId();

    String currentUsername();
}
