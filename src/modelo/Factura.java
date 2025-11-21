package modelo;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Factura {
    private int idFactura;
    private String numeroFactura;
    private int idCliente;
    private String nombreCliente;
    private int idUsuario;
    private String nombreUsuario;
    private Timestamp fechaFactura;
    private double subtotal;
    private double impuesto;
    private double total;
    private List<DetalleFactura> detalles;

    public Factura() {
        this.detalles = new ArrayList<>();
    }

    public Factura(String numeroFactura, int idCliente, int idUsuario) {
        this.numeroFactura = numeroFactura;
        this.idCliente = idCliente;
        this.idUsuario = idUsuario;
        this.detalles = new ArrayList<>();
    }

    public int getIdFactura() {
        return idFactura;
    }

    public void setIdFactura(int idFactura) {
        this.idFactura = idFactura;
    }

    public String getNumeroFactura() {
        return numeroFactura;
    }

    public void setNumeroFactura(String numeroFactura) {
        this.numeroFactura = numeroFactura;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public Timestamp getFechaFactura() {
        return fechaFactura;
    }

    public void setFechaFactura(Timestamp fechaFactura) {
        this.fechaFactura = fechaFactura;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getImpuesto() {
        return impuesto;
    }

    public void setImpuesto(double impuesto) {
        this.impuesto = impuesto;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public List<DetalleFactura> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleFactura> detalles) {
        this.detalles = detalles;
    }

    public void agregarDetalle(DetalleFactura detalle) {
        this.detalles.add(detalle);
    }

    public void calcularTotales() {
        subtotal = 0;
        for (DetalleFactura detalle : detalles) {
            subtotal += detalle.getSubtotal();
        }
        impuesto = subtotal * 0.15; // 15% de impuesto
        total = subtotal + impuesto;
    }
}
