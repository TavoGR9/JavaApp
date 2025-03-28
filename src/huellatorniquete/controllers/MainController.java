package huellatorniquete.controllers;

import com.digitalpersona.uareu.Engine;
import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.Reader.ReaderStatus;
import com.digitalpersona.uareu.ReaderCollection;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.UareUGlobal;
import huellatorniquete.models.User;
import huellatorniquete.services.ApiService;
import huellatorniquete.databaseMethods.DataInserter;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import com.fazecast.jSerialComm.*;
import huellatorniquete.HuellaTorniquete;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import static java.lang.Integer.parseInt;
import java.nio.file.Paths;
//import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import javax.swing.SwingWorker;
import static spark.Spark.*;
import com.google.gson.Gson;
import huellatorniquete.databaseMethods.ScheduledTaskManager;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;

//import javafx.scene.Parent;
//import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;



public class MainController {
    
    private ObservableList<User> userData = FXCollections.observableArrayList();
    SerialPort seleccionado = null;  // Inicializamos como null
    HuellaTorniquete ht = new HuellaTorniquete();
        
        private MediaPlayer mediaPlayerSuccess;
        private MediaPlayer mediaPlayerError;
        private String idSucursal = "";
        private boolean serviceStarted = false;
        private Stage stage;  // Atributo para almacenar la referencia del Stage
        private String estafeta;
    
    @FXML
    private Label labelMotivacion;
    @FXML
    private Button botonClick;
    String frase = "";
    
    @FXML
    private TextField buscarTextField;
    
    @FXML
    private Label nameLabel;
   
    @FXML
    private Label branchLabel;
    
    @FXML
    private Label membershipLabel;
    
    @FXML
    private Label durationLabel;
    
    @FXML
    private Label startDateLabel;
    
    @FXML
    private Label endDateLabel;
    
    @FXML
    private Label membershipStatusLabel;
    
    @FXML
    private VBox paneleft;
    
    @FXML
    private ImageView userPhotoImageView;
    

    @FXML
    public void initialize() { 
        
        ScheduledTaskManager.iniciarEliminacionDiaria(); //Eliminación periodica
        
        buscarTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                buscarTextField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        //getReaders();
        getFrase();
        labelMotivacion.setText(frase);
       
        userData.setAll(DataInserter.geth2InfoUser());
        //startWebService();
        
        convertHuellas(userData);
        compareFingerprint(userData);

        
        getPort();
    
        idSucursal = HuellaTorniquete.getIdSucursal();
       // System.out.println("Obtenemos idSucursal: "+idSucursal);
        
        buscarTextField.setOnKeyPressed(event -> {
        if (event.getCode() == KeyCode.ENTER) {
            String numero = buscarTextField.getText();
            processUserById(numero);
        }
    });
        
        
        //MEDIA PLAYER INITIALIZACION
        
        try {
        String successMediaPath = getClass().getResource("/huellatorniquete/sounds/Success.mp3").toExternalForm();
        Media successMedia = new Media(successMediaPath);
        mediaPlayerSuccess = new MediaPlayer(successMedia);
    } catch (Exception e) {
        System.err.println("Error loading success media: " + e.getMessage());
        mediaPlayerSuccess = null;
    }

    // Initialize error media player
    try {
        String errorMediaPath = getClass().getResource("/huellatorniquete/sounds/Error.mp3").toExternalForm();
        Media errorMedia = new Media(errorMediaPath);
        mediaPlayerError = new MediaPlayer(errorMedia);
    } catch (Exception e) {
        System.err.println("Error loading error media: " + e.getMessage());
        mediaPlayerError = null;
    }
    
    
    
        // Usamos runLater para asegurarnos de que el código se ejecute después de la inicialización completa
        Platform.runLater(() -> {
        // Obtener la referencia al Stage (la ventana principal)
        stage = (Stage) paneleft.getScene().getWindow();

        // Agregar manejador para el evento de cierre de la ventana
        stage.setOnCloseRequest((WindowEvent event) -> {
            // Aquí cerramos el servidor que escucha en el puerto 4567
            ScheduledTaskManager.detenerTareaProgramada();
            closePort();
        });
        });
        
        
        
}
    
    
    private void getPort() {
    SerialPort[] ports = SerialPort.getCommPorts();
    seleccionado = null;
    
    for (SerialPort port : ports) {
        System.out.println("names ports: " + port.getDescriptivePortName());
        if (port.getDescriptivePortName().contains("CP210x")) {
            seleccionado = port;
            seleccionado.setBaudRate(115200);
            seleccionado.setNumDataBits(8);
            seleccionado.setNumStopBits(1);
            seleccionado.setParity(SerialPort.NO_PARITY);
            
            if (!seleccionado.openPort()) {
                System.out.println("❌ Error al abrir el puerto");
                return;
            }
            
            seleccionado.flushIOBuffers(); // Vaciar buffers antes de empezar
            
            System.out.println("✅ Puerto seleccionado: " + seleccionado.getSystemPortName());
            
            // Buffer para datos acumulados
            StringBuilder dataBuffer = new StringBuilder();
            String[] tipoAccion = new String[1];
            //StringBuilder idBuffer = new StringBuilder();
            boolean[] esperandoID = {false}; // Estado para saber si esperamos un ID después de "Entrada"
            
            
            seleccionado.addDataListener(new SerialPortDataListener() {
                @Override
                public int getListeningEvents() {
                    return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
                }
                
                @Override
                public void serialEvent(SerialPortEvent event) {
                    byte[] buffer = new byte[seleccionado.bytesAvailable()];
                    int bytesRead = seleccionado.readBytes(buffer, buffer.length);
                    
                   if (bytesRead > 0) {
    try {
        
        String receivedData = new String(buffer, 0, bytesRead, "UTF-8");

        // Ignorar mensajes irrelevantes
        if (receivedData.contains("GM65") || receivedData.equals("?")) {
            return;
        }

        // Acumular datos en el buffer
        dataBuffer.append(receivedData);

        // Verificar si hay líneas completas en el buffer
        int newLineIndex;
        while ((newLineIndex = dataBuffer.indexOf("\n")) != -1) {
            // Extraer la línea completa
            String line = dataBuffer.substring(0, newLineIndex).trim();
            dataBuffer.delete(0, newLineIndex + 1); // Eliminar la línea procesada

            if (line.isEmpty()) continue;

            System.out.println("📩 Dato recibido: " + line);
            
            if(line.equals("QR Caducado")){
                QRcaduco();
                continue;
            }

            if(line.equals("No entro") ||  line.equals("No salio")){
             System.out.println("NO INGRESO EL USUARIO O NO SALIO");
                continue;   
            }else if(line.equals("Entro")){
                Actualizacion(); 
                continue; 
            } else if(line.equals("Salio")){
                ActualizacionSalida();
                continue;
            }
            
            
            if (line.equals("A") || line.equals("S")) {
                System.out.println("🟢 Se detectó '" + line + "', esperando ID...");
                esperandoID[0] = true;
                tipoAccion[0] = line; // Guardar si es "A" o "S"
            } else if (esperandoID[0]) {
                // Se recibió el ID después de "A" o "S"
                String cleanId = cleanAndValidateId(line);
                if (cleanId != null) {
                    System.out.println("🔹 ID limpio: " + cleanId);
                    if ("A".equals(tipoAccion[0])) {
                        processUserQr(cleanId);
                    } else {
                        processUserQrSalida(cleanId);
                    }
                } else {
                    System.out.println("❌ ID inválido recibido: " + line);
                } 
                esperandoID[0] = false; // Reiniciar estado
            }
        }
    } catch (UnsupportedEncodingException e) {
        System.err.println("Error de codificación: " + e.getMessage());
    }
}
                }
            });
            break;
        }
    }
    
    if (seleccionado == null) {
        System.out.println("⚠ No se encontró ningún puerto USB-SERIAL");
    }
}


// Método para cerrar el puerto correctamente cuando sea necesario
public void closePort() {
    if (seleccionado != null && seleccionado.isOpen()) {
        seleccionado.closePort();
        System.out.println("Puerto cerrado correctamente.");
    }
}

private String cleanAndValidateId(String rawId) {
    if (rawId == null) {
        return null;
    }
    
    // Limpieza más agresiva
    String cleaned = rawId.trim()
                        .replaceAll("[^0-9]", "")  // Solo mantiene números
                        .replaceAll("\\p{C}", ""); // Elimina caracteres de control
    
    // Validación de criterios
    if (cleaned.isEmpty() || 
        cleaned.length() < 4 ||    
        cleaned.length() > 20) {   
        System.out.println("❌ ID inválido: " + rawId);
        return null;
    }
    
    // Log para depuración
    System.out.println("🔍 Validación de ID:");
    System.out.println("   Original (hex): " + bytesToHex(rawId.getBytes()));
    System.out.println("   Limpio: '" + cleaned + "'");
    System.out.println("   Longitud: " + cleaned.length());
    
    return cleaned;
}

// Método auxiliar para ver los bytes exactos que se reciben
private static String bytesToHex(byte[] bytes) {
    StringBuilder hex = new StringBuilder();
    for (byte b : bytes) {
        hex.append(String.format("%02X ", b));
    }
    return hex.toString();
}

    private void getFrase(){
        try {
            frase = ApiService.getFrases();
            if (!frase.isBlank()) {
                //System.out.println("Frase obtenida: " + frase);
            } else {
               // System.out.println("No se encontró frase");
            }
        } catch (Exception e) {
            //System.out.println("Error al obtener la frase: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    
    @FXML
    private void searchUser(){
        String numero = buscarTextField.getText();
        processUserById(numero);
    }
    
   
   private static String getLocalIPAddress() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            return inetAddress.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return "Error al obtener la dirección IP";
        }
    }
    
    private void updateUIWithUser(User user) {
    Platform.runLater(() -> {
        if (userPhotoImageView != null) {
                Image image = new Image("/huellatorniquete/images/usuario.jpg");
                userPhotoImageView.setImage(image);
            } else {
                System.err.println("userPhotoImageView no está inicializado");
            }
        nameLabel.setText(user.getNombreCompleto());
        branchLabel.setText(user.getIdBodega());
        membershipLabel.setText(user.getTitulo());
        durationLabel.setText(user.getDuracion().toString());
        startDateLabel.setText(user.getFechaInicio());
        endDateLabel.setText(user.getFechaFin());
        // Resto de la lógica de actualización de UI...
    });
}
    
    
    
    public boolean Actualizacion(){
        ApiService.InsertarAsistencia(estafeta, idSucursal);
        ApiService.CambiarEstatus(estafeta, idSucursal);
        DataInserter.cambiarEsatusQR(estafeta);
        
        System.out.println("SE ACTUALIZO EL USUARIO ");
        return false;
    }
    
    public boolean ActualizacionSalida(){
        ApiService.CambiarEstatus(estafeta, idSucursal);
        DataInserter.cambiarEsatusQR(estafeta);
        
        System.out.println("SE ACTUALIZO EL USUARIO SALIDA ");
        return false;
    }
    
    ///QR INVALIDO O CADUCADO
    
    public boolean QRcaduco(){
        for(User user : userData){
           if (user.getEstafeta().equalsIgnoreCase(estafeta)){
               Image image = new Image("/huellatorniquete/images/usuario.jpg");
               

                   Platform.runLater(() -> {
                       userPhotoImageView.setImage(image);
                       nameLabel.setText(user.getNombreCompleto());
                       branchLabel.setText(user.getIdBodega());
                       membershipLabel.setText(user.getTitulo());
                       durationLabel.setText(user.getDuracion().toString());
                       startDateLabel.setText(user.getFechaInicio());
                       endDateLabel.setText(user.getFechaFin());
                       
                       membershipStatusLabel.setText("EL QR A CADUCADO");
                       paneleft.setStyle("-fx-background-color: red;");
                       
                       if (mediaPlayerError != null) {
                        mediaPlayerError.play();
                        mediaPlayerError.seek(Duration.ZERO);
                       }
                       
                        
                   });
                   return false;
               
               
           }
       } 
        return false;
    }
    
    
    public boolean processUserSalida(String id){
        boolean encontrado = false;
       
       System.out.println("Hola si esta entrando perros");
       System.out.println("El id que pasaron fue: " + id);
       
       if (userData.isEmpty()){
           System.out.println("Por eso no muestra nada");
           userData.setAll(DataInserter.geth2InfoUser());
       }
       // Ahora procesamos los datos independientemente de si estaban vacíos o no
       for(User user : userData){
           if (user.getEstafeta().equalsIgnoreCase(id)){
               Image image = new Image("/huellatorniquete/images/usuario.jpg");
               
               
               int estatusActual = DataInserter.obtenerEstatusQR(user.getEstafeta());
               System.out.println("Estatus actual en BD: " + estatusActual);
               
               
               if (estatusActual == 1) {
                   System.out.println("Usuario en estado de salida, procesando...");

                   Platform.runLater(() -> {
                       userPhotoImageView.setImage(image);
                       nameLabel.setText(user.getNombreCompleto());
                       branchLabel.setText(user.getIdBodega());
                       membershipLabel.setText(user.getTitulo());
                       durationLabel.setText(user.getDuracion().toString());
                       startDateLabel.setText(user.getFechaInicio());
                       endDateLabel.setText(user.getFechaFin());
                       
                       membershipStatusLabel.setText("Salida");
                       paneleft.setStyle("-fx-background-color: #00AAE4;");
                       
                       if(mediaPlayerSuccess != null){
                            mediaPlayerSuccess.play();
                            mediaPlayerSuccess.seek(Duration.ZERO);
                       }
                       
                       CompletableFuture.runAsync(() -> {
                           if(seleccionado != null){
                            enviarEstatus(seleccionado, "OK", "0");
                        }
                        //insersion o cambio de datos
                        estafeta = user.getEstafeta();
                        System.out.println("Estafeta guardada: "+estafeta);
                        
                        });
                        
                   });
                   return false;
               }
               
           }
       }
       
       if(!encontrado){
           Platform.runLater(() -> {
               nameLabel.setText("No encontrado");
               branchLabel.setText("No encontrado");
               membershipLabel.setText("No encontrado");
               durationLabel.setText("No encontrado");
               startDateLabel.setText("No encontrado");
               endDateLabel.setText("No encontrado");
               membershipStatusLabel.setText("Sin Membresia");
               paneleft.setStyle("-fx-background-color: #E1E1E1;");
               
               if (mediaPlayerError != null) {
                    mediaPlayerError.play();
                    mediaPlayerError.seek(Duration.ZERO);
                }
               
                CompletableFuture.runAsync(() -> {
                    if(seleccionado != null){
                        enviarEstatus(seleccionado, "FAIL", "1");
                    }
                });
            });
       }
       return false;
    }
    
   public boolean processUserById(String id){
       boolean encontrado = false;
       
       System.out.println("Hola si esta entrando perros");
       System.out.println("El id que pasaron fue: " + id);
       
       if (userData.isEmpty()){
           System.out.println("Por eso no muestra nada");
           userData.setAll(DataInserter.geth2InfoUser());
       }
       // Ahora procesamos los datos independientemente de si estaban vacíos o no
       for(User user : userData){
           if (user.getEstafeta().equalsIgnoreCase(id)){
               Image image = new Image("/huellatorniquete/images/usuario.jpg");
               
               
               
               int estatusActual = DataInserter.obtenerEstatusQR(user.getEstafeta());
               System.out.println("Estatus actual en BD: " + estatusActual);
               boolean asistenciaExistente = DataInserter.checkAsistenciaExistente(user.getEstafeta(), user.getDuracion());
               
               if(estatusActual == 1){
                   Platform.runLater(() -> {
                       userPhotoImageView.setImage(image);
                       nameLabel.setText(user.getNombreCompleto());
                       branchLabel.setText(user.getIdBodega());
                       membershipLabel.setText(user.getTitulo());
                       durationLabel.setText(user.getDuracion().toString());
                       startDateLabel.setText(user.getFechaInicio());
                       endDateLabel.setText(user.getFechaFin());
                       
                       membershipStatusLabel.setText("El usuario ya esta dentro");
                       paneleft.setStyle("-fx-background-color: #FFA500;");
                       
                       if (mediaPlayerError != null) {
                        mediaPlayerError.play();
                        mediaPlayerError.seek(Duration.ZERO);
                        }
                       
                       CompletableFuture.runAsync(() -> {
                        if(seleccionado != null){
                            enviarEstatus(seleccionado, "FAIL", "1");
                        }
                        
                        });
                        
                   });
                   return false;
               }
               
               if (asistenciaExistente) {
                // Si la asistencia ya fue utilizada
                Platform.runLater(() -> {
                    userPhotoImageView.setImage(image);
                    nameLabel.setText(user.getNombreCompleto());
                    branchLabel.setText(user.getIdBodega());
                    membershipLabel.setText(user.getTitulo());
                    durationLabel.setText(user.getDuracion().toString());
                    startDateLabel.setText(user.getFechaInicio());
                    endDateLabel.setText(user.getFechaFin());

                    membershipStatusLabel.setText("Membresía ya utilizada");
                    paneleft.setStyle("-fx-background-color: #FFA500;");
        
                    if (mediaPlayerError != null) {
                        mediaPlayerError.play();
                        mediaPlayerError.seek(Duration.ZERO);
                    }
                    
                    CompletableFuture.runAsync(() -> {
                        if(seleccionado != null){
                            enviarEstatus(seleccionado, "FAIL", "1");
                        }
                        
                    });
                    
                    
                });
                return false;

                } 
               
               //INSERTAR ASISTENCIA
               if (user.getDuracion() == 1 && user.getEstatus().equals("1")){
                   DataInserter.insertarAsistencia(
                    user.getClave(), 
                    user.getFechaInicio(), 
                    user.getFechaFin(),
                    user.getDuracion(),
                    user.getEstafeta(), 
                    user.getEstatus());
               }
               
               updateUIWithUser(user);
               
               
               if (user.getEstafeta().equals(id)){
                   if(user.getEstafeta().equals(id)){
                       Platform.runLater(() -> {
                        userPhotoImageView.setImage(image);
                        nameLabel.setText(user.getNombreCompleto());
                        branchLabel.setText(user.getIdBodega());
                        membershipLabel.setText(user.getTitulo());
                        durationLabel.setText(user.getDuracion().toString());
                        startDateLabel.setText(user.getFechaInicio());
                        endDateLabel.setText(user.getFechaFin());
                        
                        if(user.getEstatus().equalsIgnoreCase("1") && user.getDaysBetweenDate(user.getFechaFin()) > 3){
                            if(mediaPlayerSuccess != null){
                                mediaPlayerSuccess.play();
                                mediaPlayerSuccess.seek(Duration.ZERO);
                            }
                            
                            membershipStatusLabel.setText("Membresia Activa");
                            paneleft.setStyle("-fx-background-color: #98ff96;");
                            membershipStatusLabel.setStyle("-fx-text-fill: black;");
                            
                            CompletableFuture.runAsync(new Runnable() {
                            @Override
                            public void run() {
                                if (seleccionado != null) {
                                    enviarEstatus(seleccionado, "OK", "1");
                                }
                                // Inserción o cambio de datos
                                
                                estafeta = user.getEstafeta();
                                System.out.println("Estafeta guardada: "+estafeta);
                                
                            }
                            });
                            
                        } else if(user.getEstatus().equalsIgnoreCase("1") && user.getDaysBetweenDate(user.getFechaFin()) <= 3){
                            if(mediaPlayerSuccess != null){
                                mediaPlayerSuccess.play();
                                mediaPlayerSuccess.seek(Duration.ZERO);
                            }
                            
                            membershipStatusLabel.setText("Activo - La membresia finalizara pronto");
                            paneleft.setStyle("-fx-background-color: yellow;");
                            membershipStatusLabel.setStyle("-fx-text-fill: black;");
                            
                            CompletableFuture.runAsync(new Runnable() {
                            @Override
                            public void run() {
                                if (seleccionado != null) {
                                    enviarEstatus(seleccionado, "OK", "1");
                                }
                                
                                estafeta = user.getEstafeta();
                                System.out.println("Estafeta guardada: "+estafeta);
                                
                                
                            }
                            });
                            
                        } else if(user.getEstatus().equalsIgnoreCase("0") || user.getDaysBetweenDate(user.getFechaFin()) < 0){
                            if (mediaPlayerError != null) {
                                mediaPlayerError.play();
                                mediaPlayerError.seek(Duration.ZERO);
                            }
                            
                            membershipStatusLabel.setText("Membresia Vencida");
                            paneleft.setStyle("-fx-background-color: red;");
                            membershipStatusLabel.setStyle("-fx-color: white;");
              
                            CompletableFuture.runAsync(() -> {
                                if(seleccionado != null){
                                    enviarEstatus(seleccionado, "FAIL", "1");
                                }
                            });
                        }
                       });
                       
                       encontrado = true;
                       System.out.println("Dias prueba: "+user.getDaysBetweenDate(user.getFechaFin()));
                       break;
                   }
               }
               
               return true;
               
           }
       }
       
       if(!encontrado){
           Platform.runLater(() -> {
               nameLabel.setText("No encontrado");
               branchLabel.setText("No encontrado");
               membershipLabel.setText("No encontrado");
               durationLabel.setText("No encontrado");
               startDateLabel.setText("No encontrado");
               endDateLabel.setText("No encontrado");
               membershipStatusLabel.setText("Sin Membresia");
               paneleft.setStyle("-fx-background-color: #E1E1E1;");
               
               if (mediaPlayerError != null) {
                    mediaPlayerError.play();
                    mediaPlayerError.seek(Duration.ZERO);
                }
               
                CompletableFuture.runAsync(() -> {
                    if(seleccionado != null){
                        enviarEstatus(seleccionado, "FAIL", "1");
                    }
                });
            });
       }
       
       
       return false; //Parar la funcion 
   }
   
   
   
   
   
   ///////////////////PROCESOS DEL QR ///////// INICIO ////
   public boolean processUserQrSalida(String id){
        boolean encontrado = false;
       
       System.out.println("Hola si esta entrando perros a salida qr");
       System.out.println("El id que pasaron fue: " + id);
       
       if (userData.isEmpty()){
           System.out.println("Por eso no muestra nada");
           userData.setAll(DataInserter.geth2InfoUser());
       }
       // Ahora procesamos los datos independientemente de si estaban vacíos o no
       for(User user : userData){
           if (user.getEstafeta().equalsIgnoreCase(id)){
               Image image = new Image("/huellatorniquete/images/usuario.jpg");
               
               
               int estatusActual = DataInserter.obtenerEstatusQR(user.getEstafeta());
               System.out.println("Estatus actual en BD: " + estatusActual);
               
               
               if (estatusActual == 1) {
                   System.out.println("Usuario en estado de salida, procesando...");

                   Platform.runLater(() -> {
                       userPhotoImageView.setImage(image);
                       nameLabel.setText(user.getNombreCompleto());
                       branchLabel.setText(user.getIdBodega());
                       membershipLabel.setText(user.getTitulo());
                       durationLabel.setText(user.getDuracion().toString());
                       startDateLabel.setText(user.getFechaInicio());
                       endDateLabel.setText(user.getFechaFin());
                       
                       membershipStatusLabel.setText("Salida");
                       paneleft.setStyle("-fx-background-color: #00AAE4;");
                       
                       if(mediaPlayerSuccess != null){
                            mediaPlayerSuccess.play();
                            mediaPlayerSuccess.seek(Duration.ZERO);
                       }
                       
                        CompletableFuture.runAsync(() -> {
                           if(seleccionado != null){
                            enviarFechaYHora(seleccionado, "0");
                        }
                           
                        estafeta = user.getEstafeta();
                         System.out.println("Estafeta guardada: "+estafeta);
                        
                        
                        });
                        
                   });
                   return false;
               }
               
           }
       }
       
       if(!encontrado){
           Platform.runLater(() -> {
               nameLabel.setText("No encontrado");
               branchLabel.setText("No encontrado");
               membershipLabel.setText("No encontrado");
               durationLabel.setText("No encontrado");
               startDateLabel.setText("No encontrado");
               endDateLabel.setText("No encontrado");
               membershipStatusLabel.setText("Sin Membresia");
               paneleft.setStyle("-fx-background-color: #E1E1E1;");
               
               if (mediaPlayerError != null) {
                    mediaPlayerError.play();
                    mediaPlayerError.seek(Duration.ZERO);
                }
               
                CompletableFuture.runAsync(() -> {
                    if(seleccionado != null){
                        enviarFechaYHora(seleccionado, "1");
                    }
                });
            });
       }
       return false;
    }
   
   
   public boolean processUserQr(String id){
       boolean encontrado = false;
       
       System.out.println("Hola si esta entrando perros");
       System.out.println("El id que pasaron fue: " + id);
       
       if (userData.isEmpty()){
           System.out.println("Por eso no muestra nada");
           userData.setAll(DataInserter.geth2InfoUser());
       }
       // Ahora procesamos los datos independientemente de si estaban vacíos o no
       for(User user : userData){
           if (user.getEstafeta().equalsIgnoreCase(id)){
               Image image = new Image("/huellatorniquete/images/usuario.jpg");
               
               int estatusActual = DataInserter.obtenerEstatusQR(user.getEstafeta());
               System.out.println("Estatus actual en BD: " + estatusActual);
               boolean asistenciaExistente = DataInserter.checkAsistenciaExistente(user.getEstafeta(), user.getDuracion());
               
               if(estatusActual == 1){
                   Platform.runLater(() -> {
                       userPhotoImageView.setImage(image);
                       nameLabel.setText(user.getNombreCompleto());
                       branchLabel.setText(user.getIdBodega());
                       membershipLabel.setText(user.getTitulo());
                       durationLabel.setText(user.getDuracion().toString());
                       startDateLabel.setText(user.getFechaInicio());
                       endDateLabel.setText(user.getFechaFin());
                       
                       membershipStatusLabel.setText("El usuario ya esta dentro");
                       paneleft.setStyle("-fx-background-color: #FFA500;");
                       
                       if (mediaPlayerError != null) {
                        mediaPlayerError.play();
                        mediaPlayerError.seek(Duration.ZERO);
                        }
                       
                       CompletableFuture.runAsync(() -> {
                        if(seleccionado != null){
                            enviarFechaYHora(seleccionado, "0");
                        }
                        
                        });
                        
                   });
                   return false;
               }
               
               if (asistenciaExistente) {
                // Si la asistencia ya fue utilizada
                Platform.runLater(() -> {
                    userPhotoImageView.setImage(image);
                    nameLabel.setText(user.getNombreCompleto());
                    branchLabel.setText(user.getIdBodega());
                    membershipLabel.setText(user.getTitulo());
                    durationLabel.setText(user.getDuracion().toString());
                    startDateLabel.setText(user.getFechaInicio());
                    endDateLabel.setText(user.getFechaFin());

                    membershipStatusLabel.setText("Membresía ya utilizada");
                    paneleft.setStyle("-fx-background-color: #FFA500;");
        
                    if (mediaPlayerError != null) {
                        mediaPlayerError.play();
                        mediaPlayerError.seek(Duration.ZERO);
                    }
                    
                    CompletableFuture.runAsync(() -> {
                        if(seleccionado != null){
                            enviarFechaYHora(seleccionado, "0");
                        }
                        
                    });
                    
                    
                });
                return false;

                } 
               
               //INSERTAR ASISTENCIA
               if (user.getDuracion() == 1 && user.getEstatus().equals("1")){
                   DataInserter.insertarAsistencia(
                    user.getClave(), 
                    user.getFechaInicio(), 
                    user.getFechaFin(),
                    user.getDuracion(),
                    user.getEstafeta(), 
                    user.getEstatus());
               }
               
               updateUIWithUser(user);
               
               
               if (user.getEstafeta().equals(id)){
                   if(user.getEstafeta().equals(id)){
                       Platform.runLater(() -> {
                        userPhotoImageView.setImage(image);
                        nameLabel.setText(user.getNombreCompleto());
                        branchLabel.setText(user.getIdBodega());
                        membershipLabel.setText(user.getTitulo());
                        durationLabel.setText(user.getDuracion().toString());
                        startDateLabel.setText(user.getFechaInicio());
                        endDateLabel.setText(user.getFechaFin());
                        
                        if(user.getEstatus().equalsIgnoreCase("1") && user.getDaysBetweenDate(user.getFechaFin()) > 3){
                            if(mediaPlayerSuccess != null){
                                mediaPlayerSuccess.play();
                                mediaPlayerSuccess.seek(Duration.ZERO);
                            }
                            
                            membershipStatusLabel.setText("Membresia Activa");
                            paneleft.setStyle("-fx-background-color: #98ff96;");
                            membershipStatusLabel.setStyle("-fx-text-fill: black;");
                            
                            CompletableFuture.runAsync(new Runnable() {
                            @Override
                            public void run() {
                                if (seleccionado != null) {
                                    enviarFechaYHora(seleccionado, "1");
                                }
                                // Inserción o cambio de datos
                                estafeta = user.getEstafeta();
                                System.out.println("Estafeta guardada: "+estafeta);
                                
                                
                            }
                            });
                            
                        } else if(user.getEstatus().equalsIgnoreCase("1") && user.getDaysBetweenDate(user.getFechaFin()) <= 3){
                            if(mediaPlayerSuccess != null){
                                mediaPlayerSuccess.play();
                                mediaPlayerSuccess.seek(Duration.ZERO);
                            }
                            
                            membershipStatusLabel.setText("Activo - La membresia finalizara pronto");
                            paneleft.setStyle("-fx-background-color: yellow;");
                            membershipStatusLabel.setStyle("-fx-text-fill: black;");
                            
                            CompletableFuture.runAsync(new Runnable() {
                            @Override
                            public void run() {
                                if (seleccionado != null) {
                                    enviarFechaYHora(seleccionado, "1");
                                }
                                // Inserción o cambio de datos
                                estafeta = user.getEstafeta();
                                System.out.println("Estafeta guardada: "+estafeta);
                                
                                
                            }
                            });
                            
                        } else if(user.getEstatus().equalsIgnoreCase("0") || user.getDaysBetweenDate(user.getFechaFin()) < 0){
                            if (mediaPlayerError != null) {
                                mediaPlayerError.play();
                                mediaPlayerError.seek(Duration.ZERO);
                            }
                            
                            membershipStatusLabel.setText("Membresia Vencida");
                            paneleft.setStyle("-fx-background-color: red;");
                            membershipStatusLabel.setStyle("-fx-color: white;");
              
                            CompletableFuture.runAsync(() -> {
                                if(seleccionado != null){
                                    enviarFechaYHora(seleccionado, "0");
                                }
                            });
                        }
                       });
                       
                       encontrado = true;
                       System.out.println("Dias prueba: "+user.getDaysBetweenDate(user.getFechaFin()));
                       break;
                   }
               }
               
               return true;
               
           }
       }
       
       if(!encontrado){
           Platform.runLater(() -> {
               nameLabel.setText("No encontrado");
               branchLabel.setText("No encontrado");
               membershipLabel.setText("No encontrado");
               durationLabel.setText("No encontrado");
               startDateLabel.setText("No encontrado");
               endDateLabel.setText("No encontrado");
               membershipStatusLabel.setText("Sin Membresia");
               paneleft.setStyle("-fx-background-color: #E1E1E1;");
               
               if (mediaPlayerError != null) {
                    mediaPlayerError.play();
                    mediaPlayerError.seek(Duration.ZERO);
                }
               
                CompletableFuture.runAsync(() -> {
                    if(seleccionado != null){
                        enviarFechaYHora(seleccionado, "0");
                    }
                });
            });
       }
       
       
       return false; //Parar la funcion 
   }            
   
    
    public class FechaHora{
        public static String getFecha(){
            LocalDateTime ahora = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            System.out.println("FECHA: "+ahora);
            return ahora.format(formatter);
        }
        
        public static  String getHora(){
            LocalDateTime ahora = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            System.out.println("HORA: "+ahora);
            return ahora.format(formatter);
        }
    }

    
    public static class DatosFechaHora {
        private String fecha;
        private String hora;
        private String estatus;

        public DatosFechaHora(String fecha, String hora, String estatus) {
            this.fecha = fecha;
            this.hora = hora;
            this.estatus = estatus;
        }

        // Getters opcionales si Gson los necesita
        public String getFecha() {
            return fecha;
        }

        public String getHora() {
            return hora;
        }
        
        public String getEstatus() {
            return estatus;
        }
    }
    
    public void enviarFechaYHora(SerialPort puerto, String estatusqr) {
    if (puerto == null || !puerto.isOpen()) {
        System.out.println("Error: El puerto no está disponible.");
        return;
    }

    try {
        String fecha = FechaHora.getFecha(); // Obtiene la fecha
        String hora = FechaHora.getHora();   // Obtiene la hora
        String estatus = estatusqr;
        
        // Crear un objeto con los datos
        DatosFechaHora datos = new DatosFechaHora(fecha, hora, estatus);

        // Convertir el objeto a JSON
        Gson gson = new Gson();
        String jsonMensaje = gson.toJson(datos) + "\n"; // Agregamos salto de línea

        // Convertimos el JSON en bytes y lo enviamos
        byte[] mensajeBytes = jsonMensaje.getBytes();
        int bytesWritten = puerto.writeBytes(mensajeBytes, mensajeBytes.length);

        if (bytesWritten == mensajeBytes.length) {
            System.out.println("JSON enviado exitosamente: " + jsonMensaje);
        } else {
            System.out.println("Error al enviar el JSON: " + jsonMensaje);
        }

        puerto.flushIOBuffers(); // Limpiar buffers después del envío

    } catch (Exception e) {
        System.out.println("Error al enviar JSON: " + e.getMessage());
        e.printStackTrace();
    }
}
    
    //HUELLE Y ESTFETA 
    public static class DatosEstatus {
        private String Status;
        private String Acceso;

        public DatosEstatus(String Status, String Acceso) {
            this.Status = Status;
            this.Acceso = Acceso;
            
        }

        // Getters opcionales si Gson los necesita
        public String getStatus() {
            return Status;
        }

        public String getAcceso() {
            return Acceso;
        }
        
    }
    
    public void enviarEstatus(SerialPort puerto, String Status1, String Acceso1) {
    if (puerto == null || !puerto.isOpen()) {
        System.out.println("Error: El puerto no está disponible.");
        return;
    }

    try {
        String Status = Status1; 
        String Acceso = Acceso1;   
        
        
        // Crear un objeto con los datos
        DatosEstatus datos = new DatosEstatus(Status, Acceso);

        // Convertir el objeto a JSON
        Gson gson = new Gson();
        String Mensaje = gson.toJson(datos) + "\n"; // Agregamos salto de línea

        // Convertimos el JSON en bytes y lo enviamos
        byte[] mensajeBytes = Mensaje.getBytes();
        int bytesWritten = puerto.writeBytes(mensajeBytes, mensajeBytes.length);

        if (bytesWritten == mensajeBytes.length) {
            System.out.println("JSON enviado exitosamente: " + Mensaje);
        } else {
            System.out.println("Error al enviar el JSON: " + Mensaje);
        }

        puerto.flushIOBuffers(); // Limpiar buffers después del envío

    } catch (Exception e) {
        System.out.println("Error al enviar JSON: " + e.getMessage());
        e.printStackTrace();
    }
}


    
    
    public static Reader[] getReaders() {
         System.out.println("Lectores disponibles: ");
        Reader[] reader = new Reader[1]; // Solo almacenará el lector en la posición 0

        try {
            // Crear una instancia de ReaderCollection
            ReaderCollection readers = UareUGlobal.GetReaderCollection();
            // Actualizar la lista de lectores
            readers.GetReaders();

            // Imprimir todos los lectores disponibles
            System.out.println("Lectores disponibles: " + readers.size());
            for (int i = 0; i < readers.size(); i++) {
                System.out.println("Lector #" + i + ": " + readers.get(i).GetDescription().name);
            }

            // Asegurarse de que hay al menos un lector
            if (readers.size() > 0) {
                reader[0] = readers.get(0); // Guardar solo el lector en posición 0
                String lectorNombre = reader[0].GetDescription().name;
                System.out.println("Usando el lector #0: " + lectorNombre);
                reader[0].Open(Reader.Priority.EXCLUSIVE);
            } else {
                System.out.println("No se encontraron lectores");
            }
        } catch (UareUException e) {
            e.printStackTrace();
        }
        return reader;
    }

   

    public void compareFingerprint(ObservableList<User> userData){
        if(userData == null){
            System.out.println("userData es null en compareFingerprint");
        }
        Task<Void> task = new Task<Void>(){
            @Override
            protected Void call() throws Exception {
               try{
                   ReaderCollection readers = UareUGlobal.GetReaderCollection();
                   readers.GetReaders();
                  // readerList.addAll(readers);
                   System.out.println("Lectores disponibles: " + readers.size() );
                   
                   //Comprobar si hay al menos dos lectores disponibles
                   if(!readers.isEmpty()){
                       //Obtenemos lod primeros lectores
                       Reader entrada = readers.get(0);
                       Reader salida = readers.get(1);
                       
                       System.out.println("Lector de entrada serial: " + entrada.GetDescription().serial_number);
                       System.out.println("Lector de salida serial: " + salida.GetDescription().serial_number);
                       
                       entrada.Open(Reader.Priority.EXCLUSIVE);
                       System.out.println("✅ Lector de entrada abierto correctamente.");

                       salida.Open(Reader.Priority.EXCLUSIVE);
                       System.out.println("✅ Lector de salida abierto correctamente.");

                       
                       //Hilo para captura y comparación con lector de entrada
                       Thread hiloEntrada = new Thread(() -> {
                           try {
                               System.out.println("ENTRO A HILO DE ENTRADA");
                               procesarHuella(entrada, userData, "entrada" );
                           }catch (Exception e){
                               System.err.println("Error en hilo de entrada: " + e.getMessage());
                           }
                       });
                       
                       
                       //Hilo para captura y comparación de salida
                       Thread hiloSalida = new Thread(() -> {
                           try {
                               System.out.println("ENTRO A HILO DE SALIDA");
                               procesarHuella(salida,userData,"salida");
                           }catch (Exception e){
                               System.err.println("Error en hilo de entrada: " + e.getMessage());
                           }
                       });
                       
                       //Iniciar los hilos 
                       hiloEntrada.start();
                       hiloSalida.start();
                       
                       //Esperar a que los hilos terminen antes de cerrar los lectores
                       hiloEntrada.join();
                       hiloSalida.join();
                       
                      entrada.Close();
                       salida.Close();
                       
                       
                   }else{
                       System.out.println("No se encontraron lectores disponibles");
                   }
                   
               }catch (UareUException e){
                   System.err.println("Error de la inicialización: "+ e.getMessage());
               }
             return null;
            }
          
        };
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
    
    
    // Método para capturar huella y compararla
private void procesarHuella(Reader reader, ObservableList<User> userData, String tipoLector) {
    try {
        System.out.println("Iniciando captura en lector: " + reader.GetDescription().name);
        while (!Thread.currentThread().isInterrupted()) {
            System.out.println("Esperando huella en lector: " + reader.GetDescription().name);
            Reader.CaptureResult captureResult = reader.Capture(
                Fid.Format.ANSI_381_2004,
                Reader.ImageProcessing.IMG_PROC_DEFAULT,
                500,
                30000
            );
            
            if (captureResult == null) {
                System.out.println("⚠ No se recibió un resultado de captura.");
                continue;
            }

            if (captureResult != null && captureResult.quality == Reader.CaptureQuality.GOOD) {
                System.out.println("Huella capturada en: " + reader.GetDescription().name);
                Fmd capturedFmd = UareUGlobal.GetEngine().CreateFmd(captureResult.image, Fmd.Format.ANSI_378_2004);
                compararHuella(capturedFmd, userData, tipoLector);
            }

            Thread.sleep(100); // Pequeña pausa entre intentos de captura
        }
    } catch (UareUException | InterruptedException e) {
        System.err.println("Error en el ciclo de captura en " + reader.GetDescription().name + ": " + e.getMessage());
    }
}



///Comparar huellas
private void compararHuella(Fmd capturedFmd, ObservableList<User> userData, String tipoLector){
    if (capturedFmd != null) {
                                System.out.println("Huella capturada: OK");
                                boolean huellaEncontrada = false;

                                for (User user : userData) {
                                    if (user.getHuellaFmd() != null) {
                                        try {
                                            int score = UareUGlobal.GetEngine().Compare(capturedFmd, 0, user.getHuellaFmd(), 0);
                                            int threshold = 100000;

                                            if (score < threshold) {
                                                System.out.println("Se encontró una huella coincidente para el usuario: " + user.getNombreCompleto());
                                                huellaEncontrada = true;
                                                
                                                // Process the matched user
                                                //processUserById(user.getEstafeta());
                                                // Diferenciar la acción según el lector
                                                if ("entrada".equals(tipoLector)) {
                                                    processUserById(user.getEstafeta());
                                                } else if ("salida".equals(tipoLector)) {
                                                    processUserSalida(user.getEstafeta());
                                                }
                                                break;
                                            }
                                        } catch (UareUException e) {
                                            System.err.println("Error al comparar huellas: " + e.getMessage());
                                        }
                                    }
                                }

                                if (!huellaEncontrada) {
                                    Platform.runLater(() -> {
                                        // Actualizar UI con el estado de no encontrado
                                        nameLabel.setText("No encontrado");
                                        branchLabel.setText("No encontrado");
                                        membershipLabel.setText("No encontrado");
                                        durationLabel.setText("No encontrado");
                                        startDateLabel.setText("No encontrado");
                                        endDateLabel.setText("No encontrado");
                                        membershipStatusLabel.setText("Sin Membresía");
                                        paneleft.setStyle("-fx-background-color: #E1E1E1;");
                                        if (mediaPlayerError != null) {
                                            mediaPlayerError.play();
                                            mediaPlayerError.seek(Duration.ZERO);
                                        }
                                    });
                                    //Thread.sleep(1000);
                                }
                            } else {
                                System.out.println("❌ No se pudo capturar la huella.");
                                //Thread.sleep(500); // Retraso para intentar nuevamente
                            }
}
    
    
    
    
    
    
    
  /*  
    public void compareFingerprint(ObservableList<User> userData) {
    if (userData == null) {
        System.out.println("⚠ userData es null en compareFingerprint.");
        return;
    }
    
    Task<Void> task = new Task<Void>() {
        @Override
        protected Void call() throws Exception {
            try {
                ReaderCollection readers = UareUGlobal.GetReaderCollection();
                readers.GetReaders();
                System.out.println("Lectores disponibles: " + readers.size());

                if (readers.size() > 0) {
                    Reader reader = readers.get(1);
                    //Reader salida = readers.get(1);
                    
                    System.out.println("Lectores disponibles serial: " + reader.GetDescription().serial_number);
                    System.out.println("Lectores disponibles serial: " + reader.GetDescription().id);
                    reader.Open(Reader.Priority.EXCLUSIVE);

                    System.out.println("Estado de isCancelled() antes del while: " + isCancelled());

                    while (!isCancelled()) {
                        System.out.println("Dentro del while... esperando captura.");
                        try {
                            
                            long startTime = System.currentTimeMillis();
                            Fmd capturedFmd = capturarHuella(reader);
                            long endTime = System.currentTimeMillis();
                            
                            System.out.println("Tiempo de captura: " + (endTime - startTime) + "ms");

                            if (capturedFmd != null) {
                                System.out.println("Huella capturada: OK");
                                boolean huellaEncontrada = false;

                                for (User user : userData) {
                                    if (user.getHuellaFmd() != null) {
                                        try {
                                            int score = UareUGlobal.GetEngine().Compare(capturedFmd, 0, user.getHuellaFmd(), 0);
                                            int threshold = 100000;

                                            if (score < threshold) {
                                                System.out.println("Se encontró una huella coincidente para el usuario: " + user.getNombreCompleto());
                                                huellaEncontrada = true;
                                                
                                                // Process the matched user
                                                processUserById(user.getEstafeta());
                                                break;
                                            }
                                        } catch (UareUException e) {
                                            System.err.println("Error al comparar huellas: " + e.getMessage());
                                        }
                                    }
                                }

                                if (!huellaEncontrada) {
                                    Platform.runLater(() -> {
                                        // Actualizar UI con el estado de no encontrado
                                        nameLabel.setText("No encontrado");
                                        branchLabel.setText("No encontrado");
                                        membershipLabel.setText("No encontrado");
                                        durationLabel.setText("No encontrado");
                                        startDateLabel.setText("No encontrado");
                                        endDateLabel.setText("No encontrado");
                                        membershipStatusLabel.setText("Sin Membresía");
                                        paneleft.setStyle("-fx-background-color: #E1E1E1;");
                                        if (mediaPlayerError != null) {
                                            mediaPlayerError.play();
                                            mediaPlayerError.seek(Duration.ZERO);
                                        }
                                    });
                                    Thread.sleep(1000);
                                }
                            } else {
                                System.out.println("❌ No se pudo capturar la huella.");
                                Thread.sleep(500); // Retraso para intentar nuevamente
                            }
                        } catch (Exception e) {
                            System.err.println("Error en el ciclo de captura: " + e.getMessage());
                            Thread.sleep(1000);
                        }
                    }

                    reader.Close();
                } else {
                    System.out.println("⚠ No se encontraron lectores de huellas dactilares.");
                }
            } catch (UareUException e) {
                System.err.println("Error en la inicialización: " + e.getMessage());
            }
            return null;
        }
    };

    Thread thread = new Thread(task);
    thread.setDaemon(true);
    thread.start();
}

    
   
   
public static Fmd capturarHuella(Reader reader) {
    try {
        // Intentar capturar la huella
        Reader.CaptureResult captureResult = reader.Capture(
            Fid.Format.ANSI_381_2004,  // Formato para la huella
            Reader.ImageProcessing.IMG_PROC_DEFAULT,  // Configuración de procesamiento de la imagen
            500,  // Tiempo de espera en milisegundos
            30000    // Número de intentos (-1 para ilimitados)
        );

        if (captureResult != null && captureResult.quality == Reader.CaptureQuality.GOOD) {
            System.out.println("Huella capturada en: "+reader.GetDescription());
            return UareUGlobal.GetEngine().CreateFmd(
                
                captureResult.image,  // Usar la imagen capturada para crear el FMD
                Fmd.Format.ANSI_378_2004  // Formato de huella que estamos utilizando
            );
        }
    } catch (UareUException e) {
        System.err.println("Error al capturar la huella: " + e.getMessage());
    }
    return null;  // Retornar null si no se pudo capturar la huella
}

 */   

    
    
    
    public static void convertHuellas(ObservableList<User> dataUser) {
    if (dataUser == null || dataUser.isEmpty()) {
        System.out.println("No users found to convert fingerprints.");
        return;
    }

    for (User u : dataUser) {
        if (u.getHuella() == null || u.getHuella().trim().isEmpty()) {
            System.out.println("Skipping user " + u.getNombreCompleto() + " - No fingerprint data");
            continue;
        }

        try {
            // Intentamos decodificar la huella
           // System.out.println("Intentando decodificar huella para: " + u.getNombreCompleto());
            Fmd fmd = decodificarFMD(u.getHuella());

            if (fmd != null) {
                // Asignamos la huella convertida al usuario
                u.setHuellaFmd(fmd);
            } else {
                System.out.println("No se pudo decodificar la huella de " + u.getNombreCompleto());
            }
        } catch (Exception e) {
            System.err.println("Error convirtiendo huella para " + u.getNombreCompleto() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}



    public static Fmd decodificarFMD(String base64) {
    // Check for null or empty string
    if (base64 == null || base64.trim().isEmpty()) {
        //System.out.println("Warning: Attempt to decode null or empty Base64 string");
        return null;
    }

    try {
        // Decode Base64 string to byte array
        byte[] data = Base64.getDecoder().decode(base64);

        // Create FMD object from byte array
        Fmd fmd = UareUGlobal.GetImporter().ImportFmd(data, Fmd.Format.ANSI_378_2004, Fmd.Format.ANSI_378_2004);

        return fmd;
    } catch (IllegalArgumentException e) {
        System.err.println("Invalid Base64 encoding: " + e.getMessage());
        return null;
    } catch (UareUException e) {
        System.err.println("Error importing FMD: " + e.getMessage());
        e.printStackTrace();
        return null;
    } catch (Exception e) {
        System.err.println("Unexpected error decoding fingerprint: " + e.getMessage());
        e.printStackTrace();
        return null;
    }
}
    
    
    
@FXML
public void ActualizarLocalDatabase() {
    try {
        System.out.println(" Iniciando actualización de la base de datos local...");

        ObservableList<User> nuevaLista = FXCollections.observableArrayList(ApiService.getDataClient(idSucursal));

        if (nuevaLista == null || nuevaLista.isEmpty()) {
            System.out.println("No se recibieron datos de la API. Eliminando datos en H2...");
            new DataInserter().deleteAllUsersFromH2(); // 🔴 ELIMINAR DATOS EN H2
            Platform.runLater(() -> {
                userData.clear(); // Limpiar la UI
                recargarControlador();
            });
            return;
        }

        System.out.println("Lista recibida con " + nuevaLista.size() + " usuarios.");

        ObservableList<User> uniqueUsers = FXCollections.observableArrayList(
            nuevaLista.stream()
                .collect(Collectors.toMap(
                    User::getClave, 
                    Function.identity(), 
                    (existing, replacement) -> replacement
                ))
                .values()
        );

        //System.out.println("Lista filtrada con " + uniqueUsers.size() + " usuarios únicos.");

        DataInserter.insertData(uniqueUsers);
        System.out.println("Base de datos local actualizada con éxito.");

        ObservableList<User> updatedData = FXCollections.observableArrayList(DataInserter.geth2InfoUser());

        Platform.runLater(() -> {
            userData.clear();
            userData.addAll(updatedData);
            System.out.println("Lista userData actualizada con " + userData.size() + " usuarios.");

            recargarControlador();
        });

    } catch (Exception e) {
        e.printStackTrace();
        System.out.println("Se produjo un error durante la actualización: " + e.getMessage());
    }
}




@FXML
public void recargarControlador() {
    
    buscarTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                buscarTextField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    
    getFrase();
    labelMotivacion.setText(frase);
       
    userData.setAll(DataInserter.geth2InfoUser());
        //startWebService();
    convertHuellas(userData);
    compareFingerprint(userData);
        
    idSucursal = HuellaTorniquete.getIdSucursal();
       // System.out.println("Obtenemos idSucursal: "+idSucursal);
       
    buscarTextField.setOnKeyPressed(event -> {
        if (event.getCode() == KeyCode.ENTER) {
            String numero = buscarTextField.getText();
            processUserById(numero);
        }
    });
}



}