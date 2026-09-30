package com.x7.pasteleria.repository;

import com.x7.pasteleria.model.EstadoPedido;
import com.x7.pasteleria.model.Pedido;
import com.x7.pasteleria.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findAllByOrderByIdDesc();

    List<Pedido> findByClienteOrderByIdDesc(Usuario cliente);

    long countByEstado(EstadoPedido estado);

    long countByCliente(Usuario cliente);

    @Query("""
            SELECT p FROM Pedido p
            WHERE (:estado IS NULL OR p.estado = :estado)
              AND (:texto IS NULL
                   OR LOWER(p.codigo)         LIKE LOWER(CONCAT('%', :texto, '%'))
                   OR LOWER(p.producto)       LIKE LOWER(CONCAT('%', :texto, '%'))
                   OR LOWER(p.cliente.nombre) LIKE LOWER(CONCAT('%', :texto, '%')))
            ORDER BY p.id DESC
            """)
    List<Pedido> buscar(@Param("estado") EstadoPedido estado, @Param("texto") String texto);
}
