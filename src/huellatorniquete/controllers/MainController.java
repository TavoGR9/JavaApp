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
        
        
        //ScheduledTaskManager.iniciarActualizacionPeriodica();//actualizacion periodica 
        
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

       // System.out.println("Lista con fmd: "+userData);
       // System.out.println("Tamaño de lista FMD: "+userData.size());
        
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
            //stopWebService();
           // ScheduledTaskManager.detenerActualizacion();
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
            // Agregar estas configuraciones
            seleccionado.setNumDataBits(8);
            seleccionado.setNumStopBits(1);
            seleccionado.setParity(SerialPort.NO_PARITY);
            
            if (!seleccionado.openPort()) {
                System.out.println("❌ Error al abrir el puerto");
                return;
            }
            // Añadir un retraso para permitir que el dispositivo termine su inicialización
            
                //Thread.sleep(1000);  // Espera de 1 segundo
                //seleccionado.clearDTR();  // Limpiar línea DTR si es necesario
                seleccionado.flushIOBuffers();  // Vaciar los buffers de entrada/salida
            

            System.out.println("✅ Puerto seleccionado: " + seleccionado.getSystemPortName());
            
            // Buffer para acumular datos
            StringBuilder dataBuffer = new StringBuilder();
            
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
                            // Usar UTF-8 explícitamente
                            String receivedData = new String(buffer, 0, bytesRead, "UTF-8");
                            
                            // Ignorar mensajes del sistema y caracteres de control
                            if (receivedData.contains("GM65") || receivedData.equals("?")) {
                                return;
                            }
                            
                            // Acumular datos hasta encontrar un delimitador
                            dataBuffer.append(receivedData);
                            
                            // Si encontramos un delimitador (CR o LF), procesamos los datos
                            if (receivedData.contains("\n") || receivedData.contains("\r")) {
                                String completeData = dataBuffer.toString();
                                String cleanId = cleanAndValidateId(completeData);
                                
                                if (cleanId != null) {
                                    System.out.println("📩 ID original: " + completeData);
                                    System.out.println("🧹 ID limpio: " + cleanId);
                                    processUserQr(cleanId);
                                } else {
                                    System.out.println("❌ ID inválido recibido: " + completeData);
                                }
                                
                                // Limpiar el buffer después de procesar
                                dataBuffer.setLength(0);
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
                       membershipStatusLabel.setText("Salida");
                       paneleft.setStyle("-fx-background-color: #00AAE4;");
                       
                       if (mediaPlayerError != null) {
                        mediaPlayerError.play();
                        mediaPlayerError.seek(Duration.ZERO);
                        }
                       
                        if(seleccionado != null){
                            enviarEstatus(seleccionado, "OK", "0");
                        }
                        //insersion o cambio de datos
                        DataInserter.cambiarEsatusQR(user.getEstafeta());
                        ApiService.CambiarEstatus(user.getEstafeta(), idSucursal);
                   });
                   return false;
               }
               
               if (asistenciaExistente) {
                // Si la asistencia ya fue utilizada
                Platform.runLater(() -> {
                    nameLabel.setText(user.getNombreCompleto());
                    branchLabel.setText(user.getIdBodega());
                    membershipLabel.setText(user.getTitulo());
                    durationLabel.setText(user.getDuracion().toString());
                    startDateLabel.setText(user.getFechaInicio());
                    endDateLabel.setText(user.getFechaFin());

                    membershipStatusLabel.setText("Membresía ya utilizada");
                    paneleft.setStyle("-fx-background-color: #2271b3;");
                    membershipStatusLabel.setStyle("-fx-text-fill: white;");
        
                    if (mediaPlayerError != null) {
                        mediaPlayerError.play();
                        mediaPlayerError.seek(Duration.ZERO);
                    }
                    enviarEstatus(seleccionado, "FAIL", "0");
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
                            
                            CompletableFuture.runAsync(() -> {
                                if(seleccionado != null){
                                    enviarEstatus(seleccionado, "OK", "1");
                                }
                                //insersion o cambio de datos
                                ApiService.InsertarAsistencia(user.getEstafeta(), idSucursal);
                                ApiService.CambiarEstatus(user.getEstafeta(), idSucursal);
                                DataInserter.cambiarEsatusQR(user.getEstafeta());
                            });
                        } else if(user.getEstatus().equalsIgnoreCase("1") && user.getDaysBetweenDate(user.getFechaFin()) <= 3){
                            if(mediaPlayerSuccess != null){
                                mediaPlayerSuccess.play();
                                mediaPlayerSuccess.seek(Duration.ZERO);
                            }
                            
                            membershipStatusLabel.setText("Activo - La membresia finalizara pronto");
                            paneleft.setStyle("-fx-background-color: yellow;");
                            membershipStatusLabel.setStyle("-fx-text-fill: black;");
                            
                            CompletableFuture.runAsync(() -> {
                                if(seleccionado != null){
                                    enviarEstatus(seleccionado, "OK", "1");
                                }
                                //insersion o cambio de datos
                                ApiService.InsertarAsistencia(user.getEstafeta(), idSucursal);
                                ApiService.CambiarEstatus(user.getEstafeta(), idSucursal);
                                DataInserter.cambiarEsatusQR(user.getEstafeta());
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
    
    
    
    
    
    
    
    
    public boolean processUserQr(String id) { 
    boolean encontrado = false;
    System.out.println("Hola si esta entrando perros A QR");
    System.out.println("El id que pasaron fue: " + id);
    
    
    if (userData.isEmpty()) {
        System.out.println("Por eso no muestra nada");
        userData.setAll(DataInserter.geth2InfoUser());
    }
    
    
    // Ahora procesamos los datos independientemente de si estaban vacíos o no
    for (User user : userData) {
        if (user.getEstafeta().equalsIgnoreCase(id)) {
            
            System.out.println("el usuario es: " + user); 
  
            int estatusActual = DataInserter.obtenerEstatusQR(user.getEstafeta());
            boolean asistenciaExistente = DataInserter.checkAsistenciaExistente(user.getEstafeta(), user.getDuracion());

            //System.out.println("Estatus actual en BD: " + estatusActual);
            //System.out.println("¿Asistencia existente?: " + asistenciaExistente);

            if (estatusActual == 1) {
            // Si el usuario ya está dentro (estatusQR = 1)
            Platform.runLater(() -> {
                nameLabel.setText(user.getNombreCompleto());
                branchLabel.setText(user.getIdBodega());
                membershipLabel.setText(user.getTitulo());
                durationLabel.setText(user.getDuracion().toString());
                startDateLabel.setText(user.getFechaInicio());
                endDateLabel.setText(user.getFechaFin());
                membershipStatusLabel.setText("Salida");
                paneleft.setStyle("-fx-background-color: #2271b3;");
                membershipStatusLabel.setStyle("-fx-text-fill: white;");
        
                if (mediaPlayerError != null) {
                    mediaPlayerError.play();
                    mediaPlayerError.seek(Duration.ZERO);
                }
                
                enviarFechaYHora(seleccionado, "0");
                DataInserter.cambiarEsatusQR(user.getEstafeta());
                ApiService.InsertarAsistencia(user.getEstafeta(), idSucursal);
            });
            return false;

        } 
            
         if (asistenciaExistente) {
        // Si la asistencia ya fue utilizada
          Platform.runLater(() -> {
            nameLabel.setText(user.getNombreCompleto());
            branchLabel.setText(user.getIdBodega());
            membershipLabel.setText(user.getTitulo());
            durationLabel.setText(user.getDuracion().toString());
            startDateLabel.setText(user.getFechaInicio());
            endDateLabel.setText(user.getFechaFin());

            membershipStatusLabel.setText("Membresía ya utilizada");
            paneleft.setStyle("-fx-background-color: #2271b3;");
            membershipStatusLabel.setStyle("-fx-text-fill: white;");
        
            if (mediaPlayerError != null) {
                mediaPlayerError.play();
                mediaPlayerError.seek(Duration.ZERO);
            }
            enviarFechaYHora(seleccionado, "0");
        });
        return false;

        } 
            // Si el usuario tiene estatusQR = 0 y no ha registrado asistencia
            
            //INSERTAR ASITENCIA EN VISITA
            if(user.getDuracion() == 1 && user.getEstatus().equals("1")){
            DataInserter.insertarAsistencia(
                user.getClave(), 
                user.getFechaInicio(), 
                user.getFechaFin(),
                user.getDuracion(),
                user.getEstafeta(), 
                user.getEstatus());
            }
            
            updateUIWithUser(user);
            
            
            if (user.getEstafeta().equals(id)) {
                if (user.getEstafeta().equals(id)){
                    //System.out.println("entrando a la funcion 2" + user);
                    
                    Image image = new Image("/huellatorniquete/images/usuario.jpg");
                    
                    Platform.runLater(() -> {
                        // Actualizaciones básicas de UI
                        userPhotoImageView.setImage(image);
                        nameLabel.setText(user.getNombreCompleto());
                        branchLabel.setText(user.getIdBodega());
                        membershipLabel.setText(user.getTitulo());
                        durationLabel.setText(user.getDuracion().toString());
                        startDateLabel.setText(user.getFechaInicio());
                        endDateLabel.setText(user.getFechaFin());
                        
                    ///CUANDO FALTA MUCHO TIEMPO PARA QUE LA MEMBRESIA EXPIRE
                    if (user.getEstatus().equalsIgnoreCase("1") && user.getDaysBetweenDate(user.getFechaFin()) > 3) {
                        if (mediaPlayerSuccess != null){
                            mediaPlayerSuccess.play();
                            mediaPlayerSuccess.seek(Duration.ZERO);
                        }
                        
                        membershipStatusLabel.setText("Membresia Activa");
                        paneleft.setStyle("-fx-background-color: #98ff96;");
                        CompletableFuture.runAsync(() -> {
                                if (seleccionado != null) {
                                enviarFechaYHora(seleccionado, "1");
                                }
                                DataInserter.cambiarEsatusQR(user.getEstafeta());
                                //int estatus = DataInserter.obtenerEstatusQR(user.getEstafeta());
                                ApiService.InsertarAsistencia(user.getEstafeta(), idSucursal);
                                //ApiService.CambiarEstatus(user.getEstafeta(), idSucursal);
                               
                            
                        });
                    }
                    ///CUANDO FALTA POCO PARA QUE LA MEMBRESIA EXPIRE
                    else if (user.getEstatus().equalsIgnoreCase("1") && user.getDaysBetweenDate(user.getFechaFin()) <= 3){
                        if (mediaPlayerSuccess != null){
                            mediaPlayerSuccess.play();
                            mediaPlayerSuccess.seek(Duration.ZERO);
                        }
                        membershipStatusLabel.setText("Activo - La membresia finalizara pronto");
                        paneleft.setStyle("-fx-background-color: yellow;");
                        membershipStatusLabel.setStyle("-fx-text-fill: black;");
                        
                        CompletableFuture.runAsync(() -> {
                                if (seleccionado != null) {
                                enviarFechaYHora(seleccionado, "1");
                                }
                                DataInserter.cambiarEsatusQR(user.getEstafeta());
                                ApiService.InsertarAsistencia(user.getEstafeta(), idSucursal);
                                
                                //ApiService.CambiarEstatus(user.getEstafeta(), idSucursal);
                             
                        });
                    }
                    ///CUANDO YA ESTA CADUCA LA MEMBRESIA
                    else if (user.getEstatus().equalsIgnoreCase("0") || user.getDaysBetweenDate(user.getFechaFin()) < 0){
                        if (mediaPlayerError != null) {
                            mediaPlayerError.play();
                            mediaPlayerError.seek(Duration.ZERO);
                        }
                        membershipStatusLabel.setText("Membresia Vencida");
                        paneleft.setStyle("-fx-background-color: red;");
                        membershipStatusLabel.setStyle("-fx-color: white;");
               
                    }
                    });
                    
                    encontrado = true;
                    System.out.println("DIAS PRUEBA: " + user.getDaysBetweenDate(user.getFechaFin()));
                    break;
 
                }
            }
            
            
            return true;
            
        }
    }

    
    if (!encontrado) {
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
                
                enviarFechaYHora(seleccionado, "0");
            });
        }
   

    return false;  // Solo necesitas un return false al final
}
    
  
    /*
    public static void enviarUno(SerialPort puerto, String dato) {
    if (puerto == null || !puerto.isOpen()) {
        System.out.println("Error: El puerto no está disponible.");
        return;
    }
    try {
        // Convertimos el dato a un byte array
        byte[] data = dato.getBytes(); // Convierte el String a bytes
        int bytesWritten = puerto.writeBytes(data, data.length);

        if (bytesWritten == data.length) {
            System.out.println(dato + " enviado exitosamente");
        } else {
            System.out.println("Error al enviar el dato: " + dato);
        }

        Thread.sleep(50);
        puerto.flushIOBuffers();

    } catch (Exception e) {
        System.out.println("Error al enviar: " + e.getMessage());
        e.printStackTrace();
    }
}
*/
    
    
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


    
    //FINGERPRINT READER
    public static Reader getReaders(){
    Reader reader = null;
    try {
        // Crear una instancia de ReaderCollection
        ReaderCollection readers = UareUGlobal.GetReaderCollection();

        // Actualizar la lista de lectores
        readers.GetReaders();

        // Asegurarse de que hay al menos un lector
        if (readers.size() > 0) {
            // Obtener el primer lector
            //System.out.println("Hay lectores disponibles");
            reader = readers.get(0);
            String Lector = reader.GetDescription().name;
            //System.out.println("El lector es: "+Lector);
            reader.Open(Reader.Priority.EXCLUSIVE);
        }else {
            //System.out.println("No se encontraron lectores");
        }
    }
    catch (UareUException e) {
        e.printStackTrace();
    }
    return reader;
}
  
    
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
                    
                    if (readers.size() > 0) {
                        Reader reader = readers.get(0);
                        reader.Open(Reader.Priority.EXCLUSIVE);
                        
                        while (!isCancelled()) {
                            try {
                                Fmd capturedFmd = capturarHuella(reader);
                                System.out.println("Huella capturada: " + (capturedFmd != null ? "OK" : "NULL"));


                                if (capturedFmd != null) {
                                    boolean huellaEncontrada = false;
                                    for (User user : userData) {
                                        if (user.getHuellaFmd() != null) {
                                            try {
                                                int score = UareUGlobal.GetEngine().Compare(capturedFmd, 0, user.getHuellaFmd(), 0);
                                                int threshold = 100000;

                                                if (score < threshold) {
                                                    System.out.println("Se encontró una huella coincidente para el usuario: " + user.getNombreCompleto());
                                                    System.out.println("Usuario completo: "+user);
                                                    System.out.println("DIAS DE DIFERENCIA:" +user.getDaysBetweenDate(user.getFechaFin()));
                                                    System.out.println("STATUS:"+user.getEstatus());
                                                    System.out.println("ESTAFETA: "+user.getEstafeta());
                                                    huellaEncontrada = true;
                                                    
                                                    //actualizarDatosUsuario(user);
                                                    processUserById(user.getEstafeta());
                                                    
                                                    //Thread.sleep(2000);

                                                    break;
                                                }
                                            } catch (UareUException e) {
                                                System.err.println("Error al comparar huellas: " + e.getMessage());
                                            }
                                        }
                                    }
                                    if (!huellaEncontrada) {
                                        //System.out.println("No se encontró ninguna huella coincidente.");
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
                                        });
                                        Thread.sleep(1000);
                                    }
                                } else {
                                    //System.out.println("No se pudo capturar la huella.");
                                    Thread.sleep(500);
                                }
                            } catch (Exception e) {
                                System.err.println("Error en el ciclo de captura: " + e.getMessage());
                                Thread.sleep(1000);
                            }
                        }
                        
                        reader.Close();
                    } else {
                        //System.out.println("No se encontraron lectores de huellas dactilares.");
                    }
                } catch (UareUException e) {
                    //System.err.println("Error en esto: " + e.getMessage());
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
            Reader.CaptureResult captureResult = reader.Capture(
                Fid.Format.ANSI_381_2004,
                Reader.ImageProcessing.IMG_PROC_DEFAULT,
                500,
                -1
            );
            
            if (captureResult != null && captureResult.quality == Reader.CaptureQuality.GOOD) {
                return UareUGlobal.GetEngine().CreateFmd(
                    captureResult.image,
                    Fmd.Format.ANSI_378_2004
                );
            }
        } catch (UareUException e) {
            System.err.println("Error al capturar la huella: " + e.getMessage());
        }
        return null;
    }
    
    
    
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