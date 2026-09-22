package es.uma.alquiler.dtos;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import es.uma.alquiler.entidades.Alquiler;
import es.uma.alquiler.entidades.EstadoAlquiler;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class AlquilerDTO {

    private Long id;

    @NotNull(message = "El número de pasajeros previstos es obligatorio.")
    @Min(value = 1, message = "El número de pasajeros previstos debe ser al menos 1.")
    private Integer pasajerosPrevistos;

    @NotNull(message = "La fecha y hora de inicio es obligatoria.")
    private LocalDateTime fechaInicio;

    @NotNull(message = "La fecha y hora de fin es obligatoria.")
    private LocalDateTime fechaFin;

    // Lo asigna siempre el servicio (R1); si llega en una petición se ignora
    private EstadoAlquiler estado;

    private Long vehiculoId;

    // Solo informativo en las respuestas, para no tener que pedir el vehículo aparte
    private VehiculoDTO vehiculo;

    public AlquilerDTO() {
    }


    public Alquiler aEntidad() {
        Alquiler alquiler = new Alquiler();
        alquiler.setId(this.id);
        alquiler.setPasajerosPrevistos(this.pasajerosPrevistos);
        alquiler.setFechaInicio(this.fechaInicio);
        alquiler.setFechaFin(this.fechaFin);
        alquiler.setEstado(this.estado);
        return alquiler;
    }

    /**
     * Vehículo que se quiere alquilar: se acepta tanto {"vehiculoId": 1}
     * como {"vehiculo": {"id": 1}}.
     */
    @JsonIgnore
    public Long getVehiculoSolicitado() {
        if (vehiculoId != null) {
            return vehiculoId;
        }
        return vehiculo != null ? vehiculo.getId() : null;
    }

    public static AlquilerDTO convertirADTO(Alquiler al) {
        AlquilerDTO dto = new AlquilerDTO();
        dto.setId(al.getId());
        dto.setPasajerosPrevistos(al.getPasajerosPrevistos());
        dto.setFechaInicio(al.getFechaInicio());
        dto.setFechaFin(al.getFechaFin());
        dto.setEstado(al.getEstado());
        if (al.getVehiculo() != null) {
            dto.setVehiculoId(al.getVehiculo().getId());
            dto.setVehiculo(VehiculoDTO.convertirADTO(al.getVehiculo()));
        }
        return dto;
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

    public Long getVehiculoId() {
        return vehiculoId;
    }

    public void setVehiculoId(Long vehiculoId) {
        this.vehiculoId = vehiculoId;
    }

    public VehiculoDTO getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(VehiculoDTO vehiculo) {
        this.vehiculo = vehiculo;
    }
}
