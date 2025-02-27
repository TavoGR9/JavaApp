package huellatorniquete.models;

import com.digitalpersona.uareu.Fmd;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

public class User {
    @JsonProperty("clave")
    private String clave;

    @JsonProperty("nombreCompleto")
    private String nombreCompleto;

    @JsonProperty("huella")
    private String huella;

    @JsonProperty("estafeta")
    private String estafeta;

    @JsonProperty("id_bodega")
    private String idBodega;

    @JsonProperty("fechaInicio")
    private String fechaInicio;

    @JsonProperty("fechaFin")
    private String fechaFin;

    @JsonProperty("estatus")
    private String estatus;

    @JsonProperty("titulo")
    private String titulo;

    @JsonProperty("duracion")
    private String duracion;

    @JsonProperty("precio")
    private String precio;
    
    @JsonProperty("estatusQR")
    private String estatusQR;

    private Fmd BinHuella;

    @Override
    public String toString() {
        return "User{" +
            "clave='" + clave + '\'' +
            ", nombreCompleto='" + nombreCompleto + '\'' +
            ", huella='" + huella + '\'' +
            ", estafeta='" + estafeta + '\'' +
            ", idBodega='" + idBodega + '\'' +
            ", fechaInicio='" + fechaInicio + '\'' +
            ", fechaFin='" + fechaFin + '\'' +
            ", estatus='" + estatus + '\'' +
            ", titulo='" + titulo + '\'' +
            ", duracion='" + duracion + '\'' +
            ", precio='" + precio + '\'' +
            ", estatusQR='" + estatusQR + '\'' +
            '}';
    }

    // Getters
    public String getClave() {
        return clave;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getHuella() {
        return huella;
    }

    public String getEstafeta() {
        return estafeta;
    }

    public String getIdBodega() {
        return idBodega;
    }

    public String getFechaInicio() {
        return fechaInicio;
    }

    public String getFechaFin() {
        return fechaFin;
    }

    public String getEstatus() {
        return estatus;
    }

    public String getTitulo() {
        return titulo;
    }

    public Integer getDuracion() {
        if (duracion == null || duracion.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(duracion);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Integer getPrecio() {
        if (precio == null || precio.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(precio);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    public Integer getEstatusQR() {
        if (estatusQR == null || estatusQR.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(estatusQR);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Fmd getHuellaFmd() {
        return BinHuella;
    }
    
    
    public long getDaysBetweenDate(String fechaFin) {
    LocalDate fechaFinDate = LocalDate.parse(fechaFin);
    LocalDate fechaActual = LocalDate.now();
    
    return ChronoUnit.DAYS.between(fechaActual, fechaFinDate);
}

    // Setters
    public void setClave(String clave) {
        this.clave = clave;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public void setHuella(String huella) {
        this.huella = huella;
    }

    public void setEstafeta(String estafeta) {
        this.estafeta = estafeta;
    }

    public void setIdBodega(String idBodega) {
        this.idBodega = idBodega;
    }

    public void setFechaInicio(String fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public void setFechaFin(String fechaFin) {
        this.fechaFin = fechaFin;
    }

    public void setEstatus(String estatus) {
        this.estatus = estatus;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setDuracion(String duracion) {
        this.duracion = duracion;
    }

    public void setPrecio(String precio) {
        this.precio = precio;
    }
    
    public void setEstatusQR(String estatusQR) {
        this.estatusQR = estatusQR;
    }

    public void setHuellaFmd(Fmd huella) {
        this.BinHuella = huella;
    }
}