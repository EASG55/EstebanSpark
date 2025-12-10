package EjerciciosObligatorios4.models;
import java.io.Serializable;

/**
 * Representa un libro en la biblioteca.
 */
public class Libro implements Serializable {
    private String isbn;
    private String titulo;
    private String autor;
    private int añoPublicacion;
    private boolean disponible;

    // Constructor vacío necesario para la instanciación de Gson
    public Libro() {
    }

    public Libro(String isbn, String titulo, String autor, int añoPublicacion, boolean disponible) {
        this.isbn = isbn;
        this.titulo = titulo;
        this.autor = autor;
        this.añoPublicacion = añoPublicacion;
        this.disponible = disponible;
    }

    // Getters y Setters
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }

    public int getAñoPublicacion() { return añoPublicacion; }
    public void setAñoPublicacion(int añoPublicacion) { this.añoPublicacion = añoPublicacion; }

    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }
}