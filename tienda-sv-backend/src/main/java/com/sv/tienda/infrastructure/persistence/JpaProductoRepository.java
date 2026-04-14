package com.sv.tienda.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaProductoRepository extends JpaRepository<ProductoEntity, Long> {
    Optional<ProductoEntity> findByNombre(String nombre);
}
