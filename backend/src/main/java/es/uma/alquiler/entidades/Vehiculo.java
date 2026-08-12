package es.uma.alquiler.entidades;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vehiculos")
public class Vehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    @Column(nullable = false, unique = true)
    private String matricula; 

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoVehiculo tipo; 

    @Column(name = "capacidad_maxima", nullable = false)
    private Integer capacidadMaxima;

    @Column(name = "kilometraje_actual", nullable = false)
    private Integer kilometrajeActual;

    @OneToMany(mappedBy = "vehiculo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Alquiler> alquileres = new ArrayList<>();

    public Vehiculo() {
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

    public List<Alquiler> getAlquileres() {
        return alquileres;
    }

    public void setAlquileres(List<Alquiler> alquileres) {
        this.alquileres = alquileres;
    }
}