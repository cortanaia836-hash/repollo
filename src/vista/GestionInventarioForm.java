package vista;

import dao.CategoriaDAO;
import dao.ProductoDAO;
import modelo.Categoria;
import modelo.Producto;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class GestionInventarioForm extends JFrame {
    private JTabbedPane tabbedPane;

    // Componentes para Categorías
    private JTable tablaCategorias;
    private DefaultTableModel modeloCategorias;
    private JTextField txtNombreCategoria, txtDescCategoria, txtBuscarCategoria;
    private JButton btnAgregarCategoria, btnActualizarCategoria, btnEliminarCategoria, btnLimpiarCategoria;
    private CategoriaDAO categoriaDAO;
    private int idCategoriaSeleccionada = -1;

    // Componentes para Productos
    private JTable tablaProductos;
    private DefaultTableModel modeloProductos;
    private JTextField txtCodigo, txtNombreProducto, txtDescProducto, txtPrecio, txtStockInicial, txtStockActual;
    private JComboBox<Categoria> cmbCategoria;
    private JButton btnAgregarProducto, btnActualizarProducto, btnEliminarProducto, btnLimpiarProducto, btnActualizarStock;
    private ProductoDAO productoDAO;
    private int idProductoSeleccionado = -1;

    public GestionInventarioForm() {
        categoriaDAO = new CategoriaDAO();
        productoDAO = new ProductoDAO();
        inicializarComponentes();
        cargarCategorias();
        cargarProductos();
        cargarCategoriasCombo();
    }

    private void inicializarComponentes() {
        setTitle("Gestión de Inventario");
        setSize(1200, 700);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();

        // Panel de Categorías
        JPanel panelCategorias = crearPanelCategorias();
        tabbedPane.addTab("Categorías", panelCategorias);

        // Panel de Productos
        JPanel panelProductos = crearPanelProductos();
        tabbedPane.addTab("Productos e Inventario", panelProductos);

        add(tabbedPane);
    }

    // ==================== PANEL DE CATEGORÍAS ====================
    private JPanel crearPanelCategorias() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel de búsqueda
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBusqueda.add(new JLabel("Buscar:"));
        txtBuscarCategoria = new JTextField(20);
        JButton btnBuscarCat = new JButton("Buscar");
        JButton btnMostrarTodas = new JButton("Mostrar Todas");
        btnMostrarTodas.addActionListener(e -> cargarCategorias());
        panelBusqueda.add(txtBuscarCategoria);
        panelBusqueda.add(btnBuscarCat);
        panelBusqueda.add(btnMostrarTodas);

        // Tabla de categorías
        String[] columnasCat = {"ID", "Nombre", "Descripción"};
        modeloCategorias = new DefaultTableModel(columnasCat, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaCategorias = new JTable(modeloCategorias);
        tablaCategorias.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarCategoriaSeleccionada();
            }
        });

        JScrollPane scrollCat = new JScrollPane(tablaCategorias);

        // Panel de formulario
        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setBorder(BorderFactory.createTitledBorder("Datos de Categoría"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panelForm.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        txtNombreCategoria = new JTextField(30);
        panelForm.add(txtNombreCategoria, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panelForm.add(new JLabel("Descripción:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        txtDescCategoria = new JTextField(30);
        panelForm.add(txtDescCategoria, gbc);

        // Botones
        JPanel panelBotonesCat = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnAgregarCategoria = new JButton("Agregar");
        btnAgregarCategoria.addActionListener(e -> agregarCategoria());
        btnActualizarCategoria = new JButton("Actualizar");
        btnActualizarCategoria.addActionListener(e -> actualizarCategoria());
        btnActualizarCategoria.setEnabled(false);
        btnEliminarCategoria = new JButton("Eliminar");
        btnEliminarCategoria.addActionListener(e -> eliminarCategoria());
        btnEliminarCategoria.setEnabled(false);
        btnLimpiarCategoria = new JButton("Limpiar");
        btnLimpiarCategoria.addActionListener(e -> limpiarFormularioCategoria());

        panelBotonesCat.add(btnAgregarCategoria);
        panelBotonesCat.add(btnActualizarCategoria);
        panelBotonesCat.add(btnEliminarCategoria);
        panelBotonesCat.add(btnLimpiarCategoria);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        panelForm.add(panelBotonesCat, gbc);

        panel.add(panelBusqueda, BorderLayout.NORTH);
        panel.add(scrollCat, BorderLayout.CENTER);
        panel.add(panelForm, BorderLayout.SOUTH);

        return panel;
    }

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
            JOptionPane.showMessageDialog(this, "El nombre es obligatorio", "Campo Vacío", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (categoriaDAO.existeNombre(nombre)) {
            JOptionPane.showMessageDialog(this, "Ya existe una categoría con ese nombre", "Nombre Duplicado", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Categoria categoria = new Categoria(nombre, txtDescCategoria.getText().trim());

        if (categoriaDAO.agregar(categoria)) {
            JOptionPane.showMessageDialog(this, "Categoría agregada exitosamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            limpiarFormularioCategoria();
            cargarCategorias();
            cargarCategoriasCombo();
        } else {
            JOptionPane.showMessageDialog(this, "Error al agregar categoría", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarCategoria() {
        Categoria categoria = new Categoria();
        categoria.setIdCategoria(idCategoriaSeleccionada);
        categoria.setNombre(txtNombreCategoria.getText().trim());
        categoria.setDescripcion(txtDescCategoria.getText().trim());

        if (categoriaDAO.actualizar(categoria)) {
            JOptionPane.showMessageDialog(this, "Categoría actualizada", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            limpiarFormularioCategoria();
            cargarCategorias();
            cargarCategoriasCombo();
        } else {
            JOptionPane.showMessageDialog(this, "Error al actualizar", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarCategoria() {
        int confirmacion = JOptionPane.showConfirmDialog(this, "¿Eliminar esta categoría?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirmacion == JOptionPane.YES_OPTION) {
            if (categoriaDAO.eliminar(idCategoriaSeleccionada)) {
                JOptionPane.showMessageDialog(this, "Categoría eliminada", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                limpiarFormularioCategoria();
                cargarCategorias();
                cargarCategoriasCombo();
            } else {
                JOptionPane.showMessageDialog(this, "No se puede eliminar. Tiene productos asociados.", "Error", JOptionPane.ERROR_MESSAGE);
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

    // ==================== PANEL DE PRODUCTOS ====================
    private JPanel crearPanelProductos() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Tabla de productos
        String[] columnasProd = {"ID", "Código", "Nombre", "Categoría", "Precio", "Stock"};
        modeloProductos = new DefaultTableModel(columnasProd, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaProductos = new JTable(modeloProductos);
        tablaProductos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarProductoSeleccionado();
            }
        });

        JScrollPane scrollProd = new JScrollPane(tablaProductos);

        // Panel de formulario
        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setBorder(BorderFactory.createTitledBorder("Datos del Producto"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Código
        gbc.gridx = 0;
        gbc.gridy = 0;
        panelForm.add(new JLabel("Código:"), gbc);
        gbc.gridx = 1;
        txtCodigo = new JTextField(15);
        panelForm.add(txtCodigo, gbc);

        // Nombre
        gbc.gridx = 2;
        panelForm.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 3;
        txtNombreProducto = new JTextField(15);
        panelForm.add(txtNombreProducto, gbc);

        // Categoría
        gbc.gridx = 0;
        gbc.gridy = 1;
        panelForm.add(new JLabel("Categoría:"), gbc);
        gbc.gridx = 1;
        cmbCategoria = new JComboBox<>();
        panelForm.add(cmbCategoria, gbc);

        // Precio
        gbc.gridx = 2;
        panelForm.add(new JLabel("Precio:"), gbc);
        gbc.gridx = 3;
        txtPrecio = new JTextField(15);
        panelForm.add(txtPrecio, gbc);

        // Descripción
        gbc.gridx = 0;
        gbc.gridy = 2;
        panelForm.add(new JLabel("Descripción:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        txtDescProducto = new JTextField(40);
        panelForm.add(txtDescProducto, gbc);

        // Stock inicial (solo al agregar)
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        JLabel lblStockInicial = new JLabel("Stock Inicial:");
        panelForm.add(lblStockInicial, gbc);
        gbc.gridx = 1;
        txtStockInicial = new JTextField(15);
        panelForm.add(txtStockInicial, gbc);

        // Stock actual (solo lectura)
        gbc.gridx = 2;
        panelForm.add(new JLabel("Stock Actual:"), gbc);
        gbc.gridx = 3;
        txtStockActual = new JTextField(15);
        txtStockActual.setEditable(false);
        txtStockActual.setBackground(Color.LIGHT_GRAY);
        panelForm.add(txtStockActual, gbc);

        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnAgregarProducto = new JButton("Agregar");
        btnAgregarProducto.addActionListener(e -> agregarProducto());
        btnActualizarProducto = new JButton("Actualizar");
        btnActualizarProducto.addActionListener(e -> actualizarProducto());
        btnActualizarProducto.setEnabled(false);
        btnActualizarStock = new JButton("Actualizar Stock");
        btnActualizarStock.addActionListener(e -> actualizarStock());
        btnActualizarStock.setEnabled(false);
        btnEliminarProducto = new JButton("Eliminar");
        btnEliminarProducto.addActionListener(e -> eliminarProducto());
        btnEliminarProducto.setEnabled(false);
        btnLimpiarProducto = new JButton("Limpiar");
        btnLimpiarProducto.addActionListener(e -> limpiarFormularioProducto());

        panelBotones.add(btnAgregarProducto);
        panelBotones.add(btnActualizarProducto);
        panelBotones.add(btnActualizarStock);
        panelBotones.add(btnEliminarProducto);
        panelBotones.add(btnLimpiarProducto);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 4;
        panelForm.add(panelBotones, gbc);

        panel.add(scrollProd, BorderLayout.CENTER);
        panel.add(panelForm, BorderLayout.SOUTH);

        return panel;
    }

    private void cargarProductos() {
        modeloProductos.setRowCount(0);
        List<Producto> productos = productoDAO.listarTodos();

        for (Producto prod : productos) {
            Object[] fila = {
                    prod.getIdProducto(),
                    prod.getCodigo(),
                    prod.getNombre(),
                    prod.getNombreCategoria(),
                    String.format("L. %.2f", prod.getPrecioUnitario()),
                    prod.getCantidadDisponible()
            };
            modeloProductos.addRow(fila);
        }
    }

    private void cargarCategoriasCombo() {
        cmbCategoria.removeAllItems();
        List<Categoria> categorias = categoriaDAO.listarTodas();
        for (Categoria cat : categorias) {
            cmbCategoria.addItem(cat);
        }
    }

    private void cargarProductoSeleccionado() {
        int fila = tablaProductos.getSelectedRow();
        if (fila >= 0) {
            idProductoSeleccionado = (int) modeloProductos.getValueAt(fila, 0);
            txtCodigo.setText((String) modeloProductos.getValueAt(fila, 1));
            txtNombreProducto.setText((String) modeloProductos.getValueAt(fila, 2));

            String precioStr = (String) modeloProductos.getValueAt(fila, 4);
            txtPrecio.setText(precioStr.replace("L. ", ""));

            txtStockActual.setText(modeloProductos.getValueAt(fila, 5).toString());
            txtStockInicial.setEnabled(false);

            btnActualizarProducto.setEnabled(true);
            btnActualizarStock.setEnabled(true);
            btnEliminarProducto.setEnabled(true);
            btnAgregarProducto.setEnabled(false);
        }
    }

    private void agregarProducto() {
        if (!validarCamposProducto()) return;

        String codigo = txtCodigo.getText().trim();
        String nombre = txtNombreProducto.getText().trim();

        if (productoDAO.existeCodigo(codigo)) {
            JOptionPane.showMessageDialog(this, "Ya existe un producto con ese código", "Código Duplicado", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (productoDAO.existeNombre(nombre)) {
            JOptionPane.showMessageDialog(this, "Ya existe un producto con ese nombre", "Nombre Duplicado", JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(this, "Producto agregado exitosamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                limpiarFormularioProducto();
                cargarProductos();
            } else {
                JOptionPane.showMessageDialog(this, "Error al agregar producto", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Precio y stock deben ser números válidos", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarProducto() {
        if (!validarCamposProducto()) return;

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
                JOptionPane.showMessageDialog(this, "Producto actualizado", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                limpiarFormularioProducto();
                cargarProductos();
            } else {
                JOptionPane.showMessageDialog(this, "Error al actualizar", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Precio debe ser un número válido", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarStock() {
        String nuevoStockStr = JOptionPane.showInputDialog(this, "Ingrese la nueva cantidad en stock:", txtStockActual.getText());

        if (nuevoStockStr != null && !nuevoStockStr.trim().isEmpty()) {
            try {
                int nuevoStock = Integer.parseInt(nuevoStockStr.trim());
                if (nuevoStock < 0) {
                    JOptionPane.showMessageDialog(this, "El stock no puede ser negativo", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (productoDAO.actualizarInventario(idProductoSeleccionado, nuevoStock)) {
                    JOptionPane.showMessageDialog(this, "Stock actualizado exitosamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    cargarProductos();
                    txtStockActual.setText(String.valueOf(nuevoStock));
                } else {
                    JOptionPane.showMessageDialog(this, "Error al actualizar stock", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Debe ingresar un número válido", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void eliminarProducto() {
        int confirmacion = JOptionPane.showConfirmDialog(this, "¿Eliminar este producto?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirmacion == JOptionPane.YES_OPTION) {
            if (productoDAO.eliminar(idProductoSeleccionado)) {
                JOptionPane.showMessageDialog(this, "Producto eliminado", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                limpiarFormularioProducto();
                cargarProductos();
            } else {
                JOptionPane.showMessageDialog(this, "No se puede eliminar. El producto tiene facturas asociadas.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean validarCamposProducto() {
        if (txtCodigo.getText().trim().isEmpty() || txtNombreProducto.getText().trim().isEmpty() ||
                txtPrecio.getText().trim().isEmpty() || cmbCategoria.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Complete todos los campos obligatorios", "Campos Vacíos", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        if (btnAgregarProducto.isEnabled() && txtStockInicial.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar el stock inicial", "Campo Vacío", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        return true;
    }

    private void limpiarFormularioProducto() {
        txtCodigo.setText("");
        txtNombreProducto.setText("");
        txtDescProducto.setText("");
        txtPrecio.setText("");
        txtStockInicial.setText("");
        txtStockActual.setText("");
        txtStockInicial.setEnabled(true);
        idProductoSeleccionado = -1;
        btnAgregarProducto.setEnabled(true);
        btnActualizarProducto.setEnabled(false);
        btnActualizarStock.setEnabled(false);
        btnEliminarProducto.setEnabled(false);
        tablaProductos.clearSelection();
    }
}