package ye.gov.pmo.identity.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final long EXPIRATION_MS = 86_400_000L;

    @Test
    void acceptsBase64Secret() {
        assertDoesNotThrow(() -> new JwtService(
                "//////////////////////////////////////////8=",
                EXPIRATION_MS));
    }

    @Test
    void acceptsBase64UrlSecret() {
        assertDoesNotThrow(() -> new JwtService(
                "__________________________________________8",
                EXPIRATION_MS));
    }

    @Test
    void rejectsInvalidSecretEncoding() {
        assertThrows(IllegalStateException.class,
                () -> new JwtService("not-a-valid-secret@@@", EXPIRATION_MS));
    }

    @Test
    void rejectsShortSecret() {
        assertThrows(IllegalStateException.class,
                () -> new JwtService("c2hvcnQ=", EXPIRATION_MS));
    }
}
