package com.x7.pasteleria.controller;

import com.x7.pasteleria.repository.ProductoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    private final ProductoRepository productoRepository;

    public WebController(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @GetMapping("/")
    public String inicio() {
        return "index";
    }

    @GetMapping("/catalogo")
    public String catalogo(Model model) {
        // Los productos y las categorías se leen de PostgreSQL en cada visita.
        model.addAttribute("productos", productoRepository.findByDisponibleTrueOrderByCategoriaAscNombreAsc());
        model.addAttribute("categorias", productoRepository.categoriasDisponibles());
        return "catalogo";
    }
}
