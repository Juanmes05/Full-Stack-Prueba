package es.uma.alquiler.servicios;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.uma.alquiler.entidades.Vehiculo;
import es.uma.alquiler.repositorios.VehiculoRepository;
import es.uma.alquiler.servicios.excepciones.ReglaNegocioException;
import es.uma.alquiler.servicios.excepciones.VehiculoDuplicadoException;
import es.uma.alquiler.servicios.excepciones.VehiculoInexistenteException;

@Service
@Transactional
public class LogicaVehiculos {

    private final VehiculoRepository vehiculoRepo;

    public LogicaVehiculos(VehiculoRepository vehiculoRepo) {
        this.vehiculoRepo = vehiculoRepo;
    }

    @Transactional(readOnly = true)
    public List<Vehiculo> obtenerVehiculos() {
        return vehiculoRepo.findAll(Sort.by("id"));
    }

    @Transactional(readOnly = true)
    public Vehiculo obtenerVehiculo(Long id) {
        return vehiculoRepo.findById(id)
                .orElseThrow(() -> new VehiculoInexistenteException("No existe ningún vehículo con id " + id + "."));
    }

    public Vehiculo aniadirVehiculo(Vehiculo ve) {
        if (ve.getMatricula() == null || ve.getMatricula().isBlank()) {
            throw new ReglaNegocioException("La matrícula es obligatoria.");
        }
        if (ve.getTipo() == null) {
            throw new ReglaNegocioException("El tipo de vehículo es obligatorio (TURISMO, SUV o FURGONETA).");
        }
        if (ve.getCapacidadMaxima() == null || ve.getCapacidadMaxima() < 1) {
            throw new ReglaNegocioException("La capacidad máxima debe ser de al menos 1 pasajero.");
        }
        validarKilometraje(ve.getKilometrajeActual());

        // "1234 abc" y "1234ABC" son la misma matrícula
        String matricula = ve.getMatricula().replaceAll("\\s+", "").toUpperCase();
        if (vehiculoRepo.existsByMatricula(matricula)) {
            throw new VehiculoDuplicadoException("Ya existe un vehículo con la matrícula " + matricula + ".");
        }

        ve.setId(null);
        ve.setMatricula(matricula);
        return vehiculoRepo.save(ve);
    }

    public Vehiculo actualizarKilometraje(Long id, Integer nuevoKilometraje) {
        validarKilometraje(nuevoKilometraje);
        Vehiculo ve = obtenerVehiculo(id);
        ve.setKilometrajeActual(nuevoKilometraje);
        return vehiculoRepo.save(ve);
    }

    private void validarKilometraje(Integer kilometraje) {
        if (kilometraje == null || kilometraje < 0) {
            throw new ReglaNegocioException("El kilometraje debe ser un número mayor o igual que 0.");
        }
    }
}
