package config;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JOptionPane;

public class ConexionDB {
    // Ruta relativa al archivo SQLite en la raíz del proyecto
    private static final String URL = "jdbc:sqlite:tecnomaxdb.sqlite";
    private static Connection conexion = null;
    private static boolean dbInicializada = false;

    public static Connection getConexion() {
        try {
            if (conexion == null || conexion.isClosed()) {
                // Verificar si la base de datos existe
                File dbFile = new File("tecnomaxdb.sqlite");
                boolean esNueva = !dbFile.exists();

                // Cargar el driver de SQLite
                Class.forName("org.sqlite.JDBC");

                // Establecer conexión
                conexion = DriverManager.getConnection(URL);

                // Habilitar claves foráneas
                conexion.createStatement().execute("PRAGMA foreign_keys = ON;");

                System.out.println("Conexión exitosa a la base de datos SQLite");

                // Si es nueva O no se ha inicializado, inicializar
                if (esNueva || !dbInicializada) {
                    System.out.println("Inicializando base de datos...");
                    inicializarBaseDatos(conexion);
                    dbInicializada = true;
                }
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

    private static void inicializarBaseDatos(Connection conn) {
        try {
            Statement stmt = conn.createStatement();

            System.out.println("Creando tablas...");

            // Crear tabla usuarios
            stmt.execute("CREATE TABLE IF NOT EXISTS usuarios (" +
                    "id_usuario INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "nombre TEXT NOT NULL UNIQUE," +
                    "dni TEXT NOT NULL," +
                    "telefono TEXT," +
                    "genero TEXT NOT NULL CHECK(genero IN ('Masculino', 'Femenino', 'Otro'))," +
                    "email TEXT NOT NULL UNIQUE," +
                    "contrasena TEXT NOT NULL," +
                    "fecha_registro TEXT DEFAULT CURRENT_TIMESTAMP)");

            // Crear tabla clientes
            stmt.execute("CREATE TABLE IF NOT EXISTS clientes (" +
                    "id_cliente INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "nombre TEXT NOT NULL UNIQUE," +
                    "rtn TEXT NOT NULL UNIQUE," +
                    "telefono TEXT," +
                    "email TEXT," +
                    "fecha_cumpleanios TEXT," +
                    "fecha_registro TEXT DEFAULT CURRENT_TIMESTAMP)");

            // Crear tabla categorias
            stmt.execute("CREATE TABLE IF NOT EXISTS categorias (" +
                    "id_categoria INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "nombre TEXT NOT NULL UNIQUE," +
                    "descripcion TEXT)");

            // Crear tabla productos
            stmt.execute("CREATE TABLE IF NOT EXISTS productos (" +
                    "id_producto INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "codigo TEXT NOT NULL UNIQUE," +
                    "nombre TEXT NOT NULL UNIQUE," +
                    "descripcion TEXT," +
                    "precio_unitario REAL NOT NULL," +
                    "id_categoria INTEGER," +
                    "fecha_registro TEXT DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY(id_categoria) REFERENCES categorias(id_categoria))");

            // Crear tabla inventario
            stmt.execute("CREATE TABLE IF NOT EXISTS inventario (" +
                    "id_inventario INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "id_producto INTEGER NOT NULL UNIQUE," +
                    "cantidad_disponible INTEGER DEFAULT 0," +
                    "cantidad_minima INTEGER DEFAULT 10," +
                    "ultima_actualizacion TEXT DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY(id_producto) REFERENCES productos(id_producto))");

            // Crear tabla facturas
            stmt.execute("CREATE TABLE IF NOT EXISTS facturas (" +
                    "id_factura INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "numero_factura TEXT NOT NULL UNIQUE," +
                    "id_cliente INTEGER NOT NULL," +
                    "id_usuario INTEGER NOT NULL," +
                    "fecha_factura TEXT DEFAULT CURRENT_TIMESTAMP," +
                    "subtotal REAL NOT NULL," +
                    "impuesto REAL DEFAULT 0," +
                    "total REAL NOT NULL," +
                    "FOREIGN KEY(id_cliente) REFERENCES clientes(id_cliente)," +
                    "FOREIGN KEY(id_usuario) REFERENCES usuarios(id_usuario))");

            // Crear tabla detalle_factura
            stmt.execute("CREATE TABLE IF NOT EXISTS detalle_factura (" +
                    "id_detalle INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "id_factura INTEGER NOT NULL," +
                    "id_producto INTEGER NOT NULL," +
                    "cantidad INTEGER NOT NULL," +
                    "precio_unitario REAL NOT NULL," +
                    "subtotal REAL NOT NULL," +
                    "UNIQUE(id_factura, id_producto)," +
                    "FOREIGN KEY(id_producto) REFERENCES productos(id_producto)," +
                    "FOREIGN KEY(id_factura) REFERENCES facturas(id_factura) ON DELETE CASCADE)");

            System.out.println("Tablas creadas exitosamente");

            // Verificar si ya existen datos
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM categorias");
            int count = 0;
            if (rs.next()) {
                count = rs.getInt(1);
            }
            rs.close();

            // Solo insertar datos si no existen
            if (count == 0) {
                System.out.println("Insertando datos iniciales...");

                stmt.execute("INSERT INTO categorias (nombre, descripcion) VALUES " +
                        "('Electrónica', 'Productos electrónicos y tecnología')," +
                        "('Accesorios', 'Productos electrónicos complementarios')");

                stmt.execute("INSERT INTO productos (codigo, nombre, descripcion, precio_unitario, id_categoria) VALUES " +
                        "('PROD001', 'Laptop HP', 'Laptop HP Core i5 8GB RAM', 15000.0, 1)," +
                        "('PROD002', 'Mouse Logitech', 'Mouse inalámbrico', 350.0, 1)");

                stmt.execute("INSERT INTO inventario (id_producto, cantidad_disponible, cantidad_minima) VALUES " +
                        "(1, 25, 5)," +
                        "(2, 100, 20)");

                System.out.println("Datos iniciales insertados");
            }

            stmt.close();
            System.out.println("✓ Base de datos lista para usar");

        } catch (SQLException e) {
            System.err.println("✗ Error al inicializar la base de datos: " + e.getMessage());
            e.printStackTrace();
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