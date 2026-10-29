package com.x7.pasteleria.controller;

import com.x7.pasteleria.dto.PerfilForm;
import com.x7.pasteleria.model.EstadoPedido;
import com.x7.pasteleria.model.Pedido;
import com.x7.pasteleria.model.Producto;
import com.x7.pasteleria.model.Rol;
import com.x7.pasteleria.model.Usuario;
import com.x7.pasteleria.repository.PedidoRepository;
import com.x7.pasteleria.repository.ProductoRepository;
import com.x7.pasteleria.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
public class PanelController {

    private static final List<String> CATEGORIAS = List.of("Tortas", "Bocaditos", "Buffets", "Otros");
    private static final BigDecimal MONTO_MAXIMO = new BigDecimal("99999999.99");

    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final PedidoRepository pedidoRepository;

    public PanelController(UsuarioRepository usuarioRepository,
                           ProductoRepository productoRepository,
                           PedidoRepository pedidoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
        this.pedidoRepository = pedidoRepository;
    }

    /** El formulario de productos no puede cambiar el id: se toma de la URL. */
    @InitBinder("producto")
    public void camposProducto(WebDataBinder binder) {
        binder.setAllowedFields("nombre", "categoria", "etiqueta", "precio", "descripcion", "imagen", "disponible");
    }

    @ModelAttribute("categorias")
    public List<String> categorias() {
        return CATEGORIAS;
    }

    // =====================================================================
    //  Panel del cliente
    // =====================================================================

    @GetMapping("/cliente/panel")
    public String panelCliente(HttpSession session, Model model) {
        Usuario cliente = SesionUsuario.conRol(session, Rol.CLIENTE);
        if (cliente == null) {
            return "redirect:/login";
        }

        // SELECT ... FROM pedido p LEFT JOIN producto ... WHERE p.usuario_id = ?
        model.addAttribute("usuario", cliente);
        model.addAttribute("pedidos", pedidoRepository.findByUsuarioIdOrderByIdDesc(cliente.getId()));
        return "panel/panel-cliente";
    }

    @GetMapping("/cliente/perfil")
    public String editarPerfil(HttpSession session, Model model) {
        Usuario cliente = SesionUsuario.conRol(session, Rol.CLIENTE);
        if (cliente == null) {
            return "redirect:/login";
        }

        PerfilForm form = new PerfilForm();
        form.setNombre(cliente.getNombre());
        form.setTelefono(cliente.getTelefono());
        form.setDireccion(cliente.getDireccion());
        model.addAttribute("perfil", form);
        model.addAttribute("email", cliente.getEmail());
        return "panel/perfil";
    }

    @PostMapping("/cliente/perfil")
    public String actualizarPerfil(@Valid @ModelAttribute("perfil") PerfilForm form,
                                   BindingResult result,
                                   HttpSession session,
                                   Model model,
                                   RedirectAttributes flash) {

        Usuario cliente = SesionUsuario.conRol(session, Rol.CLIENTE);
        if (cliente == null) {
            return "redirect:/login";
        }
        if (result.hasErrors()) {
            model.addAttribute("email", cliente.getEmail());
            return "panel/perfil";
        }

        Usuario usuario = usuarioRepository.findById(cliente.getId()).orElseThrow();
        usuario.setNombre(form.getNombre());
        usuario.setTelefono(form.getTelefono());
        usuario.setDireccion(form.getDireccion());
        Usuario actualizado = usuarioRepository.save(usuario);   // UPDATE usuario SET ...

        SesionUsuario.iniciar(session, actualizado);   // la sesión muestra los datos nuevos
        flash.addFlashAttribute("exito", "Tus datos se actualizaron correctamente.");
        return "redirect:/cliente/panel";
    }

    // =====================================================================
    //  Panel del administrador
    // =====================================================================

    @GetMapping("/admin/panel")
    public String panelAdmin(@RequestParam(defaultValue = "pedidos") String tab,
                             @RequestParam(required = false) String estado,
                             @RequestParam(required = false) String q,
                             @RequestParam(required = false) String categoria,
                             HttpSession session,
                             Model model) {

        if (SesionUsuario.conRol(session, Rol.ADMINISTRADOR) == null) {
            return "redirect:/login";
        }
        if (!model.containsAttribute("producto")) {
            model.addAttribute("producto", new Producto());
        }
        cargarPanel(model, tab, estado, q, categoria);
        return "panel/panel-admin";
    }

    // ------------------------- Productos (CRUD) -------------------------

    /** CREATE: registra un producto validado con @Valid. */
    @PostMapping("/admin/productos")
    public String agregarProducto(@Valid @ModelAttribute("producto") Producto producto,
                                  BindingResult result,
                                  HttpSession session,
                                  Model model,
                                  RedirectAttributes flash) {

        if (SesionUsuario.conRol(session, Rol.ADMINISTRADOR) == null) {
            return "redirect:/login";
        }

        if (producto.getNombre() != null && productoRepository.existsByNombreIgnoreCase(producto.getNombre())) {
            result.rejectValue("nombre", "producto.nombre.duplicado");
        }
        if (result.hasErrors()) {
            // Se vuelve a mostrar el panel con los mensajes debajo de cada campo.
            cargarPanel(model, "productos", null, null, null);
            return "panel/panel-admin";
        }

        if (producto.getEtiqueta() == null) {
            producto.setEtiqueta(producto.getCategoria());
        }
        productoRepository.save(producto);   // INSERT INTO producto ...

        flash.addFlashAttribute("exito", "Se agregó « " + producto.getNombre() + " » al catálogo.");
        return "redirect:/admin/panel?tab=productos";
    }

    /** Carga el producto en el formulario del panel para editarlo. */
    @GetMapping("/admin/productos/{id}/editar")
    public String editarProducto(@PathVariable Long id, HttpSession session, Model model, RedirectAttributes flash) {
        if (SesionUsuario.conRol(session, Rol.ADMINISTRADOR) == null) {
            return "redirect:/login";
        }

        Optional<Producto> producto = productoRepository.findById(id);   // SELECT ... WHERE id = ?
        if (producto.isEmpty()) {
            flash.addFlashAttribute("error", "Ese producto ya no existe.");
            return "redirect:/admin/panel?tab=productos";
        }

        model.addAttribute("producto", producto.get());
        cargarPanel(model, "productos", null, null, null);
        return "panel/panel-admin";
    }

    /** UPDATE: actualiza un producto existente con las mismas validaciones del registro. */
    @PostMapping("/admin/productos/{id}/editar")
    public String actualizarProducto(@PathVariable Long id,
                                     @Valid @ModelAttribute("producto") Producto datos,
                                     BindingResult result,
                                     HttpSession session,
                                     Model model,
                                     RedirectAttributes flash) {

        if (SesionUsuario.conRol(session, Rol.ADMINISTRADOR) == null) {
            return "redirect:/login";
        }

        Optional<Producto> existente = productoRepository.findById(id);
        if (existente.isEmpty()) {
            flash.addFlashAttribute("error", "Ese producto ya no existe.");
            return "redirect:/admin/panel?tab=productos";
        }

        datos.setId(id);   // el formulario sigue en modo edición si hay errores
        if (datos.getNombre() != null && productoRepository.existsByNombreIgnoreCaseAndIdNot(datos.getNombre(), id)) {
            result.rejectValue("nombre", "producto.nombre.duplicado");
        }
        if (result.hasErrors()) {
            cargarPanel(model, "productos", null, null, null);
            return "panel/panel-admin";
        }

        Producto producto = existente.get();
        producto.actualizarCon(datos);
        if (producto.getEtiqueta() == null) {
            producto.setEtiqueta(producto.getCategoria());
        }
        productoRepository.save(producto);   // UPDATE producto SET ... WHERE id = ?

        flash.addFlashAttribute("exito", "Se actualizó « " + producto.getNombre() + " ».");
        return "redirect:/admin/panel?tab=productos";
    }

    /** DELETE: solo si ningún pedido usa el producto (integridad referencial). */
    @PostMapping("/admin/productos/{id}/eliminar")
    public String eliminarProducto(@PathVariable Long id, HttpSession session, RedirectAttributes flash) {
        if (SesionUsuario.conRol(session, Rol.ADMINISTRADOR) == null) {
            return "redirect:/login";
        }

        Optional<Producto> producto = productoRepository.findById(id);
        if (producto.isEmpty()) {
            flash.addFlashAttribute("error", "Ese producto ya no existe.");
            return "redirect:/admin/panel?tab=productos";
        }

        long enPedidos = pedidoRepository.countByProductoId(id);
        if (enPedidos > 0) {
            flash.addFlashAttribute("error", "No se puede eliminar « " + producto.get().getNombre()
                    + " » porque está en " + enPedidos + " pedido(s). Edítalo y desmarca «Disponible» para ocultarlo del catálogo.");
            return "redirect:/admin/panel?tab=productos";
        }

        productoRepository.deleteById(id);   // DELETE FROM producto WHERE id = ?
        flash.addFlashAttribute("exito", "Producto eliminado del catálogo.");
        return "redirect:/admin/panel?tab=productos";
    }

    // ------------------------- Pedidos -------------------------

    @PostMapping("/admin/pedidos/{id}/estado")
    public String cambiarEstado(@PathVariable Long id,
                                @RequestParam(required = false) String estado,
                                HttpSession session,
                                RedirectAttributes flash) {

        if (SesionUsuario.conRol(session, Rol.ADMINISTRADOR) == null) {
            return "redirect:/login";
        }

        Optional<Pedido> pedido = pedidoRepository.findById(id);
        Optional<EstadoPedido> nuevoEstado = convertirEstado(estado);
        if (pedido.isEmpty() || nuevoEstado.isEmpty()) {
            flash.addFlashAttribute("error", "No se pudo actualizar el pedido: el pedido o el estado no son válidos.");
            return "redirect:/admin/panel";
        }

        pedido.get().setEstado(nuevoEstado.get());
        pedidoRepository.save(pedido.get());   // UPDATE pedido SET estado = ?
        flash.addFlashAttribute("exito", "El pedido " + pedido.get().getCodigo()
                + " pasó a « " + nuevoEstado.get().getEtiqueta() + " ».");
        return "redirect:/admin/panel";
    }

    /** Confirma el monto de una cotización de catering o de un pedido personalizado. */
    @PostMapping("/admin/pedidos/{id}/monto")
    public String confirmarMonto(@PathVariable Long id,
                                 @RequestParam(required = false) String monto,
                                 HttpSession session,
                                 RedirectAttributes flash) {

        if (SesionUsuario.conRol(session, Rol.ADMINISTRADOR) == null) {
            return "redirect:/login";
        }

        Optional<Pedido> pedido = pedidoRepository.findById(id);
        if (pedido.isEmpty()) {
            flash.addFlashAttribute("error", "Ese pedido ya no existe.");
            return "redirect:/admin/panel";
        }

        BigDecimal valor = convertirMonto(monto);
        if (valor == null) {
            flash.addFlashAttribute("error", "El monto del pedido " + pedido.get().getCodigo()
                    + " debe ser un número mayor a S/ 0.00 con hasta 2 decimales.");
            return "redirect:/admin/panel";
        }

        pedido.get().setMonto(valor);
        pedidoRepository.save(pedido.get());
        flash.addFlashAttribute("exito", "Se confirmó el monto del pedido " + pedido.get().getCodigo()
                + ": S/ " + valor + ".");
        return "redirect:/admin/panel";
    }

    @PostMapping("/admin/pedidos/{id}/eliminar")
    public String eliminarPedido(@PathVariable Long id, HttpSession session, RedirectAttributes flash) {
        if (SesionUsuario.conRol(session, Rol.ADMINISTRADOR) == null) {
            return "redirect:/login";
        }

        if (!pedidoRepository.existsById(id)) {
            flash.addFlashAttribute("error", "Ese pedido ya no existe.");
            return "redirect:/admin/panel";
        }
        pedidoRepository.deleteById(id);   // DELETE FROM pedido WHERE id = ?
        flash.addFlashAttribute("exito", "Pedido eliminado.");
        return "redirect:/admin/panel";
    }

    // =====================================================================
    //  Apoyo
    // =====================================================================

    /** Datos que necesita la vista del panel administrador (tablas, filtros y contadores). */
    private void cargarPanel(Model model, String tab, String estado, String q, String categoria) {
        Optional<EstadoPedido> filtroEstado = convertirEstado(estado);
        List<Pedido> pedidos = filtroEstado
                .map(pedidoRepository::findByEstadoOrderByIdDesc)
                .orElseGet(pedidoRepository::findAllByOrderByIdDesc);

        model.addAttribute("tab", tab);
        model.addAttribute("pedidos", pedidos);
        model.addAttribute("estados", EstadoPedido.values());
        model.addAttribute("estadoFiltro", filtroEstado.orElse(null));

        model.addAttribute("productos", productoRepository.buscar(q, categoria));
        model.addAttribute("q", q);
        model.addAttribute("categoriaFiltro", categoria);

        model.addAttribute("clientes", usuarioRepository.resumenPorRol(Rol.CLIENTE));

        // Contadores (SELECT count(*) ...)
        model.addAttribute("totalPendientes", pedidoRepository.countByEstado(EstadoPedido.PENDIENTE));
        model.addAttribute("totalEnPreparacion", pedidoRepository.countByEstado(EstadoPedido.EN_PREPARACION));
        model.addAttribute("totalProductos", productoRepository.count());
        model.addAttribute("totalClientes", usuarioRepository.countByRol(Rol.CLIENTE));
    }

    private Optional<EstadoPedido> convertirEstado(String texto) {
        return Arrays.stream(EstadoPedido.values())
                .filter(e -> e.name().equals(texto))
                .findFirst();
    }

    /** Devuelve el monto si es un número entre 0.01 y 99 999 999.99 con hasta 2 decimales; si no, null. */
    private BigDecimal convertirMonto(String texto) {
        if (texto == null) return null;
        try {
            BigDecimal valor = new BigDecimal(texto.replace(",", "."));
            boolean valido = valor.compareTo(new BigDecimal("0.01")) >= 0
                    && valor.compareTo(MONTO_MAXIMO) <= 0
                    && valor.scale() <= 2;
            return valido ? valor.setScale(2) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
