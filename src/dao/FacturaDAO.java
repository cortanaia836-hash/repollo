package dao;

import config.ConexionDB;
import modelo.Factura;
import modelo.DetalleFactura;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FacturaDAO {

    // Crear factura completa (factura + detalles + actualizar inventario)
    public boolean crearFactura(Factura factura) {
        Connection conn = null;
        try {
            conn = ConexionDB.getConexion();
            conn.setAutoCommit(false);

            // 1. Insertar factura
            String sqlFactura = "INSERT INTO facturas (numero_factura, id_cliente, id_usuario, subtotal, impuesto, total) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmtFactura = conn.prepareStatement(sqlFactura);

            pstmtFactura.setString(1, factura.getNumeroFactura());
            pstmtFactura.setInt(2, factura.getIdCliente());
            pstmtFactura.setInt(3, factura.getIdUsuario());
            pstmtFactura.setDouble(4, factura.getSubtotal());
            pstmtFactura.setDouble(5, factura.getImpuesto());
            pstmtFactura.setDouble(6, factura.getTotal());

            int filasFactura = pstmtFactura.executeUpdate();

            if (filasFactura > 0) {
                // SQLite usa last_insert_rowid() para obtener el último ID insertado
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()");

                if (rs.next()) {
                    int idFactura = rs.getInt(1);

                    // 2. Insertar detalles de factura
                    String sqlDetalle = "INSERT INTO detalle_factura (id_factura, id_producto, cantidad, precio_unitario, subtotal) " +
                            "VALUES (?, ?, ?, ?, ?)";
                    PreparedStatement pstmtDetalle = conn.prepareStatement(sqlDetalle);

                    for (DetalleFactura detalle : factura.getDetalles()) {
                        pstmtDetalle.setInt(1, idFactura);
                        pstmtDetalle.setInt(2, detalle.getIdProducto());
                        pstmtDetalle.setInt(3, detalle.getCantidad());
                        pstmtDetalle.setDouble(4, detalle.getPrecioUnitario());
                        pstmtDetalle.setDouble(5, detalle.getSubtotal());
                        pstmtDetalle.executeUpdate();

                        // 3. Actualizar inventario
                        actualizarInventario(conn, detalle.getIdProducto(), detalle.getCantidad());
                    }
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
            System.err.println("Error al crear factura: " + e.getMessage());
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

    // Actualizar inventario (restar cantidad vendida)
    private void actualizarInventario(Connection conn, int idProducto, int cantidadVendida) throws SQLException {
        String sql = "UPDATE inventario SET cantidad_disponible = cantidad_disponible - ?, ultima_actualizacion = CURRENT_TIMESTAMP WHERE id_producto = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, cantidadVendida);
        pstmt.setInt(2, idProducto);
        pstmt.executeUpdate();
    }

    // Generar número de factura único
    public String generarNumeroFactura() {
        String sql = "SELECT MAX(id_factura) FROM facturas";

        try (Connection conn = ConexionDB.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                int ultimoId = rs.getInt(1);
                return String.format("FAC-%06d", ultimoId + 1);
            }
        } catch (SQLException e) {
            System.err.println("Error al generar número de factura: " + e.getMessage());
        }

        return "FAC-000001";
    }

    // Listar todas las facturas
    public List<Factura> listarTodas() {
        List<Factura> facturas = new ArrayList<>();
        String sql = "SELECT f.*, c.nombre as nombre_cliente, u.nombre as nombre_usuario " +
                "FROM facturas f " +
                "INNER JOIN clientes c ON f.id_cliente = c.id_cliente " +
                "INNER JOIN usuarios u ON f.id_usuario = u.id_usuario " +
                "ORDER BY f.fecha_factura DESC";

        try (Connection conn = ConexionDB.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Factura factura = new Factura();
                factura.setIdFactura(rs.getInt("id_factura"));
                factura.setNumeroFactura(rs.getString("numero_factura"));
                factura.setIdCliente(rs.getInt("id_cliente"));
                factura.setNombreCliente(rs.getString("nombre_cliente"));
                factura.setIdUsuario(rs.getInt("id_usuario"));
                factura.setNombreUsuario(rs.getString("nombre_usuario"));

                // Convertir fecha_factura de TEXT a Timestamp
                String fechaStr = rs.getString("fecha_factura");
                if (fechaStr != null && !fechaStr.isEmpty()) {
                    try {
                        factura.setFechaFactura(Timestamp.valueOf(fechaStr));
                    } catch (IllegalArgumentException e) {
                        factura.setFechaFactura(null);
                    }
                }

                factura.setSubtotal(rs.getDouble("subtotal"));
                factura.setImpuesto(rs.getDouble("impuesto"));
                factura.setTotal(rs.getDouble("total"));
                facturas.add(factura);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar facturas: " + e.getMessage());
        }

        return facturas;
    }

    // Buscar factura por ID con sus detalles
    public Factura buscarPorId(int idFactura) {
        String sql = "SELECT f.*, c.nombre as nombre_cliente, u.nombre as nombre_usuario " +
                "FROM facturas f " +
                "INNER JOIN clientes c ON f.id_cliente = c.id_cliente " +
                "INNER JOIN usuarios u ON f.id_usuario = u.id_usuario " +
                "WHERE f.id_factura = ?";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idFactura);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Factura factura = new Factura();
                factura.setIdFactura(rs.getInt("id_factura"));
                factura.setNumeroFactura(rs.getString("numero_factura"));
                factura.setIdCliente(rs.getInt("id_cliente"));
                factura.setNombreCliente(rs.getString("nombre_cliente"));
                factura.setIdUsuario(rs.getInt("id_usuario"));
                factura.setNombreUsuario(rs.getString("nombre_usuario"));

                // Convertir fecha_factura de TEXT a Timestamp
                String fechaStr = rs.getString("fecha_factura");
                if (fechaStr != null && !fechaStr.isEmpty()) {
                    try {
                        factura.setFechaFactura(Timestamp.valueOf(fechaStr));
                    } catch (IllegalArgumentException e) {
                        factura.setFechaFactura(null);
                    }
                }

                factura.setSubtotal(rs.getDouble("subtotal"));
                factura.setImpuesto(rs.getDouble("impuesto"));
                factura.setTotal(rs.getDouble("total"));

                // Cargar detalles
                factura.setDetalles(obtenerDetalles(conn, idFactura));

                return factura;
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar factura: " + e.getMessage());
        }

        return null;
    }

    // Obtener detalles de una factura
    private List<DetalleFactura> obtenerDetalles(Connection conn, int idFactura) throws SQLException {
        List<DetalleFactura> detalles = new ArrayList<>();
        String sql = "SELECT df.*, p.nombre as nombre_producto, p.codigo as codigo_producto " +
                "FROM detalle_factura df " +
                "INNER JOIN productos p ON df.id_producto = p.id_producto " +
                "WHERE df.id_factura = ?";

        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, idFactura);
        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {
            DetalleFactura detalle = new DetalleFactura();
            detalle.setIdDetalle(rs.getInt("id_detalle"));
            detalle.setIdFactura(rs.getInt("id_factura"));
            detalle.setIdProducto(rs.getInt("id_producto"));
            detalle.setNombreProducto(rs.getString("nombre_producto"));
            detalle.setCodigoProducto(rs.getString("codigo_producto"));
            detalle.setCantidad(rs.getInt("cantidad"));
            detalle.setPrecioUnitario(rs.getDouble("precio_unitario"));
            detalle.setSubtotal(rs.getDouble("subtotal"));
            detalles.add(detalle);
        }

        return detalles;
    }

    // Verificar disponibilidad de stock
    public boolean verificarStock(int idProducto, int cantidadRequerida) {
        String sql = "SELECT cantidad_disponible FROM inventario WHERE id_producto = ?";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idProducto);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int stockDisponible = rs.getInt("cantidad_disponible");
                return stockDisponible >= cantidadRequerida;
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar stock: " + e.getMessage());
        }

        return false;
    }
}