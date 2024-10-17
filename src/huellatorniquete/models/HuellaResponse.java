
package huellatorniquete.models;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HuellaResponse {
    private String huella;
    @JsonProperty("ID")
    private String ID;
    @JsonProperty("Status_Num")
    private String Status_Num;
    @JsonProperty("Gimnasio_idGimnasio")
    private String Gimnasio_idGimnasio;
    @JsonProperty("IDUsuario")
    private String IDUsuario;

    // Getters y Setters
    public String getHuella() {
        return huella;
    }

    public void setHuella(String huella) {
        this.huella = huella;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getStatus_Num() {
        return Status_Num;
    }

    public void setStatus_Num(String status_Num) {
        Status_Num = status_Num;
    }

    public String getGimnasio_idGimnasio() {
        return Gimnasio_idGimnasio;
    }

    public void setGimnasio_idGimnasio(String gimnasio_idGimnasio) {
        Gimnasio_idGimnasio = gimnasio_idGimnasio;
    }

    public String getIDUsuario() {
        return IDUsuario;
    }

    public void setIDUsuario(String IDUsuario) {
        this.IDUsuario = IDUsuario;
    }
}
