package com.x7.pasteleria.controller;

import com.x7.pasteleria.model.Producto;
import com.x7.pasteleria.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/productos")
public class ProductoController {

    private static final List<String> CATEGORIAS = List.of("Tortas", "Bocaditos", "Buffets", "Otros");

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @ModelAttribute("categorias")
    public List<String> categorias() {
        return CATEGORIAS;
    }

    // ---------------- CREATE ----------------

    @GetMapping("/nuevo")
    public String formularioNuevo(Model model) {
        model.addAttribute("producto", new Producto());
        return "panel/producto-form";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("producto") Producto producto,
                        BindingResult resultado,
                        RedirectAttributes flash) {

        validarNombreDisponible(producto, null, resultado);

        if (resultado.hasErrors()) {
            return "panel/producto-form";
        }

        producto.setId(null);
        Producto guardado = productoService.guardar(producto);
        flash.addFlashAttribute("exito", "Se agregó « " + guardado.getNombre() + " » al catálogo.");
        return "redirect:/admin/panel?tab=productos";
    }

    // ---------------- UPDATE ----------------

    @GetMapping("/{id}/editar")
    public String formularioEditar(@PathVariable Long id, Model model, RedirectAttributes flash) {
        Optional<Producto> producto = productoService.buscarPorId(id);
        if (producto.isEmpty()) {
            flash.addFlashAttribute("error", "El producto #" + id + " ya no existe.");
            return "redirect:/admin/panel?tab=productos";
        }

        model.addAttribute("producto", producto.get());
        return "panel/producto-form";
    }

    @PutMapping("/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("producto") Producto producto,
                             BindingResult resultado,
                             RedirectAttributes flash) {

        if (productoService.buscarPorId(id).isEmpty()) {
            flash.addFlashAttribute("error", "El producto #" + id + " ya no existe.");
            return "redirect:/admin/panel?tab=productos";
        }

        validarNombreDisponible(producto, id, resultado);

        if (resultado.hasErrors()) {
            producto.setId(id);
            return "panel/producto-form";
        }

        producto.setId(id);
        Producto guardado = productoService.guardar(producto);
        flash.addFlashAttribute("exito", "Se actualizó « " + guardado.getNombre() + " ».");
        return "redirect:/admin/panel?tab=productos";
    }

    // ---------------- DELETE ----------------

    @DeleteMapping("/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        Optional<Producto> producto = productoService.buscarPorId(id);
        if (producto.isEmpty()) {
            flash.addFlashAttribute("error", "El producto #" + id + " ya no existe.");
            return "redirect:/admin/panel?tab=productos";
        }

        String nombre = producto.get().getNombre();
        productoService.eliminar(id);
        flash.addFlashAttribute("exito", "Se eliminó « " + nombre + " » del catálogo.");
        return "redirect:/admin/panel?tab=productos";
    }

    private void validarNombreDisponible(Producto producto, Long idActual, BindingResult resultado) {
        if (resultado.hasFieldErrors("nombre")) {
            return;
        }
        if (productoService.nombreOcupadoPorOtro(producto.getNombre(), idActual)) {
            resultado.rejectValue("nombre", "duplicado", "Ya existe otro producto con ese nombre.");
        }
    }
}
