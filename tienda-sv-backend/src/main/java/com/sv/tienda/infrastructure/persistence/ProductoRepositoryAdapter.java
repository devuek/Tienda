package com.sv.tienda.infrastructure.persistence;

import com.sv.tienda.core.model.Producto;
import com.sv.tienda.core.ports.ProductoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class ProductoRepositoryAdapter implements ProductoRepository {

    private final JpaProductoRepository jpaRepository;

    public ProductoRepositoryAdapter(JpaProductoRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    private Producto toDomain(ProductoEntity entity) {
        return Producto.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .descripcion(entity.getDescripcion())
                .precio(entity.getPrecio())
                .stock(entity.getStock())
                .activo(entity.isActivo())
                .build();
    }

    private ProductoEntity toEntity(Producto domain) {
        return ProductoEntity.builder()
                .id(domain.getId())
                .nombre(domain.getNombre())
                .descripcion(domain.getDescripcion())
                .precio(domain.getPrecio())
                .stock(domain.getStock())
                .activo(domain.isActivo())
                .build();
    }

    @Override
    public List<Producto> buscarTodos() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Producto> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Producto guardar(Producto producto) {
        ProductoEntity entity = toEntity(producto);
        ProductoEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void eliminar(Long id) {
        jpaRepository.deleteById(id);
    }
}
