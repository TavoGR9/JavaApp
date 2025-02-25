package huellatorniquete.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import huellatorniquete.models.HuellaResponse;
import huellatorniquete.models.User;
import java.io.IOException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import javafx.concurrent.Task;

public class ApiService {
    
    //private static final String API_URL = "https://olympus.arvispace.com/";
    private static final String API_URL = "http://localhost/serviciosGym/";

    //este metodo retorna una frase diferente
    public static String getFrases() {
        try {
            // Crear el cliente HTTP
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + "frases.php"))
                    //.uri(URI.create(API_URL + "olimpusGym/conf/frases.php"))
                    .build();

            // Hacer la solicitud y obtener la respuesta
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Convertir el cuerpo de la respuesta (JSON) en una lista de frases
                ObjectMapper objectMapper = new ObjectMapper();
                List<String> frases = objectMapper.readValue(response.body(), new TypeReference<List<String>>() {});
                
                Random random = new Random();
                int aleatorio = random.nextInt(frases.size()); 
                
                
                return frases.get(aleatorio);   
            } else {
                System.out.println("Error en la respuesta: " + response.statusCode());
                return "Error" ;// Retorna una lista vacía si el código de estado no es 200
            }
        } catch (IOException | InterruptedException e) {
            return "Error catch";  // Retorna una lista vacía en caso de error
        }
    }
    
    public static List<User> getDataClient(String IdSucursal) {
    try {
        // Crear el cliente HTTP
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                //.uri(URI.create(API_URL + "olimpusGym/conf/Usuario.php?getusersbygym=" + IdSucursal))
                .uri(URI.create(API_URL + "Usuario.php?getusersbygym=" + IdSucursal))
                .build();

        System.out.println("URL SERVICE: " + request);

        // Hacer la solicitud y obtener la respuesta
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Verificar si la respuesta fue exitosa (código 200)
        if (response.statusCode() == 200) {
            // Deserializar el JSON en una lista de objetos User
            ObjectMapper objectMapper = new ObjectMapper();
            List<User> userList;

            // Manejar tanto una lista como un único objeto JSON
            String responseBody = response.body();
            if (responseBody.startsWith("[")) {
                // Es una lista
                userList = objectMapper.readValue(responseBody, new TypeReference<List<User>>() {});
            } else {
                // Es un solo objeto
                User singleUser = objectMapper.readValue(responseBody, User.class);
                userList = List.of(singleUser); // Convertirlo en una lista con un solo elemento
            }

            // Serializar la lista de objetos de vuelta a JSON (para depuración)
            String jsonResult = objectMapper.writeValueAsString(userList);
            System.out.println("Lista Users a JSON: " + jsonResult);

            return userList;
        } else {
            System.out.println("Error en la respuesta: " + response.statusCode());
            return List.of(); // Retornar una lista vacía en caso de error
        }
    } catch (IOException | InterruptedException e) {
        System.out.println("Error durante la solicitud: " + e.getMessage());
        return List.of(); // Retornar una lista vacía en caso de excepción
    }
}
    
    
    public static CompletableFuture<Boolean> InsertarAsistencia(String estafeta, String idGym) {
    return CompletableFuture.supplyAsync(() -> {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                  
                //.uri(URI.create(API_URL + "olimpusGym/conf/huella.php?obtenerIdClienteHuella=" + estafeta + "&idGymnasio=" + idGym))
                .uri(URI.create(API_URL + "huella.php?obtenerIdClienteHuella=" + estafeta + "&idGymnasio=" + idGym))
                .build();
            
            System.out.println("URL SERVICE: " + request);
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                System.out.println("solicitud correcta: " + response.body());
                return true;
            }
            return false;
        } catch (IOException | InterruptedException e) {
            System.out.println("Error durante la solicitud: " + e.getMessage());
            return false;
        }
    });
}


}
