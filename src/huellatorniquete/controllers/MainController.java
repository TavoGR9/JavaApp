package huellatorniquete.controllers;

import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.Reader;
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.scene.layout.VBox;
import javax.swing.SwingWorker;

public class MainController {
    
    List<User> userData = new ArrayList<>();
    SerialPort seleccionado = null;  // Inicializamos como null

    
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
        System.out.println("Lista de usuarios cargada: " + userData);
        
    SerialPort[] ports = SerialPort.getCommPorts();
    for (SerialPort port : ports) {
    System.out.println("names ports: " + port.getDescriptivePortName());
    if (port.getDescriptivePortName().contains("USB-SERIAL")) {
        seleccionado = port;
        System.out.println("seleccionado puerto: "+seleccionado);
        break;  // Detenemos el ciclo si ya encontramos el puerto
    }
}
        
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
                        membershipStatusLabel.setText("Membresia Activa");
                        //enviarSeñalApertura("COM7");
                        //enviarATodasLosPuertos();
                        enviarDato(seleccionado,1);
                        paneleft.setStyle("-fx-background-color: #98ff96;");
                    } else if(user.getStatus().equalsIgnoreCase("Activo") && user.getDuracion() == 1) {
                        membershipStatusLabel.setText("Activo - Hoy finaliza la membresia");
                        paneleft.setStyle("-fx-background-color: yellow;");
                        enviarDato(seleccionado,1);
                    } else if(user.getStatus().equalsIgnoreCase("Desactivado")){
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
        puerto.flushIOBuffers();

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
    
    
    public static Fmd capturarHuella(Reader reader) {
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
}

    //audio notificaciones
   /* public void reproducirAudioMP3(String rutaArchivo) {
    try {
        FileInputStream archivo = new FileInputStream(rutaArchivo);
        Player reproductor = new Player(archivo);
        reproductor.play();
    } catch (FileNotFoundException | JavaLayerException e) {
        e.printStackTrace();
    }
}*/

    
    
}