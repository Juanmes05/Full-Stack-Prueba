package es.uma.alquiler.dtos;

import es.uma.alquiler.entidades.TipoVehiculo;
import es.uma.alquiler.entidades.Vehiculo;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class VehiculoDTO {

    private Long id;

    @NotBlank(message = "La matrícula es obligatoria.")
    private String matricula;

    @NotNull(message = "El tipo de vehículo es obligatorio (TURISMO, SUV o FURGONETA).")
    private TipoVehiculo tipo;

    @NotNull(message = "La capacidad máxima es obligatoria.")
    @Min(value = 1, message = "La capacidad máxima debe ser de al menos 1 pasajero.")
    private Integer capacidadMaxima;

    @NotNull(message = "El kilometraje actual es obligatorio.")
    @Min(value = 0, message = "El kilometraje debe ser un número mayor o igual que 0.")
    private Integer kilometrajeActual;

    public VehiculoDTO() {
    }


    public Vehiculo aEntidad() {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(this.id);
        vehiculo.setMatricula(this.matricula);
        vehiculo.setTipo(this.tipo);
        vehiculo.setCapacidadMaxima(this.capacidadMaxima);
        vehiculo.setKilometrajeActual(this.kilometrajeActual);
        return vehiculo;
    }

    public static VehiculoDTO convertirADTO(Vehiculo v) {
        VehiculoDTO dto = new VehiculoDTO();
        dto.setId(v.getId());
        dto.setMatricula(v.getMatricula());
        dto.setTipo(v.getTipo());
        dto.setCapacidadMaxima(v.getCapacidadMaxima());
        dto.setKilometrajeActual(v.getKilometrajeActual());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public TipoVehiculo getTipo() {
        return tipo;
    }

    public void setTipo(TipoVehiculo tipo) {
        this.tipo = tipo;
    }

    public Integer getCapacidadMaxima() {
        return capacidadMaxima;
    }

    public void setCapacidadMaxima(Integer capacidadMaxima) {
        this.capacidadMaxima = capacidadMaxima;
    }

    public Integer getKilometrajeActual() {
        return kilometrajeActual;
    }

    public void setKilometrajeActual(Integer kilometrajeActual) {
        this.kilometrajeActual = kilometrajeActual;
    }
}
