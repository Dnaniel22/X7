package com.x7.pasteleria.data;

import com.x7.pasteleria.model.Pedido;
import com.x7.pasteleria.model.Producto;
import com.x7.pasteleria.model.Usuario;

import java.util.ArrayList;
import java.util.List;

public class Datos {
    public static final List<Usuario> USUARIOS = new ArrayList<>();
    public static final List<Producto> PRODUCTOS = new ArrayList<>();
    public static final List<Pedido> PEDIDOS = new ArrayList<>();

    private static long siguienteUsuario = 2;
    private static long siguienteProducto = 4;
    private static long siguientePedido = 1;

    static {
        USUARIOS.add(new Usuario(
                1L,
                "Administrador X7",
                "admin@x7.com",
                "987654321",
                "Ica",
                "123456",
                "administrador"
        ));

        PRODUCTOS.add(new Producto(
                1L,
                "Torta de Chocolate",
                "Tortas",
                "Más vendido",
                85.00,
                "Torta húmeda de chocolate con relleno cremoso.",
                "https://images.unsplash.com/photo-1578985545062-69928b1d9587?auto=format&fit=crop&w=900&q=80"
        ));

        PRODUCTOS.add(new Producto(
                2L,
                "Bocaditos Surtidos",
                "Bocaditos",
                "Eventos",
                45.00,
                "Selección de bocaditos dulces y salados para reuniones.",
                "https://images.unsplash.com/photo-1555507036-ab1f4038808a?auto=format&fit=crop&w=900&q=80"
        ));

        PRODUCTOS.add(new Producto(
                3L,
                "Buffet para Eventos",
                "Buffets",
                "Catering",
                180.00,
                "Servicio de buffet personalizado para celebraciones y eventos.",
                "https://images.unsplash.com/photo-1555244162-803834f70033?auto=format&fit=crop&w=900&q=80"
        ));
    }

    public static synchronized long nuevoUsuarioId() {
        return siguienteUsuario++;
    }

    public static synchronized long nuevoProductoId() {
        return siguienteProducto++;
    }

    public static synchronized long nuevoPedidoId() {
        return siguientePedido++;
    }

    private Datos() {}
}
