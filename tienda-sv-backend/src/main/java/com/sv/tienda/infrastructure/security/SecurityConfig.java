package com.sv.tienda.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UsuarioDetailsService usuarioDetailsService;

    @Bean
    public SpringSecurityDialect springSecurityDialect() {
        return new SpringSecurityDialect();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(usuarioDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Tras login exitoso: todos van al dashboard global.
     * La plantilla dashboard.html se adapta internamente según el rol.
     */
    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return (HttpServletRequest req, HttpServletResponse res, Authentication auth) -> {
            boolean isStaff = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_CAJERO"));
            
            if (isStaff) {
                res.sendRedirect("/dashboard");
            } else {
                res.sendRedirect("/");
            }
        };
    }

    /**
     * Tras login fallido: redirige de vuelta al formulario correcto según el origen.
     * /staff/login error → /staff/login?error
     * /login error       → /login?error
     */
    @Bean
    public AuthenticationFailureHandler failureHandler() {
        return (HttpServletRequest req, HttpServletResponse res, AuthenticationException ex) -> {
            String referer = req.getHeader("Referer");
            if (referer != null && referer.contains("/staff/login")) {
                res.sendRedirect("/staff/login?error=true");
            } else {
                res.sendRedirect("/login?error=true");
            }
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(auth -> auth
                // ── Rutas completamente públicas
                .requestMatchers(
                    "/", "/tienda",
                    "/login", "/registro",
                    "/staff/login",
                    "/do-login",        // el endpoint POST de autenticación
                    "/css/**", "/js/**", "/img/**", "/error",
                    "/webjars/**"
                ).permitAll()
                // ── Dashboard: solo usuarios autenticados
                .requestMatchers("/dashboard/**").authenticated()
                // ── APIs por rol
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/cajero/**").hasAnyRole("ADMIN", "CAJERO")
                .requestMatchers("/api/**").authenticated()
                // ── Cualquier otra ruta: autenticado
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")           // Página por defecto para redirigir si no autenticado
                .loginProcessingUrl("/do-login") // Spring procesa el POST aquí (ambos formularios apuntan a este)
                .successHandler(successHandler())
                .failureHandler(failureHandler())
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")          // Al salir, vuelve al storefront
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            // CSRF: desactivado solo para la API REST
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/do-login"));

        return http.build();
    }
}
