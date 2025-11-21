package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class ConexionDB {
    // Ruta relativa al archivo SQLite en la raíz del proyecto
    private static final String URL = "jdbc:sqlite:src/main/resources/tecnomaxdb.sqlite
";

    private static Connection conexion = null;

    public static Connection getConexion() {
        try {
            if (conexion == null || conexion.isClosed()) {
                // Cargar el driver de SQLite
                Class.forName("org.sqlite.JDBC");

                // Establecer conexión (SQLite no requiere usuario ni contraseña)
                conexion = DriverManager.getConnection(URL);

                // Habilitar claves foráneas (importante para SQLite)
                conexion.createStatement().execute("PRAGMA foreign_keys = ON;");

                System.out.println("Conexión exitosa a la base de datos SQLite");
            }
            return conexion;
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null,
                    "Error: Driver SQLite no encontrado\n" + e.getMessage(),
                    "Error de Driver",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Error al conectar con la base de datos\n" + e.getMessage(),
                    "Error de Conexión",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    public static void cerrarConexion() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
                System.out.println("Conexión cerrada");
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar conexión: " + e.getMessage());
        }
    }

    public static boolean probarConexion() {
        Connection conn = getConexion();
        return conn != null;
    }
}