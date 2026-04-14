package com.sv.tienda.api.controllers;

import com.sv.tienda.infrastructure.persistence.JpaUsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/usuarios/ui")
@RequiredArgsConstructor
public class UsuarioUIController {

    private final JpaUsuarioRepository usuarioRepository;
    private final com.sv.tienda.infrastructure.persistence.JpaRolRepository rolRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @org.springframework.web.bind.annotation.PostMapping("/guardar")
    public String guardarUsuario(com.sv.tienda.infrastructure.persistence.UsuarioEntity user,
                                 @org.springframework.web.bind.annotation.RequestParam(required = false) Long rolId,
                                 @org.springframework.web.bind.annotation.RequestParam(required = false) String password,
                                 RedirectAttributes redirectAttributes) {
        try {
            if (user.getId() != null) {
                // Edición
                var existing = usuarioRepository.findById(user.getId()).orElseThrow();
                existing.setUsername(user.getUsername());
                existing.setEmail(user.getEmail());
                if (rolId != null) rolRepository.findById(rolId).ifPresent(existing::setRol);
                usuarioRepository.save(existing);
                redirectAttributes.addFlashAttribute("successMsg", "¡Usuario '" + user.getUsername() + "' actualizado!");
            } else {
                // Nuevo
                if (rolId != null) rolRepository.findById(rolId).ifPresent(user::setRol);
                if (password != null && !password.isEmpty()) {
                    user.setPasswordHash(passwordEncoder.encode(password));
                }
                user.setEstado(true);
                usuarioRepository.save(user);
                redirectAttributes.addFlashAttribute("successMsg", "¡Nuevo usuario '" + user.getUsername() + "' creado!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Error al procesar el usuario: " + e.getMessage());
        }
        return "redirect:/dashboard/usuarios";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMsg", "El usuario ha sido eliminado del sistema.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "No se puede eliminar el usuario. Puede que tenga ventas o registros asociados.");
        }
        return "redirect:/dashboard/usuarios";
    }

    @GetMapping("/toggle/{id}")
    public String toggleUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        usuarioRepository.findById(id).ifPresent(u -> {
            u.setEstado(!u.isEstado());
            usuarioRepository.save(u);
            String action = u.isEstado() ? "activado" : "desactivado";
            redirectAttributes.addFlashAttribute("successMsg", "Usuario '" + u.getUsername() + "' " + action + " correctamente.");
        });
        return "redirect:/dashboard/usuarios";
    }
}
