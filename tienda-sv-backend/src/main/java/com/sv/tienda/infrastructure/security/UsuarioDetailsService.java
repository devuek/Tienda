package com.sv.tienda.infrastructure.security;

import com.sv.tienda.infrastructure.persistence.JpaUsuarioRepository;
import com.sv.tienda.infrastructure.persistence.UsuarioEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio que Spring Security usa para cargar el usuario desde la BD
 * al momento de autenticar con usuario/contraseña.
 */
@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {

    private final JpaUsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("🔍 Intentando cargar usuario: " + username);
        UsuarioEntity usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> {
                    System.out.println("❌ Usuario NO encontrado: " + username);
                    return new UsernameNotFoundException("Usuario no encontrado: " + username);
                });

        System.out.println("✅ Usuario encontrado: " + usuario.getUsername() + " con rol: " + usuario.getRol().getNombre());

        if (!usuario.isEstado()) {
            throw new UsernameNotFoundException("Cuenta desactivada: " + username);
        }

        // El prefijo ROLE_ es requerido por Spring Security para hasRole()
        String rol = "ROLE_" + usuario.getRol().getNombre();

        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPasswordHash())
                .authorities(List.of(new SimpleGrantedAuthority(rol)))
                .build();
    }
}
