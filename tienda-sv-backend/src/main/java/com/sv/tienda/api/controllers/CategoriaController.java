package com.sv.tienda.api.controllers;

import com.sv.tienda.infrastructure.persistence.CategoriaEntity;
import com.sv.tienda.infrastructure.persistence.JpaCategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final JpaCategoriaRepository categoriaRepository;

    @PostMapping("/guardar")
    public String guardarCategoria(CategoriaEntity categoria, RedirectAttributes redirectAttributes) {
        try {
            if (categoria.getActivo() == null) categoria.setActivo(true);
            categoriaRepository.save(categoria);
            redirectAttributes.addFlashAttribute("successMsg", "¡Categoría '" + categoria.getNombre() + "' guardada con éxito!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Error al guardar la categoría. Verifique que el nombre no esté duplicado.");
        }
        return "redirect:/dashboard/categorias";
    }

    @org.springframework.web.bind.annotation.GetMapping("/eliminar/{id}")
    public String eliminarCategoria(@org.springframework.web.bind.annotation.PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoriaRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMsg", "La categoría y sus productos asociados han sido eliminados.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Error al eliminar: " + e.getMessage());
        }
        return "redirect:/dashboard/categorias";
    }


    @org.springframework.web.bind.annotation.GetMapping("/toggle/{id}")
    public String toggleCategoria(@org.springframework.web.bind.annotation.PathVariable Long id, RedirectAttributes redirectAttributes) {
        categoriaRepository.findById(id).ifPresent(c -> {
            c.setActivo(!c.getActivo());
            categoriaRepository.save(c);
            String status = c.getActivo() ? "activada" : "desactivada";
            redirectAttributes.addFlashAttribute("successMsg", "Categoría '" + c.getNombre() + "' " + status + " correctamente.");
        });
        return "redirect:/dashboard/categorias";
    }
}

