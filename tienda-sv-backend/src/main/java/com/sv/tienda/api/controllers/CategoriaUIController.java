package com.sv.tienda.api.controllers;

import com.sv.tienda.infrastructure.persistence.JpaCategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/categorias/ui")
@RequiredArgsConstructor
public class CategoriaUIController {

    private final JpaCategoriaRepository categoriaRepository;

    @GetMapping("/eliminar/{id}")
    public String eliminarCategoria(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoriaRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMsg", "¡Categoría eliminada con éxito!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Fallo al eliminar categoría. Verifique que no tenga productos asociados.");
        }
        return "redirect:/dashboard/categorias";
    }

    @GetMapping("/toggle/{id}")
    public String toggleCategoria(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        categoriaRepository.findById(id).ifPresent(c -> {
            c.setActivo(!c.getActivo());
            categoriaRepository.save(c);
            String status = c.getActivo() ? "activada" : "desactivada";
            redirectAttributes.addFlashAttribute("successMsg", "La categoría '" + c.getNombre() + "' fue " + status + ".");
        });
        return "redirect:/dashboard/categorias";
    }
}
