package com.sv.tienda.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JpaPedidoRepository extends JpaRepository<PedidoEntity, Long> {
    List<PedidoEntity> findByUsuarioOrderByFechaDesc(UsuarioEntity usuario);
    List<PedidoEntity> findByFechaBetweenOrderByFechaDesc(java.time.LocalDateTime start, java.time.LocalDateTime end);
}
