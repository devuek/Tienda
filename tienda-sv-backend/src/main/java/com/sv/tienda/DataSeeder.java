package com.sv.tienda;

import com.sv.tienda.infrastructure.persistence.JpaUsuarioRepository;
import com.sv.tienda.infrastructure.persistence.UsuarioEntity;
import com.sv.tienda.infrastructure.persistence.JpaRolRepository;
import com.sv.tienda.infrastructure.persistence.RolEntity;
import com.sv.tienda.infrastructure.persistence.JpaProductoRepository;
import com.sv.tienda.infrastructure.persistence.ProductoEntity;
import com.sv.tienda.infrastructure.persistence.JpaCategoriaRepository;
import com.sv.tienda.infrastructure.persistence.CategoriaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final JpaUsuarioRepository usuarioRepository;
    private final JpaRolRepository rolRepository;
    private final JpaProductoRepository productoRepository;
    private final JpaCategoriaRepository categoriaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Asegurar que existan los roles
        RolEntity adminRole = ensureRole("ADMIN", "Administrador Total");
        RolEntity cajeroRole = ensureRole("CAJERO", "Operador de Caja");
        RolEntity clienteRole = ensureRole("CLIENTE", "Cliente Final");

        // Asegurar usuarios base
        ensureUser("admin", "admin@tienda.sv", "admin123", adminRole);
        ensureUser("cajero", "cajero@tienda.sv", "cajero123", cajeroRole);
        // Asegurar categorías semilla
        ensureCategory("Granos Básicos", "Maíz, frijol y harinas esenciales");
        ensureCategory("Nostálgicos", "Productos típicos salvadoreños para el mundo");
        ensureCategory("Gourmet", "Café de especialidad y productos premium");

        // Asegurar productos semilla
        ensureProduct("Café de Altura (500g)", "Gourmet", "Café gourmet de las montañas de Santa Ana", 12.50, 50);
        ensureProduct("Harina para Pupusas (1kg)", "Granos Básicos", "Harina de maíz premium para pupusas tradicionales", 2.25, 100);
        ensureProduct("Semita Pachuca", "Nostálgicos", "Tradicional semita salvadoreña rellena de piña", 4.50, 30);

        System.out.println("✅ SEMILLA DE DATOS COMPLETADA EXITOSAMENTE");
    }

    private void ensureCategory(String nombre, String desc) {
        if (categoriaRepository.findByNombre(nombre).isEmpty()) {
            CategoriaEntity c = new CategoriaEntity();
            c.setNombre(nombre);
            c.setDescripcion(desc);
            categoriaRepository.save(c);
            System.out.println("🏷️ Categoría creada: " + nombre);
        }
    }

    private void ensureProduct(String nombre, String catNombre, String desc, double precio, int stock) {
        if (productoRepository.findByNombre(nombre).isEmpty()) {
            ProductoEntity p = new ProductoEntity();
            p.setNombre(nombre);
            p.setDescripcion(desc);
            p.setPrecio(java.math.BigDecimal.valueOf(precio));
            p.setStock(stock);
            p.setActivo(true);
            
            // Asignar categoría
            categoriaRepository.findByNombre(catNombre).ifPresent(p::setCategoria);
            
            productoRepository.save(p);
            System.out.println("🛍️ Producto semilla creado: " + nombre + " [" + catNombre + "]");
        } else {
            // Actualizar categoría si ya existe pero no la tiene
            ProductoEntity p = productoRepository.findByNombre(nombre).get();
            if (p.getCategoria() == null) {
                categoriaRepository.findByNombre(catNombre).ifPresent(p::setCategoria);
                productoRepository.save(p);
            }
        }
    }

    private RolEntity ensureRole(String nombre, String desc) {
        return rolRepository.findByNombre(nombre).orElseGet(() -> {
            RolEntity rol = new RolEntity();
            rol.setNombre(nombre);
            rol.setDescripcion(desc);
            return rolRepository.save(rol);
        });
    }

    private void ensureUser(String username, String email, String pass, RolEntity role) {
        Optional<UsuarioEntity> existing = usuarioRepository.findByUsername(username);
        if (existing.isEmpty()) {
            UsuarioEntity user = new UsuarioEntity();
            user.setUsername(username);
            user.setEmail(email);
            user.setPasswordHash(passwordEncoder.encode(pass));
            user.setRol(role);
            user.setEstado(true);
            usuarioRepository.save(user);
            System.out.println("🔧 Usuario creado: " + username);
        } else {
             // Forzar actualización de contraseña
             UsuarioEntity user = existing.get();
             user.setPasswordHash(passwordEncoder.encode(pass));
             usuarioRepository.save(user);
             System.out.println("🔄 Password actualizado/verificado para: " + username);
        }
    }
}
