package es.uma.alquiler.repositorios;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.uma.alquiler.entidades.Alquiler;
import es.uma.alquiler.entidades.EstadoAlquiler;
import es.uma.alquiler.entidades.Vehiculo;

@Repository
public interface AlquilerRepository extends JpaRepository<Alquiler, Long> {

    List<Alquiler> findByVehiculoId(Long vehiculoId);

    List<Alquiler> findByEstado(EstadoAlquiler estado);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Alquiler a " +
           "WHERE a.vehiculo = :vehiculo AND a.estado = :estado " +
           "AND a.fechaInicio < :fin AND a.fechaFin > :inicio")
    boolean existsOverlappingAlquileres(
            @Param("vehiculo") Vehiculo vehiculo, 
            @Param("estado") EstadoAlquiler estado, 
            @Param("inicio") LocalDateTime inicio, 
            @Param("fin") LocalDateTime fin);
}