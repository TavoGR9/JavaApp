package huellatorniquete;

import com.digitalpersona.uareu.Reader;
import huellatorniquete.models.HuellaResponse;
import huellatorniquete.models.User;
import huellatorniquete.services.ApiService;
import huellatorniquete.databaseMethods.DataInserter;
import huellatorniquete.controllers.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HuellaTorniquete extends Application {

    private static final Logger LOGGER = Logger.getLogger(HuellaTorniquete.class.getName());
    private static String idSucursal = "";
    private List<User> userData = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) throws Exception {
        consumegetDataUser();
        
        java.net.URL url = getClass().getResource("/HuellaTorniquete/views/mainview.fxml");
        FXMLLoader loader = new FXMLLoader(url);
        Parent root = loader.load();
        MainController mc = loader.getController();
        
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/HuellaTorniquete/css/style.css").toExternalForm());
        
        primaryStage.setTitle("Huella Torniquete");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();

        
        userData = DataInserter.geth2InfoUser();

        
        MainController.convertHuellas(userData);
        System.out.println("Lista con fmd desde main: "+userData);
        System.out.println("Tamaño de lista de FMD desde main: "+userData.size());
        

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
                        if (keyValue.length > 1 && "idSucursal".equals(keyValue[0])) {
                            idSucursal = keyValue[1];
                            break;
                        }
                    }
                }
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Error processing arguments", ex);
            }
        }
    }
    
    private void consumegetDataUser(){
       try {
            userData = ApiService.getDataClient(idSucursal);
            if (!userData.isEmpty()) {
                LOGGER.log(Level.INFO, "Data client: {0}", userData);
                System.out.println("tamaño: "+userData.size());
                DataInserter.insertData(userData);
            } else {
                LOGGER.info("No se encontraron huellas");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener las huellas", e);
        } 
    }
    
    public static String getIdSucursal(){
        return idSucursal;
    }

}