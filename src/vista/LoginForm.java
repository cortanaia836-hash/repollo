package vista;

import dao.UsuarioDAO;
import modelo.Usuario;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginForm extends JFrame {
    private JTextField txtEmail;
    private JPasswordField txtContrasena;
    private JButton btnLogin;
    private JButton btnRegistro;
    private UsuarioDAO usuarioDAO;

    public LoginForm() {
        usuarioDAO = new UsuarioDAO();
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        setTitle("Sistema de Gestión Comercial - Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Panel principal
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Título
        JLabel lblTitulo = new JLabel("Iniciar Sesión", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(lblTitulo, gbc);

        // Email
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        JLabel lblEmail = new JLabel("Email:");
        panel.add(lblEmail, gbc);

        gbc.gridx = 1;
        txtEmail = new JTextField(20);
        panel.add(txtEmail, gbc);

        // Contraseña
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel lblContrasena = new JLabel("Contraseña:");
        panel.add(lblContrasena, gbc);

        gbc.gridx = 1;
        txtContrasena = new JPasswordField(20);
        panel.add(txtContrasena, gbc);

        // Botón Login
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        btnLogin = new JButton("Iniciar Sesión");
        btnLogin.setPreferredSize(new Dimension(200, 35));
        btnLogin.addActionListener(this::realizarLogin);
        panel.add(btnLogin, gbc);

        // Botón Registro
        gbc.gridy = 4;
        btnRegistro = new JButton("Registrarse");
        btnRegistro.setPreferredSize(new Dimension(200, 35));
        btnRegistro.addActionListener(this::abrirRegistro);
        panel.add(btnRegistro, gbc);

        add(panel);
    }

    private void realizarLogin(ActionEvent e) {
        String email = txtEmail.getText().trim();
        String contrasena = new String(txtContrasena.getPassword());

        if (email.isEmpty() || contrasena.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Por favor complete todos los campos",
                    "Campos Vacíos",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Usuario usuario = usuarioDAO.validarLogin(email, contrasena);

        if (usuario != null) {
            JOptionPane.showMessageDialog(this,
                    "¡Bienvenido " + usuario.getNombre() + "!",
                    "Login Exitoso",
                    JOptionPane.INFORMATION_MESSAGE);

            // Abrir menú principal
            MenuPrincipal menu = new MenuPrincipal(usuario);
            menu.setVisible(true);
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Email o contraseña incorrectos",
                    "Error de Autenticación",
                    JOptionPane.ERROR_MESSAGE);
            txtContrasena.setText("");
        }
    }

    private void abrirRegistro(ActionEvent e) {
        RegistroForm registro = new RegistroForm(this);
        registro.setVisible(true);
        this.setVisible(false);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginForm login = new LoginForm();
            login.setVisible(true);
        });
    }
}