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
        String insertSQL = "INSERT INTO DATACLIENT ("
            + "ID, Nombre, Telefono, Email, Sucursal, Membresia, Info_Membresia, Duracion, Precio, Fecha_Inicio, Fecha_Fin, Status, IdDetMem, Membresia_idMem, Gimnasio_idGimnasio, Accion, Foto, Huella, Status_Num, EstatusPago, Rol, Estafeta, Servicio, FechaRegistro"
            + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {    
            for (User user : userData) {
                if (user != null) {
                    preparedStatement.setInt(1, user.getId());
                    preparedStatement.setString(2, user.getNombre() != null ? user.getNombre() : "");
                    preparedStatement.setString(3, user.getTelefono() != null ? user.getTelefono() : "");
                    preparedStatement.setString(4, user.getEmail() != null ? user.getEmail() : "");
                    preparedStatement.setString(5, user.getSucursal() != null ? user.getSucursal() : "");
                    preparedStatement.setString(6, user.getMembresia() != null ? user.getMembresia() : "");
                    preparedStatement.setString(7, user.getInfoMembresia() != null ? user.getInfoMembresia() : "");
                    preparedStatement.setInt(8, user.getDuracion() != null ? user.getDuracion() : 0);
                    preparedStatement.setInt(9, user.getPrecio() != null ? user.getPrecio() : 0);  // Precio agregado

                    if (user.getFechaInicio() != null && !user.getFechaInicio().isEmpty()) {
                        preparedStatement.setDate(10, java.sql.Date.valueOf(user.getFechaInicio()));
                    } else {
                        preparedStatement.setNull(10, java.sql.Types.DATE);
                    }

                    if (user.getFechaFin() != null && !user.getFechaFin().isEmpty()) {
                        preparedStatement.setDate(11, java.sql.Date.valueOf(user.getFechaFin()));
                    } else {
                        preparedStatement.setNull(11, java.sql.Types.DATE);
                    }

                    preparedStatement.setString(12, user.getStatus() != null ? user.getStatus() : "");
                    preparedStatement.setInt(13, user.getIdDetMem() != null ? user.getIdDetMem() : 0);
                    preparedStatement.setInt(14, user.getMembresiaIdMem() != null ? user.getMembresiaIdMem() : 0);
                    preparedStatement.setInt(15, user.getGimnasioIdGimnasio() != null ? user.getGimnasioIdGimnasio() : 0);
                    preparedStatement.setString(16, user.getAccion() != null ? user.getAccion() : "");
                    preparedStatement.setString(17, user.getFoto() != null ? user.getFoto() : "");
                    preparedStatement.setString(18, user.getHuella() != null ? user.getHuella() : "");
                    preparedStatement.setInt(19, user.getStatusNum() != null ? user.getStatusNum() : 0);
                    preparedStatement.setString(20, user.getEstatusPago() != null ? user.getEstatusPago() : "");
                    preparedStatement.setString(21, user.getRol() != null ? user.getRol() : "");
                    preparedStatement.setString(22, user.getEstafeta() != null ? user.getEstafeta() : "");
                    preparedStatement.setString(23, user.getServicio() != null ? user.getServicio() : "");

                    if (user.getFechaRegistro() != null && !user.getFechaRegistro().isEmpty()) {
                        preparedStatement.setTimestamp(24, Timestamp.valueOf(user.getFechaRegistro()));
                    } else {
                        preparedStatement.setNull(24, java.sql.Types.TIMESTAMP);
                    }

                    preparedStatement.addBatch();
                }
            }
            preparedStatement.executeBatch();
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
