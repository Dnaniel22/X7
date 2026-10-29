package com.x7.pasteleria.repository;

import com.x7.pasteleria.model.EstadoPedido;
import com.x7.pasteleria.model.Pedido;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @EntityGraph(attributePaths = {"usuario", "producto"})
    List<Pedido> findAllByOrderByIdDesc();

    @EntityGraph(attributePaths = {"usuario", "producto"})
    List<Pedido> findByEstadoOrderByIdDesc(EstadoPedido estado);

    @EntityGraph(attributePaths = {"usuario", "producto"})
    List<Pedido> findByUsuarioIdOrderByIdDesc(Long usuarioId);

    @EntityGraph(attributePaths = {"usuario", "producto"})
    Optional<Pedido> findCompletoById(Long id);

    long countByEstado(EstadoPedido estado);

    long countByProductoId(Long productoId);
}
