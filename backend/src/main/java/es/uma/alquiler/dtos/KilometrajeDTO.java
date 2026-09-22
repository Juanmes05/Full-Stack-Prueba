package es.uma.alquiler.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * Cuerpo de PUT /vehiculos/{id}/kilometraje: {"kilometraje": 123456}.
 * También acepta {"kilometrajeActual": 123456}, el nombre del campo en VehiculoDTO.
 */
public class KilometrajeDTO {

    @JsonAlias("kilometrajeActual")
    private Integer kilometraje;

    public KilometrajeDTO() {
    }

    public Integer getKilometraje() {
        return kilometraje;
    }

    public void setKilometraje(Integer kilometraje) {
        this.kilometraje = kilometraje;
    }
}
