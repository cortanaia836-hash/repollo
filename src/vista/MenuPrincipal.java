package vista;

import modelo.Usuario;
import javax.swing.*;
import java.awt.*;

public class MenuPrincipal extends JFrame {
    private Usuario usuarioActual;
    private JLabel lblUsuario;


    public MenuPrincipal(Usuario usuario) {
        this.usuarioActual = usuario;
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        setTitle("Sistema de Gestión Comercial");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Panel superior con info del usuario
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelSuperior.setBackground(new Color(63, 81, 181));

        lblUsuario = new JLabel("Usuario: " + usuarioActual.getNombre());
        lblUsuario.setFont(new Font("Arial", Font.BOLD, 16));
        lblUsuario.setForeground(Color.WHITE);

        JButton btnCerrarSesion = new JButton("Cerrar Sesión");
        btnCerrarSesion.addActionListener(e -> cerrarSesion());

        panelSuperior.add(lblUsuario, BorderLayout.WEST);
        panelSuperior.add(btnCerrarSesion, BorderLayout.EAST);

        // Panel central con los botones del menú
        JPanel panelCentral = new JPanel(new GridLayout(2, 2, 20, 20));
        panelCentral.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        // Botones del menú

        JButton btnClientes = crearBotonMenu("Gestión de Clientes", "icons/clientes.png");
        btnClientes.addActionListener(e -> abrirGestionClientes());

        JButton btnInventario = crearBotonMenu("Gestión de Inventario", "icons/inventario.png");
        btnInventario.addActionListener(e -> abrirGestionInventario());

        JButton btnFacturacion = crearBotonMenu("Facturación", "icons/factura.png");
        btnFacturacion.addActionListener(e -> abrirFacturacion());

        JButton btnReportes = crearBotonMenu("Reportes", "icons/reportes.png");
        btnReportes.addActionListener(e -> abrirReportes());

        panelCentral.add(btnClientes);
        panelCentral.add(btnInventario);
        panelCentral.add(btnFacturacion);
        panelCentral.add(btnReportes);

        // Agregar paneles al frame
        add(panelSuperior, BorderLayout.NORTH);
        add(panelCentral, BorderLayout.CENTER);
    }

    private JButton crearBotonMenu(String texto, String rutaIcono) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Arial", Font.BOLD, 16));
        boton.setPreferredSize(new Dimension(200, 100));
        boton.setFocusPainted(false);
        boton.setBackground(new Color(33, 150, 243));
        boton.setForeground(Color.WHITE);
        boton.setBorder(BorderFactory.createRaisedBevelBorder());

        // Si existe el icono, agregarlo
        try {
            ImageIcon icono = new ImageIcon(rutaIcono);
            boton.setIcon(icono);
            boton.setHorizontalTextPosition(SwingConstants.CENTER);
            boton.setVerticalTextPosition(SwingConstants.BOTTOM);
        } catch (Exception e) {
            // Si no existe el icono, continuar sin él
        }

        return boton;
    }

    private void abrirGestionClientes() {
        GestionClientesForm gestionClientes = new GestionClientesForm();
        gestionClientes.setVisible(true);
    }

    private void abrirGestionInventario() {
        GestionInventarioForm gestionInventario = new GestionInventarioForm();
        gestionInventario.setVisible(true);
    }

    private void abrirFacturacion() {
        FacturacionForm facturacion = new FacturacionForm(usuarioActual);
        facturacion.setVisible(true);
    }

    private void abrirReportes() {
        ReportesForm reportes = new ReportesForm();
        reportes.setVisible(true);
    }

    private void cerrarSesion() {
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Está seguro que desea cerrar sesión?",
                "Confirmar",
                JOptionPane.YES_NO_OPTION);

        if (respuesta == JOptionPane.YES_OPTION) {
            LoginForm login = new LoginForm();
            login.setVisible(true);
            this.dispose();
        }
    }
}