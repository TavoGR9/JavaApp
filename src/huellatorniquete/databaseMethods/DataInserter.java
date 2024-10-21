package huellatorniquete.databaseMethods;

import huellatorniquete.h2connection.DatabaseConnection;
import huellatorniquete.models.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.sql.ResultSet;


/**
 *
 * @author guzma
 */
public class DataInserter {
    public static void insertData(List<User> userData) {
        String deleteSQL = "DELETE FROM DATACLIENT";  // SQL para eliminar todos los registros
        String insertSQL = "INSERT INTO DATACLIENT ("
            + "ID, Nombre, Telefono, Email, Sucursal, Membresia, Info_Membresia, Duracion, Precio, Fecha_Inicio, Fecha_Fin, Status, IdDetMem, Membresia_idMem, Gimnasio_idGimnasio, Accion, Foto, Huella, Status_Num, EstatusPago, Rol, Estafeta, Servicio, FechaRegistro"
            + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement deleteStatement = connection.prepareStatement(deleteSQL);
             PreparedStatement insertStatement = connection.prepareStatement(insertSQL)) {

            // 1. Eliminar todos los registros actuales en la tabla
            deleteStatement.executeUpdate();

            // 2. Insertar los nuevos datos
            for (User user : userData) {
                if (user != null) {
                    insertStatement.setInt(1, user.getId());
                    insertStatement.setString(2, user.getNombre() != null ? user.getNombre() : "");
                    insertStatement.setString(3, user.getTelefono() != null ? user.getTelefono() : "");
                    insertStatement.setString(4, user.getEmail() != null ? user.getEmail() : "");
                    insertStatement.setString(5, user.getSucursal() != null ? user.getSucursal() : "");
                    insertStatement.setString(6, user.getMembresia() != null ? user.getMembresia() : "");
                    insertStatement.setString(7, user.getInfoMembresia() != null ? user.getInfoMembresia() : "");
                    insertStatement.setInt(8, user.getDuracion() != null ? user.getDuracion() : 0);
                    insertStatement.setInt(9, user.getPrecio() != null ? user.getPrecio() : 0);  // Precio agregado

                    if (user.getFechaInicio() != null && !user.getFechaInicio().isEmpty()) {
                        insertStatement.setDate(10, java.sql.Date.valueOf(user.getFechaInicio()));
                    } else {
                        insertStatement.setNull(10, java.sql.Types.DATE);
                    }

                    if (user.getFechaFin() != null && !user.getFechaFin().isEmpty()) {
                        insertStatement.setDate(11, java.sql.Date.valueOf(user.getFechaFin()));
                    } else {
                        insertStatement.setNull(11, java.sql.Types.DATE);
                    }

                    insertStatement.setString(12, user.getStatus() != null ? user.getStatus() : "");
                    insertStatement.setInt(13, user.getIdDetMem() != null ? user.getIdDetMem() : 0);
                    insertStatement.setInt(14, user.getMembresiaIdMem() != null ? user.getMembresiaIdMem() : 0);
                    insertStatement.setInt(15, user.getGimnasioIdGimnasio() != null ? user.getGimnasioIdGimnasio() : 0);
                    insertStatement.setString(16, user.getAccion() != null ? user.getAccion() : "");
                    insertStatement.setString(17, user.getFoto() != null ? user.getFoto() : "");
                    insertStatement.setString(18, user.getHuella() != null ? user.getHuella() : "");
                    insertStatement.setInt(19, user.getStatusNum() != null ? user.getStatusNum() : 0);
                    insertStatement.setString(20, user.getEstatusPago() != null ? user.getEstatusPago() : "");
                    insertStatement.setString(21, user.getRol() != null ? user.getRol() : "");
                    insertStatement.setString(22, user.getEstafeta() != null ? user.getEstafeta() : "");
                    insertStatement.setString(23, user.getServicio() != null ? user.getServicio() : "");

                    if (user.getFechaRegistro() != null && !user.getFechaRegistro().isEmpty()) {
                        insertStatement.setTimestamp(24, Timestamp.valueOf(user.getFechaRegistro()));
                    } else {
                        insertStatement.setNull(24, java.sql.Types.TIMESTAMP);
                    }

                    insertStatement.addBatch();
                }
            }

            // Ejecutar el batch para insertar los nuevos registros
            insertStatement.executeBatch();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Método para recuperar los datos
    public static List<User> geth2InfoUser() {
    List<User> userList = new ArrayList<>();
    String selectSQL = "SELECT * FROM DATACLIENT";

    try (Connection connection = DatabaseConnection.getConnection();
         PreparedStatement preparedStatement = connection.prepareStatement(selectSQL);
         ResultSet resultSet = preparedStatement.executeQuery()) {
        
        while (resultSet.next()) {
            User user = new User();
            user.setID(String.valueOf(resultSet.getInt("ID")));
            user.setNombre(resultSet.getString("Nombre"));
            user.setTelefono(resultSet.getString("Telefono"));
            user.setEmail(resultSet.getString("Email"));
            user.setSucursal(resultSet.getString("Sucursal"));
            user.setMembresia(resultSet.getString("Membresia"));
            user.setInfoMembresia(resultSet.getString("Info_Membresia"));
            user.setDuracion(String.valueOf(resultSet.getInt("Duracion")));
            user.setPrecio(resultSet.getBigDecimal("Precio") != null ? resultSet.getBigDecimal("Precio").toString() : null);
            
            java.sql.Date fechaInicio = resultSet.getDate("Fecha_Inicio");
            user.setFechaInicio(fechaInicio != null ? fechaInicio.toString() : null);
            
            java.sql.Date fechaFin = resultSet.getDate("Fecha_Fin");
            user.setFechaFin(fechaFin != null ? fechaFin.toString() : null);
            
            user.setSTATUS(resultSet.getString("Status"));
            user.setIdDetMem(String.valueOf(resultSet.getInt("IdDetMem")));
            user.setMembresiaIdMem(String.valueOf(resultSet.getInt("Membresia_idMem")));
            user.setGimnasioIdGimnasio(String.valueOf(resultSet.getInt("Gimnasio_idGimnasio")));
            user.setAccion(resultSet.getString("Accion"));
            user.setFoto(resultSet.getString("Foto"));
            user.setHuella(resultSet.getString("Huella"));
            user.setStatusNum(String.valueOf(resultSet.getInt("Status_Num")));
            user.setEstatusPago(resultSet.getString("EstatusPago"));
            user.setRol(resultSet.getString("Rol"));
            user.setEstafeta(resultSet.getString("Estafeta"));
            user.setServicio(resultSet.getString("Servicio"));
            
            Timestamp fechaRegistro = resultSet.getTimestamp("FechaRegistro");
            user.setFechaRegistro(fechaRegistro != null ? fechaRegistro.toString() : null);

            userList.add(user);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    
    return userList;
}
}
