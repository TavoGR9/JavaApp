package huellatorniquete.databaseMethods;

import huellatorniquete.h2connection.DatabaseConnection;
import huellatorniquete.models.User;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.sql.ResultSet;
import javafx.application.Platform;
import java.sql.Statement;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DataInserter {
    public static void insertData(List<User> userData) {
        String deleteSQL = "DELETE FROM DATACLIENT";
        String insertSQL = "INSERT INTO DATACLIENT ("
            + "Clave, NombreCompleto, Estafeta, IdBodega, FechaInicio, FechaFin, Estatus, Titulo, Duracion, Precio, estatusQR, Huella"
            + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement deleteStatement = connection.prepareStatement(deleteSQL);
             PreparedStatement insertStatement = connection.prepareStatement(insertSQL)) {

            // 1. Eliminar todos los registros actuales en la tabla
            deleteStatement.executeUpdate();
            connection.commit();  // Confirma la eliminación

            // 2. Insertar los nuevos datos
            for (User user : userData) {
                if (user != null) {
                    insertStatement.setString(1, user.getClave());
                    insertStatement.setString(2, user.getNombreCompleto());
                    insertStatement.setString(3, user.getEstafeta());
                    insertStatement.setString(4, user.getIdBodega());

                    if (user.getFechaInicio() != null && !user.getFechaInicio().isEmpty()) {
                        insertStatement.setDate(5, java.sql.Date.valueOf(user.getFechaInicio()));
                    } else {
                        insertStatement.setNull(5, java.sql.Types.DATE);
                    }

                    if (user.getFechaFin() != null && !user.getFechaFin().isEmpty()) {
                        insertStatement.setDate(6, java.sql.Date.valueOf(user.getFechaFin()));
                    } else {
                        insertStatement.setNull(6, java.sql.Types.DATE);
                    }

                    insertStatement.setString(7, user.getEstatus());
                    insertStatement.setString(8, user.getTitulo());
                    insertStatement.setInt(9, user.getDuracion() != null ? user.getDuracion() : 0);
                    insertStatement.setInt(10, user.getPrecio() != null ? user.getPrecio() : 0);
                    insertStatement.setInt(11, user.getEstatusQR() != null ? user.getEstatusQR() : 0);
                    insertStatement.setString(12, user.getHuella());

                    insertStatement.addBatch();
                }
            }

            // Ejecutar el batch para insertar los nuevos registros
            insertStatement.executeBatch();
            connection.commit();  // Confirmar inserciones
        
        System.out.println("✅ Datos insertados correctamente.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Método para recuperar los datos
    public static List<User> geth2InfoUser() {
    List<User> userList = new ArrayList<>();
    String selectSQL = "SELECT * FROM DATACLIENT";

    try (Connection connection = DatabaseConnection.getConnection();
         Statement statement = connection.createStatement();  // Asegúrate de usar Statement en lugar de PreparedStatement
         ResultSet resultSet = statement.executeQuery(selectSQL)) { // Ejecutar la consulta

        while (resultSet.next()) {
            User user = new User();
            user.setClave(resultSet.getString("Clave"));
            user.setNombreCompleto(resultSet.getString("NombreCompleto"));
            user.setEstafeta(resultSet.getString("Estafeta"));
            user.setIdBodega(resultSet.getString("IdBodega"));

            java.sql.Date fechaInicio = resultSet.getDate("FechaInicio");
            user.setFechaInicio(fechaInicio != null ? fechaInicio.toString() : null);

            java.sql.Date fechaFin = resultSet.getDate("FechaFin");
            user.setFechaFin(fechaFin != null ? fechaFin.toString() : null);

            user.setEstatus(resultSet.getString("Estatus"));
            user.setTitulo(resultSet.getString("Titulo"));
            user.setDuracion(String.valueOf(resultSet.getInt("Duracion")));
            user.setPrecio(String.valueOf(resultSet.getInt("Precio")));
            user.setHuella(resultSet.getString("Huella"));
            user.setEstatusQR(String.valueOf(resultSet.getInt("EstatusQR")));
            

            userList.add(user);
        }
    } catch (SQLException e) {
        e.printStackTrace();
        System.out.println("❌ Error al obtener datos de H2: " + e.getMessage());
    }

    return userList;
}
    
    //Metodo de eliminacion de datos 
    public void deleteAllUsersFromH2() {
        String deleteSQL = "DELETE FROM DATACLIENT";

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {
            int rowsDeleted = statement.executeUpdate(deleteSQL);
            System.out.println("Usuarios eliminados en H2: " + rowsDeleted);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error al eliminar usuarios en H2: " + e.getMessage());
        }
    }
    
    
    //INSERTAR ASISTENCIAS VISITAS
    public static void insertarAsistencia(String clave, String fechaInicio, String fechaFin, int duracion, String estafeta, String estado) {
        String sql ="INSERT INTO ASISTENCIA (CLAVE, FECHAINICIO, FECHAFIN, DURACION, ESTAFETA, ESTADO) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection connection = DatabaseConnection.getConnection();
         PreparedStatement pstmt = connection.prepareStatement(sql)) {

        pstmt.setString(1, clave);
        // Convertir String a java.sql.Date
        if (fechaInicio != null && !fechaInicio.isEmpty()) {
            pstmt.setDate(2, java.sql.Date.valueOf(fechaInicio));
        } else {
            pstmt.setNull(2, java.sql.Types.DATE);
        }

        if (fechaFin != null && !fechaFin.isEmpty()) {
            pstmt.setDate(3, java.sql.Date.valueOf(fechaFin));
        } else {
            pstmt.setNull(3, java.sql.Types.DATE);
        }
        pstmt.setInt(4, duracion);
        pstmt.setString(5, estafeta);
        pstmt.setString(6, estado);

        pstmt.executeUpdate();
        System.out.println("Datos insertados correctamente en H2.");
    } catch (SQLException e) {
        e.printStackTrace();
        System.out.println("Error al insertar datos en H2.");
    }
    }
    
    
    public static boolean checkAsistenciaExistente(String estafeta, int duracion) {
    // Realiza la consulta a la base de datos o la tabla de asistencia
    String query = "SELECT COUNT(*) FROM ASISTENCIA WHERE ESTAFETA = ? AND DURACION = ?";
    try (Connection connection = DatabaseConnection.getConnection();
            PreparedStatement stmt = connection.prepareStatement(query)) {
        stmt.setString(1, estafeta);
        stmt.setInt(2, duracion);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getInt(1) > 0; // Si existe, devuelve true
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return false; // No existe
}
    
    
    ///CAMBIAR ESTATUS
    public static boolean cambiarEsatusQR(String estafeta) {
    String query = "UPDATE DATACLIENT SET ESTATUSQR = CASE " +
                   "WHEN ESTATUSQR = 0 THEN 1 " +
                   "WHEN ESTATUSQR = 1 THEN 0 " +
                   "ELSE ESTATUSQR END " +
                   "WHERE ESTAFETA = ?";
    try (Connection connection = DatabaseConnection.getConnection();
         PreparedStatement stmt = connection.prepareStatement(query)) {

        stmt.setString(1, estafeta);
        int rowsAffected = stmt.executeUpdate(); 

        return rowsAffected > 0; 
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return false; 
}
    
    
    //Obtener estatus
    public static int obtenerEstatusQR(String estafeta) {
    String query = "SELECT ESTATUSQR FROM DATACLIENT WHERE ESTAFETA = ?";
    try (Connection connection = DatabaseConnection.getConnection();
         PreparedStatement stmt = connection.prepareStatement(query)) {
        
        stmt.setString(1, estafeta);
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("ESTATUSQR"); // Devuelve el estatus actual
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return -1; 
}
    
    
    ///CAMBIAR SALIDA
    ///
    public static boolean cambiarSalida(String estafeta) {
    String query = "UPDATE DATACLIENT SET SALIDA = CASE " +
                   "WHEN SALIDA = 0 THEN 1 " +
                   "WHEN SALIDA = 1 THEN 0 " +
                   "ELSE SALIDA END " +
                   "WHERE ESTAFETA = ?";
    try (Connection connection = DatabaseConnection.getConnection();
         PreparedStatement stmt = connection.prepareStatement(query)) {

        stmt.setString(1, estafeta);
        int rowsAffected = stmt.executeUpdate(); 

        return rowsAffected > 0; 
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return false; 
}
    
    
    //Obtener SALIDA
    public static Integer obtenerSalida(String estafeta) {
    String query = "SELECT SALIDA FROM DATACLIENT WHERE ESTAFETA = ?";
    try (Connection connection = DatabaseConnection.getConnection();
         PreparedStatement stmt = connection.prepareStatement(query)) {
        
        stmt.setString(1, estafeta);
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("SALIDA"); // Devuelve el estatus actual
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return -1; 
}
    
 


   
}


