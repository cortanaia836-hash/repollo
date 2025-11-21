package modelo;

public class Categoria {
    private int id;
    private String nombre;
    private String descripcion;

    public Categoria(){}

    public Categoria(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getIdCategoria() {
        return id;
    }

    public void setIdCategoria(int id) {
        this.id = id;
    }



    @Override
    public String toString() {
        return nombre;
    }

}
