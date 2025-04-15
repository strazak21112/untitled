package pl.wiktor.koprowski.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.function.Function;

@Slf4j
@Component
public class JwtUtilities {

    private final String secret = "9df8505bb4eb7833d0f07cebec765d065344850a4548a5f8c3a9ffea5c01e6bd";
    private final Long jwtExpiration = 36000000L; // 10 godzin

    // Generowanie klucza do podpisywania JWT
    private SecretKey generateSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    // Generowanie tokenu JWT
    public String generateToken(String email, String role) {
        Instant now = Instant.now();
        Instant expirationTime = now.plus(jwtExpiration, ChronoUnit.MILLIS);


        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expirationTime))
                .signWith(generateSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Walidacja tokenu
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(generateSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("⚠️ Token wygasł: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("⚠️ Token nie jest wspierany: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("⚠️ Token jest uszkodzony: {}", e.getMessage());
        } catch (SignatureException e) {
            log.warn("⚠️ Błędny podpis tokenu: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Niepoprawny token: {}", e.getMessage());
        }
        return false;
    }

    // Ekstrakcja claims z tokenu
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(generateSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Pobranie konkretnej wartości z claims
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Pobranie emaila z tokenu
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Pobranie roli użytkownika z tokenu
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    // Pobranie daty wygaśnięcia tokenu
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Sprawdzenie, czy token wygasł
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Pobranie tokenu z nagłówka HTTP
    public String getToken(HttpServletRequest request) {
        final String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // Walidacja tokenu na podstawie użytkownika
    public boolean validateToken(String token, UserDetails userDetails) {
        final String email = extractEmail(token);
        return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }
}
