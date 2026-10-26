package com.x7.pasteleria.config;

import com.x7.pasteleria.model.Producto;
import com.x7.pasteleria.model.Rol;
import com.x7.pasteleria.model.Usuario;
import com.x7.pasteleria.repository.ProductoRepository;
import com.x7.pasteleria.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Carga los datos de partida la primera vez que la aplicación se conecta a una base vacía.
 * Si las tablas ya tienen registros no hace nada, así que los datos existentes se conservan.
 */
@Component
public class DatosIniciales implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosIniciales.class);

    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;

    public DatosIniciales(UsuarioRepository usuarioRepository, ProductoRepository productoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            usuarioRepository.saveAll(List.of(
                    new Usuario("Administrador X7", "admin@x7.com", "987654321",
                            "Ica", "123456", Rol.ADMINISTRADOR),
                    new Usuario("Cliente de Prueba", "cliente@x7.com", "912345678",
                            "Urb. Las Flores Mz. A Lt. 5, Ica", "123456", Rol.CLIENTE)
            ));
            log.info("Usuarios iniciales creados: admin@x7.com y cliente@x7.com (contraseña 123456)");
        }

        if (productoRepository.count() == 0) {
            productoRepository.saveAll(List.of(
                    new Producto("Torta de Chocolate", "Tortas", "Más vendido", new BigDecimal("85.00"),
                            "Torta húmeda de chocolate con relleno cremoso.",
                            "https://images.unsplash.com/photo-1578985545062-69928b1d9587?auto=format&fit=crop&w=900&q=80"),
                    new Producto("Bocaditos Surtidos", "Bocaditos", "Eventos", new BigDecimal("45.00"),
                            "Selección de bocaditos dulces y salados para reuniones.",
                            "https://images.unsplash.com/photo-1555507036-ab1f4038808a?auto=format&fit=crop&w=900&q=80"),
                    new Producto("Buffet para Eventos", "Buffets", "Catering", new BigDecimal("180.00"),
                            "Servicio de buffet personalizado para celebraciones y eventos.",
                            "https://images.unsplash.com/photo-1555244162-803834f70033?auto=format&fit=crop&w=900&q=80"),
                    new Producto("Torta Tres Leches", "Tortas", "Clásico", new BigDecimal("90.00"),
                            "Torta esponjosa bañada en tres leches, con un toque de canela.",
                            "https://i.blogs.es/4d76ad/pastel-tres-leches/840_560.jpg")
            ));
            log.info("Catálogo inicial creado con 4 productos");
        }
    }
}
