package es.uma.alquiler.servicios;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.uma.alquiler.entidades.Alquiler;
import es.uma.alquiler.entidades.EstadoAlquiler;
import es.uma.alquiler.entidades.TipoVehiculo;
import es.uma.alquiler.entidades.Vehiculo;
import es.uma.alquiler.repositorios.AlquilerRepository;
import es.uma.alquiler.repositorios.VehiculoRepository;
import es.uma.alquiler.servicios.excepciones.AlquilerInexistenteException;
import es.uma.alquiler.servicios.excepciones.ReglaNegocioException;
import es.uma.alquiler.servicios.excepciones.VehiculoDuplicadoExcception;
import es.uma.alquiler.servicios.excepciones.VehiculoInexistenteException;

@Service
@Transactional
public class LogicaAlquileres {

    private final AlquilerRepository alquilerRepo;
    private final VehiculoRepository vehiculoRepo;
    
    @Autowired
    public LogicaAlquileres(AlquilerRepository alquilerRepo, VehiculoRepository vehiculoRepo) {
        this.alquilerRepo = alquilerRepo;
        this.vehiculoRepo = vehiculoRepo;
    }
    

    public List<Alquiler> obtenerAlquileres() {
        return alquilerRepo.findAll();
    }
    
    public List<Alquiler> obtenerAlquileresPorEstado(EstadoAlquiler estado){
		return alquilerRepo.findByEstado(estado);
    }
    
    public Alquiler obtenerAlquiler(Long id) {
        return alquilerRepo.findById(id)
                .orElseThrow(() -> new AlquilerInexistenteException());
    }
    
    public Alquiler aniadirAlquiler(Alquiler al, Long vehiculoId) {
        Vehiculo ve = vehiculoRepo.findById(vehiculoId)
                .orElseThrow(() -> new VehiculoInexistenteException("Vehículo no encontrado"));

        // R2. Capacidad del vehículo
        if (al.getPasajerosPrevistos() > ve.getCapacidadMaxima()) {
            throw new ReglaNegocioException("Los pasajeros superan la capacidad máxima del vehículo.");
        }

        // R3. Horario permitido
        if (!al.getFechaFin().isAfter(al.getFechaInicio())) {
            throw new ReglaNegocioException("La fecha de fin debe ser posterior a la fecha de inicio.");
        }

        long horasDuracion = Duration.between(al.getFechaInicio(), al.getFechaFin()).toHours();
        long diasDuracion = Duration.between(al.getFechaInicio(), al.getFechaFin()).toDays();

        if (ve.getTipo() == TipoVehiculo.FURGONETA) {
            if (horasDuracion < 2) {
                throw new ReglaNegocioException("Las furgonetas deben alquilarse un mínimo de dos horas.");
            }
        } else {
            if (horasDuracion < 24) {
                throw new ReglaNegocioException("Los alquileres deben tener una duración mínima de 24 horas.");
            }
        }

        // R5. Duración máxima
        if (diasDuracion > 5) {
            throw new ReglaNegocioException("La duración de un alquiler no podrá superar los cinco días.");
        }


        if (ve.getTipo() == TipoVehiculo.FURGONETA && ve.getKilometrajeActual() > 150000) {
            if (horasDuracion >= 6) {
                throw new ReglaNegocioException("Furgoneta de alta rotación: no se puede alquilar 6 horas o más.");
            }
        }

        if (ve.getKilometrajeActual() > 200000) {
            if (al.getPasajerosPrevistos() > (ve.getCapacidadMaxima() / 2.0)) {
                throw new ReglaNegocioException("Límite de carga: pasajeros no pueden superar el 50% de la capacidad.");
            }
        }

        // R1. Estado inicial
        al.setId(null);
        al.setVehiculo(ve);
        al.setEstado(EstadoAlquiler.PENDIENTE);
        
        return alquilerRepo.save(al);
    }

    public void confirmarAlquiler(Long id) {
        Alquiler al = alquilerRepo.findById(id).orElseThrow(() -> new AlquilerInexistenteException(null));
        
        // R7. Cambio de estado
        if (al.getEstado() != EstadoAlquiler.PENDIENTE) {
            throw new ReglaNegocioException("Solo se pueden confirmar alquileres en estado PENDIENTE.");
        }
        
        // R6. Solapamiento
        if (alquilerRepo.existsOverlappingAlquileres(al.getVehiculo(), EstadoAlquiler.CONFIRMADO, al.getFechaInicio(), al.getFechaFin())) {
            throw new ReglaNegocioException("Ya existe un alquiler confirmado que se solapa en fechas.");
        }

        al.setEstado(EstadoAlquiler.CONFIRMADO);
        alquilerRepo.save(al);
    }

    public void cancelarAlquiler(Long id) {
        Alquiler al = alquilerRepo.findById(id).orElseThrow(() -> new AlquilerInexistenteException(null));
        
        // R7. Cambio de estado
        if (al.getEstado() == EstadoAlquiler.CANCELADO) {
            throw new ReglaNegocioException("Un alquiler CANCELADO no puede cambiarse de estado.");
        }

        al.setEstado(EstadoAlquiler.CANCELADO);
        alquilerRepo.save(al);
    }
    

    public List<Vehiculo> obtenerVehiculos() {
        return vehiculoRepo.findAll();
    }

    public Vehiculo obtenerVehiculo(Long id) {
        return vehiculoRepo.findById(id)
                .orElseThrow(() -> new VehiculoInexistenteException("Vehículo no encontrado"));
    }

    public Vehiculo aniadirVehiculo(Vehiculo ve) {
    	if(vehiculoRepo.existsByMatricula(ve.getMatricula())) { throw new VehiculoDuplicadoExcception("Vehiculo existente");}
        ve.setId(null);
        return vehiculoRepo.save(ve);
    }

    public void actualizarKilometraje(Long id, Integer nuevoKilometraje) {
        Vehiculo ve = obtenerVehiculo(id);
        ve.setKilometrajeActual(nuevoKilometraje);
        vehiculoRepo.save(ve);
    }

    public List<Alquiler> obtenerAlquileresPorVehiculo(Long vehiculoId) {
        Vehiculo ve = obtenerVehiculo(vehiculoId); 
        return alquilerRepo.findByVehiculoId(ve.getId());
    }

    public List<Vehiculo> obtenerVehiculosLibres(LocalDateTime inicio, LocalDateTime fin, Integer pasajeros) {
        if (!fin.isAfter(inicio)) {
            throw new ReglaNegocioException("El rango de fechas es inválido.");
        }
        return vehiculoRepo.findVehiculosLibres(inicio, fin, pasajeros);
    }
}