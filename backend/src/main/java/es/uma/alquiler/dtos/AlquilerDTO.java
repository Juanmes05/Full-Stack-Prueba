package es.uma.alquiler.dtos;

import java.time.LocalDateTime;
import es.uma.alquiler.entidades.Alquiler;
import es.uma.alquiler.entidades.EstadoAlquiler;

public class AlquilerDTO {

    private Long id;
    private Integer pasajerosPrevistos;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private EstadoAlquiler estado;
    private Long vehiculoId;

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

    public static AlquilerDTO convertirADTO(Alquiler al) {
        AlquilerDTO dto = new AlquilerDTO();
        dto.setId(al.getId());
        dto.setPasajerosPrevistos(al.getPasajerosPrevistos());
        dto.setFechaInicio(al.getFechaInicio());
        dto.setFechaFin(al.getFechaFin());
        dto.setEstado(al.getEstado());
        if (al.getVehiculo() != null) {
            dto.setVehiculoId(al.getVehiculo().getId());
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
}
