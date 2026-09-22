package es.uma.alquiler.repositorios;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.uma.alquiler.entidades.Alquiler;
import es.uma.alquiler.entidades.EstadoAlquiler;

@Repository
public interface AlquilerRepository extends JpaRepository<Alquiler, Long> {

    // El vehículo se carga en la misma consulta porque los DTO de respuesta lo incluyen
    @Override
    @EntityGraph(attributePaths = "vehiculo")
    List<Alquiler> findAll(Sort sort);

    @Override
    @EntityGraph(attributePaths = "vehiculo")
    Optional<Alquiler> findById(Long id);

    @EntityGraph(attributePaths = "vehiculo")
    List<Alquiler> findByVehiculoIdOrderByIdAsc(Long vehiculoId);

    @EntityGraph(attributePaths = "vehiculo")
    List<Alquiler> findByEstadoOrderByIdAsc(EstadoAlquiler estado);

    // Dos intervalos se solapan si cada uno empieza antes de que termine el otro
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Alquiler a " +
           "WHERE a.vehiculo.id = :vehiculoId AND a.estado = :estado " +
           "AND a.fechaInicio < :fin AND a.fechaFin > :inicio")
    boolean existeSolapamiento(
            @Param("vehiculoId") Long vehiculoId,
            @Param("estado") EstadoAlquiler estado,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);
}
