package PracticaCRUD.EjercicioObligatorio2;

import static spark.Spark.*;
import PracticaCRUD.EjercicioObligatorio2.models.Libro;
import com.google.gson.Gson;
import spark.Request;
import spark.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Clase principal que configura el servidor Spark y maneja las rutas de la biblioteca.
 */
public class Main {

    // Almacenamiento en memoria usando HashMap (buena práctica sugerida)
    private static Map<String, Libro> biblioteca = new HashMap<>();
    private static Gson gson = new Gson();

    public static void main(String[] args) {
        // Configuración del puerto
        port(4567);

        // Configuración de rutas
        configurarRutas();

        // Inicializar el servidor
        init();
    }

    /**
     * Registra todas las rutas de la API.
     */
    public static void configurarRutas() {

        // Filtro para asegurar que siempre devolvemos JSON
        after((req, res) -> res.type("application/json"));

        // Definición de Endpoints

        // Búsqueda (orden importante: antes de /libros/:isbn para evitar conflictos)
        get("/libros/buscar", Main::buscarLibros, gson::toJson);

        // Obtener todos o filtrar por autor
        get("/libros", Main::obtenerLibros, gson::toJson);

        // Crear libro
        post("/libros", Main::crearLibro, gson::toJson);

        // Obtener libro por ISBN
        get("/libros/:isbn", Main::obtenerLibroPorIsbn, gson::toJson);

        // Actualizar libro
        put("/libros/:isbn", (req, res) -> {
            String isbn = req.params(":isbn");
            Libro libroExistente = biblioteca.get(isbn);

            if (libroExistente == null) {
                res.status(404);
                return Map.of("error", "Libro no encontrado");
            }

            Libro libroActualizado = gson.fromJson(req.body(), Libro.class);
            // Validación básica de integridad
            if (libroActualizado.getIsbn() != null && !isbn.equals(libroActualizado.getIsbn())) {
                res.status(400);
                return Map.of("error", "El ISBN de la URL no coincide con el cuerpo");
            }

            // Mantener el ISBN original si no viene en el body o es igual
            libroActualizado.setIsbn(isbn);
            biblioteca.put(isbn, libroActualizado);
            return libroActualizado;
        }, gson::toJson);

        // Eliminar libro
        delete("/libros/:isbn", (req, res) -> {
            String isbn = req.params(":isbn");
            if (!biblioteca.containsKey(isbn)) {
                res.status(404);
                return "";
            }
            biblioteca.remove(isbn);
            res.status(204); // No Content
            return "";
        });
    }

    /**
     * Obtiene todos los libros o filtra por autor.
     * @param req request de Spark
     * @param res response de Spark
     * @return lista de libros en JSON
     */
    public static Object obtenerLibros(Request req, Response res) {
        String autorParam = req.queryParams("autor");

        if (autorParam != null && !autorParam.isEmpty()) {
            // Filtrado por autor
            List<Libro> librosFiltrados = biblioteca.values().stream()
                    .filter(l -> l.getAutor().equalsIgnoreCase(autorParam))
                    .collect(Collectors.toList());
            return librosFiltrados;
        }

        // Retornar todos
        return new ArrayList<>(biblioteca.values());
    }

    /**
     * Busca libros por título (búsqueda parcial).
     * @param req request de Spark con query param ?q=
     * @param res response de Spark
     * @return lista de libros que coinciden
     */
    public static Object buscarLibros(Request req, Response res) {
        String query = req.queryParams("q");

        if (query == null || query.isEmpty()) {
            res.status(400);
            return Map.of("error", "Debe proporcionar el parámetro 'q'");
        }

        // Búsqueda case-insensitive
        List<Libro> resultado = biblioteca.values().stream()
                .filter(l -> l.getTitulo().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());

        return resultado;
    }

    /**
     * Obtiene un libro específico por ISBN.
     * @param req request de Spark con parámetro :isbn
     * @param res response de Spark
     * @return libro en JSON o error 404
     */
    public static Object obtenerLibroPorIsbn(Request req, Response res) {
        String isbn = req.params(":isbn");
        Libro libro = biblioteca.get(isbn);

        if (libro == null) {
            res.status(404);
            return Map.of("error", "Libro no encontrado con ISBN: " + isbn);
        }

        return libro;
    }

    /**
     * Crea un nuevo libro.
     * @param req request de Spark con body JSON
     * @param res response de Spark
     * @return libro creado en JSON
     */
    public static Object crearLibro(Request req, Response res) {
        try {
            // Deserializar JSON a Objeto
            Libro nuevoLibro = gson.fromJson(req.body(), Libro.class);

            // Validaciones básicas
            if (nuevoLibro.getIsbn() == null || nuevoLibro.getIsbn().isEmpty()) {
                res.status(400);
                return Map.of("error", "El ISBN es obligatorio");
            }

            if (biblioteca.containsKey(nuevoLibro.getIsbn())) {
                res.status(409); // Conflict
                return Map.of("error", "Ya existe un libro con ese ISBN");
            }

            // Guardar en memoria
            if (!nuevoLibro.isDisponible()) {
                nuevoLibro.setDisponible(true); // Default a true si no se especifica
            }
            biblioteca.put(nuevoLibro.getIsbn(), nuevoLibro);

            res.status(201); // Created
            return nuevoLibro;

        } catch (Exception e) {
            res.status(400);
            return Map.of("error", "JSON malformado o inválido");
        }
    }
}