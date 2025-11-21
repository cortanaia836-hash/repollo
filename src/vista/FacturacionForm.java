package vista;

import dao.ClienteDAO;
import dao.ProductoDAO;
import dao.FacturaDAO;
import modelo.Usuario;
import modelo.Cliente;
import modelo.Producto;
import modelo.Factura;
import modelo.DetalleFactura;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FacturacionForm extends JFrame {
    private Usuario usuarioActual;
    private ClienteDAO clienteDAO;
    private ProductoDAO productoDAO;
    private FacturaDAO facturaDAO;

    // Componentes de UI
    private JLabel lblNumeroFactura, lblCliente, lblSubtotal, lblImpuesto, lblTotal;
    private JComboBox<Cliente> cmbClientes;
    private JComboBox<Producto> cmbProductos;
    private JTextField txtCantidad;
    private JButton btnAgregarProducto, btnQuitarProducto, btnGenerarFactura, btnNuevaFactura;

    private JTable tablaDetalles;
    private DefaultTableModel modeloDetalles;

    private List<DetalleFactura> listaDetalles;
    private double subtotal = 0;
    private double impuesto = 0;
    private double total = 0;

    public FacturacionForm(Usuario usuario) {
        this.usuarioActual = usuario;
        clienteDAO = new ClienteDAO();
        productoDAO = new ProductoDAO();
        facturaDAO = new FacturaDAO();
        listaDetalles = new ArrayList<>();

        inicializarComponentes();
        cargarClientes();
        cargarProductos();
        generarNumeroFactura();
    }

    private void inicializarComponentes() {
        setTitle("Sistema de Facturación");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel superior - Información de la factura
        JPanel panelSuperior = crearPanelSuperior();

        // Panel central - Tabla de detalles
        JPanel panelCentral = crearPanelCentral();

        // Panel inferior - Totales y botones
        JPanel panelInferior = crearPanelInferior();

        add(panelSuperior, BorderLayout.NORTH);
        add(panelCentral, BorderLayout.CENTER);
        add(panelInferior, BorderLayout.SOUTH);
    }

    private JPanel crearPanelSuperior() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Datos de la Factura"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Número de factura
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Número Factura:"), gbc);
        gbc.gridx = 1;
        lblNumeroFactura = new JLabel();
        lblNumeroFactura.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(lblNumeroFactura, gbc);

        // Usuario
        gbc.gridx = 2;
        panel.add(new JLabel("Usuario:"), gbc);
        gbc.gridx = 3;
        JLabel lblUsuario = new JLabel(usuarioActual.getNombre());
        lblUsuario.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(lblUsuario, gbc);

        // Cliente
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Cliente:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        cmbClientes = new JComboBox<>();
        panel.add(cmbClientes, gbc);

        gbc.gridwidth = 1;

        // Selección de producto
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Producto:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        cmbProductos = new JComboBox<>();
        cmbProductos.addActionListener(e -> mostrarStockDisponible());
        panel.add(cmbProductos, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 3;
        JLabel lblStock = new JLabel("Stock: ");
        lblStock.setForeground(Color.BLUE);
        lblStock.setName("lblStock");
        panel.add(lblStock, gbc);

        // Cantidad
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Cantidad:"), gbc);
        gbc.gridx = 1;
        txtCantidad = new JTextField(10);
        panel.add(txtCantidad, gbc);

        gbc.gridx = 2;
        btnAgregarProducto = new JButton("Agregar Producto");
        btnAgregarProducto.addActionListener(e -> agregarProducto());
        panel.add(btnAgregarProducto, gbc);

        return panel;
    }

    private JPanel crearPanelCentral() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Detalles de la Factura"));

        String[] columnas = {"Código", "Producto", "Cantidad", "Precio Unit.", "Subtotal"};
        modeloDetalles = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaDetalles = new JTable(modeloDetalles);
        JScrollPane scroll = new JScrollPane(tablaDetalles);

        btnQuitarProducto = new JButton("Quitar Producto Seleccionado");
        btnQuitarProducto.addActionListener(e -> quitarProducto());

        panel.add(scroll, BorderLayout.CENTER);
        panel.add(btnQuitarProducto, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel crearPanelInferior() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel de totales
        JPanel panelTotales = new JPanel(new GridLayout(3, 2, 5, 5));
        panelTotales.setBorder(BorderFactory.createTitledBorder("Totales"));

        panelTotales.add(new JLabel("Subtotal:"));
        lblSubtotal = new JLabel("L. 0.00");
        lblSubtotal.setFont(new Font("Arial", Font.BOLD, 14));
        panelTotales.add(lblSubtotal);

        panelTotales.add(new JLabel("Impuesto (15%):"));
        lblImpuesto = new JLabel("L. 0.00");
        lblImpuesto.setFont(new Font("Arial", Font.BOLD, 14));
        panelTotales.add(lblImpuesto);

        panelTotales.add(new JLabel("TOTAL:"));
        lblTotal = new JLabel("L. 0.00");
        lblTotal.setFont(new Font("Arial", Font.BOLD, 16));
        lblTotal.setForeground(new Color(0, 150, 0));
        panelTotales.add(lblTotal);

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        btnGenerarFactura = new JButton("Generar Factura");
        btnGenerarFactura.setFont(new Font("Arial", Font.BOLD, 14));
        btnGenerarFactura.setBackground(new Color(76, 175, 80));
        btnGenerarFactura.setForeground(Color.WHITE);
        btnGenerarFactura.setFocusPainted(false);
        btnGenerarFactura.addActionListener(e -> generarFactura());

        btnNuevaFactura = new JButton("Nueva Factura");
        btnNuevaFactura.addActionListener(e -> nuevaFactura());

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dispose());

        panelBotones.add(btnGenerarFactura);
        panelBotones.add(btnNuevaFactura);
        panelBotones.add(btnCerrar);

        panel.add(panelTotales, BorderLayout.WEST);
        panel.add(panelBotones, BorderLayout.EAST);

        return panel;
    }

    private void cargarClientes() {
        cmbClientes.removeAllItems();
        List<Cliente> clientes = clienteDAO.listarTodos();
        for (Cliente cliente : clientes) {
            cmbClientes.addItem(cliente);
        }
    }

    private void cargarProductos() {
        cmbProductos.removeAllItems();
        List<Producto> productos = productoDAO.listarTodos();
        for (Producto producto : productos) {
            cmbProductos.addItem(producto);
        }
    }

    private void generarNumeroFactura() {
        String numeroFactura = facturaDAO.generarNumeroFactura();
        lblNumeroFactura.setText(numeroFactura);
    }

    private void mostrarStockDisponible() {
        Producto productoSeleccionado = (Producto) cmbProductos.getSelectedItem();
        if (productoSeleccionado != null) {
            // Buscar el label de stock
            Component[] componentes = ((JPanel) getContentPane().getComponent(0)).getComponents();
            for (Component comp : componentes) {
                if (comp instanceof JLabel && "lblStock".equals(comp.getName())) {
                    ((JLabel) comp).setText("Stock disponible: " + productoSeleccionado.getCantidadDisponible());
                    break;
                }
            }
        }
    }

    private void agregarProducto() {
        // Validar cliente seleccionado
        if (cmbClientes.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un cliente", "Cliente Requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validar producto seleccionado
        Producto productoSeleccionado = (Producto) cmbProductos.getSelectedItem();
        if (productoSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un producto", "Producto Requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validar cantidad
        String cantidadStr = txtCantidad.getText().trim();
        if (cantidadStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar una cantidad", "Cantidad Requerida", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int cantidad;
        try {
            cantidad = Integer.parseInt(cantidadStr);
            if (cantidad <= 0) {
                JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor a cero", "Cantidad Inválida", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Cantidad inválida", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Verificar si el producto ya está en la factura
        for (DetalleFactura detalle : listaDetalles) {
            if (detalle.getIdProducto() == productoSeleccionado.getIdProducto()) {
                JOptionPane.showMessageDialog(this, "El producto ya está en la factura.\nPuede quitarlo y agregarlo nuevamente con la cantidad correcta.",
                        "Producto Duplicado", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        // Verificar stock disponible
        if (cantidad > productoSeleccionado.getCantidadDisponible()) {
            JOptionPane.showMessageDialog(this,
                    "Stock insuficiente. Disponible: " + productoSeleccionado.getCantidadDisponible(),
                    "Stock Insuficiente", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Crear detalle y agregarlo
        DetalleFactura detalle = new DetalleFactura(
                productoSeleccionado.getIdProducto(),
                productoSeleccionado.getNombre(),
                cantidad,
                productoSeleccionado.getPrecioUnitario()
        );
        detalle.setCodigoProducto(productoSeleccionado.getCodigo());

        listaDetalles.add(detalle);

        // Agregar a la tabla
        Object[] fila = {
                productoSeleccionado.getCodigo(),
                productoSeleccionado.getNombre(),
                cantidad,
                String.format("L. %.2f", productoSeleccionado.getPrecioUnitario()),
                String.format("L. %.2f", detalle.getSubtotal())
        };
        modeloDetalles.addRow(fila);

        // Actualizar totales
        calcularTotales();

        // Limpiar cantidad
        txtCantidad.setText("");
    }

    private void quitarProducto() {
        int filaSeleccionada = tablaDetalles.getSelectedRow();

        if (filaSeleccionada >= 0) {
            listaDetalles.remove(filaSeleccionada);
            modeloDetalles.removeRow(filaSeleccionada);
            calcularTotales();
        } else {
            JOptionPane.showMessageDialog(this, "Seleccione un producto para quitar", "Selección Requerida", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void calcularTotales() {
        subtotal = 0;
        for (DetalleFactura detalle : listaDetalles) {
            subtotal += detalle.getSubtotal();
        }

        impuesto = subtotal * 0.15; // 15% de impuesto
        total = subtotal + impuesto;

        lblSubtotal.setText(String.format("L. %.2f", subtotal));
        lblImpuesto.setText(String.format("L. %.2f", impuesto));
        lblTotal.setText(String.format("L. %.2f", total));
    }

    private void generarFactura() {
        // Validar que haya un cliente seleccionado
        Cliente clienteSeleccionado = (Cliente) cmbClientes.getSelectedItem();
        if (clienteSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un cliente", "Cliente Requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validar que haya productos en la factura
        if (listaDetalles.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe agregar al menos un producto", "Factura Vacía", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Crear factura
        Factura factura = new Factura(
                lblNumeroFactura.getText(),
                clienteSeleccionado.getIdCliente(),
                usuarioActual.getIdUsuario()
        );

        factura.setSubtotal(subtotal);
        factura.setImpuesto(impuesto);
        factura.setTotal(total);
        factura.setDetalles(new ArrayList<>(listaDetalles));

        // Guardar en base de datos
        if (facturaDAO.crearFactura(factura)) {
            JOptionPane.showMessageDialog(this,
                    "Factura generada exitosamente\nNúmero: " + lblNumeroFactura.getText() +
                            "\nTotal: L. " + String.format("%.2f", total),
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);

            // Deshabilitar botones
            btnGenerarFactura.setEnabled(false);
            btnAgregarProducto.setEnabled(false);
            btnQuitarProducto.setEnabled(false);
            cmbClientes.setEnabled(false);
            cmbProductos.setEnabled(false);
            txtCantidad.setEnabled(false);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Error al generar factura. Intente nuevamente.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void nuevaFactura() {
        // Limpiar todo
        modeloDetalles.setRowCount(0);
        listaDetalles.clear();
        txtCantidad.setText("");

        subtotal = 0;
        impuesto = 0;
        total = 0;

        lblSubtotal.setText("L. 0.00");
        lblImpuesto.setText("L. 0.00");
        lblTotal.setText("L. 0.00");

        // Habilitar controles
        btnGenerarFactura.setEnabled(true);
        btnAgregarProducto.setEnabled(true);
        btnQuitarProducto.setEnabled(true);
        cmbClientes.setEnabled(true);
        cmbProductos.setEnabled(true);
        txtCantidad.setEnabled(true);

        // Generar nuevo número de factura
        generarNumeroFactura();

        // Recargar productos (para actualizar stock)
        cargarProductos();
    }
}