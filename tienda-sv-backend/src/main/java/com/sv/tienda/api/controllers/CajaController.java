package com.sv.tienda.api.controllers;

import com.sv.tienda.infrastructure.persistence.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/dashboard/caja")
@RequiredArgsConstructor
public class CajaController {

    private final JpaProductoRepository productoRepository;
    private final JpaPedidoRepository pedidoRepository;
    private final JpaUsuarioRepository usuarioRepository;
    private final JpaCategoriaRepository categoriaRepository;

    @GetMapping("/nueva-venta")
    public String nuevaVenta(Authentication auth, Model model) {
        model.addAttribute("section", "pos");
        model.addAttribute("username", auth.getName());
        model.addAttribute("rol", auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", ""));
        model.addAttribute("productos", productoRepository.findAll());
        model.addAttribute("categorias", categoriaRepository.findAll());
        return "dashboard"; // Reutilizamos dashboard con sección 'pos'
    }

    @PostMapping("/procesar-pago")
    public String procesarPago(@RequestParam(required = false) List<Long> productoIds,
                               @RequestParam(required = false) List<Integer> cantidades,
                               @RequestParam BigDecimal montoRecibido,
                               Authentication auth,
                               RedirectAttributes attrs) {
        
        if (productoIds == null || productoIds.isEmpty()) {
            attrs.addFlashAttribute("errorMsg", "Error: No se pueden procesar ventas con el carrito vacío.");
            return "redirect:/dashboard/caja/nueva-venta";
        }

        var usuario = usuarioRepository.findByUsername(auth.getName()).orElseThrow();
        
        BigDecimal total = BigDecimal.ZERO;
        List<DetallePedidoEntity> detalles = new ArrayList<>();
        PedidoEntity pedido = new PedidoEntity();
        pedido.setUsuario(usuario);
        pedido.setMetodoPago("EFECTIVO");
        pedido.setEstado("COMPLETADO");
        pedido.setMontoRecibido(montoRecibido);

        for (int i = 0; i < productoIds.size(); i++) {
            var producto = productoRepository.findById(productoIds.get(i)).orElseThrow();
            int cantidad = cantidades.get(i);
            
            if (producto.getStock() < cantidad) {
                attrs.addFlashAttribute("errorMsg", "Stock insuficiente para: " + producto.getNombre());
                return "redirect:/dashboard/caja/nueva-venta";
            }

            BigDecimal subtotal = producto.getPrecio().multiply(BigDecimal.valueOf(cantidad));
            total = total.add(subtotal);

            DetallePedidoEntity detalle = new DetallePedidoEntity();
            detalle.setPedido(pedido);
            detalle.setProducto(producto);
            detalle.setCantidad(cantidad);
            detalle.setPrecioUnitario(producto.getPrecio());
            detalles.add(detalle);

            // Actualizar stock
            producto.setStock(producto.getStock() - cantidad);
            productoRepository.save(producto);
        }

        if (montoRecibido.compareTo(total) < 0) {
            attrs.addFlashAttribute("errorMsg", "El monto recibido es menor al total: $" + total);
            return "redirect:/dashboard/caja/nueva-venta";
        }

        pedido.setTotal(total);
        pedido.setVuelto(montoRecibido.subtract(total));
        pedido.setDetalles(detalles);

        pedidoRepository.save(pedido);

        attrs.addFlashAttribute("successMsg", "Venta realizada con éxito. Vuelto: $" + pedido.getVuelto());
        return "redirect:/dashboard/caja/nueva-venta";
    }
}
