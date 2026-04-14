package com.sv.tienda.api.controllers;

import com.sv.tienda.infrastructure.persistence.JpaCategoriaRepository;
import com.sv.tienda.infrastructure.persistence.JpaProductoRepository;
import com.sv.tienda.infrastructure.persistence.JpaUsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class TemplateUIController {

    private final JpaUsuarioRepository usuarioRepository;
    private final JpaProductoRepository productoRepository;
    private final JpaCategoriaRepository categoriaRepository;
    private final com.sv.tienda.infrastructure.persistence.JpaPedidoRepository pedidoRepository;
    private final com.sv.tienda.infrastructure.persistence.JpaRolRepository rolRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @GetMapping({"/", "/tienda"})
    public String renderStoreFront(@RequestParam(value = "logout", required = false) String logout,
                                   @RequestParam(value = "cat", required = false) String categoriaNombre,
                                   Authentication auth,
                                   Model model) {
        if (logout != null) model.addAttribute("logoutMsg", "¡Hasta pronto! Tu sesión fue cerrada.");
        
        var productos = (categoriaNombre == null || categoriaNombre.isEmpty())
                ? productoRepository.findAll()
                : productoRepository.findAll().stream()
                    .filter(p -> p.getCategoria() != null && p.getCategoria().getNombre().equalsIgnoreCase(categoriaNombre))
                    .toList();
        
        System.out.println("DEBUG: Cargando Storefront [" + (categoriaNombre != null ? categoriaNombre : "ALL") + "] con " + productos.size() + " productos.");
        
        if (auth != null && auth.isAuthenticated()) {
            usuarioRepository.findByUsername(auth.getName()).ifPresent(u -> model.addAttribute("user", u));
        }

        model.addAttribute("productos", productos);
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("catActiva", categoriaNombre);
        
        return "storefront-template";
    }

    /**
     * Login de CLIENTES — visible en la tienda, con opción de registro.
     * No muestra credenciales de staff.
     */
    @GetMapping("/login")
    public String loginCliente(
            @RequestParam(value = "error", required = false) String error,
            Authentication auth,
            Model model) {
        if (auth != null && auth.isAuthenticated()) return "redirect:/dashboard";
        if (error != null) model.addAttribute("errorMsg", "Usuario o contraseña incorrectos.");
        model.addAttribute("esStaff", false);
        return "login";
    }

    @GetMapping("/staff/login")
    public String loginStaff(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Authentication auth,
            Model model) {
        if (auth != null && auth.isAuthenticated()) return "redirect:/dashboard";
        if (error != null)  model.addAttribute("errorMsg", "Credenciales incorrectas. Acceso denegado.");
        if (logout != null) model.addAttribute("logoutMsg", "Sesión cerrada correctamente.");
        model.addAttribute("esStaff", true);
        return "login";
    }

    @GetMapping("/registro")
    public String registroCliente() {
        return "registro";
    }

    @org.springframework.web.bind.annotation.PostMapping("/registro")
    public String procesarRegistro(@RequestParam String username,
                                   @RequestParam String email,
                                   @RequestParam String password,
                                   @RequestParam String confirmPassword,
                                   Model model) {
        
        if (!password.equals(confirmPassword)) {
            model.addAttribute("errorMsg", "Las contraseñas no coinciden.");
            return "registro";
        }

        if (usuarioRepository.findByUsername(username).isPresent()) {
            model.addAttribute("errorMsg", "El nombre de usuario ya está en uso.");
            return "registro";
        }

        var clienteRole = rolRepository.findByNombre("CLIENTE")
                .orElseThrow(() -> new RuntimeException("Error fatal: Rol CLIENTE no encontrado"));

        var user = new com.sv.tienda.infrastructure.persistence.UsuarioEntity();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRol(clienteRole);
        user.setEstado(true);

        usuarioRepository.save(user);

        return "redirect:/login?success=registrado";
    }

    /**
     * Dashboard global — 1 sola plantilla, contenido adaptado por rol.
     */
    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        setDashboardMetadata(auth, model, "general");
        
        // Inyectar KPIs reales para el Dashboard
        model.addAttribute("totalUsuarios", usuarioRepository.count());
        model.addAttribute("totalProductos", productoRepository.count());
        model.addAttribute("totalCategorias", categoriaRepository.count());
        
        var pedidos = pedidoRepository.findAll();
        // Ingresos totales del sistema
        double ingresos = pedidos.stream().mapToDouble(p -> p.getTotal().doubleValue()).sum();
        model.addAttribute("ingresosTotales", String.format("%.2f", ingresos));
        model.addAttribute("totalVentas", pedidos.size());
        
        // Últimos 5 pedidos para la tabla de actividad
        var recientes = pedidos.stream()
                .sorted((a, b) -> b.getId().compareTo(a.getId()))
                .limit(5)
                .toList();
        model.addAttribute("pedidosRecientes", recientes);
        
        return "dashboard";
    }

    @GetMapping("/dashboard/usuarios")
    public String listaUsuarios(Authentication auth, Model model) {
        setDashboardMetadata(auth, model, "usuarios");
        model.addAttribute("usuarios", usuarioRepository.findAll());
        return "dashboard";
    }

    @GetMapping("/dashboard/productos")
    public String listaProductos(Authentication auth, Model model) {
        setDashboardMetadata(auth, model, "productos");
        model.addAttribute("productos", productoRepository.findAll());
        return "dashboard";
    }

    @GetMapping("/dashboard/categorias")
    public String listaCategorias(Authentication auth, Model model) {
        setDashboardMetadata(auth, model, "categorias");
        model.addAttribute("categorias", categoriaRepository.findAll());
        return "dashboard";
    }

    @GetMapping("/dashboard/finanzas")
    public String finanzas(Authentication auth, Model model) {
        setDashboardMetadata(auth, model, "finanzas");
        return "dashboard";
    }

    @GetMapping("/dashboard/reportes")
    public String reportes(Authentication auth, Model model) {
        setDashboardMetadata(auth, model, "reportes");
        return "dashboard";
    }

    @GetMapping("/dashboard/config")
    public String configuracion(Authentication auth, Model model) {
        setDashboardMetadata(auth, model, "config");
        return "dashboard";
    }

    @GetMapping("/dashboard/config/pagos")
    public String configPagos(Authentication auth, Model model) {
        setDashboardMetadata(auth, model, "config-pagos");
        return "dashboard";
    }

    @GetMapping("/dashboard/pedidos")
    public String misPedidos(Authentication auth, Model model) {
        setDashboardMetadata(auth, model, "pedidos");
        
        java.time.LocalDateTime start = java.time.LocalDate.now().atStartOfDay();
        java.time.LocalDateTime end   = java.time.LocalDate.now().atTime(23, 59, 59, 999999999);

        if (auth != null) {
            String rol = auth.getAuthorities().iterator().next().getAuthority();
            if (rol.equals("ROLE_CAJERO")) {
                // Solo ventas de HOY para el cuadre
                model.addAttribute("pedidos", pedidoRepository.findByFechaBetweenOrderByFechaDesc(start, end));
                model.addAttribute("esVentaDiaria", true);
                model.addAttribute("fechaHoy", java.time.LocalDate.now());
            } else if (rol.equals("ROLE_ADMIN")) {
                // Admin ve TODO para el historial general
                model.addAttribute("pedidos", pedidoRepository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "fecha")));
                model.addAttribute("esVentaDiaria", false);
            } else {
                usuarioRepository.findByUsername(auth.getName()).ifPresent(u -> {
                    model.addAttribute("pedidos", pedidoRepository.findByUsuarioOrderByFechaDesc(u));
                });
            }
        }
        
        return "dashboard";
    }

    @GetMapping("/dashboard/favoritos")
    public String misFavoritos(Authentication auth, Model model) {
        setDashboardMetadata(auth, model, "favoritos");
        return "dashboard";
    }

    @GetMapping("/dashboard/perfil")
    public String miPerfil(Authentication auth, Model model) {
        setDashboardMetadata(auth, model, "perfil");
        if (auth != null) {
            usuarioRepository.findByUsername(auth.getName()).ifPresent(u -> model.addAttribute("user", u));
        }
        return "dashboard";
    }

    @org.springframework.web.bind.annotation.PostMapping("/dashboard/perfil/save")
    public String guardarPerfil(Authentication auth,
                                @RequestParam String nombre,
                                @RequestParam String apellido,
                                @RequestParam String telefono,
                                @RequestParam String direccion,
                                org.springframework.web.servlet.mvc.support.RedirectAttributes attrs) {
        if (auth != null) {
            usuarioRepository.findByUsername(auth.getName()).ifPresent(u -> {
                u.setNombre(nombre);
                u.setApellido(apellido);
                u.setTelefono(telefono);
                u.setDireccion(direccion);
                usuarioRepository.save(u);
                attrs.addFlashAttribute("successMsg", "¡Tu perfil ha sido actualizado con éxito!");
            });
        }
        return "redirect:/dashboard/perfil";
    }

    private void setDashboardMetadata(Authentication auth, Model model, String section) {
        if (auth != null) {
            model.addAttribute("username", auth.getName());
            String rol = auth.getAuthorities().iterator().next().getAuthority()
                             .replace("ROLE_", "");
            model.addAttribute("rol", rol);
        }
        model.addAttribute("section", section);
    }

    @GetMapping("/dashboard/usuarios/nuevo")
    public String nuevoUsuario(Authentication auth, Model model) {
        setDashboardMetadata(auth, model, "usuarios");
        model.addAttribute("roles", rolRepository.findAll());
        return "dashboard/form-usuario";
    }

    @GetMapping("/dashboard/productos/nuevo")
    public String nuevoProducto(Authentication auth, Model model) {
        setDashboardMetadata(auth, model, "productos");
        model.addAttribute("categorias", categoriaRepository.findAll());
        return "dashboard/form-producto";
    }

    @GetMapping("/dashboard/categorias/nueva")
    public String nuevaCategoria() {
        return "dashboard/form-categoria";
    }

    @GetMapping("/dashboard/usuarios/editar/{id}")
    public String editarUsuario(@org.springframework.web.bind.annotation.PathVariable Long id, Authentication auth, Model model) {
        setDashboardMetadata(auth, model, "usuarios");
        usuarioRepository.findById(id).ifPresent(u -> model.addAttribute("u", u));
        model.addAttribute("roles", rolRepository.findAll());
        return "dashboard/form-usuario";
    }

    @GetMapping("/dashboard/productos/editar/{id}")
    public String editarProducto(@org.springframework.web.bind.annotation.PathVariable Long id, Authentication auth, Model model) {
        setDashboardMetadata(auth, model, "productos");
        productoRepository.findById(id).ifPresent(p -> model.addAttribute("p", p));
        model.addAttribute("categorias", categoriaRepository.findAll());
        return "dashboard/form-producto";
    }

    @GetMapping("/dashboard/categorias/editar/{id}")
    public String editarCategoria(@org.springframework.web.bind.annotation.PathVariable Long id, Authentication auth, Model model) {
        setDashboardMetadata(auth, model, "categorias");
        categoriaRepository.findById(id).ifPresent(c -> model.addAttribute("c", c));
        return "dashboard/form-categoria";
    }
}



