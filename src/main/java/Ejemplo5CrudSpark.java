//Importacion de librerias
import com.google.gson.Gson;
import models.Producto;
import static spark.Spark.*;
import java.util.HashMap;
import java.util.Map;

/*
* Ejemplo 5: CRUD completo con Spark y Gson
* API REST completa con operaciones (Create, read, update y delete)
* Utilizamos un HashMap como almacenamiento en memoria
* Gson para transformar objetos a Json
 */


public class Ejemplo5CrudSpark {
    //inicializar variables y objetos
    private static Map<Integer, Producto> productos = new HashMap();
    private static int siguienteId = 1;
    private static Gson gson = new Gson();

    public static void main(String[] args) {
        //Creamos el endpoint en el puerto 4567
        port(4567);

        // Datos iniciales
        productos.put(1, new Producto(1, "Portatil", 999.99));
        productos.put(2, new Producto(2, "Tablet", 999.99));
        siguienteId = 3;

        //Get - Todos los productos
        get("/productos", (req, res) ->{
            res.type("application/json");
            return productos.values();
        }, gson::toJson);

        //Get - Buscar productos por ID
        get("/productos/:id", (req, res) -> {
            int id = Integer.parseInt(req.params("id"));
            Producto producto = productos.get(id);
            if(producto != null){
                res.type("application/json");
                return producto;
            }else{
                res.status(404);
                return Map.of("error", "producto no encontrado");
            }
        }, gson::toJson);

        //POST - Crear producto
        post("/productos", (req, res) -> {
            Producto producto = gson.fromJson(req.body(), Producto.class);
            producto.setId(siguienteId++);
            productos.put(producto.getId(), producto);
            res.status(201);
            res.type("application/json");
            return producto;
        }, gson::toJson);

        // PUT - Actualizar el producto
        put("/productos/:id", (req, res) ->{
            int id = Integer.parseInt(req.params("id"));
            Producto producto = gson.fromJson(req.body(), Producto.class);
            producto.setId(id);
            productos.put(producto.getId(), producto);
            res.type("application/json");
            return producto;
        }, gson::toJson);

        //DELETE - Eliminar el producto
        delete("/productos/:id", (req, res) ->{
            int id = Integer.parseInt(req.params("id"));
            productos.remove(id);
            res.status(204);
            return "";
        });

        System.out.println("Servidor iniciado en http://localhost:4567");
        System.out.println("Endpoints disponibles:");
        System.out.println("    GET     /productos");
        System.out.println("    GET     /productos/:id");
        System.out.println("    POST     /productos");
        System.out.println("    PUT     /productos/:id");
        System.out.println("    DELETE     /productos/:id");
    }
}
