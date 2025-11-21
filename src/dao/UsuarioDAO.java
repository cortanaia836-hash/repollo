package dao;

import config.ConexionDB;
import modelo.Usuario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    // Registrar nuevo usuario
    public boolean registrarUsuario(Usuario usuario) {
        String sql = "INSERT INTO usuarios (nombre, dni, telefono, genero, email, contrasena) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, usuario.getNombre());
            pstmt.setString(2, usuario.getDni());
            pstmt.setString(3, usuario.getTelefono());
            pstmt.setString(4, usuario.getGenero());
            pstmt.setString(5, usuario.getEmail());
            pstmt.setString(6, usuario.getContrasena());

            int filasAfectadas = pstmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al registrar usuario: " + e.getMessage());
            return false;
        }
    }

    // Validar login
    public Usuario validarLogin(String email, String contrasena) {
        String sql = "SELECT * FROM usuarios WHERE email = ? AND contrasena = ?";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            pstmt.setString(2, contrasena);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Usuario usuario = new Usuario();
                usuario.setIdUsuario(rs.getInt("id_usuario"));
                usuario.setNombre(rs.getString("nombre"));
                usuario.setDni(rs.getString("dni"));
                usuario.setTelefono(rs.getString("telefono"));
                usuario.setGenero(rs.getString("genero"));
                usuario.setEmail(rs.getString("email"));

                // Convertir fecha_registro de TEXT a Timestamp
                String fechaRegStr = rs.getString("fecha_registro");
                if (fechaRegStr != null && !fechaRegStr.isEmpty()) {
                    try {
                        usuario.setFechaRegistro(Timestamp.valueOf(fechaRegStr));
                    } catch (IllegalArgumentException e) {
                        usuario.setFechaRegistro(null);
                    }
                }

                return usuario;
            }

        } catch (SQLException e) {
            System.err.println("Error al validar login: " + e.getMessage());
        }

        return null;
    }

    // Verificar si email ya existe
    public boolean existeEmail(String email) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE email = ?";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error al verificar email: " + e.getMessage());
        }

        return false;
    }

    // Verificar si nombre ya existe
    public boolean existeNombre(String nombre) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE nombre = ?";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nombre);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error al verificar nombre: " + e.getMessage());
        }

        return false;
    }

    // Listar todos los usuarios
    public List<Usuario> listarUsuarios() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM usuarios ORDER BY nombre";

        try (Connection conn = ConexionDB.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Usuario usuario = new Usuario();
                usuario.setIdUsuario(rs.getInt("id_usuario"));
                usuario.setNombre(rs.getString("nombre"));
                usuario.setDni(rs.getString("dni"));
                usuario.setTelefono(rs.getString("telefono"));
                usuario.setGenero(rs.getString("genero"));
                usuario.setEmail(rs.getString("email"));

                // Convertir fecha_registro de TEXT a Timestamp
                String fechaRegStr = rs.getString("fecha_registro");
                if (fechaRegStr != null && !fechaRegStr.isEmpty()) {
                    try {
                        usuario.setFechaRegistro(Timestamp.valueOf(fechaRegStr));
                    } catch (IllegalArgumentException e) {
                        usuario.setFechaRegistro(null);
                    }
                }

                usuarios.add(usuario);
            }

        } catch (SQLException e) {
            System.err.println("Error al listar usuarios: " + e.getMessage());
        }

        return usuarios;
    }
}