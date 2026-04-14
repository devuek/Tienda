package com.sv.tienda.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductoDTO {
    private Long id;

    @NotBlank(message = "{validation.field.required}")
    private String nombre;

    private String descripcion;

    @NotNull(message = "{validation.field.required}")
    @Min(value = 0, message = "El precio debe ser un valor positivo")
    private BigDecimal precio;

    @NotNull(message = "{validation.field.required}")
    @Min(value = 0, message = "El stock no debe ser negativo")
    private Integer stock;
}
