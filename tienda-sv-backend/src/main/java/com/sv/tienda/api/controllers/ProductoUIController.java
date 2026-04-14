package com.sv.tienda.api.controllers;

import com.sv.tienda.infrastructure.persistence.ProductoEntity;
import com.sv.tienda.infrastructure.persistence.JpaProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/productos/ui")
@RequiredArgsConstructor
public class ProductoUIController {

    private final JpaProductoRepository productoRepository;
    private final com.sv.tienda.infrastructure.persistence.JpaCategoriaRepository categoriaRepository;

    @PostMapping("/guardar")
    public String guardarProducto(ProductoEntity producto, 
                                 @org.springframework.web.bind.annotation.RequestParam(required = false) Long categoriaId,
                                 RedirectAttributes redirectAttributes) {
        try {
            if (categoriaId != null) {
                categoriaRepository.findById(categoriaId).ifPresent(producto::setCategoria);
            }
            productoRepository.save(producto);
            redirectAttributes.addFlashAttribute("successMsg", "¡Producto '" + producto.getNombre() + "' gestionado con éxito!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Fallo al procesar producto en inventario.");
        }
        return "redirect:/dashboard/productos";
    }

    @org.springframework.web.bind.annotation.GetMapping("/eliminar/{id}")
    public String eliminarProducto(@org.springframework.web.bind.annotation.PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productoRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMsg", "El producto ha sido removido del catálogo.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Fallo al eliminar producto. Asegúrese de que no tenga pedidos asociados.");
        }
        return "redirect:/dashboard/productos";
    }

    @org.springframework.web.bind.annotation.GetMapping("/toggle/{id}")
    public String toggleProducto(@org.springframework.web.bind.annotation.PathVariable Long id, RedirectAttributes redirectAttributes) {
        productoRepository.findById(id).ifPresent(p -> {
            p.setActivo(!p.isActivo());
            productoRepository.save(p);
            String status = p.isActivo() ? "visible en tienda" : "oculto para clientes";
            redirectAttributes.addFlashAttribute("successMsg", "El producto '" + p.getNombre() + "' ahora está " + status + ".");
        });
        return "redirect:/dashboard/productos";
    }
}

