package com.x7.pasteleria.controller;

import com.x7.pasteleria.config.SesionActual;
import com.x7.pasteleria.dto.PedidoForm;
import com.x7.pasteleria.dto.ReservaForm;
import com.x7.pasteleria.model.Pedido;
import com.x7.pasteleria.model.Producto;
import com.x7.pasteleria.model.Rol;
import com.x7.pasteleria.model.TamanoTorta;
import com.x7.pasteleria.model.Usuario;
import com.x7.pasteleria.service.PedidoService;
import com.x7.pasteleria.service.ProductoService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
public class PedidoController {

    private static final List<String> TIPOS_EVENTO = List.of(
            "Matrimonio", "Quinceañero", "Evento Corporativo", "Aniversario / Cumpleaños");

    private final PedidoService pedidoService;
    private final ProductoService productoService;
    private final SesionActual sesionActual;

    public PedidoController(PedidoService pedidoService,
                            ProductoService productoService,
                            SesionActual sesionActual) {
        this.pedidoService = pedidoService;
        this.productoService = productoService;
        this.sesionActual = sesionActual;
    }

    @ModelAttribute("productos")
    public List<Producto> productos() {
        return productoService.listar();
    }

    @ModelAttribute("tamanos")
    public TamanoTorta[] tamanos() {
        return TamanoTorta.values();
    }

    @ModelAttribute("tiposEvento")
    public List<String> tiposEvento() {
        return TIPOS_EVENTO;
    }

    @ModelAttribute("productoPersonalizado")
    public String productoPersonalizado() {
        return PedidoForm.PRODUCTO_PERSONALIZADO;
    }

    /** Se usa como atributo min de los campos <input type="date">. */
    @ModelAttribute("fechaMinima")
    public String fechaMinima() {
        return LocalDate.now().plusDays(1).toString();
    }

    // ---------------- Encargar pedido ----------------

    @GetMapping("/pedido")
    public String formularioPedido(@RequestParam(required = false) String producto,
                                   HttpSession session,
                                   Model model) {

        Usuario cliente = sesionActual.usuarioConRol(session, Rol.CLIENTE).orElse(null);
        model.addAttribute("pedidoForm", PedidoForm.paraCliente(cliente, producto));
        return "pedido";
    }

    @PostMapping("/pedido")
    public String registrarPedido(@Valid @ModelAttribute("pedidoForm") PedidoForm pedidoForm,
                                  BindingResult resultado,
                                  HttpSession session,
                                  Model model,
                                  RedirectAttributes flash) {

        Optional<Usuario> cliente = sesionActual.usuarioConRol(session, Rol.CLIENTE);
        if (cliente.isEmpty()) {
            model.addAttribute("mostrarModalAuth", true);
            return "pedido";
        }

        validarProductoExiste(pedidoForm, resultado);

        if (resultado.hasErrors()) {
            return "pedido";
        }

        Pedido pedido = pedidoService.registrarPedido(pedidoForm, cliente.get());
        flash.addFlashAttribute("exito",
                "¡Pedido " + pedido.getCodigo() + " registrado! Te contactaremos para confirmar los detalles.");
        return "redirect:/cliente/panel";
    }

    // ---------------- Cotizar catering ----------------

    @GetMapping("/reservar")
    public String formularioReserva(Model model) {
        model.addAttribute("reservaForm", new ReservaForm());
        return "reservar";
    }

    @PostMapping("/reservar")
    public String registrarReserva(@Valid @ModelAttribute("reservaForm") ReservaForm reservaForm,
                                   BindingResult resultado,
                                   HttpSession session,
                                   Model model,
                                   RedirectAttributes flash) {

        Optional<Usuario> cliente = sesionActual.usuarioConRol(session, Rol.CLIENTE);
        if (cliente.isEmpty()) {
            model.addAttribute("mostrarModalAuth", true);
            return "reservar";
        }

        if (resultado.hasErrors()) {
            return "reservar";
        }

        Pedido pedido = pedidoService.registrarReserva(reservaForm, cliente.get());
        flash.addFlashAttribute("exito",
                "¡Solicitud " + pedido.getCodigo() + " registrada! Te enviaremos la cotización a tu correo.");
        return "redirect:/cliente/panel";
    }

    /** El desplegable puede traer un producto que el administrador acaba de eliminar. */
    private void validarProductoExiste(PedidoForm pedidoForm, BindingResult resultado) {
        if (resultado.hasFieldErrors("producto") || pedidoForm.esPersonalizado()) {
            return;
        }
        if (productoService.buscarPorNombre(pedidoForm.getProducto()).isEmpty()) {
            resultado.rejectValue("producto", "inexistente",
                    "El producto seleccionado ya no está disponible en el catálogo.");
        }
    }
}
