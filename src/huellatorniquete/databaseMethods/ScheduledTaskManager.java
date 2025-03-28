package huellatorniquete.databaseMethods;

import huellatorniquete.h2connection.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author wendo
 */
public class ScheduledTaskManager {
    private static ScheduledExecutorService scheduler;

    // Iniciar la tarea programada para ejecutarse a las 12:00 AM todos los días
    public static void iniciarEliminacionDiaria() {
        if (scheduler == null || scheduler.isShutdown()) {
            scheduler = Executors.newScheduledThreadPool(1); 

            long delayInicial = calcularTiempoHastaMedianoche(); // Tiempo hasta las 12:00 AM
            long intervaloDiario = TimeUnit.DAYS.toMillis(1); // Ejecutar cada 24 horas

            scheduler.scheduleAtFixedRate(() -> {
                System.out.println("Eliminando todos los registros de la tabla...");
                eliminarTodosLosRegistros();
            }, delayInicial, intervaloDiario, TimeUnit.MILLISECONDS);
        }
    }

    // Detener la tarea programada
    public static void detenerTareaProgramada() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            System.out.println("Tareas programadas detenidas.");
        }
    }

    // Método para eliminar todos los registros de la tabla
    private static void eliminarTodosLosRegistros() {
        String query = "DELETE FROM ASISTENCIA"; // Elimina todos los registros

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            int rowsDeleted = stmt.executeUpdate();
            System.out.println(rowsDeleted + " registros eliminados.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Calcular el tiempo en milisegundos hasta la próxima medianoche
    private static long calcularTiempoHastaMedianoche() {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime medianoche = ahora.plusDays(1).truncatedTo(ChronoUnit.DAYS);
        return ChronoUnit.MILLIS.between(ahora, medianoche);
    }
}
