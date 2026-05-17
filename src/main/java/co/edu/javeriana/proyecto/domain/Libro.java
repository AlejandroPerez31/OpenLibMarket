package co.edu.javeriana.proyecto.domain;

public class Libro {
    private Long id;
    private String titulo;
    private String autor;
    private String isbn;
    private String categoria;
    private String etiquetas; // Comma-separated tags
    private int clics;
    private double precio;
    private String portada;
    private int stock;
    private double calificacionPromedio;

    public Libro() {}

    public Libro(Long id, String titulo, String autor, String isbn, String categoria,
                 String etiquetas, int clics, double precio, String portada, int stock,
                 double calificacionPromedio) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.isbn = isbn;
        this.categoria = categoria;
        this.etiquetas = etiquetas;
        this.clics = clics;
        this.precio = precio;
        this.portada = portada;
        this.stock = stock;
        this.calificacionPromedio = calificacionPromedio;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getEtiquetas() { return etiquetas; }
    public void setEtiquetas(String etiquetas) { this.etiquetas = etiquetas; }

    public int getClics() { return clics; }
    public void setClics(int clics) { this.clics = clics; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public String getPortada() { return portada; }
    public void setPortada(String portada) { this.portada = portada; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public double getCalificacionPromedio() { return calificacionPromedio; }
    public void setCalificacionPromedio(double calificacionPromedio) { this.calificacionPromedio = calificacionPromedio; }

    @Override
    public String toString() {
        return "Libro{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", autor='" + autor + '\'' +
                ", isbn='" + isbn + '\'' +
                ", categoria='" + categoria + '\'' +
                ", clics=" + clics +
                ", precio=" + precio +
                '}';
    }
}
