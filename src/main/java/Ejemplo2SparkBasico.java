//importación de la libreria de Spark
import static spark.Spark.*;


/*
 *API REST BASICA EN JAVA CON Spark
 *Aplicacion simple que proporciona un endpoint tipo GET para obtener un saludo
 *El servidor se inicia en el puerto 4567 con un mensaje de texto plano
 */

// http://localhost:4567/

public class Ejemplo2SparkBasico {
    public static void main(String[] args) {
        //levantar el endpoint en el puerto 4567
        port(4567);

        //Creacion de saludo con texto plano con el metodo GET
        get("/", (req, res) -> {
            return "Hola desde Spark!";
        });



        System.out.println("Servidor iniciado en: http://localhost:4567");
    }
}
