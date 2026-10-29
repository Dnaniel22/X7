package com.x7.pasteleria.config;

import com.x7.pasteleria.model.EstadoPedido;
import com.x7.pasteleria.model.Pedido;
import com.x7.pasteleria.model.Producto;
import com.x7.pasteleria.model.Rol;
import com.x7.pasteleria.model.TipoPedido;
import com.x7.pasteleria.model.Usuario;
import com.x7.pasteleria.repository.PedidoRepository;
import com.x7.pasteleria.repository.ProductoRepository;
import com.x7.pasteleria.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class DatosIniciales implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosIniciales.class);

    private static final String CLIENTE_DEMO = "rosa.quispe@correo.com";

    private static final String IMAGEN = "https://images.unsplash.com/%s?auto=format&fit=crop&w=900&q=80";

    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final PedidoRepository pedidoRepository;

    public DatosIniciales(UsuarioRepository usuarioRepository,
                          ProductoRepository productoRepository,
                          PedidoRepository pedidoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
        this.pedidoRepository = pedidoRepository;
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

        if (!usuarioRepository.existsByEmailIgnoreCase(CLIENTE_DEMO)) {
            cargarDemostracion();
        }
    }

    // =====================================================================
    //  Datos de demostración
    // =====================================================================

    private void cargarDemostracion() {
        // 1. Más productos: uno por línea de negocio que faltaba (no se repite un nombre que ya exista).
        List<Producto> nuevos = List.of(
                new Producto("Cheesecake de Arándanos", "Tortas", "Nuevo", new BigDecimal("75.00"),
                        "Cheesecake cremoso horneado sobre base de galleta y cubierto con arándanos frescos.",
                        imagen("photo-1754908886530-2bf8025e860f")),
                new Producto("Cupcakes Red Velvet", "Bocaditos", "Media docena", new BigDecimal("40.00"),
                        "Seis cupcakes red velvet con frosting de queso crema, ideales para cumpleaños y baby showers.",
                        imagen("photo-1543287920-26349b5b1376")),
                new Producto("Brownies de Chocolate", "Bocaditos", "Favorito", new BigDecimal("30.00"),
                        "Caja de nueve brownies húmedos de chocolate semiamargo.",
                        imagen("photo-1717768024023-73768e73f630")),
                new Producto("Macarons Surtidos", "Otros", "Caja x12", new BigDecimal("35.00"),
                        "Caja de doce macarons franceses en sabores de frambuesa, pistacho, vainilla y chocolate.",
                        imagen("photo-1735637705237-86385f719d98")),
                new Producto("Coffee Break Corporativo", "Buffets", "Empresas", new BigDecimal("250.00"),
                        "Coffee break para 20 personas con croissants, bocaditos dulces, café y jugos naturales.",
                        imagen("photo-1692742249337-3bf05e2b1ed7"))
        );
        int agregados = 0;
        for (Producto producto : nuevos) {
            if (!productoRepository.existsByNombreIgnoreCase(producto.getNombre())) {
                productoRepository.save(producto);
                agregados++;
            }
        }

        Map<String, Producto> catalogo = new HashMap<>();
        productoRepository.findAll().forEach(p -> catalogo.put(p.getNombre(), p));

        // 2. Clientes de ejemplo (contraseña 123456).
        Usuario rosa = cliente("Rosa Quispe Huamán", CLIENTE_DEMO, "956781234", "Calle Lima 245, Ica");
        Usuario carlos = cliente("Carlos Mendoza Ríos", "carlos.mendoza@correo.com", "934567812",
                "Av. Los Maestros 1020, Ica");
        Usuario andrea = cliente("Andrea Flores Salas", "andrea.flores@correo.com", "987123456",
                "Urb. San José Mz. C Lt. 8, Ica");
        Usuario jorge = cliente("Jorge Huamán Torres", "jorge.huaman@correo.com", "945612378",
                "Jr. Ayacucho 310, Parcona, Ica");
        Usuario prueba = usuarioRepository.findByEmailIgnoreCase("cliente@x7.com").orElse(null);

        // 3. Pedidos en distintos estados. Los números son días: hace cuánto se registró
        //    y en cuántos días se entrega (negativo = ya se entregó).
        //    Monto = precio × factor del tamaño × cantidad, igual que en PedidoController.
        List<Pedido> pedidos = new ArrayList<>();
        pedidos.add(pedido(prueba, catalogo.get("Brownies de Chocolate"),
                "Cantidad: 2", "60.00", EstadoPedido.ENTREGADO, 14, -10));
        pedidos.add(pedido(rosa, catalogo.get("Torta de Chocolate"),
                "Mediana (12-15 porciones) · Dedicatoria: «Feliz cumpleaños, mamá»", "127.50",
                EstadoPedido.ENTREGADO, 12, -6));
        pedidos.add(pedido(andrea, catalogo.get("Macarons Surtidos"),
                "Cantidad: 3 · Tonos pastel para baby shower", "105.00", EstadoPedido.ENTREGADO, 10, -4));
        pedidos.add(pedido(jorge, catalogo.get("Cupcakes Red Velvet"),
                "Cantidad: 2", "80.00", EstadoPedido.ENTREGADO, 8, -2));
        pedidos.add(catering(rosa, "Quinceañero", "Club Campestre, Ica",
                "150 invitados · Lugar: Club Campestre, Ica · Buffet criollo, mesa de postres y 6 mozos",
                "6750.00", EstadoPedido.EN_PREPARACION, 7, 18));
        pedidos.add(pedido(andrea, catalogo.get("Cheesecake de Arándanos"),
                "Grande (20-25 porciones) · Base de galleta integral", "150.00",
                EstadoPedido.EN_PREPARACION, 3, 1));
        pedidos.add(pedido(carlos, catalogo.get("Coffee Break Corporativo"),
                "Cantidad: 2 · Reunión de directorio a las 9:00 a. m.", "500.00",
                EstadoPedido.EN_PREPARACION, 2, 2));
        pedidos.add(personalizado(jorge,
                "Torta temática de fútbol de dos pisos · Azul y blanco · Con el nombre «Mateo»",
                EstadoPedido.PENDIENTE, 2, 6));
        pedidos.add(catering(carlos, "Evento Corporativo", "Auditorio principal, Av. Los Maestros 1020, Ica",
                "80 invitados · Lugar: Auditorio principal, Av. Los Maestros 1020, Ica · Coffee break y almuerzo ejecutivo",
                null, EstadoPedido.PENDIENTE, 1, 25));
        pedidos.add(pedido(prueba, catalogo.get("Torta Tres Leches"),
                "Pequeña (6-8 porciones)", "90.00", EstadoPedido.PENDIENTE, 1, 3));
        pedidos.add(catering(andrea, "Matrimonio", "Local de eventos en Pueblo Nuevo, Ica",
                "200 invitados · Lugar: Local de eventos en Pueblo Nuevo, Ica · Menú de tres tiempos y torta de bodas",
                null, EstadoPedido.PENDIENTE, 0, 60));
        pedidos.add(pedido(rosa, catalogo.get("Bocaditos Surtidos"),
                "Cantidad: 3", "135.00", EstadoPedido.PENDIENTE, 0, 5));

        // Se guardan del más antiguo al más reciente para que los códigos sigan ese orden.
        List<Pedido> validos = pedidos.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Pedido::getFechaRegistro))
                .toList();
        for (Pedido pedido : validos) {
            Pedido guardado = pedidoRepository.save(pedido);
            guardado.setCodigo(String.format("#PED-%04d", 1000 + guardado.getId()));
            pedidoRepository.save(guardado);
        }

        log.info("Datos de demostración cargados: {} productos, 4 clientes y {} pedidos", agregados, validos.size());
    }

    private String imagen(String foto) {
        return String.format(IMAGEN, foto);
    }

    private Usuario cliente(String nombre, String email, String telefono, String direccion) {
        return usuarioRepository.findByEmailIgnoreCase(email)
                .orElseGet(() -> usuarioRepository.save(
                        new Usuario(nombre, email, telefono, direccion, "123456", Rol.CLIENTE)));
    }

    private Pedido pedido(Usuario cliente, Producto producto, String detalle, String monto,
                          EstadoPedido estado, int diasDesdeRegistro, int diasHastaEntrega) {
        if (producto == null) {
            return null;
        }
        return base(cliente, TipoPedido.PEDIDO, producto, detalle, monto, estado, diasDesdeRegistro, diasHastaEntrega);
    }

    private Pedido personalizado(Usuario cliente, String detalle, EstadoPedido estado,
                                 int diasDesdeRegistro, int diasHastaEntrega) {
        return base(cliente, TipoPedido.PEDIDO, null, detalle, null, estado, diasDesdeRegistro, diasHastaEntrega);
    }

    private Pedido catering(Usuario cliente, String tipoEvento, String lugar, String detalle, String monto,
                            EstadoPedido estado, int diasDesdeRegistro, int diasHastaEntrega) {
        Pedido pedido = base(cliente, TipoPedido.CATERING, null, detalle, monto, estado,
                diasDesdeRegistro, diasHastaEntrega);
        if (pedido != null) {
            pedido.setTipoEvento(tipoEvento);
            pedido.setDireccion(lugar);
        }
        return pedido;
    }

    private Pedido base(Usuario cliente, TipoPedido tipo, Producto producto, String detalle, String monto,
                        EstadoPedido estado, int diasDesdeRegistro, int diasHastaEntrega) {
        if (cliente == null) {
            return null;
        }
        Pedido pedido = new Pedido();
        pedido.setTipo(tipo);
        pedido.setUsuario(cliente);
        pedido.setProducto(producto);
        pedido.setDetalle(detalle);
        pedido.setMonto(monto == null ? null : new BigDecimal(monto));
        pedido.setEstado(estado);
        pedido.setNombreContacto(cliente.getNombre());
        pedido.setTelefono(cliente.getTelefono() != null ? cliente.getTelefono() : "987654321");
        pedido.setDireccion(cliente.getDireccion() != null ? cliente.getDireccion() : "Ica");
        pedido.setFechaRegistro(LocalDateTime.now().minusDays(diasDesdeRegistro));
        pedido.setFechaEntrega(LocalDate.now().plusDays(diasHastaEntrega));
        return pedido;
    }
}
