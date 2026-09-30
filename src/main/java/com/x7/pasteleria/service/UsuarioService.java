package com.x7.pasteleria.service;

import com.x7.pasteleria.dto.PerfilForm;
import com.x7.pasteleria.dto.RegistroForm;
import com.x7.pasteleria.model.Rol;
import com.x7.pasteleria.model.Usuario;
import com.x7.pasteleria.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> listar() {
        return usuarioRepository.findAllByOrderByNombreAsc();
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmailIgnoreCase(normalizar(email));
    }

    public boolean emailRegistrado(String email) {
        return usuarioRepository.existsByEmailIgnoreCase(normalizar(email));
    }

    public boolean emailOcupadoPorOtro(String email, Long idActual) {
        return usuarioRepository.findByEmailIgnoreCase(normalizar(email))
                .filter(u -> !u.getId().equals(idActual))
                .isPresent();
    }

    public Optional<Usuario> autenticar(String email, String password) {
        return buscarPorEmail(email)
                .filter(u -> passwordEncoder.matches(password, u.getPassword()));
    }

    @Transactional
    public Usuario registrarCliente(RegistroForm form) {
        Usuario usuario = new Usuario(
                form.getNombre().trim(),
                normalizar(form.getEmail()),
                form.getTelefono().trim(),
                form.getDireccion().trim(),
                passwordEncoder.encode(form.getPassword()),
                Rol.CLIENTE
        );
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario actualizarPerfil(Usuario usuario, PerfilForm form) {
        usuario.setNombre(form.getNombre().trim());
        usuario.setEmail(normalizar(form.getEmail()));
        usuario.setTelefono(form.getTelefono().trim());
        usuario.setDireccion(form.getDireccion().trim());
        if (form.cambiaPassword()) {
            usuario.setPassword(passwordEncoder.encode(form.getPasswordNuevo()));
        }
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void eliminar(Long id) {
        usuarioRepository.deleteById(id);
    }

    public long totalClientes() {
        return usuarioRepository.countByRol(Rol.CLIENTE);
    }

    private String normalizar(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
