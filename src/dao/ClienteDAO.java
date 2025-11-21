package dao;
import config.ConexionDB;
import modelo.Cliente;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    public boolean agregar(Cliente cliente) {
        // SQLite usa fecha_cumpleanios según el schema proporcionado
        String sql = "INSERT INTO clientes (nombre, rtn, telefono, email, fecha_cumpleanios) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, cliente.getNombre());
            pstmt.setString(2, cliente.getRtn());
            pstmt.setString(3, cliente.getTelefono());
            pstmt.setString(4, cliente.getEmail());
            pstmt.setString(5, cliente.getFechaNacimiento() != null ? cliente.getFechaNacimiento().toString() : null);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al agregar cliente: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizar(Cliente cliente) {
        String sql = "UPDATE clientes SET nombre=?, rtn=?, telefono=?, email=?, fecha_cumpleanios=? WHERE id_cliente=?";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, cliente.getNombre());
            pstmt.setString(2, cliente.getRtn());
            pstmt.setString(3, cliente.getTelefono());
            pstmt.setString(4, cliente.getEmail());
            pstmt.setString(5, cliente.getFechaNacimiento() != null ? cliente.getFechaNacimiento().toString() : null);
            pstmt.setInt(6, cliente.getIdCliente());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar cliente: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(int idCliente) {
        // Verificar si tiene facturas asociadas
        if (tieneFacturas(idCliente)) {
            return false;
        }

        String sql = "DELETE FROM clientes WHERE id_cliente=?";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idCliente);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar cliente: " + e.getMessage());
            return false;
        }
    }

    public Cliente buscarPorId(int idCliente) {
        String sql = "SELECT * FROM clientes WHERE id_cliente=?";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idCliente);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extraerCliente(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar cliente: " + e.getMessage());
        }
        return null;
    }

    public List<Cliente> buscarPorNombre(String nombre) {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT * FROM clientes WHERE nombre LIKE ? ORDER BY nombre";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + nombre + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                clientes.add(extraerCliente(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar clientes: " + e.getMessage());
        }
        return clientes;
    }

    public List<Cliente> listarTodos() {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT * FROM clientes ORDER BY nombre";

        try (Connection conn = ConexionDB.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                clientes.add(extraerCliente(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar clientes: " + e.getMessage());
        }
        return clientes;
    }

    public boolean existeNombre(String nombre) {
        String sql = "SELECT COUNT(*) FROM clientes WHERE nombre=?";

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

    public boolean existeRtn(String rtn) {
        String sql = "SELECT COUNT(*) FROM clientes WHERE rtn=?";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, rtn);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar RTN: " + e.getMessage());
        }
        return false;
    }

    private boolean tieneFacturas(int idCliente) {
        String sql = "SELECT COUNT(*) FROM facturas WHERE id_cliente=?";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idCliente);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar facturas: " + e.getMessage());
        }
        return false;
    }

    private Cliente extraerCliente(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(rs.getInt("id_cliente"));
        cliente.setNombre(rs.getString("nombre"));
        cliente.setRtn(rs.getString("rtn"));
        cliente.setTelefono(rs.getString("telefono"));
        cliente.setEmail(rs.getString("email"));

        // SQLite almacena fechas como TEXT, convertir a java.sql.Date
        String fechaStr = rs.getString("fecha_cumpleanios");
        if (fechaStr != null && !fechaStr.isEmpty()) {
            try {
                cliente.setFechaNacimiento(Date.valueOf(fechaStr));
            } catch (IllegalArgumentException e) {
                cliente.setFechaNacimiento(null);
            }
        }

        // SQLite almacena fecha_registro como TEXT con formato CURRENT_TIMESTAMP
        String fechaRegStr = rs.getString("fecha_registro");
        if (fechaRegStr != null && !fechaRegStr.isEmpty()) {
            try {
                cliente.setFechaRegistro(Timestamp.valueOf(fechaRegStr));
            } catch (IllegalArgumentException e) {
                cliente.setFechaRegistro(null);
            }
        }

        return cliente;
    }
}