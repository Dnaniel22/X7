package com.x7.pasteleria.repository;

import com.x7.pasteleria.model.Producto;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // ---------- Consultas derivadas ----------

    List<Producto> findByDisponibleTrueOrderByCategoriaAscNombreAsc();

    List<Producto> findByNombreContainingIgnoreCase(String nombre, Sort orden);

    List<Producto> findByCategoria(String categoria, Sort orden);

    List<Producto> findByCategoriaAndNombreContainingIgnoreCase(String categoria, String nombre, Sort orden);

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    // ---------- Consulta JPQL ----------

    @Query("select distinct p.categoria from Producto p where p.disponible = true order by p.categoria")
    List<String> categoriasDisponibles();

    default List<Producto> buscar(String nombre, String categoria) {
        Sort orden = Sort.by("id");
        boolean conNombre = nombre != null && !nombre.isBlank();
        boolean conCategoria = categoria != null && !categoria.isBlank();

        if (conNombre && conCategoria) {
            return findByCategoriaAndNombreContainingIgnoreCase(categoria, nombre.trim(), orden);
        }
        if (conNombre) {
            return findByNombreContainingIgnoreCase(nombre.trim(), orden);
        }
        if (conCategoria) {
            return findByCategoria(categoria, orden);
        }
        return findAll(orden);
    }
}
