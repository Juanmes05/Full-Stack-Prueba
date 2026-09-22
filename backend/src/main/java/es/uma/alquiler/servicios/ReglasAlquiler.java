package es.uma.alquiler.servicios;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Component;

import es.uma.alquiler.entidades.TipoVehiculo;
import es.uma.alquiler.entidades.Vehiculo;
import es.uma.alquiler.servicios.excepciones.ReglaNegocioException;

/**
 * Reglas de negocio de los alquileres (R2-R5 y R8) que no dependen del estado
 * del resto de alquileres. Las usan tanto el alta y la confirmación de alquileres
 * como la búsqueda de vehículos libres, para que todas apliquen el mismo criterio.
 */
@Component
public class ReglasAlquiler {

    static final Duration DURACION_MINIMA = Duration.ofHours(24);
    static final Duration DURACION_MINIMA_FURGONETA = Duration.ofHours(2);
    static final Duration DURACION_MAXIMA = Duration.ofDays(5);
    static final int KM_FURGONETA_ALTA_ROTACION = 150_000;
    static final Duration DURACION_MAXIMA_ALTA_ROTACION = Duration.ofHours(6);
    static final int KM_LIMITE_CARGA = 200_000;

    /**
     * Comprueba que el intervalo solicitado es válido con independencia del vehículo.
     */
    public void validarIntervalo(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null || fin == null) {
            throw new ReglaNegocioException("Las fechas de inicio y fin son obligatorias.");
        }
        // R3. Horario permitido
        if (!fin.isAfter(inicio)) {
            throw new ReglaNegocioException("La fecha de fin debe ser posterior a la fecha de inicio.");
        }
        // R5. Duración máxima
        if (Duration.between(inicio, fin).compareTo(DURACION_MAXIMA) > 0) {
            throw new ReglaNegocioException("La duración de un alquiler no puede superar los cinco días.");
        }
    }

    public void validarPasajeros(Integer pasajeros) {
        if (pasajeros == null || pasajeros < 1) {
            throw new ReglaNegocioException("El número de pasajeros previstos debe ser al menos 1.");
        }
    }

    /**
     * Valida todas las reglas que se exigen al crear un alquiler (R2, R3, R4, R5 y R8).
     */
    public void validarNuevoAlquiler(Vehiculo ve, Integer pasajeros, LocalDateTime inicio, LocalDateTime fin) {
        validarPasajeros(pasajeros);
        validarIntervalo(inicio, fin);
        incumplimientoVehiculo(ve, pasajeros, Duration.between(inicio, fin)).ifPresent(mensaje -> {
            throw new ReglaNegocioException(mensaje);
        });
    }

    /**
     * Valida las restricciones por kilometraje (R8), que también se exigen al confirmar,
     * porque el kilometraje del vehículo puede haber cambiado desde que se creó el alquiler.
     */
    public void validarKilometraje(Vehiculo ve, Integer pasajeros, LocalDateTime inicio, LocalDateTime fin) {
        incumplimientoKilometraje(ve, pasajeros, Duration.between(inicio, fin)).ifPresent(mensaje -> {
            throw new ReglaNegocioException(mensaje);
        });
    }

    /**
     * Indica si el vehículo admite un alquiler con esos pasajeros y fechas según las
     * reglas que dependen del propio vehículo. El intervalo debe haberse validado antes.
     */
    public boolean admiteAlquiler(Vehiculo ve, int pasajeros, LocalDateTime inicio, LocalDateTime fin) {
        return incumplimientoVehiculo(ve, pasajeros, Duration.between(inicio, fin)).isEmpty();
    }

    private Optional<String> incumplimientoVehiculo(Vehiculo ve, int pasajeros, Duration duracion) {
        // R2. Capacidad del vehículo
        if (pasajeros > ve.getCapacidadMaxima()) {
            return Optional.of("El número de pasajeros (" + pasajeros + ") supera la capacidad máxima del vehículo "
                    + ve.getMatricula() + " (" + ve.getCapacidadMaxima() + ").");
        }

        // R4. Duración mínima
        if (ve.getTipo() == TipoVehiculo.FURGONETA) {
            if (duracion.compareTo(DURACION_MINIMA_FURGONETA) < 0) {
                return Optional.of("Las furgonetas deben alquilarse un mínimo de dos horas.");
            }
        } else if (duracion.compareTo(DURACION_MINIMA) < 0) {
            return Optional.of("Los alquileres de vehículos de tipo " + ve.getTipo()
                    + " deben durar como mínimo un día completo (24 horas).");
        }

        return incumplimientoKilometraje(ve, pasajeros, duracion);
    }

    private Optional<String> incumplimientoKilometraje(Vehiculo ve, int pasajeros, Duration duracion) {
        // R8. Furgonetas de alta rotación
        if (ve.getTipo() == TipoVehiculo.FURGONETA && ve.getKilometrajeActual() > KM_FURGONETA_ALTA_ROTACION
                && duracion.compareTo(DURACION_MAXIMA_ALTA_ROTACION) >= 0) {
            return Optional.of("La furgoneta " + ve.getMatricula() + " supera los 150.000 km: "
                    + "solo puede alquilarse para usos de menos de seis horas.");
        }

        // R8. Límite de carga por desgaste
        if (ve.getKilometrajeActual() > KM_LIMITE_CARGA && pasajeros * 2 > ve.getCapacidadMaxima()) {
            return Optional.of("El vehículo " + ve.getMatricula() + " supera los 200.000 km: "
                    + "los pasajeros previstos no pueden superar el 50% de su capacidad ("
                    + ve.getCapacidadMaxima() / 2 + " como máximo).");
        }

        return Optional.empty();
    }
}
