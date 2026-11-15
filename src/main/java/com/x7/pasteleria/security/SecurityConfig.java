package com.x7.pasteleria.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.x7.pasteleria.security.jwt.ErroresApiSeguridad;
import com.x7.pasteleria.security.jwt.JwtAuthenticationFilter;
import com.x7.pasteleria.security.jwt.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración de Spring Security.
 *
 * Hay dos cadenas de filtros (SecurityFilterChain), una para cada tipo de cliente:
 *
 *  1. API REST (/api/**): sin sesión (STATELESS). Cada petición se autentica con un JWT
 *     en la cabecera "Authorization: Bearer <token>". Errores en JSON (401 / 403).
 *
 *  2. Sitio web (todo lo demás): formulario de login propio, sesión con cookie JSESSIONID,
 *     protección CSRF y cierre de sesión. Errores con redirección al login o página 403.
 *
 * Roles: CLIENTE y ADMINISTRADOR (enum Rol). hasRole("CLIENTE") revisa la autoridad ROLE_CLIENTE.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** Atributo de sesión con el último correo que falló al iniciar sesión (lo lee AuthController). */
    public static final String LOGIN_EMAIL = "LOGIN_EMAIL";

    /** BCrypt guarda la contraseña como un hash irreversible, por ejemplo $2a$10$e9B... */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Lo usa POST /api/auth/login para comprobar el correo y la contraseña. */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuracion) throws Exception {
        return configuracion.getAuthenticationManager();
    }

    // =====================================================================
    //  1. API REST con JWT
    // =====================================================================

    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http,
                                              JwtService jwtService,
                                              UserDetailsService userDetailsService,
                                              ObjectMapper objectMapper) throws Exception {

        ErroresApiSeguridad errores = new ErroresApiSeguridad(objectMapper);

        http
            .securityMatcher("/api/**")
            // Sin cookies de sesión no hay riesgo CSRF: el token viaja en una cabecera que el navegador no envía solo.
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Público: obtener el token y consultar el catálogo.
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/productos", "/api/productos/**").permitAll()
                // Solo administrador: registrar, editar y eliminar productos.
                .requestMatchers(HttpMethod.POST, "/api/productos").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.PUT, "/api/productos/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.DELETE, "/api/productos/**").hasRole("ADMINISTRADOR")
                // Solo administrador: cambiar estado/monto y eliminar pedidos.
                .requestMatchers(HttpMethod.PUT, "/api/pedidos/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.DELETE, "/api/pedidos/**").hasRole("ADMINISTRADOR")
                // Lo demás (ver pedidos, ver mi perfil) requiere un token válido de cualquier rol.
                .anyRequest().authenticated())
            .exceptionHandling(e -> e
                .authenticationEntryPoint(errores)   // 401
                .accessDeniedHandler(errores))       // 403
            .addFilterBefore(new JwtAuthenticationFilter(jwtService, userDetailsService),
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // =====================================================================
    //  2. Sitio web con formulario de login y sesión
    // =====================================================================

    @Bean
    @Order(2)
    public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Recursos estáticos y página de error.
                .requestMatchers("/css/**", "/js/**", "/img/**", "/favicon.ico", "/error").permitAll()
                // Páginas públicas.
                .requestMatchers("/", "/catalogo", "/login", "/registro").permitAll()
                // Los formularios se pueden ver sin cuenta, pero solo un cliente puede enviarlos.
                .requestMatchers(HttpMethod.GET, "/pedido", "/reservar").permitAll()
                .requestMatchers(HttpMethod.POST, "/pedido", "/reservar").hasRole("CLIENTE")
                // Paneles privados por rol.
                .requestMatchers("/cliente/**").hasRole("CLIENTE")
                .requestMatchers("/admin/**").hasRole("ADMINISTRADOR")
                // Cualquier otra ruta requiere haber iniciado sesión.
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")                 // nuestra vista autenticacion/login.html
                .usernameParameter("email")          // el campo del formulario se llama "email"
                .passwordParameter("password")
                .successHandler((request, response, autenticacion) -> {
                    // Cada rol entra a su propio panel.
                    UsuarioPrincipal usuario = (UsuarioPrincipal) autenticacion.getPrincipal();
                    response.sendRedirect(request.getContextPath() + usuario.rutaPanel());
                })
                .failureHandler((request, response, error) -> {
                    // Se recuerda el correo para no tener que escribirlo otra vez.
                    request.getSession().setAttribute(LOGIN_EMAIL, request.getParameter("email"));
                    String motivo = error instanceof DisabledException ? "desactivada" : "error";
                    response.sendRedirect(request.getContextPath() + "/login?" + motivo);
                })
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/logout")                // POST /logout (con token CSRF)
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll());
        // CSRF queda activado (valor por defecto): th:action agrega el token oculto a cada formulario POST.
        // Un usuario con sesión pero sin el rol requerido recibe 403 y se muestra templates/error/403.html.

        return http.build();
    }
}
