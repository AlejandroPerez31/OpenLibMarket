package co.edu.javeriana.proyecto.domain;

public class Libro {
    private Long id;
    private String titulo;
    private String autor;
    private int clics;

    public Libro() {}

    public Libro(Long id, String titulo, String autor, int clics) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.clics = clics;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public int getClics() {
        return clics;
    }

    public void setClics(int clics) {
        this.clics = clics;
    }

    @Override
    public String toString() {
        return "Libro{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", autor='" + autor + '\'' +
                ", clics=" + clics +
                '}';
    }
}
