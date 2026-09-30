package com.x7.pasteleria.repository;

import com.x7.pasteleria.model.Rol;
import com.x7.pasteleria.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<Usuario> findAllByOrderByNombreAsc();

    long countByRol(Rol rol);
}
