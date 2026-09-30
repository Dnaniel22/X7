package com.x7.pasteleria.service;

import com.x7.pasteleria.model.Producto;
import com.x7.pasteleria.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<Producto> listar() {
        return productoRepository.findAllByOrderByNombreAsc();
    }

    public Optional<Producto> buscarPorId(Long id) {
        return productoRepository.findById(id);
    }

    public Optional<Producto> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreIgnoreCase(nombre == null ? "" : nombre.trim());
    }

    public boolean nombreOcupadoPorOtro(String nombre, Long idActual) {
        return productoRepository.findByNombreIgnoreCase(nombre == null ? "" : nombre.trim())
                .filter(p -> !p.getId().equals(idActual))
                .isPresent();
    }

    @Transactional
    public Producto guardar(Producto producto) {
        producto.setNombre(producto.getNombre().trim());
        if (producto.getEtiqueta() == null || producto.getEtiqueta().isBlank()) {
            producto.setEtiqueta(producto.getCategoria());
        }
        if (producto.getImagen() != null && producto.getImagen().isBlank()) {
            producto.setImagen(null);
        }
        return productoRepository.save(producto);
    }

    @Transactional
    public void eliminar(Long id) {
        productoRepository.deleteById(id);
    }

    public long total() {
        return productoRepository.count();
    }
}
