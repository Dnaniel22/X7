package com.x7.pasteleria.config;

import com.x7.pasteleria.model.Producto;
import com.x7.pasteleria.model.Rol;
import com.x7.pasteleria.model.Usuario;
import com.x7.pasteleria.repository.ProductoRepository;
import com.x7.pasteleria.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Siembra los datos mínimos la primera vez que arranca la aplicación.
 * A partir del segundo arranque la información ya vive en la base de datos H2.
 */
@Component
public class DataLoader implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UsuarioRepository usuarioRepository,
                      ProductoRepository productoRepository,
                      PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            usuarioRepository.save(new Usuario(
                    "Administrador X7",
                    "admin@x7.com",
                    "987654321",
                    "Ica",
                    passwordEncoder.encode("123456"),
                    Rol.ADMINISTRADOR
            ));
        }

        if (productoRepository.count() == 0) {
            productoRepository.saveAll(List.of(
                    new Producto(
                            "Torta de Chocolate",
                            "Tortas",
                            "Más vendido",
                            new BigDecimal("85.00"),
                            "Torta húmeda de chocolate con relleno cremoso.",
                            "https://images.unsplash.com/photo-1578985545062-69928b1d9587?auto=format&fit=crop&w=900&q=80"
                    ),
                    new Producto(
                            "Bocaditos Surtidos",
                            "Bocaditos",
                            "Eventos",
                            new BigDecimal("45.00"),
                            "Selección de bocaditos dulces y salados para reuniones.",
                            "https://images.unsplash.com/photo-1555507036-ab1f4038808a?auto=format&fit=crop&w=900&q=80"
                    ),
                    new Producto(
                            "Buffet para Eventos",
                            "Buffets",
                            "Catering",
                            new BigDecimal("180.00"),
                            "Servicio de buffet personalizado para celebraciones y eventos.",
                            "https://images.unsplash.com/photo-1555244162-803834f70033?auto=format&fit=crop&w=900&q=80"
                    ),
                    new Producto(
                            "Torta Tres Leches",
                            "Tortas",
                            "Clásico",
                            new BigDecimal("90.00"),
                            "Torta esponjosa bañada en tres leches, con un toque de canela.",
                            "https://i.blogs.es/4d76ad/pastel-tres-leches/840_560.jpg"
                    )
            ));
        }
    }
}
