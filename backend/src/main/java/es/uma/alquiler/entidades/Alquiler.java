package es.uma.alquiler.entidades;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alquileres")
public class Alquiler {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pasajeros_previstos", nullable = false)
    private Integer pasajerosPrevistos; 

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio; 

    @Column(name = "fecha_fin", nullable = false)
    private LocalDateTime fechaFin; 

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoAlquiler estado; 

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehiculo_id", nullable = false)
    private Vehiculo vehiculo; 

    
    public Alquiler() {
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getPasajerosPrevistos() {
        return pasajerosPrevistos;
    }

    public void setPasajerosPrevistos(Integer pasajerosPrevistos) {
        this.pasajerosPrevistos = pasajerosPrevistos;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public EstadoAlquiler getEstado() {
        return estado;
    }

    public void setEstado(EstadoAlquiler estado) {
        this.estado = estado;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(Vehiculo vehiculo) {
        this.vehiculo = vehiculo;
    }
}