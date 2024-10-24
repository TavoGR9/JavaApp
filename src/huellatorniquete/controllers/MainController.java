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
import java.nio.file.Paths;
import java.util.Base64;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import javax.swing.SwingWorker;

public class MainController {
    
    List<User> userData = new ArrayList<>();
    SerialPort seleccionado = null;  // Inicializamos como null
    HuellaTorniquete ht = new HuellaTorniquete();
    String audioFilePath = "src/huellatorniquete/sounds/Success.mp3"; // Cambia por la ruta de tu archivo
    String audioFilePath2 = "src/huellatorniquete/sounds/Error.mp3";

    // Cargar el archivo de audio
        Media soundSuccess = new Media(Paths.get(audioFilePath).toUri().toString());
        Media soundError = new Media(Paths.get(audioFilePath2).toUri().toString());
        
        private MediaPlayer mediaPlayerSuccess;
        private MediaPlayer mediaPlayerError;
    
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
    private void initialize() { 
        buscarTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                buscarTextField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        
        getFrase();
        labelMotivacion.setText(frase);
        
        
        
        userData = DataInserter.geth2InfoUser();
        /*System.out.println("Lista de usuarios cargada: " + userData);
        System.out.println("Tamaño antes de convertirla a FMD controller: "+userData.size());*/

        
        convertHuellas(userData);
        System.out.println("Lista con fmd: "+userData);
        System.out.println("Tamaño de lista FMD: "+userData.size());
        
        
        getPort();
    
    
    
    mediaPlayerSuccess = new MediaPlayer(soundSuccess);
    mediaPlayerError = new MediaPlayer(soundError);
    
    
    
    
    System.out.println("Ruta de éxito: " + Paths.get(audioFilePath).toUri().toString());
    System.out.println("Ruta de error: " + Paths.get(audioFilePath2).toUri().toString());
        
 // Verificar puertos COM disponibles y usar el primero disponible
    /*SerialPort[] ports = SerialPort.getCommPorts();
    if (ports.length > 0) {
        String firstPortName = ports[0].getSystemPortName();
        System.out.println("Primer puerto COM disponible: " + firstPortName);
        enviarSeñalApertura(firstPortName);  // Enviar señal de apertura al primer puerto encontrado
    } else {
        System.out.println("No se encontraron puertos COM disponibles.");
    }*/
        
    }
    
    
    /*private void getPort() {
        SerialPort[] ports = SerialPort.getCommPorts();
        for (SerialPort port : ports) {
            System.out.println("names ports: " + port.getDescriptivePortName());
            if (port.getDescriptivePortName().contains("USB-SERIAL")) {
                seleccionado = port;
                System.out.println("seleccionado puerto: "+seleccionado);
                break;  // Detenemos el ciclo si ya encontramos el puerto
            }
        }
    }*/
    
    private void getPort() {
        SerialPort[] ports = SerialPort.getCommPorts();
        seleccionado = null; // Reset seleccionado antes de buscar
        
        for (SerialPort port : ports) {
            System.out.println("names ports: " + port.getDescriptivePortName());
            if (port.getDescriptivePortName().contains("USB-SERIAL")) {
                seleccionado = port;
                System.out.println("seleccionado puerto: " + seleccionado);
                break;
            }
        }
        
        if (seleccionado == null) {
            System.out.println("No se encontró ningún puerto USB-SERIAL");
        }
    }

    private void getFrase(){
        try {
            frase = ApiService.getFrases();
            if (!frase.isBlank()) {
                System.out.println("Frase obtenida: " + frase);
            } else {
                System.out.println("No se encontró frase");
            }
        } catch (Exception e) {
            System.out.println("Error al obtener la frase: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void getIdToSearch() {
        System.out.println("Iniciando búsqueda...");
        String numero = buscarTextField.getText();
        System.out.println("Número a buscar: " + numero);

        boolean encontrado = false;

        if (!userData.isEmpty()) {
            for (User user : userData) {
                if (user.getEstafeta().equals(numero)) {
                    System.out.println("Usuario encontrado: " + user);
                    nameLabel.setText(user.getNombre());
                    branchLabel.setText(user.getSucursal());
                    membershipLabel.setText(user.getMembresia());
                    durationLabel.setText(user.getDuracion().toString());
                    startDateLabel.setText(user.getFechaInicio());
                    endDateLabel.setText(user.getFechaFin());
                    encontrado = true;
                    if(user.getStatus().equalsIgnoreCase("Activo") && user.getDuracion() >= 10){
                        mediaPlayerSuccess.stop();  // Detener si está reproduciendo
                        mediaPlayerSuccess.seek(Duration.ZERO);  // Reiniciar al inicio
                        mediaPlayerSuccess.play();
                        membershipStatusLabel.setText("Membresia Activa");
                        //enviarSeñalApertura("COM7");
                        //enviarATodasLosPuertos();
                        if (seleccionado != null) {
                            enviarSeñalApertura(seleccionado.getSystemPortName());
                            enviarDato(seleccionado, 1);
                            System.out.println("Puerto: " + seleccionado);
                        } else {
                            System.out.println("El puerto seleccionado es nulo. Verifica la selección del puerto.");
                        }
                        paneleft.setStyle("-fx-background-color: #98ff96;");
                    } else if(user.getStatus().equalsIgnoreCase("Activo") && user.getDuracion() == 1) {
                        mediaPlayerSuccess.stop();
                        mediaPlayerSuccess.seek(Duration.ZERO);
                        mediaPlayerSuccess.play();                        
                        membershipStatusLabel.setText("Activo - Hoy finaliza la membresia");
                        paneleft.setStyle("-fx-background-color: yellow;");
                        enviarSeñalApertura(seleccionado.getSystemPortName()); // Aquí envías la señal
                        enviarDato(seleccionado,1);
                    } else if(user.getStatus().equalsIgnoreCase("Desactivado")){
                        mediaPlayerError.stop();
                        mediaPlayerError.seek(Duration.ZERO);
                        mediaPlayerError.play();
                        membershipStatusLabel.setText("Sin membresia");
                        paneleft.setStyle("-fx-background-color: red;");
                    }
                    break;
                }
            }
            if (!encontrado) {
                System.out.println("Usuario no encontrado");
                nameLabel.setText("No encontrado");
                branchLabel.setText("No encontrado");
                membershipLabel.setText("No encontrado");
                durationLabel.setText("No encontrado");
                startDateLabel.setText("No encontrado");
                endDateLabel.setText("No encontrado");
                membershipStatusLabel.setText("Sin Membresia");
                paneleft.setStyle("-fx-background-color: #E1E1E1;");
            }
        } else {
            System.out.println("La lista de usuarios está vacía");
        }
    }
    
   /* public static void enviarSeñalApertura(String portName) {
        System.out.println("Intentando abrir el puerto: " + portName);
        SerialPort port = SerialPort.getCommPort(portName);
        
        if (port.openPort()) {
            System.out.println("Puerto " + portName + " abierto exitosamente");
            try {
                port.setComPortParameters(9600, 8, 1, SerialPort.NO_PARITY);
                port.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0);

                byte[] signal = {0x01};  // Enviando byte 0x01 en lugar de "1"
                int bytesWritten = 0;
                int maxRetries = 3;
                int retries = 0;

                while (bytesWritten < signal.length && retries < maxRetries) {
                    bytesWritten += port.writeBytes(signal, signal.length - bytesWritten);
                    retries++;
                    if (bytesWritten < signal.length) {
                        System.out.println("Reintento " + retries + " de envío de señal");
                        Thread.sleep(50);  // Pequeña pausa antes de reintentar
                    }
                }

                if (bytesWritten == signal.length) {
                    System.out.println("Señal enviada exitosamente al puerto " + portName);
                } else {
                    System.out.println("Error al enviar la señal al puerto " + portName + ". Bytes escritos: " + bytesWritten);
                }

                Thread.sleep(100);  // Espera 100ms para asegurar que la señal se envíe completamente
                port.flushIOBuffers();
            } catch (Exception e) {
                System.out.println("Error en la comunicación serial con el puerto " + portName + ": " + e.getMessage());
                e.printStackTrace();
            } finally {
                port.closePort();
                System.out.println("Puerto " + portName + " cerrado");
            }
        } else {
            System.out.println("No se pudo abrir el puerto " + portName);
        }
    }*/

   /* public static void enviarATodasLosPuertos() {
        SerialPort[] ports = SerialPort.getCommPorts();
        System.out.println("Puertos disponibles: " + ports.length);
        for (SerialPort port : ports) {
            System.out.println("Intentando enviar señal al puerto: " + port.getSystemPortName());
            enviarSeñalApertura(port.getSystemPortName());
        }
    }*/
    
     public static void enviarSeñalApertura(String portName) {
        SerialPort port = SerialPort.getCommPort(portName);
        
        if (port.openPort()) {
            try {
                port.setComPortParameters(9600, 8, 1, SerialPort.NO_PARITY);
                port.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0);

                byte[] signal = "1".getBytes();
                int bytesWritten = port.writeBytes(signal, signal.length);

                if (bytesWritten == signal.length) {
                    System.out.println("Señal enviada exitosamente");
                } else {
                    System.out.println("Error al enviar la señal");
                }
            } finally {
                port.closePort();
            }
        } else {
            System.out.println("No se pudo abrir el puerto " + portName);
        }
    }

    /*public static void enviarATodasLosPuertos() {
        SerialPort[] ports = SerialPort.getCommPorts();
        for (SerialPort port : ports) {
            enviarSeñalApertura(port.getSystemPortName());
        }
    }*/
    public void enviarATodasLosPuertos() {
    Task<Void> task = new Task<Void>() {
        @Override
        protected Void call() {
            SerialPort[] ports = SerialPort.getCommPorts();
            System.out.println("Puertos disponibles: " + ports.length);
            for (SerialPort port : ports) {
                System.out.println("Intentando enviar señal al puerto: " + port.getSystemPortName());
                enviarSeñalApertura(port.getSystemPortName());
            }
            return null;
        }
    };

    // Ejecutar el task en un hilo separado
    new Thread(task).start();
}
    
    //nuevo metodo
    /*public static void enviarDato(SerialPort puerto, String dato) {
    if (puerto != null && puerto.openPort()) {
        try {
            byte[] data = dato.getBytes();  // Convertimos el dato a bytes
            puerto.writeBytes(data, data.length);  // Enviamos los datos al puerto
            System.out.println("Dato enviado: " + dato);
        } catch (Exception e) {
            System.out.println("Error al enviar el dato: " + e.getMessage());
        } finally {
            puerto.closePort();  // Cerramos el puerto después de enviar el dato
        }
    } else {
        System.out.println("El puerto no se pudo abrir o es nulo.");
    }
}*/
    
    public static void enviarDato(SerialPort puerto, int dato) {
    if (puerto == null) {
        System.out.println("Error: El puerto es nulo.");
        return;
    }

    if (!puerto.isOpen() && !puerto.openPort()) {
        System.out.println("Error: No se pudo abrir el puerto " + puerto.getSystemPortName());
        return;
    }

    try {
        puerto.setComPortParameters(9600, 8, 1, SerialPort.NO_PARITY);
        puerto.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0);

        byte[] data = {(byte) dato};  // Convertimos el entero a un byte
        int bytesWritten = puerto.writeBytes(data, data.length);

        if (bytesWritten == data.length) {
            System.out.println("Dato enviado exitosamente: " + dato);
        } else {
            System.out.println("No se pudo enviar el dato completo. Bytes enviados: " + bytesWritten + " de " + data.length);
        }

        Thread.sleep(100);  // Pequeña pausa para asegurar que el dato se envíe completamente
        puerto.flushIOBuffers(); // Correcto
    } catch (Exception e) {
        System.out.println("Error al enviar el dato: " + e.getMessage());
        e.printStackTrace();
    } finally {
        if (puerto.isOpen()) {
            puerto.closePort();
            System.out.println("Puerto cerrado.");
        }
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
            System.out.println("Hay lectores disponibles");
            reader = readers.get(0);
            String Lector = reader.GetDescription().name;
            System.out.println("El lector es: "+Lector);
            reader.Open(Reader.Priority.EXCLUSIVE);
        }else {
            System.out.println("No se encontraron lectores");
        }
    }
    catch (UareUException e) {
        e.printStackTrace();
    }
    return reader;
}
    
   
     /*public void compareFingerprint(List<User> userData) {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    ReaderCollection readers = UareUGlobal.GetReaderCollection();
                    readers.GetReaders();
                    if (readers.size() > 0) {
                        Reader reader = readers.get(0);
                        try {
                            reader.Open(Reader.Priority.EXCLUSIVE);
                            Fmd capturedFmd = capturarHuella(reader);
                            System.out.println("capturada: "+capturedFmd);
                            
                            if (capturedFmd != null) {
                                boolean huellaEncontrada = false;
                                for (User user : userData) {
                                    //Fmd storedFmd = user.getHuellaFmd();
                                    //System.out.println("se comparara con: "+storedFmd);
                                    System.out.println("MIRA LOS QUE SE RECORREN:" +user.getHuellaFmd());

                                    if (user.getHuellaFmd() != null) {
                                        try {
                                            int score = UareUGlobal.GetEngine().Compare(capturedFmd, 0, user.getHuellaFmd(), 0);
                                            //int targetFalsematchRate = Engine.PROBABILITY_ONE / 100000; // Target rate is 0.00001
                                            int threshold = 100000; // Ajusta este valor según tus necesidades

                                            if (score < threshold) {
                                                //System.out.println("comparando: capturada: "+capturedFmd + "se compara con: "+storedFmd);
                                                System.out.println("Se encontró una huella coincidente para el usuario: " + user.getNombre());
                                                huellaEncontrada = true;
                                                System.out.println("huella encntrada: " + huellaEncontrada);
                                                
                                                getPort();
        
                                                
                                                if (seleccionado != null) {
                                                    if (user.getStatus().equalsIgnoreCase("Activo")){
                                                       enviarSeñalApertura(seleccionado.getSystemPortName());
                                                        enviarDato(seleccionado, 1);
                                                        System.out.println("Usuario encontrado: " + user);
                                                        System.out.println("Valor de Nombre: " + user.getNombre());
                                                        System.out.println("Actualizando la interfaz con los datos del usuario...");
                                                        nameLabel.setText(user.getNombre()); 
                                                    } else {
                                                        System.out.println("Usuario desactivado");
                                                    }
                                                     
                                                } else {
                                                    System.out.println("El puerto seleccionado es nulo.");
                                                    System.out.println("Valor de seleccionado: " + seleccionado);
                                                }
                                                break;
                                            }
                                        } catch (UareUException e) {
                                            System.err.println("Error al comparar huellas: " + e.getMessage());
                                        }
                                    }
                                }
                                if (!huellaEncontrada) {
                                    System.out.println("No se encontró ninguna huella coincidente.");
                                   
                                    //capturarHuella(reader);
                                    compareFingerprint(userData);
                                }
                            } else {
                                System.out.println("No se pudo capturar la huella.");
                            }
                        } finally {
                            try {
                                reader.Close();
                            } catch (UareUException e) {
                                System.err.println("Error al cerrar el lector: " + e.getMessage());
                            }
                        }
                    } else {
                        System.out.println("No se encontraron lectores de huellas dactilares.");
                    }
                } catch (UareUException e) {
                    System.err.println("Error: " + e.getMessage());
                }
                return null;
            }
        };
        worker.execute();
    }*/
    
    
    public void compareFingerprint(List<User> userData) {
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
                                System.out.println("capturada: " + capturedFmd);
                                
                                if (capturedFmd != null) {
                                    boolean huellaEncontrada = false;
                                    for (User user : userData) {
                                        if (user.getHuellaFmd() != null) {
                                            try {
                                                int score = UareUGlobal.GetEngine().Compare(capturedFmd, 0, user.getHuellaFmd(), 0);
                                                int threshold = 100000;
                                                
                                                if (score < threshold) {
                                                    System.out.println("Se encontró una huella coincidente para el usuario: " + user.getNombre());
                                                    huellaEncontrada = true;
                                                    
                                                    actualizarDatosUsuario(user);
                                                    
                                                    //Thread.sleep(2000);
                                                    break;
                                                }
                                            } catch (UareUException e) {
                                                System.err.println("Error al comparar huellas: " + e.getMessage());
                                            }
                                        }
                                    }
                                    if (!huellaEncontrada) {
                                        System.out.println("No se encontró ninguna huella coincidente.");
                                        Thread.sleep(1000);
                                    }
                                } else {
                                    System.out.println("No se pudo capturar la huella.");
                                    Thread.sleep(500);
                                }
                            } catch (Exception e) {
                                System.err.println("Error en el ciclo de captura: " + e.getMessage());
                                Thread.sleep(1000);
                            }
                        }
                        
                        reader.Close();
                    } else {
                        System.out.println("No se encontraron lectores de huellas dactilares.");
                    }
                } catch (UareUException e) {
                    System.err.println("Error: " + e.getMessage());
                }
                return null;
            }
        };
        
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
    
    
    
    // Método para actualizar los datos del usuario y gestionar el puerto
    private void actualizarDatosUsuario(User user) {
        Platform.runLater(() -> {
        try {
            System.out.println("Actualizando datos del usuario en el hilo de JavaFX.");
            getPort(); // Actualiza el puerto seleccionado

            if (seleccionado != null && user.getStatus().equalsIgnoreCase("Activo") && user.getDuracion() >= 10) {
                System.out.println("Nombre del usuario: " + user.getNombre());
                System.out.println("Membresia: " + user.getMembresia());
                enviarSeñalApertura(seleccionado.getSystemPortName());
                enviarDato(seleccionado, 1);

                // Actualiza los labels con la información del usuario
                nameLabel.setText(user.getNombre());
                branchLabel.setText(user.getSucursal());
                membershipLabel.setText(user.getMembresia());
                durationLabel.setText(user.getDuracion().toString());
                startDateLabel.setText(user.getFechaInicio());
                endDateLabel.setText(user.getFechaFin());
                membershipStatusLabel.setText("Membresía Activa");

                // Cambia el color del panel si la membresía es válida
                paneleft.setStyle("-fx-background-color: #98ff96;");
                mediaPlayerSuccess.stop();
                mediaPlayerSuccess.seek(Duration.ZERO);
                mediaPlayerSuccess.play();
            } else if(user.getStatus().equalsIgnoreCase("Activo") && user.getDuracion() == 1) {
                mediaPlayerSuccess.stop();
                mediaPlayerSuccess.seek(Duration.ZERO);
                mediaPlayerSuccess.play();                        
                membershipStatusLabel.setText("Activo - Hoy finaliza la membresia");
                paneleft.setStyle("-fx-background-color: yellow;");
                enviarSeñalApertura(seleccionado.getSystemPortName()); // Aquí envías la señal
                enviarDato(seleccionado,1);
                
                // Actualiza los labels con la información del usuario
                nameLabel.setText(user.getNombre());
                branchLabel.setText(user.getSucursal());
                membershipLabel.setText(user.getMembresia());
                durationLabel.setText(user.getDuracion().toString());
                startDateLabel.setText(user.getFechaInicio());
                endDateLabel.setText(user.getFechaFin());
                membershipStatusLabel.setText("Visita");
            } else {
                System.out.println("Usuario no activo o puerto no disponible.");
                membershipStatusLabel.setText("Usuario desactivado");
                paneleft.setStyle("-fx-background-color: red;");
                mediaPlayerError.stop();
                mediaPlayerError.seek(Duration.ZERO);
                mediaPlayerError.play();
                
                // Actualiza los labels con la información del usuario
                nameLabel.setText(user.getNombre());
                branchLabel.setText(user.getSucursal());
                membershipLabel.setText(user.getMembresia());
                durationLabel.setText(user.getDuracion().toString());
                startDateLabel.setText(user.getFechaInicio());
                endDateLabel.setText(user.getFechaFin());
                membershipStatusLabel.setText("Sin Membresía");
            }
        } catch (Exception e) {
            System.err.println("Error al actualizar datos del usuario: " + e.getMessage());
            e.printStackTrace();
        }
        });
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
    
    
    //METODO OKE OKEY OKEY
  /*  public static Fmd capturarHuella(Reader reader) {
    System.out.println("ESPERANDO LECTURA JEJEJE");

    SwingWorker<Fmd, Void> worker = new SwingWorker<Fmd, Void>() {
        @Override
        protected Fmd doInBackground() throws Exception {
            Reader.CaptureResult captureResult = null;
            try {
                captureResult = reader.Capture(
                    Fid.Format.ANSI_381_2004,
                    Reader.ImageProcessing.IMG_PROC_DEFAULT,
                    500,
                    -1
                );
            } catch (UareUException e) {
                e.printStackTrace();
                return null;
            }

            // Verificar si la captura fue exitosa y la calidad es buena
            if (captureResult != null && captureResult.quality == Reader.CaptureQuality.GOOD) {
                try {
                    // Crear un FMD a partir de la huella capturada
                    Fmd fmd = UareUGlobal.GetEngine().CreateFmd(
                        captureResult.image,
                        Fmd.Format.ANSI_378_2004
                    );

                    System.out.println("Captura de huella dactilar exitosa!");
                    System.out.println("FMD capturado: " + fmd);
                    return fmd;

                } catch (Exception ex) {
                    System.out.println("Error al crear el FMD: " + ex.getMessage());
                    ex.printStackTrace();
                    return null;
                }
            } else {
                System.out.println("La captura de la huella dactilar no fue exitosa. Calidad: " + (captureResult != null ? captureResult.quality : "null"));
                return null;
            }
        }

        @Override
        protected void done() {
            try {
                Fmd fmd = null;
                try {
                    fmd = get();
                } catch (ExecutionException ex) {
                    Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
                }

                if (fmd != null) {
                    System.out.println("Captura completada con éxito.");
                } else {
                    System.out.println("No se pudo capturar la huella.");
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    };

    worker.execute();
    return null;
}*/
    
   
    public static void convertHuellas(List<User> dataUser) {
    if (!dataUser.isEmpty()) {
        for (User u : dataUser) {
            String huella = u.getHuella();
            if (!"".equals(huella)) {
                try {
                    // Usar el método decodificarFMD para convertir la huella
                    Fmd fmd = decodificarFMD(huella);
                    
                    if (fmd != null) {
                        // Establecer el Fmd en el objeto User
                        u.setHuella(fmd);
                        System.out.println("Huella a fmd jijiji: "+u.getHuellaFmd());
                    } else {
                        System.err.println("No se pudo decodificar la huella para el usuario " + u.getNombre());
                    }
                } catch (Exception e) {
                    System.err.println("Error al convertir la huella para el usuario " + u.getNombre() + ": " + e.getMessage());
                }
            }
        }
    }
}

    public static Fmd decodificarFMD(String base64) {
        try {
            // Decodificar la cadena Base64 a un arreglo de bytes
            byte[] data = Base64.getDecoder().decode(base64);

            // Crear un objeto FMD a partir del arreglo de bytes
            Fmd fmd = UareUGlobal.GetImporter().ImportFmd(data, Fmd.Format.ANSI_378_2004, Fmd.Format.ANSI_378_2004);

            return fmd;
        } catch (Exception e) {
            e.printStackTrace();
            return null; // En caso de error, retornar null
        }
    }
    
}