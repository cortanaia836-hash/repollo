package vista;

import java.sql.*;
import config.ConexionDB;

import dao.CategoriaDAO;
import dao.ProductoDAO;
import modelo.Categoria;
import modelo.Producto;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

public class GestionInventarioForm extends JFrame {
    private JTabbedPane tabbedPane;

    // ========== COMPONENTES PARA CATEGORÍAS ==========
    private JTable tablaCategorias;
    private DefaultTableModel modeloCategorias;
    private JTextField txtNombreCategoria, txtDescCategoria, txtBuscarCategoria;
    private JButton btnAgregarCategoria, btnActualizarCategoria, btnEliminarCategoria, btnLimpiarCategoria;
    private CategoriaDAO categoriaDAO;
    private int idCategoriaSeleccionada = -1;
    private TableRowSorter<DefaultTableModel> sorterCategorias;

    // ========== COMPONENTES PARA PRODUCTOS ==========
    private JTable tablaProductos;
    private DefaultTableModel modeloProductos;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtCodigo, txtNombreProducto, txtDescProducto, txtPrecio, txtStockInicial, txtBuscarProducto;
    private JComboBox<Categoria> cmbCategoria, cmbFiltroCat;
    private JButton btnAgregarProducto, btnActualizarProducto, btnEliminarProducto, btnLimpiarProducto;
    private JButton btnBuscar, btnMostrarTodos, btnAjustarStock;
    private ProductoDAO productoDAO;
    private int idProductoSeleccionado = -1;
    private JLabel lblStockActual, lblStockStatus;

    public GestionInventarioForm() {
        categoriaDAO = new CategoriaDAO();
        productoDAO = new ProductoDAO();
        inicializarComponentes();
        cargarCategorias();
        cargarProductos();
        cargarCategoriasCombo();
        txtCodigo.setText(generarCodigoAutomatico());

    }

    private String generarCodigoAutomatico() {
        String sql = "SELECT MAX(CAST(SUBSTR(codigo, 5) AS INTEGER)) FROM productos WHERE codigo LIKE 'PROD%'";

        try (Connection conn = ConexionDB.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                int ultimoNumero = rs.getInt(1);
                return String.format("PROD%03d", ultimoNumero + 1);
            }
        } catch (SQLException e) {
            System.err.println("Error al generar código: " + e.getMessage());
        }

        return "PROD001";
    }

    private void inicializarComponentes() {
        setTitle("Gestión de Inventario - TecnoMax");
        setSize(1400, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));

        // ========== PESTAÑA DE CATEGORÍAS ==========
        JPanel panelCategorias = crearPanelCategorias();
        tabbedPane.addTab("📁 Categorías", panelCategorias);

        // ========== PESTAÑA DE PRODUCTOS E INVENTARIO ==========
        JPanel panelProductos = crearPanelProductos();
        tabbedPane.addTab("📦 Productos e Inventario", panelProductos);

        add(tabbedPane, BorderLayout.CENTER);

        // Panel de información inferior
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelInferior.setBackground(new Color(240, 240, 240));
        panelInferior.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        JLabel lblInfo = new JLabel("💡 Sugerencia: Haga doble clic en una fila para editar rápidamente");
        lblInfo.setFont(new Font("Arial", Font.ITALIC, 11));
        panelInferior.add(lblInfo);
        add(panelInferior, BorderLayout.SOUTH);
    }



    // ========================================
    // PANEL DE CATEGORÍAS
    // ========================================
    private JPanel crearPanelCategorias() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setBackground(Color.WHITE);

        // ========== PANEL SUPERIOR: BÚSQUEDA ==========
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelBusqueda.setBackground(new Color(240, 248, 255));
        panelBusqueda.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                "🔍 Búsqueda de Categorías",
                0, 0, new Font("Arial", Font.BOLD, 13), new Color(70, 130, 180)
        ));

        panelBusqueda.add(new JLabel("Buscar:"));
        txtBuscarCategoria = new JTextField(25);
        txtBuscarCategoria.setFont(new Font("Arial", Font.PLAIN, 12));
        panelBusqueda.add(txtBuscarCategoria);

        JButton btnBuscarCat = new JButton("🔎 Buscar");
        estilizarBoton(btnBuscarCat, new Color(33, 150, 243));
        btnBuscarCat.addActionListener(e -> buscarCategorias());
        panelBusqueda.add(btnBuscarCat);

        JButton btnMostrarTodasCat = new JButton("📋 Mostrar Todas");
        estilizarBoton(btnMostrarTodasCat, new Color(76, 175, 80));
        btnMostrarTodasCat.addActionListener(e -> {
            txtBuscarCategoria.setText("");
            sorterCategorias.setRowFilter(null);
            cargarCategorias();
        });
        panelBusqueda.add(btnMostrarTodasCat);

        // ========== PANEL CENTRAL: TABLA ==========
        String[] columnasCat = {"ID", "Nombre", "Descripción"};
        modeloCategorias = new DefaultTableModel(columnasCat, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaCategorias = new JTable(modeloCategorias);
        tablaCategorias.setFont(new Font("Arial", Font.PLAIN, 12));
        tablaCategorias.setRowHeight(25);
        sorterCategorias = new TableRowSorter<>(modeloCategorias);
        tablaCategorias.setRowSorter(sorterCategorias);
        tablaCategorias.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tablaCategorias.getTableHeader().setBackground(new Color(70, 130, 180));
        tablaCategorias.getTableHeader().setForeground(Color.WHITE);
        tablaCategorias.setSelectionBackground(new Color(173, 216, 230));

        tablaCategorias.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarCategoriaSeleccionada();
            }
        });

        JScrollPane scrollCat = new JScrollPane(tablaCategorias);
        scrollCat.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        // ========== PANEL INFERIOR: FORMULARIO ==========
        JPanel panelFormCat = new JPanel(new GridBagLayout());
        panelFormCat.setBackground(Color.WHITE);
        panelFormCat.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                "📝 Datos de la Categoría",
                0, 0, new Font("Arial", Font.BOLD, 13), new Color(70, 130, 180)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);

        // Nombre
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        JLabel lblNombreCat = new JLabel("Nombre:");
        lblNombreCat.setFont(new Font("Arial", Font.BOLD, 12));
        panelFormCat.add(lblNombreCat, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        txtNombreCategoria = new JTextField(30);
        txtNombreCategoria.setFont(new Font("Arial", Font.PLAIN, 12));
        panelFormCat.add(txtNombreCategoria, gbc);

        // Descripción
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        JLabel lblDescCat = new JLabel("Descripción:");
        lblDescCat.setFont(new Font("Arial", Font.BOLD, 12));
        panelFormCat.add(lblDescCat, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        txtDescCategoria = new JTextField(30);
        txtDescCategoria.setFont(new Font("Arial", Font.PLAIN, 12));
        panelFormCat.add(txtDescCategoria, gbc);

        // Botones
        JPanel panelBotonesCat = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panelBotonesCat.setBackground(Color.WHITE);

        btnAgregarCategoria = new JButton("➕ Agregar");
        estilizarBoton(btnAgregarCategoria, new Color(76, 175, 80));
        btnAgregarCategoria.addActionListener(e -> agregarCategoria());

        btnActualizarCategoria = new JButton("✏️ Actualizar");
        estilizarBoton(btnActualizarCategoria, new Color(255, 152, 0));
        btnActualizarCategoria.addActionListener(e -> actualizarCategoria());
        btnActualizarCategoria.setEnabled(false);

        btnEliminarCategoria = new JButton("🗑️ Eliminar");
        estilizarBoton(btnEliminarCategoria, new Color(244, 67, 54));
        btnEliminarCategoria.addActionListener(e -> eliminarCategoria());
        btnEliminarCategoria.setEnabled(false);

        btnLimpiarCategoria = new JButton("🔄 Limpiar");
        estilizarBoton(btnLimpiarCategoria, new Color(158, 158, 158));
        btnLimpiarCategoria.addActionListener(e -> limpiarFormularioCategoria());

        panelBotonesCat.add(btnAgregarCategoria);
        panelBotonesCat.add(btnActualizarCategoria);
        panelBotonesCat.add(btnEliminarCategoria);
        panelBotonesCat.add(btnLimpiarCategoria);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        panelFormCat.add(panelBotonesCat, gbc);

        // ========== AGREGAR TODO AL PANEL PRINCIPAL ==========
        panel.add(panelBusqueda, BorderLayout.NORTH);
        panel.add(scrollCat, BorderLayout.CENTER);
        panel.add(panelFormCat, BorderLayout.SOUTH);

        return panel;
    }

    // ========================================
    // PANEL DE PRODUCTOS
    // ========================================
    private JPanel crearPanelProductos() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setBackground(Color.WHITE);

        // ========== PANEL SUPERIOR: BÚSQUEDA Y FILTROS ==========
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelBusqueda.setBackground(new Color(255, 248, 240));
        panelBusqueda.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(255, 140, 0), 2),
                "🔍 Búsqueda y Filtros",
                0, 0, new Font("Arial", Font.BOLD, 13), new Color(255, 140, 0)
        ));

        panelBusqueda.add(new JLabel("Buscar:"));
        txtBuscarProducto = new JTextField(20);
        txtBuscarProducto.setFont(new Font("Arial", Font.PLAIN, 12));
        panelBusqueda.add(txtBuscarProducto);

        btnBuscar = new JButton("🔎 Buscar");
        estilizarBoton(btnBuscar, new Color(33, 150, 243));
        btnBuscar.addActionListener(e -> buscarProductos());
        panelBusqueda.add(btnBuscar);

        panelBusqueda.add(new JLabel("   |   Categoría:"));
        cmbFiltroCat = new JComboBox<>();
        cmbFiltroCat.setFont(new Font("Arial", Font.PLAIN, 11));
        cmbFiltroCat.addItem(null); // Opción "Todas"
        panelBusqueda.add(cmbFiltroCat);

        JButton btnFiltrar = new JButton("🎯 Filtrar");
        estilizarBoton(btnFiltrar, new Color(156, 39, 176));
        btnFiltrar.addActionListener(e -> filtrarPorCategoria());
        panelBusqueda.add(btnFiltrar);

        btnMostrarTodos = new JButton("📋 Mostrar Todos");
        estilizarBoton(btnMostrarTodos, new Color(76, 175, 80));
        btnMostrarTodos.addActionListener(e -> {
            txtBuscarProducto.setText("");
            cmbFiltroCat.setSelectedIndex(0);
            sorter.setRowFilter(null);
            cargarProductos();
        });
        panelBusqueda.add(btnMostrarTodos);

        // ========== PANEL CENTRAL: TABLA ==========
        String[] columnasProd = {"ID", "Código", "Nombre", "Categoría", "Precio Unitario", "Stock Disponible", "Estado"};
        modeloProductos = new DefaultTableModel(columnasProd, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaProductos = new JTable(modeloProductos);
        tablaProductos.setFont(new Font("Arial", Font.PLAIN, 12));
        tablaProductos.setRowHeight(28);
        tablaProductos.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tablaProductos.getTableHeader().setBackground(new Color(255, 140, 0));
        tablaProductos.getTableHeader().setForeground(Color.WHITE);
        tablaProductos.setSelectionBackground(new Color(255, 224, 178));

        // Ocultar columna ID
        tablaProductos.getColumnModel().getColumn(0).setMinWidth(0);
        tablaProductos.getColumnModel().getColumn(0).setMaxWidth(0);
        tablaProductos.getColumnModel().getColumn(0).setWidth(0);

        sorter = new TableRowSorter<>(modeloProductos);
        tablaProductos.setRowSorter(sorter);

        tablaProductos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarProductoSeleccionado();
            }
        });

        JScrollPane scrollProd = new JScrollPane(tablaProductos);
        scrollProd.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        // ========== PANEL INFERIOR: FORMULARIO ==========
        JPanel panelFormProd = new JPanel(new GridBagLayout());
        panelFormProd.setBackground(Color.WHITE);
        panelFormProd.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(255, 140, 0), 2),
                "📝 Datos del Producto",
                0, 0, new Font("Arial", Font.BOLD, 13), new Color(255, 140, 0)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 10, 6, 10);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        panelFormProd.add(crearLabel("Código:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.4;
        txtCodigo = new JTextField();
        txtCodigo.setFont(new Font("Arial", Font.PLAIN, 12));
        txtCodigo.setEditable(false);
        txtCodigo.setBackground(new Color(240, 240, 240));
        panelFormProd.add(txtCodigo, gbc);

        gbc.gridx = 2; gbc.weightx = 0.0;
        panelFormProd.add(crearLabel("Nombre:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.6;
        txtNombreProducto = new JTextField();
        txtNombreProducto.setFont(new Font("Arial", Font.PLAIN, 12));
        panelFormProd.add(txtNombreProducto, gbc);

        // Fila 2: Categoría y Precio
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        panelFormProd.add(crearLabel("Categoría:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.4;
        cmbCategoria = new JComboBox<>();
        cmbCategoria.setFont(new Font("Arial", Font.PLAIN, 11));
        panelFormProd.add(cmbCategoria, gbc);

        gbc.gridx = 2; gbc.weightx = 0.0;
        panelFormProd.add(crearLabel("Precio (L.):"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.6;
        txtPrecio = new JTextField();
        txtPrecio.setFont(new Font("Arial", Font.PLAIN, 12));
        panelFormProd.add(txtPrecio, gbc);

        // Fila 3: Descripción
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        panelFormProd.add(crearLabel("Descripción:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.weightx = 1.0;
        txtDescProducto = new JTextField();
        txtDescProducto.setFont(new Font("Arial", Font.PLAIN, 12));
        panelFormProd.add(txtDescProducto, gbc);

        // Fila 4: Stock
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1; gbc.weightx = 0.0;
        panelFormProd.add(crearLabel("Stock Inicial:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.4;
        txtStockInicial = new JTextField();
        txtStockInicial.setFont(new Font("Arial", Font.PLAIN, 12));
        panelFormProd.add(txtStockInicial, gbc);

        gbc.gridx = 2; gbc.weightx = 0.0;
        JLabel lblStockActualText = crearLabel("Stock Actual:");
        panelFormProd.add(lblStockActualText, gbc);

        gbc.gridx = 3; gbc.weightx = 0.6;
        JPanel panelStock = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelStock.setBackground(Color.WHITE);
        lblStockActual = new JLabel("---");
        lblStockActual.setFont(new Font("Arial", Font.BOLD, 14));
        lblStockStatus = new JLabel();
        lblStockStatus.setFont(new Font("Arial", Font.ITALIC, 11));
        panelStock.add(lblStockActual);
        panelStock.add(lblStockStatus);
        panelFormProd.add(panelStock, gbc);

        // Fila 5: Botones
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 4; gbc.weightx = 1.0;
        JPanel panelBotonesProd = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        panelBotonesProd.setBackground(Color.WHITE);

        btnAgregarProducto = new JButton("➕ Agregar Producto");
        estilizarBoton(btnAgregarProducto, new Color(76, 175, 80));
        btnAgregarProducto.addActionListener(e -> agregarProducto());

        btnActualizarProducto = new JButton("✏️ Actualizar");
        estilizarBoton(btnActualizarProducto, new Color(255, 152, 0));
        btnActualizarProducto.addActionListener(e -> actualizarProducto());
        btnActualizarProducto.setEnabled(false);

        btnAjustarStock = new JButton("📊 Ajustar Stock");
        estilizarBoton(btnAjustarStock, new Color(33, 150, 243));
        btnAjustarStock.addActionListener(e -> ajustarStock());
        btnAjustarStock.setEnabled(false);

        btnEliminarProducto = new JButton("🗑️ Eliminar");
        estilizarBoton(btnEliminarProducto, new Color(244, 67, 54));
        btnEliminarProducto.addActionListener(e -> eliminarProducto());
        btnEliminarProducto.setEnabled(false);

        btnLimpiarProducto = new JButton("🔄 Limpiar");
        estilizarBoton(btnLimpiarProducto, new Color(158, 158, 158));
        btnLimpiarProducto.addActionListener(e -> limpiarFormularioProducto());

        panelBotonesProd.add(btnAgregarProducto);
        panelBotonesProd.add(btnActualizarProducto);
        panelBotonesProd.add(btnAjustarStock);
        panelBotonesProd.add(btnEliminarProducto);
        panelBotonesProd.add(btnLimpiarProducto);

        panelFormProd.add(panelBotonesProd, gbc);

        // ========== AGREGAR TODO AL PANEL PRINCIPAL ==========
        panel.add(panelBusqueda, BorderLayout.NORTH);
        panel.add(scrollProd, BorderLayout.CENTER);
        panel.add(panelFormProd, BorderLayout.SOUTH);

        return panel;
    }

    // ========================================
    // MÉTODOS AUXILIARES DE UI
    // ========================================
    private JLabel crearLabel(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Arial", Font.BOLD, 12));
        return lbl;
    }

    private void estilizarBoton(JButton boton, Color color) {
        boton.setBackground(color);
        boton.setForeground(Color.WHITE);
        boton.setFont(new Font("Arial", Font.BOLD, 11));
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setPreferredSize(new Dimension(140, 32));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // ========================================
    // MÉTODOS DE CATEGORÍAS
    // ========================================
    private void cargarCategorias() {
        modeloCategorias.setRowCount(0);
        List<Categoria> categorias = categoriaDAO.listarTodas();
        for (Categoria cat : categorias) {
            Object[] fila = {cat.getIdCategoria(), cat.getNombre(), cat.getDescripcion()};
            modeloCategorias.addRow(fila);
        }
    }

    private void cargarCategoriaSeleccionada() {
        int fila = tablaCategorias.getSelectedRow();
        if (fila >= 0) {
            idCategoriaSeleccionada = (int) modeloCategorias.getValueAt(fila, 0);
            txtNombreCategoria.setText((String) modeloCategorias.getValueAt(fila, 1));
            txtDescCategoria.setText((String) modeloCategorias.getValueAt(fila, 2));
            btnActualizarCategoria.setEnabled(true);
            btnEliminarCategoria.setEnabled(true);
            btnAgregarCategoria.setEnabled(false);
        }
    }

    private void agregarCategoria() {
        String nombre = txtNombreCategoria.getText().trim();
        if (nombre.isEmpty()) {
            mostrarError("El nombre de la categoría es obligatorio");
            return;
        }

        if (categoriaDAO.existeNombre(nombre)) {
            mostrarError("Ya existe una categoría con ese nombre");
            return;
        }

        Categoria categoria = new Categoria(nombre, txtDescCategoria.getText().trim());
        if (categoriaDAO.agregar(categoria)) {
            mostrarExito("Categoría agregada exitosamente");
            limpiarFormularioCategoria();
            cargarCategorias();
            cargarCategoriasCombo();
        } else {
            mostrarError("Error al agregar la categoría");
        }
    }

    private void actualizarCategoria() {
        Categoria categoria = new Categoria();
        categoria.setIdCategoria(idCategoriaSeleccionada);
        categoria.setNombre(txtNombreCategoria.getText().trim());
        categoria.setDescripcion(txtDescCategoria.getText().trim());

        if (categoriaDAO.actualizar(categoria)) {
            mostrarExito("Categoría actualizada exitosamente");
            limpiarFormularioCategoria();
            cargarCategorias();
            cargarCategoriasCombo();
        } else {
            mostrarError("Error al actualizar la categoría");
        }
    }

    private void eliminarCategoria() {
        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de eliminar esta categoría?",
                "Confirmar Eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmacion == JOptionPane.YES_OPTION) {
            if (categoriaDAO.eliminar(idCategoriaSeleccionada)) {
                mostrarExito("Categoría eliminada exitosamente");
                limpiarFormularioCategoria();
                cargarCategorias();
                cargarCategoriasCombo();
            } else {
                mostrarError("No se puede eliminar. La categoría tiene productos asociados");
            }
        }
    }

    private void limpiarFormularioCategoria() {
        txtNombreCategoria.setText("");
        txtDescCategoria.setText("");
        idCategoriaSeleccionada = -1;
        btnAgregarCategoria.setEnabled(true);
        btnActualizarCategoria.setEnabled(false);
        btnEliminarCategoria.setEnabled(false);
        tablaCategorias.clearSelection();
    }

    // ========================================
    // MÉTODOS DE PRODUCTOS
    // ========================================
    private void cargarProductos() {
        modeloProductos.setRowCount(0);
        List<Producto> productos = productoDAO.listarTodos();

        for (Producto prod : productos) {
            String estado = obtenerEstadoStock(prod.getCantidadDisponible());
            Object[] fila = {
                    prod.getIdProducto(),
                    prod.getCodigo(),
                    prod.getNombre(),
                    prod.getNombreCategoria(),
                    String.format("L. %.2f", prod.getPrecioUnitario()),
                    prod.getCantidadDisponible(),
                    estado
            };
            modeloProductos.addRow(fila);
        }
    }

    private String obtenerEstadoStock(int cantidad) {
        if (cantidad == 0) return "❌ Sin stock";
        if (cantidad < 10) return "⚠️ Stock bajo";
        if (cantidad < 50) return "✅ Normal";
        return "✅ Bueno";
    }

    private void cargarCategoriasCombo() {
        cmbCategoria.removeAllItems();
        cmbFiltroCat.removeAllItems();
        cmbFiltroCat.addItem(null); // Opción "Todas"

        List<Categoria> categorias = categoriaDAO.listarTodas();
        for (Categoria cat : categorias) {
            cmbCategoria.addItem(cat);
            cmbFiltroCat.addItem(cat);
        }
    }

    private void cargarProductoSeleccionado() {
        int fila = tablaProductos.getSelectedRow();
        if (fila >= 0) {
            int filaModelo = tablaProductos.convertRowIndexToModel(fila);
            idProductoSeleccionado = (int) modeloProductos.getValueAt(filaModelo, 0);

            txtCodigo.setText((String) modeloProductos.getValueAt(filaModelo, 1));
            txtNombreProducto.setText((String) modeloProductos.getValueAt(filaModelo, 2));

            String precioStr = (String) modeloProductos.getValueAt(filaModelo, 4);
            txtPrecio.setText(precioStr.replace("L. ", ""));

            int stockActual = (int) modeloProductos.getValueAt(filaModelo, 5);
            actualizarVisualizacionStock(stockActual);

            txtStockInicial.setEnabled(false);
            txtStockInicial.setText("");

            btnActualizarProducto.setEnabled(true);
            btnAjustarStock.setEnabled(true);
            btnEliminarProducto.setEnabled(true);
            btnAgregarProducto.setEnabled(false);
        }
    }

    private void actualizarVisualizacionStock(int stock) {
        lblStockActual.setText(String.valueOf(stock));

        if (stock == 0) {
            lblStockActual.setForeground(Color.RED);
            lblStockStatus.setText("❌ Sin stock");
            lblStockStatus.setForeground(Color.RED);
        } else if (stock < 10) {
            lblStockActual.setForeground(new Color(255, 140, 0));
            lblStockStatus.setText("⚠️ Stock bajo");
            lblStockStatus.setForeground(new Color(255, 140, 0));
        } else {
            lblStockActual.setForeground(new Color(76, 175, 80));
            lblStockStatus.setText("✅ Stock normal");
            lblStockStatus.setForeground(new Color(76, 175, 80));
        }
    }

    private void agregarProducto() {
        if (!validarCamposProducto(true)) return;

        String codigo = txtCodigo.getText().trim();
        String nombre = txtNombreProducto.getText().trim();

        if (productoDAO.existeCodigo(codigo)) {
            mostrarError("Ya existe un producto con ese código");
            return;
        }

        if (productoDAO.existeNombre(nombre)) {
            mostrarError("Ya existe un producto con ese nombre");
            return;
        }

        try {
            Categoria catSeleccionada = (Categoria) cmbCategoria.getSelectedItem();
            Producto producto = new Producto(
                    codigo,
                    nombre,
                    txtDescProducto.getText().trim(),
                    Double.parseDouble(txtPrecio.getText().trim()),
                    catSeleccionada.getIdCategoria()
            );

            int stockInicial = Integer.parseInt(txtStockInicial.getText().trim());

            if (productoDAO.agregar(producto, stockInicial)) {
                mostrarExito("Producto agregado exitosamente con stock inicial de " + stockInicial + " unidades");
                limpiarFormularioProducto();
                cargarProductos();
            } else {
                mostrarError("Error al agregar el producto");
            }
        } catch (NumberFormatException e) {
            mostrarError("El precio y el stock deben ser números válidos");
        }
    }

    private void actualizarProducto() {
        if (!validarCamposProducto(false)) return;

        try {
            Categoria catSeleccionada = (Categoria) cmbCategoria.getSelectedItem();
            Producto producto = new Producto();
            producto.setIdProducto(idProductoSeleccionado);
            producto.setCodigo(txtCodigo.getText().trim());
            producto.setNombre(txtNombreProducto.getText().trim());
            producto.setDescripcion(txtDescProducto.getText().trim());
            producto.setPrecioUnitario(Double.parseDouble(txtPrecio.getText().trim()));
            producto.setIdCategoria(catSeleccionada.getIdCategoria());

            if (productoDAO.actualizar(producto)) {
                mostrarExito("Producto actualizado exitosamente");
                limpiarFormularioProducto();
                cargarProductos();
            } else {
                mostrarError("Error al actualizar el producto");
            }
        } catch (NumberFormatException e) {
            mostrarError("El precio debe ser un número válido");
        }
    }

    private void ajustarStock() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        int stockActual = Integer.parseInt(lblStockActual.getText());

        JLabel lblActual = new JLabel("Stock Actual:");
        JLabel lblActualValor = new JLabel(String.valueOf(stockActual));
        lblActualValor.setFont(new Font("Arial", Font.BOLD, 14));

        JLabel lblNuevo = new JLabel("Nuevo Stock:");
        JTextField txtNuevoStock = new JTextField(String.valueOf(stockActual), 10);
        txtNuevoStock.setFont(new Font("Arial", Font.PLAIN, 12));

        JLabel lblDiferencia = new JLabel("Diferencia:");
        JLabel lblDifValor = new JLabel("0");

        // Listener para calcular diferencia automáticamente
        txtNuevoStock.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { actualizar(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { actualizar(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { actualizar(); }

            public void actualizar() {
                try {
                    int nuevo = Integer.parseInt(txtNuevoStock.getText().trim());
                    int diferencia = nuevo - stockActual;
                    lblDifValor.setText((diferencia > 0 ? "+" : "") + diferencia);
                    lblDifValor.setForeground(diferencia > 0 ? new Color(76, 175, 80) : Color.RED);
                } catch (NumberFormatException ex) {
                    lblDifValor.setText("---");
                }
            }
        });

        panel.add(lblActual);
        panel.add(lblActualValor);
        panel.add(lblNuevo);
        panel.add(txtNuevoStock);
        panel.add(lblDiferencia);
        panel.add(lblDifValor);

        int resultado = JOptionPane.showConfirmDialog(this, panel,
                "Ajustar Stock del Producto",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (resultado == JOptionPane.OK_OPTION) {
            try {
                int nuevoStock = Integer.parseInt(txtNuevoStock.getText().trim());
                if (nuevoStock < 0) {
                    mostrarError("El stock no puede ser negativo");
                    return;
                }

                if (productoDAO.actualizarInventario(idProductoSeleccionado, nuevoStock)) {
                    int diferencia = nuevoStock - stockActual;
                    String mensaje = "Stock actualizado exitosamente\n";
                    mensaje += "Stock anterior: " + stockActual + "\n";
                    mensaje += "Stock nuevo: " + nuevoStock + "\n";
                    mensaje += "Diferencia: " + (diferencia > 0 ? "+" : "") + diferencia;

                    mostrarExito(mensaje);
                    cargarProductos();
                    actualizarVisualizacionStock(nuevoStock);
                } else {
                    mostrarError("Error al actualizar el stock");
                }
            } catch (NumberFormatException e) {
                mostrarError("Debe ingresar un número válido");
            }
        }
    }

    private void eliminarProducto() {
        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de eliminar este producto?\nEsta acción no se puede deshacer.",
                "Confirmar Eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmacion == JOptionPane.YES_OPTION) {
            if (productoDAO.eliminar(idProductoSeleccionado)) {
                mostrarExito("Producto eliminado exitosamente");
                limpiarFormularioProducto();
                cargarProductos();
            } else {
                mostrarError("No se puede eliminar. El producto tiene facturas asociadas");
            }
        }
    }

    private void buscarProductos() {
        String busqueda = txtBuscarProducto.getText().trim().toLowerCase();
        if (busqueda.isEmpty()) {
            mostrarAdvertencia("Ingrese un término de búsqueda");
            return;
        }

        RowFilter<DefaultTableModel, Object> rf = RowFilter.regexFilter("(?i)" + busqueda);
        sorter.setRowFilter(rf);

        if (tablaProductos.getRowCount() == 0) {
            mostrarAdvertencia("No se encontraron productos que coincidan con: " + busqueda);
        }
    }

    private void filtrarPorCategoria() {
        Categoria catSeleccionada = (Categoria) cmbFiltroCat.getSelectedItem();

        if (catSeleccionada == null) {
            sorter.setRowFilter(null);
            return;
        }

        RowFilter<DefaultTableModel, Object> rf = RowFilter.regexFilter(catSeleccionada.getNombre(), 3);
        sorter.setRowFilter(rf);

        if (tablaProductos.getRowCount() == 0) {
            mostrarAdvertencia("No hay productos en la categoría: " + catSeleccionada.getNombre());
        }
    }

    private boolean validarCamposProducto(boolean esNuevo) {
        if (txtCodigo.getText().trim().isEmpty() ||
                txtNombreProducto.getText().trim().isEmpty() ||
                txtPrecio.getText().trim().isEmpty() ||
                cmbCategoria.getSelectedItem() == null) {
            mostrarError("Complete todos los campos obligatorios (Código, Nombre, Categoría, Precio)");
            return false;
        }

        if (esNuevo && txtStockInicial.getText().trim().isEmpty()) {
            mostrarError("Debe ingresar el stock inicial del producto");
            return false;
        }

        try {
            double precio = Double.parseDouble(txtPrecio.getText().trim());
            if (precio <= 0) {
                mostrarError("El precio debe ser mayor a cero");
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarError("El precio debe ser un número válido");
            return false;
        }

        if (esNuevo) {
            try {
                int stock = Integer.parseInt(txtStockInicial.getText().trim());
                if (stock < 0) {
                    mostrarError("El stock inicial no puede ser negativo");
                    return false;
                }
            } catch (NumberFormatException e) {
                mostrarError("El stock inicial debe ser un número válido");
                return false;
            }
        }

        return true;
    }

    private void limpiarFormularioProducto() {
        txtCodigo.setText(generarCodigoAutomatico());
        txtNombreProducto.setText("");
        txtDescProducto.setText("");
        txtPrecio.setText("");
        txtStockInicial.setText("");
        txtStockInicial.setEnabled(true);
        lblStockActual.setText("---");
        lblStockStatus.setText("");

        idProductoSeleccionado = -1;
        btnAgregarProducto.setEnabled(true);
        btnActualizarProducto.setEnabled(false);
        btnAjustarStock.setEnabled(false);
        btnEliminarProducto.setEnabled(false);
        tablaProductos.clearSelection();
    }

    private void buscarCategorias() {
        String busqueda = txtBuscarCategoria.getText().trim().toLowerCase();
        if (busqueda.isEmpty()) {
            mostrarAdvertencia("Ingrese un término de búsqueda");
            return;
        }

        RowFilter<DefaultTableModel, Object> rf = RowFilter.regexFilter("(?i)" + busqueda);
        sorterCategorias.setRowFilter(rf);

        if (tablaCategorias.getRowCount() == 0) {
            mostrarAdvertencia("No se encontraron categorías que coincidan con: " + busqueda);
        }
    }

    // ========================================
    // MÉTODOS DE MENSAJES
    // ========================================
    private void mostrarExito(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "✅ Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "❌ Error", JOptionPane.ERROR_MESSAGE);
    }

    private void mostrarAdvertencia(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "⚠️ Advertencia", JOptionPane.WARNING_MESSAGE);
    }
}