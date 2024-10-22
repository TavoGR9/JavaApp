
package huellatorniquete.models;
import com.digitalpersona.uareu.Fmd;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.sql.Blob;
import java.util.Arrays;
import java.util.Base64;

/**
 *
 * @author guzma
 */
public class User {
    @JsonProperty("ID")
    private String ID;
    
    @JsonProperty("Nombre")
    private String Nombre;

    @JsonProperty("telefono")
    private String telefono;

    @JsonProperty("email")
    private String email;

    @JsonProperty("Sucursal")
    private String Sucursal;

    @JsonProperty("Membresia")
    private String Membresia;

    @JsonProperty("Info_Membresia")
    private String Info_Membresia;

    @JsonProperty("Duracion")
    private String Duracion;

    @JsonProperty("Precio")
    private String Precio;

    @JsonProperty("Fecha_Inicio")
    private String Fecha_Inicio;

    @JsonProperty("Fecha_Fin")
    private String Fecha_Fin;

    @JsonProperty("STATUS")
    private String STATUS;

    @JsonProperty("idDetMem")
    private String idDetMem;

    @JsonProperty("Membresia_idMem")
    private String Membresia_idMem;

    @JsonProperty("Gimnasio_idGimnasio")
    private String Gimnasio_idGimnasio;

    @JsonProperty("accion")
    private String accion;

    @JsonProperty("foto")
    private String foto;

    @JsonProperty("huella")
    private String huella;

    @JsonProperty("Status_Num")
    private String Status_Num;

    @JsonProperty("estatusPago")
    private String estatusPago;

    @JsonProperty("rol")
    private String rol;

    @JsonProperty("estafeta")
    private String estafeta;

    @JsonProperty("servicio")
    private String servicio;

    @JsonProperty("fechaRegistro")
    private String fechaRegistro;
    
    private Fmd BinHuella;

    
    
    @Override
public String toString() {
     //String huellaString = (huella != null) ? Base64.getEncoder().encodeToString(huella) : "null"; // Convertir huella a Base64
    return "User{" +
            "ID='" + ID + '\'' +
            ", Nombre='" + Nombre + '\'' +
            ", telefono='" + telefono + '\'' +
            ", email='" + email + '\'' +
            ", Sucursal='" + Sucursal + '\'' +
            ", Membresia='" + Membresia + '\'' +
            ", Info_Membresia='" + Info_Membresia + '\'' +
            ", Duracion='" + Duracion + '\'' +
            ", Precio='" + Precio + '\'' +
            ", Fecha_Inicio='" + Fecha_Inicio + '\'' +
            ", Fecha_Fin='" + Fecha_Fin + '\'' +
            ", STATUS='" + STATUS + '\'' +
            ", idDetMem='" + idDetMem + '\'' +
            ", Membresia_idMem='" + Membresia_idMem + '\'' +
            ", Gimnasio_idGimnasio='" + Gimnasio_idGimnasio + '\'' +
            ", accion='" + accion + '\'' +
            ", foto='" + foto + '\'' +
            ", huella='" + huella + '\'' + // Mostrar array de huella de forma legible
            ", Status_Num='" + Status_Num + '\'' +
            ", estatusPago='" + estatusPago + '\'' +
            ", rol='" + rol + '\'' +
            ", estafeta='" + estafeta + '\'' +
            ", servicio='" + servicio + '\'' +
            ", fechaRegistro='" + fechaRegistro + '\'' +
            ", BinHuella='" + BinHuella + '\'' +
            '}';
}

     public Integer getId() {
        return ID != null ? Integer.parseInt(ID) : null;
    }

    public String getNombre() {
        return Nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getEmail() {
        return email;
    }

    public String getSucursal() {
        return Sucursal;
    }

    public String getMembresia() {
        return Membresia;
    }

    public String getInfoMembresia() {
       return Info_Membresia;
    }

public Integer getDuracion() {
        if (Duracion == null || Duracion.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(Duracion);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Integer getPrecio() {
        if (Precio == null || Precio.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(Precio);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public String getFechaInicio() {
        return Fecha_Inicio;
    }

    public String getFechaFin() {
        return Fecha_Fin;
    }

    public String getStatus() {
        return STATUS;
    }

    public Integer getIdDetMem() {
        if ( idDetMem == null || idDetMem.isEmpty()) {
        return null;
    }
        try{
            return Integer.parseInt(idDetMem);
        } catch(NumberFormatException e){
            return null;
        }
    }

    public Integer getMembresiaIdMem() {
        if (Membresia_idMem == null || Membresia_idMem.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(Membresia_idMem);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    public Integer getGimnasioIdGimnasio() {
        if (Gimnasio_idGimnasio == null || Gimnasio_idGimnasio.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(Gimnasio_idGimnasio);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    public Integer getStatusNum() {
        if (Status_Num == null || Status_Num.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(Status_Num);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public String getAccion() {
        return accion;
    }

    public String getFoto() {
        return foto;
    }
    
    public String getHuella(){
        return huella;
    }

    public String getEstatusPago() {
        return estatusPago;
    }

    public String getRol() {
        return rol;
    }

    public String getEstafeta() {
        return estafeta;
    }

    public String getServicio() {
        return servicio;
    }

    public String getFechaRegistro() {
        return fechaRegistro;
    }
    
    public Fmd getHuellaFmd() {
        return BinHuella;
    }
    
    //setters

    public void setID(String ID) {
        this.ID = ID;
    }
    
    public void setNombre(String Nombre) {
        this.Nombre = Nombre;
    }
    
     public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
     
     public void setEmail(String email) {
        this.email = email;
    }
    
     public void setSucursal(String Sucursal) {
        this.Sucursal = Sucursal;
    }
     
     public void setMembresia(String Membresia) {
        this.Membresia = Membresia;
    }
     
      public void setInfoMembresia(String Info_Membresia) {
        this.Info_Membresia = Info_Membresia;
    }
      
     public void setDuracion(String Duracion) {
        this.Duracion = Duracion;
    }
     
     public void setPrecio(String Precio) {
        this.Precio = Precio;
    }
     
     public void setFechaInicio(String Fecha_Inicio) {
        this.Fecha_Inicio = Fecha_Inicio;
    }
     
     public void setFechaFin(String Fecha_Fin) {
        this.Fecha_Fin = Fecha_Fin;
    }
     
     public void setSTATUS(String STATUS) {
        this.STATUS = STATUS;
    }
     
     public void setIdDetMem(String idDetMem) {
        this.idDetMem = idDetMem;
    }
    
     public void setMembresiaIdMem(String Membresia_idMem) {
        this.Membresia_idMem = Membresia_idMem;
    }

    public void setGimnasioIdGimnasio(String Gimnasio_idGimnasio) {
        this.Gimnasio_idGimnasio = Gimnasio_idGimnasio;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public void setHuella(String huella) {
        this.huella = huella;
    }

    public void setStatusNum(String Status_Num) {
        this.Status_Num = Status_Num;
    }

    public void setEstatusPago(String estatusPago) {
        this.estatusPago = estatusPago;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public void setEstafeta(String estafeta) {
        this.estafeta = estafeta;
    }

    public void setServicio(String servicio) {
        this.servicio = servicio;
    }

    public void setFechaRegistro(String fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
    
    public void setHuella(Fmd huella){
        this.BinHuella = huella;
    }
}
