package com.x7.pasteleria.controller;

import com.x7.pasteleria.data.Datos;
import com.x7.pasteleria.model.Producto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class WebController {

    @GetMapping("/")
    public String inicio() {
        return "index";
    }

    @GetMapping("/catalogo")
    public String catalogo(Model model) {
        List<String> categorias = Datos.PRODUCTOS.stream()
                .map(Producto::getCategoria)
                .distinct()
                .sorted()
                .toList();

        model.addAttribute("productos", Datos.PRODUCTOS);
        model.addAttribute("categorias", categorias);
        return "catalogo";
    }
}
