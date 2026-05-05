package co.edu.javeriana.proyecto.domain;

public class Libro {
    private Long id;
    private String titulo;
    private String autor;
    private int clics;
    private double precio;
    private String portada;

    public Libro() {}

    public Libro(Long id, String titulo, String autor, int clics, double precio, String portada) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.clics = clics;
        this.precio = precio;
        this.portada = portada;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }

    public int getClics() { return clics; }
    public void setClics(int clics) { this.clics = clics; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public String getPortada() { return portada; }
    public void setPortada(String portada) { this.portada = portada; }

    @Override
    public String toString() {
        return "Libro{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", autor='" + autor + '\'' +
                ", clics=" + clics +
                ", precio=" + precio +
                ", portada='" + portada + '\'' +
                '}';
    }
}
