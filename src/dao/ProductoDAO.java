package dao;

import config.ConexionDB;
import modelo.Producto;
import modelo.Categoria;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    // Agregar producto con inventario inicial
    public boolean agregar(Producto producto, int cantidadInicial) {
        Connection conn = null;
        try {
            conn = ConexionDB.getConexion();
            conn.setAutoCommit(false);

            // Insertar producto
            String sqlProducto = "INSERT INTO productos (codigo, nombre, descripcion, precio_unitario, id_categoria) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmtProducto = conn.prepareStatement(sqlProducto);

            pstmtProducto.setString(1, producto.getCodigo());
            pstmtProducto.setString(2, producto.getNombre());
            pstmtProducto.setString(3, producto.getDescripcion());
            pstmtProducto.setDouble(4, producto.getPrecioUnitario());
            pstmtProducto.setInt(5, producto.getIdCategoria());

            int filasProducto = pstmtProducto.executeUpdate();

            if (filasProducto > 0) {
                // SQLite usa last_insert_rowid() para obtener el último ID insertado
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()");

                if (rs.next()) {
                    int idProducto = rs.getInt(1);

                    // Insertar en inventario
                    String sqlInventario = "INSERT INTO inventario (id_producto, cantidad_disponible) VALUES (?, ?)";
                    PreparedStatement pstmtInventario = conn.prepareStatement(sqlInventario);
                    pstmtInventario.setInt(1, idProducto);
                    pstmtInventario.setInt(2, cantidadInicial);
                    pstmtInventario.executeUpdate();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            System.err.println("Error al agregar producto: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean actualizar(Producto producto) {
        String sql = "UPDATE productos SET codigo=?, nombre=?, descripcion=?, precio_unitario=?, id_categoria=? WHERE id_producto=?";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, producto.getCodigo());
            pstmt.setString(2, producto.getNombre());
            pstmt.setString(3, producto.getDescripcion());
            pstmt.setDouble(4, producto.getPrecioUnitario());
            pstmt.setInt(5, producto.getIdCategoria());
            pstmt.setInt(6, producto.getIdProducto());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar producto: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(int idProducto) {
        if (tieneFacturas(idProducto)) {
            return false;
        }

        Connection conn = null;
        try {
            conn = ConexionDB.getConexion();
            conn.setAutoCommit(false);

            // Eliminar de inventario
            String sqlInventario = "DELETE FROM inventario WHERE id_producto=?";
            PreparedStatement pstmtInv = conn.prepareStatement(sqlInventario);
            pstmtInv.setInt(1, idProducto);
            pstmtInv.executeUpdate();

            // Eliminar producto
            String sqlProducto = "DELETE FROM productos WHERE id_producto=?";
            PreparedStatement pstmtProd = conn.prepareStatement(sqlProducto);
            pstmtProd.setInt(1, idProducto);
            pstmtProd.executeUpdate();

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            System.err.println("Error al eliminar producto: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<Producto> listarTodos() {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT p.*, c.nombre as nombre_categoria, i.cantidad_disponible " +
                "FROM productos p " +
                "LEFT JOIN categorias c ON p.id_categoria = c.id_categoria " +
                "LEFT JOIN inventario i ON p.id_producto = i.id_producto " +
                "ORDER BY p.nombre";

        try (Connection conn = ConexionDB.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                productos.add(extraerProducto(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar productos: " + e.getMessage());
        }
        return productos;
    }

    public boolean existeCodigo(String codigo) {
        String sql = "SELECT COUNT(*) FROM productos WHERE codigo=?";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, codigo);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar código: " + e.getMessage());
        }
        return false;
    }

    public boolean existeNombre(String nombre) {
        String sql = "SELECT COUNT(*) FROM productos WHERE nombre=?";

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

    public boolean actualizarInventario(int idProducto, int nuevaCantidad) {
        String sql = "UPDATE inventario SET cantidad_disponible=?, ultima_actualizacion=CURRENT_TIMESTAMP WHERE id_producto=?";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, nuevaCantidad);
            pstmt.setInt(2, idProducto);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar inventario: " + e.getMessage());
            return false;
        }
    }

    private boolean tieneFacturas(int idProducto) {
        String sql = "SELECT COUNT(*) FROM detalle_factura WHERE id_producto=?";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idProducto);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar facturas: " + e.getMessage());
        }
        return false;
    }

    private Producto extraerProducto(ResultSet rs) throws SQLException {
        Producto producto = new Producto();
        producto.setIdProducto(rs.getInt("id_producto"));
        producto.setCodigo(rs.getString("codigo"));
        producto.setNombre(rs.getString("nombre"));
        producto.setDescripcion(rs.getString("descripcion"));
        producto.setPrecioUnitario(rs.getDouble("precio_unitario"));
        producto.setIdCategoria(rs.getInt("id_categoria"));
        producto.setNombreCategoria(rs.getString("nombre_categoria"));
        producto.setCantidadDisponible(rs.getInt("cantidad_disponible"));

        // Convertir fecha_registro de TEXT a Timestamp
        String fechaRegStr = rs.getString("fecha_registro");
        if (fechaRegStr != null && !fechaRegStr.isEmpty()) {
            try {
                producto.setFechaRegistro(Timestamp.valueOf(fechaRegStr));
            } catch (IllegalArgumentException e) {
                producto.setFechaRegistro(null);
            }
        }

        return producto;
    }
}