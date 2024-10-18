
package huellatorniquete.databaseMethods;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import huellatorniquete.h2connection.DatabaseConnection;
/**
 *
 * @author guzma
 */
public class TableCreator {
    public static void createTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS DATACLIENT ("
            + "ID INT PRIMARY KEY, "
            + "Nombre VARCHAR(255), "
            + "Telefono VARCHAR(20), "
            + "Email VARCHAR(255), "
            + "Sucursal VARCHAR(255), "
            + "Membresia VARCHAR(255), "
            + "Info_Membresia VARCHAR(255), "
            + "Duracion INT, "
            + "Precio DECIMAL(10, 2), "
            + "Fecha_Inicio DATE, "
            + "Fecha_Fin DATE, "
            + "Status VARCHAR(50), "
            + "IdDetMem INT, "
            + "Membresia_idMem INT, "
            + "Gimnasio_idGimnasio INT, "
            + "Accion VARCHAR(50), "
            + "Foto BLOB, "
            + "Huella BLOB, "
            + "Status_Num INT, "
            + "EstatusPago VARCHAR(50), "
            + "Rol VARCHAR(50), "
            + "Estafeta VARCHAR(50), "
            + "Servicio VARCHAR(255), "
            + "FechaRegistro TIMESTAMP"
            + ");";

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(createTableSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
