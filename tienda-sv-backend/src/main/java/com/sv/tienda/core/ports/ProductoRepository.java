package com.sv.tienda.core.ports;

import com.sv.tienda.core.model.Producto;
import java.util.List;
import java.util.Optional;

public interface ProductoRepository {
    List<Producto> buscarTodos();
    Optional<Producto> buscarPorId(Long id);
    Producto guardar(Producto producto);
    void eliminar(Long id);
}
