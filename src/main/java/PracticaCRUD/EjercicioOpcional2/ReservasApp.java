package PracticaCRUD.EjercicioOpcional2;

import com.google.gson.Gson;
import spark.ResponseTransformer;
import java.time.LocalTime;
import java.util.*;
import static spark.Spark.*;

// Modelo interno
class Reserva {
    int id;
    String recurso;
    String fecha; // "2025-12-01"
    String horaInicio; // "10:00"
    String horaFin;   // "12:00"
    String nombreUsuario;
}

public class ReservasApp {
    private static List<Reserva> reservas = new ArrayList<>();
    private static int idCounter = 1;
    private static Gson gson = new Gson();

    public static void main(String[] args) {
        port(4567);

        post("/reservas", (req, res) -> {
            Reserva nueva = gson.fromJson(req.body(), Reserva.class);
            
            // Validación de conflictos
            boolean conflicto = reservas.stream().anyMatch(existente -> {
                // Misma fecha y mismo recurso
                if (existente.recurso.equals(nueva.recurso) && existente.fecha.equals(nueva.fecha)) {
                    // Lógica de solapamiento de horas
                    LocalTime newStart = LocalTime.parse(nueva.horaInicio);
                    LocalTime newEnd = LocalTime.parse(nueva.horaFin);
                    LocalTime exStart = LocalTime.parse(existente.horaInicio);
                    LocalTime exEnd = LocalTime.parse(existente.horaFin);

                    // (Nueva empieza antes de que termine existente) Y (Nueva termina después de que empiece existente)
                    return newStart.isBefore(exEnd) && newEnd.isAfter(exStart);
                }
                return false;
            });

            if (conflicto) {
                res.status(409); // Conflicto [cite: 836]
                return new ErrorMsg("Conflicto de horario", "La sala ya está ocupada en ese rango");
            }

            nueva.id = idCounter++;
            reservas.add(nueva);
            res.status(201);
            return nueva;
        }, json());
        
        // Middleware para JSON header
        after((req, res) -> res.type("application/json"));
    }

    // Utilidad para transformar a JSON
    private static ResponseTransformer json() {
        return model -> gson.toJson(model);
    }
    
    static class ErrorMsg {
        String error; String detalle;
        ErrorMsg(String e, String d) { error=e; detalle=d;}
    }
}