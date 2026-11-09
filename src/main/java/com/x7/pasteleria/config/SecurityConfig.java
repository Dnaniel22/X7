package com.x7.pasteleria.config;

import com.x7.pasteleria.controller.SesionUsuario;
import com.x7.pasteleria.model.Usuario;
import com.x7.pasteleria.repository.UsuarioRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de Spring Security.
 * Define qué rutas son públicas, qué rol necesita cada sección, cómo se inicia y se cierra
 * sesión, de dónde salen los usuarios (tabla "usuario") y cómo se cifran sus contraseñas.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filtros(HttpSecurity http, UsuarioRepository usuarioRepository) throws Exception {
        http
                // 1. Autorización: las reglas se revisan de arriba hacia abajo y gana la primera que coincide.
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/catalogo", "/pedido", "/reservar",
                                "/login", "/registro", "/css/**", "/js/**", "/error").permitAll()
                        // La API REST sigue validando la sesión y el rol en sus controladores (ApiRespuestas).
                        .requestMatchers("/api/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMINISTRADOR")
                        .requestMatchers("/cliente/**").hasRole("CLIENTE")
                        .anyRequest().authenticated())

                // 2. Autenticación con formulario: se usa nuestra vista login.html, no la de Spring.
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        .failureUrl("/login?error")
                        .successHandler((request, response, authentication) -> {
                            // Se guarda el usuario en la sesión para las vistas (${session.usuario}) y los controladores.
                            Usuario usuario = usuarioRepository.findByEmailIgnoreCase(authentication.getName()).orElseThrow();
                            SesionUsuario.iniciar(request.getSession(), usuario);
                            response.sendRedirect(request.getContextPath() + SesionUsuario.rutaPanel(usuario));
                        })
                        .permitAll())

                // 3. Cerrar sesión: POST /logout invalida la sesión y vuelve al inicio.
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll())

                // 4. CSRF: los formularios Thymeleaf (th:action) envían el token solos.
                //    La API REST se prueba desde Postman, que no tiene ese token.
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"));

        return http.build();
    }

    /** Spring Security busca a los usuarios en PostgreSQL usando el correo como nombre de usuario. */
    @Bean
    public UserDetailsService userDetailsService(UsuarioRepository usuarioRepository) {
        return email -> usuarioRepository.findByEmailIgnoreCase(email.trim())
                .map(u -> User.withUsername(u.getEmail())
                        .password(u.getPassword())
                        .roles(u.getRol().name())   // ADMINISTRADOR → ROLE_ADMINISTRADOR
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("No existe un usuario con el correo " + email));
    }

    /** BCrypt: las contraseñas se guardan cifradas y no se pueden descifrar, solo comparar. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
