package com.sv.tienda.api.controllers;

import com.sv.tienda.api.dto.ProductoDTO;
import com.sv.tienda.core.model.Producto;
import com.sv.tienda.core.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {

    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    private ProductoDTO toDto(Producto domain) {
        ProductoDTO dto = new ProductoDTO();
        dto.setId(domain.getId());
        dto.setNombre(domain.getNombre());
        dto.setDescripcion(domain.getDescripcion());
        dto.setPrecio(domain.getPrecio());
        dto.setStock(domain.getStock());
        return dto;
    }

    private Producto toDomain(ProductoDTO dto) {
        return Producto.builder()
                .id(dto.getId())
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .precio(dto.getPrecio())
                .stock(dto.getStock())
                .activo(true)
                .build();
    }

    @GetMapping
    public ResponseEntity<List<ProductoDTO>> obtenerTodos() {
        List<ProductoDTO> dtos = service.obtenerTodos().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<ProductoDTO> crear(@Valid @RequestBody ProductoDTO dto) {
        Producto domain = toDomain(dto);
        Producto guardado = service.guardarProducto(domain);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(guardado));
    }
}
