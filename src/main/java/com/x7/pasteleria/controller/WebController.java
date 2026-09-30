package com.x7.pasteleria.controller;

import com.x7.pasteleria.model.Producto;
import com.x7.pasteleria.service.ProductoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Comparator;
import java.util.List;

@Controller
public class WebController {

    private final ProductoService productoService;

    public WebController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping("/")
    public String inicio() {
        return "index";
    }

    @GetMapping("/catalogo")
    public String catalogo(Model model) {
        List<Producto> productos = productoService.listar();

        List<String> categorias = productos.stream()
                .map(Producto::getCategoria)
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();

        model.addAttribute("productos", productos);
        model.addAttribute("categorias", categorias);
        return "catalogo";
    }
}
