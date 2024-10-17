package huellatorniquete.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import huellatorniquete.models.HuellaResponse;
import java.io.IOException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Random;

public class ApiService {
    private static final String API_URL = "https://olympus.arvispace.com/";

    // Este método devolverá una lista de objetos HuellaResponse
    public static List<HuellaResponse> getHuellas(String idSucursal) {
        try {
            // Crear el cliente HTTP
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + "olimpusGym/conf/huella.php?consultaHuellasVD="+idSucursal))
                    .build();

            // Hacer la solicitud y obtener la respuesta
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Verificar si la respuesta fue exitosa (código 200)
                if (response.statusCode() == 200) {
                // Deserializar el JSON en una lista de objetos HuellaResponse
                ObjectMapper objectMapper = new ObjectMapper();
                List<HuellaResponse> huellasList = objectMapper.readValue(
                        response.body(), 
                        new TypeReference<List<HuellaResponse>>() {}
                );
                
                // Serializar la lista de objetos de vuelta a JSON
                String jsonResult = objectMapper.writeValueAsString(huellasList);
                System.out.println("Lista serializada a JSON: " + jsonResult);

                return huellasList;
            } else {
                System.out.println("Error en la respuesta: " + response.statusCode());
                return List.of(); // Retornar una lista vacía en caso de error
            }
        } catch (IOException | InterruptedException e) {
            return List.of(); // Retornar una lista vacía en caso de excepción
        }
    }
    
    
    //este metodo retorna una frase diferente
    public static String getFrases() {
        try {
            // Crear el cliente HTTP
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + "olimpusGym/conf/frases.php"))
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
}
