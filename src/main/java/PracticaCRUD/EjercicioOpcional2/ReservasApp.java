package PracticaCRUD.EjercicioOpcional2;

import com.google.gson.Gson;
import spark.ResponseTransformer;
import java.time.LocalTime;
import java.util.*;
import static spark.Spark.*;

/**
 * Modelo de datos para una Reserva.
 * Utiliza Strings para fechas/horas para simplificar la serialización JSON,
 * pero se convierten a LocalTime para la lógica.
 */
class Reserva {
    int id;
    String recurso;      // Ej: "Sala A"
    String fecha;        // Formato: "YYYY-MM-DD"
    String horaInicio;   // Formato: "HH:MM"
    String horaFin;      // Formato: "HH:MM"
    String nombreUsuario;
}

/**
 * API de Sistema de Reservas.
 * Implementa validación de solapamiento de horarios (Time Overlap).
 */
public class ReservasApp {
    private static List<Reserva> reservas = new ArrayList<>();
    private static int idCounter = 1;
    private static Gson gson = new Gson();

    public static void main(String[] args) {
        port(4567);

        /**
         * Crea una nueva reserva verificando disponibilidad.
         * <p>
         * Algoritmo de detección de conflictos:
         * Una nueva reserva solapa con una existente si:
         * (NuevaInicio < ExistenteFin) Y (NuevaFin > ExistenteInicio)
         * </p>
         */
        post("/reservas", (req, res) -> {
            Reserva nueva = gson.fromJson(req.body(), Reserva.class);

            // Validación: Verificar conflictos usando Streams
            boolean conflicto = reservas.stream().anyMatch(existente -> {
                // 1. Filtrar por mismo recurso y fecha
                if (existente.recurso.equals(nueva.recurso) && existente.fecha.equals(nueva.fecha)) {

                    // 2. Parsear horas para comparación temporal
                    LocalTime newStart = LocalTime.parse(nueva.horaInicio);
                    LocalTime newEnd = LocalTime.parse(nueva.horaFin);
                    LocalTime exStart = LocalTime.parse(existente.horaInicio);
                    LocalTime exEnd = LocalTime.parse(existente.horaFin);

                    // 3. Lógica matemática de intervalo
                    // Devuelve true si hay solapamiento
                    return newStart.isBefore(exEnd) && newEnd.isAfter(exStart);
                }
                return false;
            });

            if (conflicto) {
                res.status(409); // HTTP 409 Conflict
                return new ErrorMsg("Conflicto de horario", "La sala ya está ocupada en ese rango");
            }

            // Si no hay conflicto, guardar y retornar
            nueva.id = idCounter++;
            reservas.add(nueva);
            res.status(201);
            return nueva;
        }, json());

        // Middleware: Asegurar que todas las respuestas tengan header JSON
        after((req, res) -> res.type("application/json"));
    }

    /**
     * Utilidad para transformar objetos Java a JSON usando Gson.
     * @return ResponseTransformer funcional.
     */
    private static ResponseTransformer json() {
        return model -> gson.toJson(model);
    }

    /** Clase auxiliar para estructurar errores en JSON. */
    static class ErrorMsg {
        String error;
        String detalle;
        ErrorMsg(String e, String d) { error=e; detalle=d;}
    }
}