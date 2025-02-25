package huellatorniquete;

import com.digitalpersona.uareu.Reader;
import huellatorniquete.models.HuellaResponse;
import huellatorniquete.models.User;
import huellatorniquete.services.ApiService;
import huellatorniquete.databaseMethods.DataInserter;
import huellatorniquete.controllers.MainController;
import huellatorniquete.webService.UserValidationService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Window;

public class HuellaTorniquete extends Application {

    private static final Logger LOGGER = Logger.getLogger(HuellaTorniquete.class.getName());
    private static String id = "1";
    private ObservableList<User> userData = FXCollections.observableArrayList();

    @Override
    /*public void start(Stage primaryStage) throws Exception {
        consumegetDataUser();
        
        java.net.URL url = getClass().getResource("views/mainview.fxml");
        FXMLLoader loader = new FXMLLoader(url);
        Parent root = loader.load();
        MainController mc = loader.getController();
        
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("css/style.css").toExternalForm());
        
        primaryStage.setTitle("Huella Torniquete");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();

        
        userData = DataInserter.geth2InfoUser();

        
        MainController.convertHuellas(userData);
        System.out.println("Lista con fmd desde main: "+userData);
        System.out.println("Tamaño de lista de FMD desde main: "+userData.size());
        

        mc.compareFingerprint(userData);
    }*/
    
    
    
    
    public void start(Stage primaryStage) throws Exception {
    // Inicia el servicio web en un nuevo hilo
    /*new Thread(() -> {
        UserValidationService.main(new String[0]);
    }).start();*/

    // Resto de la lógica de tu aplicación
    consumegetDataUser();
    java.net.URL url = getClass().getResource("views/mainview.fxml");
    FXMLLoader loader = new FXMLLoader(url);
    Parent root = loader.load();
    MainController mc = loader.getController();

    Scene scene = new Scene(root);
    scene.getStylesheets().add(getClass().getResource("css/style.css").toExternalForm());

    primaryStage.setTitle("Huella Torniquete");
    primaryStage.setScene(scene);
    primaryStage.setMaximized(true);
    primaryStage.show();

    
    userData.setAll(DataInserter.geth2InfoUser());
    MainController.convertHuellas(userData);
    mc.compareFingerprint(userData);

}
   


    public static void main(String[] args) {
        processArgs(args);
        launch(args);
    }

    private static void processArgs(String[] args) {
        if (args.length > 0) {
            try {
                URI uri = new URI(args[0]);
                String query = uri.getQuery();
                if (query != null) {
                    for (String param : query.split("&")) {
                        String[] keyValue = param.split("=");
                        if (keyValue.length > 1 && "id".equals(keyValue[0])) {
                            id = keyValue[1];
                            break;
                        }
                    }
                }
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Error processing arguments", ex);
            }
        }
    }
    
private void consumegetDataUser() {
    try {
        // Obtener datos desde la API
        ObservableList<User> apiData = FXCollections.observableArrayList(ApiService.getDataClient(id));

        if (!apiData.isEmpty()) {
            LOGGER.log(Level.INFO, "Datos obtenidos desde API: {0}", apiData);
            System.out.println("Usuarios obtenidos desde API: " + apiData.size());

            // Eliminar duplicados
            ObservableList<User> uniqueUsers = FXCollections.observableArrayList(
                apiData.stream()
                    .collect(Collectors.toMap(
                        User::getClave, 
                        Function.identity(), 
                        (existing, replacement) -> replacement
                    ))
                    .values()
            );

            // Guardar en la base de datos local
            DataInserter.insertData(uniqueUsers);

            // Actualizar userData con los datos únicos
            userData.setAll(uniqueUsers);
        } else {
            LOGGER.info("No se encontraron datos en la API. Eliminando datos en H2...");
            new DataInserter().deleteAllUsersFromH2();  //LLamamos al metodo eliminar
            userData.clear(); // También limpia la lista observable en la UI
        }

    } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Error al obtener datos desde la API", e);
    }
}



    
    public static String getIdSucursal(){
        return id;
    }
    
  

   
    
    

}

