package vista;

import dao.ClienteDAO;
import modelo.Cliente;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

public class GestionClientesForm extends JFrame {
    private JTable tablaClientes;
    private DefaultTableModel modeloTabla;
    private JTextField txtNombre, txtRtn, txtTelefono, txtEmail, txtBuscar;
    private JTextField txtFechaNac;
    private JButton btnAgregar, btnActualizar, btnEliminar, btnLimpiar, btnBuscar;
    private ClienteDAO clienteDAO;
    private int idClienteSeleccionado = -1;

    public GestionClientesForm() {
        clienteDAO = new ClienteDAO();
        inicializarComponentes();
        cargarClientes();
    }

    private void inicializarComponentes() {
        setTitle("Gestión de Clientes");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel superior - Búsqueda
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBusqueda.setBorder(BorderFactory.createTitledBorder("Búsqueda"));
        panelBusqueda.add(new JLabel("Buscar por nombre:"));
        txtBuscar = new JTextField(20);
        btnBuscar = new JButton("Buscar");
        btnBuscar.addActionListener(e -> buscarClientes());
        panelBusqueda.add(txtBuscar);
        panelBusqueda.add(btnBuscar);

        JButton btnMostrarTodos = new JButton("Mostrar Todos");
        btnMostrarTodos.addActionListener(e -> cargarClientes());
        panelBusqueda.add(btnMostrarTodos);

        // Panel central - Tabla
        String[] columnas = {"ID", "Nombre", "RTN", "Teléfono", "Email", "Fecha Nac."};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaClientes = new JTable(modeloTabla);
        tablaClientes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarClienteSeleccionado();
            }
        });

        JScrollPane scrollPane = new JScrollPane(tablaClientes);

        // Panel inferior - Formulario
        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Datos del Cliente"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Nombre
        gbc.gridx = 0;
        gbc.gridy = 0;
        panelFormulario.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1;
        txtNombre = new JTextField(20);
        panelFormulario.add(txtNombre, gbc);

        // RTN
        gbc.gridx = 2;
        panelFormulario.add(new JLabel("RTN:"), gbc);
        gbc.gridx = 3;
        txtRtn = new JTextField(20);
        panelFormulario.add(txtRtn, gbc);

        // Teléfono
        gbc.gridx = 0;
        gbc.gridy = 1;
        panelFormulario.add(new JLabel("Teléfono:"), gbc);
        gbc.gridx = 1;
        txtTelefono = new JTextField(20);
        panelFormulario.add(txtTelefono, gbc);

        // Email
        gbc.gridx = 2;
        panelFormulario.add(new JLabel("Email:"), gbc);
        gbc.gridx = 3;
        txtEmail = new JTextField(20);
        panelFormulario.add(txtEmail, gbc);

        // Fecha Nacimiento
        gbc.gridx = 0;
        gbc.gridy = 2;
        panelFormulario.add(new JLabel("Fecha Nac. (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        txtFechaNac = new JTextField(20);
        panelFormulario.add(txtFechaNac, gbc);

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnAgregar = new JButton("Agregar");
        btnAgregar.addActionListener(e -> agregarCliente());
        btnActualizar = new JButton("Actualizar");
        btnActualizar.addActionListener(e -> actualizarCliente());
        btnActualizar.setEnabled(false);
        btnEliminar = new JButton("Eliminar");
        btnEliminar.addActionListener(e -> eliminarCliente());
        btnEliminar.setEnabled(false);
        btnLimpiar = new JButton("Limpiar");
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        panelBotones.add(btnAgregar);
        panelBotones.add(btnActualizar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnLimpiar);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 4;
        panelFormulario.add(panelBotones, gbc);

        // Agregar componentes al frame
        add(panelBusqueda, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(panelFormulario, BorderLayout.SOUTH);
    }

    private void cargarClientes() {
        modeloTabla.setRowCount(0);
        List<Cliente> clientes = clienteDAO.listarTodos();

        for (Cliente cliente : clientes) {
            Object[] fila = {
                    cliente.getIdCliente(),
                    cliente.getNombre(),
                    cliente.getRtn(),
                    cliente.getTelefono(),
                    cliente.getEmail(),
                    cliente.getFechaNacimiento()
            };
            modeloTabla.addRow(fila);
        }
    }

    private void buscarClientes() {
        String nombre = txtBuscar.getText().trim();
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Ingrese un nombre para buscar",
                    "Campo Vacío",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        modeloTabla.setRowCount(0);
        List<Cliente> clientes = clienteDAO.buscarPorNombre(nombre);

        for (Cliente cliente : clientes) {
            Object[] fila = {
                    cliente.getIdCliente(),
                    cliente.getNombre(),
                    cliente.getRtn(),
                    cliente.getTelefono(),
                    cliente.getEmail(),
                    cliente.getFechaNacimiento()
            };
            modeloTabla.addRow(fila);
        }

        if (clientes.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No se encontraron clientes con ese nombre",
                    "Sin Resultados",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void cargarClienteSeleccionado() {
        int filaSeleccionada = tablaClientes.getSelectedRow();

        if (filaSeleccionada >= 0) {
            idClienteSeleccionado = (int) modeloTabla.getValueAt(filaSeleccionada, 0);
            txtNombre.setText((String) modeloTabla.getValueAt(filaSeleccionada, 1));
            txtRtn.setText((String) modeloTabla.getValueAt(filaSeleccionada, 2));
            txtTelefono.setText((String) modeloTabla.getValueAt(filaSeleccionada, 3));
            txtEmail.setText((String) modeloTabla.getValueAt(filaSeleccionada, 4));

            Date fecha = (Date) modeloTabla.getValueAt(filaSeleccionada, 5);
            txtFechaNac.setText(fecha != null ? fecha.toString() : "");

            btnActualizar.setEnabled(true);
            btnEliminar.setEnabled(true);
            btnAgregar.setEnabled(false);
        }
    }

    private void agregarCliente() {
        if (!validarCampos()) return;

        String nombre = txtNombre.getText().trim();
        String rtn = txtRtn.getText().trim();

        if (clienteDAO.existeNombre(nombre)) {
            JOptionPane.showMessageDialog(this,
                    "Ya existe un cliente con ese nombre",
                    "Nombre Duplicado",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (clienteDAO.existeRtn(rtn)) {
            JOptionPane.showMessageDialog(this,
                    "Ya existe un cliente con ese RTN",
                    "RTN Duplicado",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Cliente cliente = new Cliente(
                    nombre,
                    rtn,
                    txtTelefono.getText().trim(),
                    txtEmail.getText().trim(),
                    Date.valueOf(txtFechaNac.getText().trim())
            );

            if (clienteDAO.agregar(cliente)) {
                JOptionPane.showMessageDialog(this,
                        "Cliente agregado exitosamente",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                limpiarFormulario();
                cargarClientes();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Error al agregar cliente",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this,
                    "Formato de fecha inválido. Use YYYY-MM-DD",
                    "Error de Fecha",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarCliente() {
        if (!validarCampos()) return;

        try {
            Cliente cliente = new Cliente();
            cliente.setIdCliente(idClienteSeleccionado);
            cliente.setNombre(txtNombre.getText().trim());
            cliente.setRtn(txtRtn.getText().trim());
            cliente.setTelefono(txtTelefono.getText().trim());
            cliente.setEmail(txtEmail.getText().trim());
            cliente.setFechaNacimiento(Date.valueOf(txtFechaNac.getText().trim()));

            if (clienteDAO.actualizar(cliente)) {
                JOptionPane.showMessageDialog(this,
                        "Cliente actualizado exitosamente",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                limpiarFormulario();
                cargarClientes();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Error al actualizar cliente",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this,
                    "Formato de fecha inválido. Use YYYY-MM-DD",
                    "Error de Fecha",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarCliente() {
        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de eliminar este cliente?",
                "Confirmar Eliminación",
                JOptionPane.YES_NO_OPTION);

        if (confirmacion == JOptionPane.YES_OPTION) {
            if (clienteDAO.eliminar(idClienteSeleccionado)) {
                JOptionPane.showMessageDialog(this,
                        "Cliente eliminado exitosamente",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                limpiarFormulario();
                cargarClientes();
            } else {
                JOptionPane.showMessageDialog(this,
                        "No se puede eliminar. El cliente tiene facturas asociadas.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean validarCampos() {
        if (txtNombre.getText().trim().isEmpty() || txtRtn.getText().trim().isEmpty() ||
                txtFechaNac.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Complete todos los campos obligatorios",
                    "Campos Vacíos",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void limpiarFormulario() {
        txtNombre.setText("");
        txtRtn.setText("");
        txtTelefono.setText("");
        txtEmail.setText("");
        txtFechaNac.setText("");
        idClienteSeleccionado = -1;
        btnAgregar.setEnabled(true);
        btnActualizar.setEnabled(false);
        btnEliminar.setEnabled(false);
        tablaClientes.clearSelection();
    }
}