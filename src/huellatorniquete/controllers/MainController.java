package huellatorniquete.controllers;

import huellatorniquete.models.HuellaResponse;
import huellatorniquete.models.User;
import huellatorniquete.services.ApiService;
import huellatorniquete.databaseMethods.DataInserter;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class MainController {
    @FXML
    private Label labelMotivacion;
    @FXML
    private Button botonClick;
    String frase = "";
    
    @FXML
    private TextField buscarTextField;

    @FXML
    private void initialize() {
        
         // Restricción para que el TextField solo acepte números
    buscarTextField.textProperty().addListener((observable, oldValue, newValue) -> {
        if (!newValue.matches("\\d*")) { // Solo permite dígitos (números)
            buscarTextField.setText(newValue.replaceAll("[^\\d]", ""));
        }
    });
        
        //botonClick.setOnAction(event -> onBotonClick());
        getFrase();
        labelMotivacion.setText(frase);
        
        
        //Se traen los registros de la base de datos local h2
        
        List<User> userData = DataInserter.geth2InfoUser();
        System.out.println("LISTAAAAAAA: "+userData);
    }

    private void onBotonClick() {
        //labelSaludo.setText("¡Has hecho clic en el botón!");
    }
    
    private void getFrase(){
        try {
            frase = ApiService.getFrases();
            if (!frase.isBlank()) {
                System.out.println("Frase: " +frase);
            } else {
                System.out.println("No se encontro frase");
            }
        } catch (Exception e) {
            System.out.println("Error al obtener la frase");
            //labelSaludo.setText("Error al obtener las huellas.");
        }
    }
   
       
}