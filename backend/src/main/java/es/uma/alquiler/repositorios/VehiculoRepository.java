package es.uma.alquiler.repositorios;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.uma.alquiler.entidades.Vehiculo;


@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {

    @Query("SELECT v FROM Vehiculo v WHERE v.capacidadMaxima >= :pasajeros AND v.id NOT IN (" +
           "SELECT a.vehiculo.id FROM Alquiler a WHERE a.estado = 'CONFIRMADO' " +
           "AND a.fechaInicio < :fin AND a.fechaFin > :inicio)")
    List<Vehiculo> findVehiculosLibres(
            @Param("inicio") LocalDateTime inicio, 
            @Param("fin") LocalDateTime fin, 
            @Param("pasajeros") Integer pasajeros);
    boolean existsByMatricula(String matricula);
}
