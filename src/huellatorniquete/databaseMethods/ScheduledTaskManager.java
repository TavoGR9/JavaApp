
package huellatorniquete.databaseMethods;

import huellatorniquete.h2connection.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author dtide
 */
public class ScheduledTaskManager {
    private static ScheduledExecutorService scheduler; // Referencia al scheduler

    // Iniciar la tarea periódica
    public static void iniciarActualizacionPeriodica() {
        if (scheduler == null || scheduler.isShutdown()) {
            scheduler = Executors.newScheduledThreadPool(1);  // Inicia un nuevo scheduler
            long intervaloMinutos = 2; // Ejecutar cada 1 minuto
            long intervaloEnMilisegundos = TimeUnit.MINUTES.toMillis(intervaloMinutos);
            //long intervaloHoras = 2; // Ejecutar cada 2 horas
            //long intervaloEnMilisegundos = TimeUnit.HOURS.toMillis(intervaloHoras);

            // Programa la tarea para que se ejecute cada 1 minuto
            scheduler.scheduleAtFixedRate(() -> {
                System.out.println("Actualizando estatus de todos los registros a 0...");
                actualizarEstatusTodosLosRegistros(); // Llamada al método de actualización
            }, intervaloEnMilisegundos, intervaloEnMilisegundos, TimeUnit.MILLISECONDS);
        }
    }

    // Detener la tarea programada
    public static void detenerActualizacion() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown(); // Detiene el scheduler
            System.out.println("Tareas programadas detenidas.");
        }
    }

    // Método que actualiza la columna ESTATUSQR para todos los registros a 0
    private static void actualizarEstatusTodosLosRegistros() {
        String query = "UPDATE DATACLIENT SET ESTATUSQR = 0"; // Pone ESTATUSQR en 0 para todos los registros

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            int rowsAffected = stmt.executeUpdate(); // Ejecuta la actualización
            System.out.println(rowsAffected + " filas actualizadas.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
