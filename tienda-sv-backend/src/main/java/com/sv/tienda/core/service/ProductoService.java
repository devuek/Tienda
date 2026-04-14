package com.sv.tienda.core.service;

import com.sv.tienda.core.model.Producto;
import com.sv.tienda.core.ports.ProductoRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<Producto> obtenerTodos() {
        return productoRepository.buscarTodos();
    }

    public Producto buscarPorId(Long id) {
        return productoRepository.buscarPorId(id).orElseThrow(() -> new RuntimeException("error.product.not.found"));
    }

    public Producto guardarProducto(Producto producto) {
        return productoRepository.guardar(producto);
    }
}
