package es.uma.alquiler.repositorios;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.uma.alquiler.entidades.EstadoAlquiler;
import es.uma.alquiler.entidades.Vehiculo;
import jakarta.persistence.LockModeType;

@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {

    boolean existsByMatricula(String matricula);

    /**
     * Vehículos con capacidad suficiente y sin alquileres en el estado indicado
     * (normalmente CONFIRMADO) que se solapen con el intervalo [inicio, fin).
     */
    @Query("SELECT v FROM Vehiculo v WHERE v.capacidadMaxima >= :pasajeros AND NOT EXISTS (" +
           "SELECT a FROM Alquiler a WHERE a.vehiculo = v AND a.estado = :estado " +
           "AND a.fechaInicio < :fin AND a.fechaFin > :inicio) ORDER BY v.id")
    List<Vehiculo> findVehiculosLibres(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("pasajeros") Integer pasajeros,
            @Param("estado") EstadoAlquiler estado);

    /**
     * Carga el vehículo bloqueando su fila hasta el final de la transacción. Así dos
     * confirmaciones simultáneas sobre el mismo vehículo no pueden saltarse la regla R6.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM Vehiculo v WHERE v.id = :id")
    Optional<Vehiculo> findByIdParaActualizar(@Param("id") Long id);
}
