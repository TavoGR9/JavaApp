package huellatorniquete.databaseMethods;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import huellatorniquete.h2connection.DatabaseConnection;

public class TableCreator {
    public static void createTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS DATACLIENT ("
            + "Clave VARCHAR(50) PRIMARY KEY, "
            + "NombreCompleto VARCHAR(255), "
            + "Estafeta VARCHAR(50), "
            + "IdBodega VARCHAR(50), "
            + "FechaInicio DATE, "
            + "FechaFin DATE, "
            + "Estatus VARCHAR(50), "
            + "Titulo VARCHAR(100), "
            + "Duracion INT, "
            + "Precio INT, "
            + "Huella LONGTEXT"
            + ");";
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(createTableSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static void createTable2(){
        String createTableSQL = "CREATE TABLE IF NOT EXISTS ASISTENCIA ("
                + "Clave VARCHAR(50) PRIMARY KEY, "
                + "Fechainicio DATE, "
                + "Fechafin DATE,"
                + "Duracion INT, "
                + "Estafeta VARCHAR(50),"
                + "Estado VARCHAR(10)"
                +");";
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(createTableSQL);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}