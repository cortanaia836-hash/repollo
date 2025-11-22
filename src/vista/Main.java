package vista;

import config.ConexionDB;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Establecer Look and Feel del sistema
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("=================================");
        System.out.println("SISTEMA DE GESTIÓN COMERCIAL");
        System.out.println("=================================");

        // Inicializar la conexión a la base de datos ANTES de abrir cualquier ventana
        System.out.println("Inicializando sistema...");

        if (ConexionDB.probarConexion()) {
            System.out.println("✓ Sistema listo");
            System.out.println("=================================\n");

            // Abrir la ventana de login en el hilo de eventos de Swing
            SwingUtilities.invokeLater(() -> {
                LoginForm login = new LoginForm();
                login.setVisible(true);
            });
        } else {
            System.err.println("✗ Error: No se pudo inicializar la base de datos");
            JOptionPane.showMessageDialog(null,
                    "No se pudo inicializar la base de datos.\nVerifique que el archivo sqlite-jdbc JAR esté en el classpath.",
                    "Error Fatal",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
}