package vista;

import dao.FacturaDAO;
import modelo.Factura;
import modelo.DetalleFactura;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class ReportesForm extends JFrame {
    private FacturaDAO facturaDAO;

    // Componentes de UI
    private JTable tablaFacturas;
    private DefaultTableModel modeloFacturas;
    private JTable tablaDetalles;
    private DefaultTableModel modeloDetalles;
    private JButton btnVerDetalles, btnCerrar;
    private JLabel lblTotalFacturas, lblMontoTotal;

    private SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public ReportesForm() {
        facturaDAO = new FacturaDAO();
        inicializarComponentes();
        cargarFacturas();
    }

    private void inicializarComponentes() {
        setTitle("Reportes - Facturas");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel superior - Título y estadísticas
        JPanel panelSuperior = crearPanelSuperior();

        // Panel central - Tabla de facturas
        JPanel panelCentral = crearPanelCentral();

        // Panel inferior - Detalles y botones
        JPanel panelInferior = crearPanelInferior();

        add(panelSuperior, BorderLayout.NORTH);
        add(panelCentral, BorderLayout.CENTER);
        add(panelInferior, BorderLayout.SOUTH);
    }

    private JPanel crearPanelSuperior() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Título
        JLabel lblTitulo = new JLabel("Facturas Registradas");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));

        // Panel de estadísticas
        JPanel panelEstadisticas = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));

        lblTotalFacturas = new JLabel("Total Facturas: 0");
        lblTotalFacturas.setFont(new Font("Arial", Font.BOLD, 14));

        lblMontoTotal = new JLabel("Monto Total: L. 0.00");
        lblMontoTotal.setFont(new Font("Arial", Font.BOLD, 14));
        lblMontoTotal.setForeground(new Color(0, 150, 0));

        panelEstadisticas.add(lblTotalFacturas);
        panelEstadisticas.add(new JSeparator(SwingConstants.VERTICAL));
        panelEstadisticas.add(lblMontoTotal);

        panel.add(lblTitulo, BorderLayout.WEST);
        panel.add(panelEstadisticas, BorderLayout.EAST);

        return panel;
    }

    private JPanel crearPanelCentral() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Lista de Facturas"));

        // Tabla de facturas
        String[] columnas = {"N° Factura", "Cliente", "Usuario", "Fecha", "Subtotal", "Impuesto", "Total"};
        modeloFacturas = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaFacturas = new JTable(modeloFacturas);
        tablaFacturas.setFont(new Font("Arial", Font.PLAIN, 12));
        tablaFacturas.setRowHeight(25);
        tablaFacturas.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tablaFacturas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Ajustar ancho de columnas
        tablaFacturas.getColumnModel().getColumn(0).setPreferredWidth(100);
        tablaFacturas.getColumnModel().getColumn(1).setPreferredWidth(200);
        tablaFacturas.getColumnModel().getColumn(2).setPreferredWidth(150);
        tablaFacturas.getColumnModel().getColumn(3).setPreferredWidth(130);
        tablaFacturas.getColumnModel().getColumn(4).setPreferredWidth(80);
        tablaFacturas.getColumnModel().getColumn(5).setPreferredWidth(80);
        tablaFacturas.getColumnModel().getColumn(6).setPreferredWidth(80);

        JScrollPane scroll = new JScrollPane(tablaFacturas);
        panel.add(scroll, BorderLayout.CENTER);

        // Panel de botones para la tabla
        JPanel panelBotonesTabla = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        btnVerDetalles = new JButton("Ver Detalles");
        btnVerDetalles.setFont(new Font("Arial", Font.BOLD, 12));
        btnVerDetalles.setEnabled(false);
        btnVerDetalles.addActionListener(e -> verDetallesFactura());

        JButton btnActualizar = new JButton("Actualizar");
        btnActualizar.setFont(new Font("Arial", Font.PLAIN, 12));
        btnActualizar.addActionListener(e -> cargarFacturas());

        panelBotonesTabla.add(btnActualizar);
        panelBotonesTabla.add(btnVerDetalles);

        panel.add(panelBotonesTabla, BorderLayout.SOUTH);

        // Habilitar botón al seleccionar fila
        tablaFacturas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                btnVerDetalles.setEnabled(tablaFacturas.getSelectedRow() >= 0);
            }
        });

        return panel;
    }

    private JPanel crearPanelInferior() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel de detalles
        JPanel panelDetalles = new JPanel(new BorderLayout());
        panelDetalles.setBorder(BorderFactory.createTitledBorder("Detalles de la Factura Seleccionada"));
        panelDetalles.setPreferredSize(new Dimension(0, 200));

        String[] columnasDetalles = {"Código", "Producto", "Cantidad", "Precio Unitario", "Subtotal"};
        modeloDetalles = new DefaultTableModel(columnasDetalles, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaDetalles = new JTable(modeloDetalles);
        tablaDetalles.setFont(new Font("Arial", Font.PLAIN, 12));
        tablaDetalles.setRowHeight(25);
        tablaDetalles.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        JScrollPane scrollDetalles = new JScrollPane(tablaDetalles);
        panelDetalles.add(scrollDetalles, BorderLayout.CENTER);

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        btnCerrar = new JButton("Cerrar");
        btnCerrar.setFont(new Font("Arial", Font.PLAIN, 12));
        btnCerrar.addActionListener(e -> dispose());

        panelBotones.add(btnCerrar);

        panel.add(panelDetalles, BorderLayout.CENTER);
        panel.add(panelBotones, BorderLayout.SOUTH);

        return panel;
    }

    private void cargarFacturas() {
        modeloFacturas.setRowCount(0);
        modeloDetalles.setRowCount(0);

        List<Factura> facturas = facturaDAO.listarTodas();
        double montoTotal = 0;

        for (Factura factura : facturas) {
            String fechaStr = factura.getFechaFactura() != null ?
                    formatoFecha.format(factura.getFechaFactura()) : "N/A";

            Object[] fila = {
                    factura.getNumeroFactura(),
                    factura.getNombreCliente(),
                    factura.getNombreUsuario(),
                    fechaStr,
                    String.format("L. %.2f", factura.getSubtotal()),
                    String.format("L. %.2f", factura.getImpuesto()),
                    String.format("L. %.2f", factura.getTotal())
            };
            modeloFacturas.addRow(fila);
            montoTotal += factura.getTotal();
        }

        // Actualizar estadísticas
        lblTotalFacturas.setText("Total Facturas: " + facturas.size());
        lblMontoTotal.setText(String.format("Monto Total: L. %.2f", montoTotal));
    }

    private void verDetallesFactura() {
        int filaSeleccionada = tablaFacturas.getSelectedRow();

        if (filaSeleccionada < 0) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione una factura para ver sus detalles",
                    "Selección Requerida",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Limpiar tabla de detalles
        modeloDetalles.setRowCount(0);

        // Obtener número de factura
        String numeroFactura = (String) modeloFacturas.getValueAt(filaSeleccionada, 0);

        // Buscar la factura por número
        List<Factura> facturas = facturaDAO.listarTodas();
        Factura facturaSeleccionada = null;

        for (Factura f : facturas) {
            if (f.getNumeroFactura().equals(numeroFactura)) {
                facturaSeleccionada = facturaDAO.buscarPorId(f.getIdFactura());
                break;
            }
        }

        if (facturaSeleccionada != null && facturaSeleccionada.getDetalles() != null) {
            for (DetalleFactura detalle : facturaSeleccionada.getDetalles()) {
                Object[] fila = {
                        detalle.getCodigoProducto(),
                        detalle.getNombreProducto(),
                        detalle.getCantidad(),
                        String.format("L. %.2f", detalle.getPrecioUnitario()),
                        String.format("L. %.2f", detalle.getSubtotal())
                };
                modeloDetalles.addRow(fila);
            }

            // Mostrar información adicional
            JOptionPane.showMessageDialog(this,
                    "Factura: " + facturaSeleccionada.getNumeroFactura() + "\n" +
                            "Cliente: " + facturaSeleccionada.getNombreCliente() + "\n" +
                            "Usuario: " + facturaSeleccionada.getNombreUsuario() + "\n" +
                            "Fecha: " + formatoFecha.format(facturaSeleccionada.getFechaFactura()) + "\n\n" +
                            "Subtotal: L. " + String.format("%.2f", facturaSeleccionada.getSubtotal()) + "\n" +
                            "Impuesto (15%): L. " + String.format("%.2f", facturaSeleccionada.getImpuesto()) + "\n" +
                            "TOTAL: L. " + String.format("%.2f", facturaSeleccionada.getTotal()),
                    "Información de la Factura",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "No se pudieron cargar los detalles de la factura",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}