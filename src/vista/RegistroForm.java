package vista;

import dao.UsuarioDAO;
import modelo.Usuario;
import javax.swing.*;
import java.awt.*;

public class RegistroForm extends JFrame {
    private JTextField txtNombre, txtDni, txtTelefono, txtEmail;
    private JPasswordField txtContrasena, txtConfirmarContrasena;
    private JComboBox<String> cmbGenero;
    private JButton btnRegistrar, btnCancelar;
    private UsuarioDAO usuarioDAO;
    private LoginForm loginForm;

    public RegistroForm(LoginForm loginForm) {
        this.loginForm = loginForm;
        usuarioDAO = new UsuarioDAO();
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        setTitle("Registro de Usuario");
        setSize(450, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Título
        JLabel lblTitulo = new JLabel("Registro de Usuario", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(lblTitulo, gbc);

        gbc.gridwidth = 1;

        // Nombre
        gbc.gridy = 1;
        gbc.gridx = 0;
        panel.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1;
        txtNombre = new JTextField(20);
        panel.add(txtNombre, gbc);

        // DNI
        gbc.gridy = 2;
        gbc.gridx = 0;
        panel.add(new JLabel("DNI:"), gbc);
        gbc.gridx = 1;
        txtDni = new JTextField(20);
        panel.add(txtDni, gbc);

        // Teléfono
        gbc.gridy = 3;
        gbc.gridx = 0;
        panel.add(new JLabel("Teléfono:"), gbc);
        gbc.gridx = 1;
        txtTelefono = new JTextField(20);
        panel.add(txtTelefono, gbc);

        // Género
        gbc.gridy = 4;
        gbc.gridx = 0;
        panel.add(new JLabel("Género:"), gbc);
        gbc.gridx = 1;
        cmbGenero = new JComboBox<>(new String[]{"Masculino", "Femenino", "Otro"});
        panel.add(cmbGenero, gbc);

        // Email
        gbc.gridy = 5;
        gbc.gridx = 0;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        txtEmail = new JTextField(20);
        panel.add(txtEmail, gbc);

        // Contraseña
        gbc.gridy = 6;
        gbc.gridx = 0;
        panel.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1;
        txtContrasena = new JPasswordField(20);
        panel.add(txtContrasena, gbc);

        // Confirmar Contraseña
        gbc.gridy = 7;
        gbc.gridx = 0;
        panel.add(new JLabel("Confirmar:"), gbc);
        gbc.gridx = 1;
        txtConfirmarContrasena = new JPasswordField(20);
        panel.add(txtConfirmarContrasena, gbc);

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnRegistrar = new JButton("Registrar");
        btnRegistrar.addActionListener(e -> registrarUsuario());
        btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> cancelar());
        panelBotones.add(btnRegistrar);
        panelBotones.add(btnCancelar);

        gbc.gridy = 8;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        panel.add(panelBotones, gbc);

        add(panel);
    }

    private void registrarUsuario() {
        // Validar campos vacíos
        if (txtNombre.getText().trim().isEmpty() || txtDni.getText().trim().isEmpty() ||
                txtEmail.getText().trim().isEmpty() || txtContrasena.getPassword().length == 0) {
            JOptionPane.showMessageDialog(this,
                    "Por favor complete todos los campos obligatorios",
                    "Campos Vacíos",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nombre = txtNombre.getText().trim();
        String email = txtEmail.getText().trim();
        String contrasena = new String(txtContrasena.getPassword());
        String confirmar = new String(txtConfirmarContrasena.getPassword());

        // Validar que las contraseñas coincidan
        if (!contrasena.equals(confirmar)) {
            JOptionPane.showMessageDialog(this,
                    "Las contraseñas no coinciden",
                    "Error de Validación",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validar que el nombre no exista
        if (usuarioDAO.existeNombre(nombre)) {
            JOptionPane.showMessageDialog(this,
                    "El nombre de usuario ya está registrado",
                    "Nombre Duplicado",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validar que el email no exista
        if (usuarioDAO.existeEmail(email)) {
            JOptionPane.showMessageDialog(this,
                    "El email ya está registrado",
                    "Email Duplicado",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Crear usuario
        Usuario usuario = new Usuario(
                nombre,
                txtDni.getText().trim(),
                txtTelefono.getText().trim(),
                (String) cmbGenero.getSelectedItem(),
                email,
                contrasena
        );

        // Registrar en base de datos
        if (usuarioDAO.registrarUsuario(usuario)) {
            JOptionPane.showMessageDialog(this,
                    "Usuario registrado exitosamente",
                    "Registro Exitoso",
                    JOptionPane.INFORMATION_MESSAGE);
            cancelar();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Error al registrar usuario. Intente nuevamente.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelar() {
        loginForm.setVisible(true);
        this.dispose();
    }
}