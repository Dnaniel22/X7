package com.x7.pasteleria.repository;

import com.x7.pasteleria.dto.ClienteResumen;
import com.x7.pasteleria.model.Rol;
import com.x7.pasteleria.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Consultas derivadas: Spring Data genera el SQL a partir del nombre del método.
    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    long countByRol(Rol rol);

    /**
     * Clientes con la cantidad de pedidos de cada uno.
     * Recorre la relación @OneToMany (u.pedidos) con un LEFT JOIN para incluir
     * también a los clientes que todavía no tienen pedidos.
     */
    @Query("""
            select new com.x7.pasteleria.dto.ClienteResumen(
                       u.id, u.nombre, u.email, u.telefono, u.direccion, count(p))
            from Usuario u left join u.pedidos p
            where u.rol = :rol
            group by u.id, u.nombre, u.email, u.telefono, u.direccion
            order by u.nombre
            """)
    List<ClienteResumen> resumenPorRol(@Param("rol") Rol rol);
}
