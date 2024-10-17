package huellatorniquete.controllers;

import huellatorniquete.models.HuellaResponse;
import huellatorniquete.services.ApiService;
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