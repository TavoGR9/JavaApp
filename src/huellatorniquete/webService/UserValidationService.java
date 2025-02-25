
    package huellatorniquete.webService;

    
    import static spark.Spark.*;
    import com.google.gson.Gson;
    import huellatorniquete.controllers.MainController;
    import huellatorniquete.models.User;
    import huellatorniquete.databaseMethods.DataInserter;
    import java.util.ArrayList;
    import java.util.List;
    /**
     *
     * @author AGtaB
     */
    public class UserValidationService {
                
        
        
        
        
    public static void main(String[] args) {
        // Inicializar datos de usuarios
        MainController mainController = new MainController();
        
        // Configurar puerto para el servicio
        port(4567);
        // Endpoint para validar usuario por ID
       
        post("/processUser/:id", (req, res) -> {
            String id = req.params(":id");
            // Validar que el ID no sea nulo
            if (id == null || id.isEmpty()) {
                res.status(400);
                return "{\"status\": \"ID inválido\"}";
            }
            // Llamar al método processUserById del MainController
            boolean result = mainController.processUserById(id);
            if (result) {
                res.status(200);
                return "{\"status\": \"Usuario procesado correctamente\"}";
            } else {
                res.status(404);
                return "{\"status\": \"Usuario no encontrado\"}";
            }
        });
    }
    
    
   
}
