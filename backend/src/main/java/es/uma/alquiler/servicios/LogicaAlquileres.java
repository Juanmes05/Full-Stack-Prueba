package es.uma.alquiler.servicios;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import es.uma.alquiler.entidades.Alquiler;
import es.uma.alquiler.entidades.EstadoAlquiler;
import es.uma.alquiler.entidades.Vehiculo;
import es.uma.alquiler.repositorios.AlquilerRepository;
import es.uma.alquiler.repositorios.VehiculoRepository;
import es.uma.alquiler.servicios.excepciones.AlquilerInexistenteException;
import es.uma.alquiler.servicios.excepciones.ReglaNegocioException;
import es.uma.alquiler.servicios.excepciones.VehiculoInexistenteException;

@Service
@Transactional
public class LogicaAlquileres {

    private final AlquilerRepository alquilerRepo;
    private final VehiculoRepository vehiculoRepo;
    private final ReglasAlquiler reglas;

    public LogicaAlquileres(AlquilerRepository alquilerRepo, VehiculoRepository vehiculoRepo, ReglasAlquiler reglas) {
        this.alquilerRepo = alquilerRepo;
        this.vehiculoRepo = vehiculoRepo;
        this.reglas = reglas;
    }

    @Transactional(readOnly = true)
    public List<Alquiler> obtenerAlquileres() {
        return alquilerRepo.findAll(Sort.by("id"));
    }

    @Transactional(readOnly = true)
    public List<Alquiler> obtenerAlquileresPorEstado(EstadoAlquiler estado) {
        return alquilerRepo.findByEstadoOrderByIdAsc(estado);
    }

    @Transactional(readOnly = true)
    public Alquiler obtenerAlquiler(Long id) {
        return alquilerRepo.findById(id)
                .orElseThrow(() -> new AlquilerInexistenteException("No existe ningún alquiler con id " + id + "."));
    }

    @Transactional(readOnly = true)
    public List<Alquiler> obtenerAlquileresPorVehiculo(Long vehiculoId) {
        if (!vehiculoRepo.existsById(vehiculoId)) {
            throw vehiculoInexistente(vehiculoId);
        }
        return alquilerRepo.findByVehiculoIdOrderByIdAsc(vehiculoId);
    }

    public Alquiler aniadirAlquiler(Alquiler al, Long vehiculoId) {
        if (vehiculoId == null) {
            throw new ReglaNegocioException("Debe indicarse el vehículo que se quiere alquilar.");
        }
        Vehiculo ve = vehiculoRepo.findById(vehiculoId).orElseThrow(() -> vehiculoInexistente(vehiculoId));

        // R2, R3, R4, R5 y R8
        reglas.validarNuevoAlquiler(ve, al.getPasajerosPrevistos(), al.getFechaInicio(), al.getFechaFin());

        // R1. Estado inicial
        al.setId(null);
        al.setVehiculo(ve);
        al.setEstado(EstadoAlquiler.PENDIENTE);
        return alquilerRepo.save(al);
    }

    // READ_COMMITTED: tras esperar el bloqueo del vehículo, la consulta de solapamiento
    // debe ver las confirmaciones que otras transacciones acaban de guardar
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Alquiler confirmarAlquiler(Long id) {
        Alquiler al = obtenerAlquiler(id);

        // R7. Cambio de estado: solo PENDIENTE -> CONFIRMADO
        if (al.getEstado() != EstadoAlquiler.PENDIENTE) {
            throw new ReglaNegocioException("Solo se pueden confirmar alquileres en estado PENDIENTE (el alquiler "
                    + id + " está " + al.getEstado() + ").");
        }

        Long vehiculoId = al.getVehiculo().getId();
        Vehiculo ve = vehiculoRepo.findByIdParaActualizar(vehiculoId).orElseThrow(() -> vehiculoInexistente(vehiculoId));

        // R8. El kilometraje puede haber cambiado desde que se creó el alquiler
        reglas.validarKilometraje(ve, al.getPasajerosPrevistos(), al.getFechaInicio(), al.getFechaFin());

        // R6. Solapamiento con otros alquileres confirmados
        if (alquilerRepo.existeSolapamiento(vehiculoId, EstadoAlquiler.CONFIRMADO, al.getFechaInicio(), al.getFechaFin())) {
            throw new ReglaNegocioException("El vehículo " + ve.getMatricula()
                    + " ya tiene un alquiler confirmado que se solapa con estas fechas.");
        }

        al.setEstado(EstadoAlquiler.CONFIRMADO);
        return alquilerRepo.save(al);
    }

    public Alquiler cancelarAlquiler(Long id) {
        Alquiler al = obtenerAlquiler(id);

        // R7. Cambio de estado: PENDIENTE o CONFIRMADO -> CANCELADO
        if (al.getEstado() == EstadoAlquiler.CANCELADO) {
            throw new ReglaNegocioException("El alquiler " + id + " ya está CANCELADO y no puede cambiar de estado.");
        }

        al.setEstado(EstadoAlquiler.CANCELADO);
        return alquilerRepo.save(al);
    }

    /**
     * Vehículos que pueden alquilarse en ese intervalo para ese número de pasajeros:
     * los que cumplen las reglas de creación (R2, R4, R8) y no tienen alquileres
     * confirmados que se solapen (R6), de modo que el alquiler podría confirmarse.
     */
    @Transactional(readOnly = true)
    public List<Vehiculo> obtenerVehiculosLibres(LocalDateTime inicio, LocalDateTime fin, Integer pasajeros) {
        reglas.validarPasajeros(pasajeros);
        reglas.validarIntervalo(inicio, fin);
        return vehiculoRepo.findVehiculosLibres(inicio, fin, pasajeros, EstadoAlquiler.CONFIRMADO).stream()
                .filter(ve -> reglas.admiteAlquiler(ve, pasajeros, inicio, fin))
                .toList();
    }

    private VehiculoInexistenteException vehiculoInexistente(Long id) {
        return new VehiculoInexistenteException("No existe ningún vehículo con id " + id + ".");
    }
}
