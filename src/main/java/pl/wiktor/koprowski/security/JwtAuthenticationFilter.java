package pl.wiktor.koprowski.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import pl.wiktor.koprowski.domain.User;
import pl.wiktor.koprowski.service.basic.UserService;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtilities jwtUtilities;
    private final ApplicationContext applicationContext;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // Uzyskujemy UserService przez ApplicationContext
        UserService userService = applicationContext.getBean(UserService.class);

        String token = jwtUtilities.getToken(request);

        if (StringUtils.hasText(token) && jwtUtilities.validateToken(token)) {

            if ("ADMIN".equals(jwtUtilities.extractRole(token))) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                "ADMIN",
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                        );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                try {
                    String email = jwtUtilities.extractEmail(token);
                    log.info("Extracted email from token: {}", email);

                    User user = userService.loadUserByEmail(email);
                    log.info("User found: {}", user.getEmail());

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    user,
                                    null,
                                    user.getAuthorities()
                            );
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                } catch (Exception e) {
                    log.error("Error during JWT validation or user lookup", e);
                }
            }
        } else {
            log.info("Token is missing or invalid.");
        }

        filterChain.doFilter(request, response);
    }
}
