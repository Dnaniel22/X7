package com.x7.pasteleria.repository;

import com.x7.pasteleria.model.EstadoPedido;
import com.x7.pasteleria.model.Pedido;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * @EntityGraph indica a Hibernate que traiga el usuario y el producto de cada pedido
 * en la misma consulta (LEFT JOIN), para mostrarlos en las vistas sin consultas extra.
 */
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    /** Panel del administrador: todos los pedidos, del más reciente al más antiguo. */
    @EntityGraph(attributePaths = {"usuario", "producto"})
    List<Pedido> findAllByOrderByIdDesc();

    /** Panel del administrador filtrado por estado. */
    @EntityGraph(attributePaths = {"usuario", "producto"})
    List<Pedido> findByEstadoOrderByIdDesc(EstadoPedido estado);

    /** Panel del cliente: solo sus pedidos (se navega por la relación usuario.id). */
    @EntityGraph(attributePaths = {"usuario", "producto"})
    List<Pedido> findByUsuarioIdOrderByIdDesc(Long usuarioId);

    /** Un pedido con su usuario y su producto (API REST). */
    @EntityGraph(attributePaths = {"usuario", "producto"})
    Optional<Pedido> findCompletoById(Long id);

    long countByEstado(EstadoPedido estado);

    /** Cuántos pedidos usan un producto: si hay alguno, el producto no se puede eliminar. */
    long countByProductoId(Long productoId);
}
