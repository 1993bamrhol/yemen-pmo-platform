package ye.gov.pmo.identity.security;

import java.time.Instant;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.Set;

import javax.crypto.SecretKey;

import ye.gov.pmo.identity.entity.Permission;
import ye.gov.pmo.identity.entity.Role;
import ye.gov.pmo.identity.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration-ms:86400000}") long expirationMs) {

        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("SECURITY_JWT_SECRET must be configured");
        }

        byte[] decodedSecret = decodeSecret(secret);
        if (decodedSecret.length < 32) {
            throw new IllegalStateException("SECURITY_JWT_SECRET must decode to at least 32 bytes");
        }
        this.signingKey = Keys.hmacShaKeyFor(decodedSecret);

        if (expirationMs <= 0) {
            throw new IllegalStateException("SECURITY_JWT_EXPIRATION_MS must be greater than zero");
        }
        this.expirationMs = expirationMs;
    }

    private byte[] decodeSecret(String secret) {
        boolean usesBase64UrlAlphabet = secret.indexOf('-') >= 0 || secret.indexOf('_') >= 0;
        try {
            return usesBase64UrlAlphabet
                    ? Decoders.BASE64URL.decode(secret)
                    : Decoders.BASE64.decode(secret);
        } catch (DecodingException exception) {
            throw new IllegalStateException(
                    "SECURITY_JWT_SECRET must be valid Base64 or Base64URL",
                    exception);
        }
    }

    public String generateToken(User user) {
        return generateToken(
                user,
                user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()),
                user.getRoles().stream()
                        .flatMap(role -> role.getPermissions().stream())
                        .map(Permission::getName)
                        .collect(Collectors.toSet()));
    }

    public String generateToken(User user, Set<String> roles, Set<String> permissions) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(user.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .claim("roles", roles)
                .claim("permissions", permissions)
                .signWith(signingKey)
                .compact();
    }

    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, String username) {
        Claims claims = getClaims(token);
        return username.equals(claims.getSubject()) && claims.getExpiration().after(new Date());
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
