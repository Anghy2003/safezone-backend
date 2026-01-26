package com.ista.springboot.web.app.controllers;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.ista.springboot.web.app.config.FirebasePrincipal;
import com.ista.springboot.web.app.dto.UsuarioDTO;
import com.ista.springboot.web.app.models.entity.Usuario;
import com.ista.springboot.web.app.models.entity.UsuarioComunidad;
import com.ista.springboot.web.app.models.services.IUsuarioService;
import com.ista.springboot.web.app.models.services.MailService;
import com.ista.springboot.web.app.models.services.ResetTokenService;

@CrossOrigin(origins = { "http://localhost:4200", "*" })
@RestController
@RequestMapping("/api")
public class UsuarioRestController {

    @Autowired
    private IUsuarioService usuarioService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ✅ Password Reset (sin tablas nuevas)
    @Autowired
    private ResetTokenService resetTokenService;

    @Autowired
    private MailService mailService;

    // Ej: https://tu-frontend.com/reset-password?token=
    @Value("${app.reset.base-url}")
    private String resetBaseUrl;

    // ===================== VALIDACIONES =====================

    // Email "razonable" (no garantiza que exista, solo formato).
    private static final Pattern EMAIL_RX =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // Ecuador E.164:
    //  - Móvil: +5939XXXXXXXX
    //  - Fijo:  +593[2-7]XXXXXXX
    private static final Pattern EC_PHONE_E164_RX =
            Pattern.compile("^\\+593(?:9\\d{8}|[2-7]\\d{7})$");

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    /**
     * Normaliza teléfono Ecuador a E.164:
     *  - "09XXXXXXXX"     -> "+5939XXXXXXXX"
     *  - "0[2-7]XXXXXXX"  -> "+593[2-7]XXXXXXX"
     *  - "+5939XXXXXXXX"  -> "+5939XXXXXXXX"
     *  - "+593[2-7]XXXXXXX" -> "+593[2-7]XXXXXXX"
     * Soporta espacios/guiones/paréntesis.
     */
    private String normalizeTelefonoEcuador(String telefono) {
        if (telefono == null) return null;

        String t = telefono.trim();
        if (t.isEmpty()) return null;

        // deja solo + y dígitos
        t = t.replaceAll("[^0-9+]", "");

        // ya está en +593...
        if (t.startsWith("+593")) {
            return t;
        }

        // si viene sin +, solo dígitos
        String digits = t.replaceAll("\\D", "");

        // 09XXXXXXXX (10 dígitos)
        if (digits.matches("^09\\d{8}$")) {
            return "+593" + digits.substring(1); // quita 0
        }

        // 0[2-7]XXXXXXX (9 dígitos)
        if (digits.matches("^0[2-7]\\d{7}$")) {
            return "+593" + digits.substring(1); // quita 0
        }

        // casos inválidos
        return null;
    }

    private void validarRegistroLegal(Usuario u) {
        if (u == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Body inválido");

        String nombre = u.getNombre() != null ? u.getNombre().trim() : "";
        String apellido = u.getApellido() != null ? u.getApellido().trim() : "";
        String email = normalizeEmail(u.getEmail());
        String pass = u.getPasswordHash() != null ? u.getPasswordHash().trim() : "";
        String telNorm = normalizeTelefonoEcuador(u.getTelefono());

        if (nombre.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "nombre es obligatorio");
        if (apellido.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "apellido es obligatorio");

        if (email == null || email.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email es obligatorio");
        }
        if (!EMAIL_RX.matcher(email).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email inválido");
        }

        if (telNorm == null || !EC_PHONE_E164_RX.matcher(telNorm).matches()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "telefono inválido. Use móvil (09XXXXXXXX) o fijo (0[2-7]XXXXXXX) de Ecuador"
            );
        }

        if (pass.isEmpty() || pass.length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "password es obligatorio (mínimo 6 caracteres)");
        }

        // aplicar normalizaciones al objeto
        u.setNombre(nombre);
        u.setApellido(apellido);
        u.setEmail(email);
        u.setTelefono(telNorm);
    }

    // ===================== HELPERS DTO =====================

    private UsuarioComunidad elegirComunidadUsuario(Usuario usuario) {
        Set<UsuarioComunidad> comunidades = usuario.getUsuarioComunidades();
        if (comunidades == null || comunidades.isEmpty()) return null;

        if (comunidades.size() == 1) return comunidades.iterator().next();

        return comunidades.stream()
                .sorted(Comparator.comparing(
                        UsuarioComunidad::getFechaUnion,
                        Comparator.nullsFirst(Comparator.naturalOrder())
                ))
                .reduce((first, second) -> second)
                .orElse(null);
    }

    private UsuarioDTO toDto(Usuario usuario) {
        if (usuario == null) return null;
        UsuarioComunidad usuarioComunidad = elegirComunidadUsuario(usuario);
        return new UsuarioDTO(usuario, usuarioComunidad);
    }

    private FirebasePrincipal requireFirebasePrincipal(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado con Firebase");
        }
        Object principal = auth.getPrincipal();
        if (!(principal instanceof FirebasePrincipal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Principal inválido");
        }
        return (FirebasePrincipal) principal;
    }

    // ===================== ENDPOINTS =====================

    // protegido por SecurityConfig
    @GetMapping("/usuarios")
    public List<UsuarioDTO> index() {
        return usuarioService.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // protegido por SecurityConfig
    @GetMapping("/usuarios/{id}")
    public UsuarioDTO show(@PathVariable Long id) {
        Usuario usuario = usuarioService.findById(id);
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }
        return toDto(usuario);
    }

    // ✅ REGISTRO LEGAL (Supabase) - público
    @PostMapping("/usuarios")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioDTO create(@RequestBody Usuario usuario) {
        // 1) Validar y normalizar (Ecuador)
        validarRegistroLegal(usuario);

        // 2) Verificar duplicado por email
        Usuario existente = usuarioService.findByEmail(usuario.getEmail());
        if (existente != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese correo ya está registrado");
        }

        // 3) Encriptar password (OBLIGATORIO)
        usuario.setPasswordHash(passwordEncoder.encode(usuario.getPasswordHash().trim()));

        OffsetDateTime ahora = OffsetDateTime.now();
        usuario.setFechaRegistro(ahora);
        usuario.setUltimoAcceso(ahora);
        usuario.setActivo(true);

        Usuario guardado = usuarioService.save(usuario);
        return toDto(guardado);
    }

    // ✅ LOGIN tradicional email/password - público
    @PostMapping("/usuarios/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email = normalizeEmail(credentials.get("email"));
        String password = credentials.get("password");

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "email es obligatorio"));
        }
        if (password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "password es obligatorio"));
        }

        Usuario usuario = usuarioService.findByEmail(email);

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Usuario no encontrado"));
        }

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Usuario inactivo"));
        }

        if (usuario.getPasswordHash() == null || usuario.getPasswordHash().isBlank()
                || !passwordEncoder.matches(password, usuario.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Contraseña incorrecta"));
        }

        usuario.setUltimoAcceso(OffsetDateTime.now());
        usuario = usuarioService.save(usuario);

        return ResponseEntity.ok(toDto(usuario));
    }

    // ✅ GOOGLE LOGIN (Firebase) - protegido
    // Solo autentica (correo real por token) y verifica si está registrado.
    // NO crea usuario.
    @PostMapping("/usuarios/google-login")
    public ResponseEntity<?> loginGoogle(Authentication auth) {
        FirebasePrincipal p = requireFirebasePrincipal(auth);

        String email = normalizeEmail(p.email());
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "No se pudo obtener email desde Google/Firebase"));
        }

        Usuario usuario = usuarioService.findByEmail(email);

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "registered", false,
                            "email", email,
                            "message", "Correo verificado con Google, pero falta registro legal."
                    ));
        }

        // Opcional: completar nombre/foto si están vacíos
        if ((usuario.getNombre() == null || usuario.getNombre().isBlank())
                && p.name() != null && !p.name().isBlank()) {
            usuario.setNombre(p.name().trim());
        }
        if (p.picture() != null && !p.picture().isBlank()) {
            usuario.setFotoUrl(p.picture().trim());
        }

        usuario.setUltimoAcceso(OffsetDateTime.now());
        usuario = usuarioService.save(usuario);

        return ResponseEntity.ok(Map.of(
                "registered", true,
                "usuario", toDto(usuario)
        ));
    }

    // ===================== ✅ OLVIDÉ MI CONTRASEÑA (NUEVO) =====================

    // ✅ 1) Solicitar correo de recuperación (público)
    @PostMapping("/usuarios/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        String email = normalizeEmail(body.get("email"));

        if (email == null || email.isBlank() || !EMAIL_RX.matcher(email).matches()) {
            return ResponseEntity.badRequest().body(Map.of("message", "email inválido"));
        }

        // Respuesta ciega: NO revelar si existe o no
        Usuario usuario = usuarioService.findByEmail(email);

        if (usuario != null && Boolean.TRUE.equals(usuario.getActivo())) {
            String token = resetTokenService.createResetToken(email);
            String link = resetBaseUrl + token;

            try {
                mailService.sendResetLink(email, link);
            } catch (Exception ex) {
                // opcional: log interno
            }
        }

        return ResponseEntity.ok(Map.of(
                "message", "Si el correo existe, se enviará un enlace para restablecer la contraseña."
        ));
    }

    // ✅ 2) Cambiar contraseña con token (público)
    @PostMapping("/usuarios/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("newPassword");

        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "token es obligatorio"));
        }
        if (newPassword == null || newPassword.trim().length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("message", "password mínimo 6 caracteres"));
        }

        final String email;
        try {
            email = resetTokenService.validateAndGetEmail(token.trim());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Token inválido o expirado"));
        }

        Usuario usuario = usuarioService.findByEmail(email);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Token inválido o expirado"));
        }

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Usuario inactivo"));
        }

        usuario.setPasswordHash(passwordEncoder.encode(newPassword.trim()));
        usuario.setUltimoAcceso(OffsetDateTime.now());
        usuarioService.save(usuario);

        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada"));
    }

    // ===================== CRUD EXTRA =====================

    // Actualizar usuario - protegido
    @PutMapping("/usuarios/{id}")
    public UsuarioDTO update(@RequestBody Usuario usuario, @PathVariable Long id) {
        Usuario actual = usuarioService.findById(id);
        if (actual == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }

        usuario.setId(id);

        // Mantener password si no viene
        if (usuario.getPasswordHash() != null && !usuario.getPasswordHash().isBlank()) {
            if (usuario.getPasswordHash().trim().length() < 6) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "password mínimo 6 caracteres");
            }
            usuario.setPasswordHash(passwordEncoder.encode(usuario.getPasswordHash().trim()));
        } else {
            usuario.setPasswordHash(actual.getPasswordHash());
        }

        // Normalizar email si viene
        if (usuario.getEmail() != null && !usuario.getEmail().isBlank()) {
            String emailNorm = normalizeEmail(usuario.getEmail());
            if (!EMAIL_RX.matcher(emailNorm).matches()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email inválido");
            }
            Usuario otro = usuarioService.findByEmail(emailNorm);
            if (otro != null && !otro.getId().equals(id)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese correo ya está registrado");
            }
            usuario.setEmail(emailNorm);
        } else {
            usuario.setEmail(actual.getEmail());
        }

        // Normalizar teléfono si viene
        if (usuario.getTelefono() != null && !usuario.getTelefono().isBlank()) {
            String telNorm = normalizeTelefonoEcuador(usuario.getTelefono());
            if (telNorm == null || !EC_PHONE_E164_RX.matcher(telNorm).matches()) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "telefono inválido. Use móvil (09XXXXXXXX) o fijo (0[2-7]XXXXXXX) de Ecuador"
                );
            }
            usuario.setTelefono(telNorm);
        } else {
            usuario.setTelefono(actual.getTelefono());
        }

        if (usuario.getNombre() == null || usuario.getNombre().isBlank()) usuario.setNombre(actual.getNombre());
        if (usuario.getApellido() == null || usuario.getApellido().isBlank()) usuario.setApellido(actual.getApellido());
        if (usuario.getFotoUrl() == null) usuario.setFotoUrl(actual.getFotoUrl());

        if (usuario.getFechaRegistro() == null) usuario.setFechaRegistro(actual.getFechaRegistro());
        if (usuario.getUltimoAcceso() == null) usuario.setUltimoAcceso(actual.getUltimoAcceso());
        if (usuario.getActivo() == null) usuario.setActivo(actual.getActivo());

        Usuario guardado = usuarioService.save(usuario);
        return toDto(guardado);
    }

    // Desactivar usuario - protegido
    @PutMapping("/usuarios/{id}/desactivar")
    public UsuarioDTO desactivar(@PathVariable Long id) {
        Usuario usuario = usuarioService.findById(id);
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }
        usuario.setActivo(false);
        Usuario guardado = usuarioService.save(usuario);
        return toDto(guardado);
    }

    // Eliminar usuario - protegido
    @DeleteMapping("/usuarios/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        Usuario usuario = usuarioService.findById(id);
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }
        usuarioService.delete(id);
    }

    // ✅ Guardar FCM token + deviceInfo - protegido (requiere Firebase)
    @PutMapping("/usuarios/{id}/fcm-token")
    public UsuarioDTO actualizarToken(@PathVariable Long id, @RequestBody Map<String, String> body, Authentication auth) {
        FirebasePrincipal p = requireFirebasePrincipal(auth);

        Usuario u = usuarioService.findById(id);
        if (u == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }

        if (u.getEmail() == null || p.email() == null || !u.getEmail().equalsIgnoreCase(p.email())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado para modificar este usuario");
        }

        String token = body.get("token");
        String deviceInfo = body.get("deviceInfo");

        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "token es obligatorio");
        }

        u.setFcmToken(token.trim());
        if (deviceInfo != null && !deviceInfo.isBlank()) {
            u.setDeviceInfo(deviceInfo.trim());
        }

        Usuario guardado = usuarioService.save(u);
        return toDto(guardado);
    }

    @GetMapping("/usuarios/me")
    public UsuarioDTO me(Authentication auth) {
        FirebasePrincipal p = requireFirebasePrincipal(auth);

        Usuario usuario = usuarioService.findByEmail(p.email());
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "No estás registrado. Completa el registro legal.");
        }

        usuario.setUltimoAcceso(OffsetDateTime.now());
        usuario = usuarioService.save(usuario);

        return toDto(usuario);
    }
}
