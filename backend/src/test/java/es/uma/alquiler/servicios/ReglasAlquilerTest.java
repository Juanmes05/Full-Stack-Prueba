package es.uma.alquiler.servicios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import es.uma.alquiler.entidades.TipoVehiculo;
import es.uma.alquiler.entidades.Vehiculo;
import es.uma.alquiler.servicios.excepciones.ReglaNegocioException;

@DisplayName("Las reglas de negocio de los alquileres")
class ReglasAlquilerTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2030, 1, 10, 9, 0);

    private final ReglasAlquiler reglas = new ReglasAlquiler();

    private static Vehiculo vehiculo(TipoVehiculo tipo, int capacidad, int kilometraje) {
        Vehiculo ve = new Vehiculo();
        ve.setId(1L);
        ve.setMatricula("1234ABC");
        ve.setTipo(tipo);
        ve.setCapacidadMaxima(capacidad);
        ve.setKilometrajeActual(kilometraje);
        return ve;
    }

    @Nested
    @DisplayName("sobre el intervalo y los pasajeros")
    class Intervalo {

        @Test
        @DisplayName("rechazan una fecha de fin igual o anterior a la de inicio (R3)")
        void finNoPosterior() {
            assertThatThrownBy(() -> reglas.validarIntervalo(INICIO, INICIO))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("posterior");
            assertThatThrownBy(() -> reglas.validarIntervalo(INICIO, INICIO.minusHours(1)))
                    .isInstanceOf(ReglaNegocioException.class);
        }

        @Test
        @DisplayName("rechazan fechas nulas")
        void fechasNulas() {
            assertThatThrownBy(() -> reglas.validarIntervalo(null, INICIO))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("obligatorias");
        }

        @Test
        @DisplayName("admiten exactamente cinco días (R5)")
        void cincoDiasExactos() {
            assertThatCode(() -> reglas.validarIntervalo(INICIO, INICIO.plusDays(5)))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("rechazan un minuto más de cinco días (R5)")
        void masDeCincoDias() {
            assertThatThrownBy(() -> reglas.validarIntervalo(INICIO, INICIO.plusDays(5).plusMinutes(1)))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("cinco días");
        }

        @Test
        @DisplayName("rechazan un número de pasajeros nulo o menor que uno")
        void pasajerosNoValidos() {
            assertThatThrownBy(() -> reglas.validarPasajeros(0)).isInstanceOf(ReglaNegocioException.class);
            assertThatThrownBy(() -> reglas.validarPasajeros(null)).isInstanceOf(ReglaNegocioException.class);
            assertThatCode(() -> reglas.validarPasajeros(1)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("al crear un alquiler")
    class NuevoAlquiler {

        @Test
        @DisplayName("admiten tantos pasajeros como plazas tiene el vehículo (R2)")
        void capacidadJusta() {
            Vehiculo turismo = vehiculo(TipoVehiculo.TURISMO, 5, 1000);
            assertThatCode(() -> reglas.validarNuevoAlquiler(turismo, 5, INICIO, INICIO.plusDays(1)))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("rechazan más pasajeros que plazas (R2)")
        void capacidadSuperada() {
            Vehiculo turismo = vehiculo(TipoVehiculo.TURISMO, 5, 1000);
            assertThatThrownBy(() -> reglas.validarNuevoAlquiler(turismo, 6, INICIO, INICIO.plusDays(1)))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("capacidad");
        }

        @Test
        @DisplayName("exigen 24 horas a turismos y SUV (R4)")
        void duracionMinimaTurismoYSuv() {
            Vehiculo turismo = vehiculo(TipoVehiculo.TURISMO, 5, 1000);
            Vehiculo suv = vehiculo(TipoVehiculo.SUV, 7, 1000);
            assertThatThrownBy(() -> reglas.validarNuevoAlquiler(turismo, 2, INICIO, INICIO.plusHours(23).plusMinutes(59)))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("24 horas");
            assertThatThrownBy(() -> reglas.validarNuevoAlquiler(suv, 2, INICIO, INICIO.plusHours(12)))
                    .isInstanceOf(ReglaNegocioException.class);
            assertThatCode(() -> reglas.validarNuevoAlquiler(suv, 2, INICIO, INICIO.plusHours(24)))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("permiten alquilar furgonetas desde dos horas (R4)")
        void duracionMinimaFurgoneta() {
            Vehiculo furgoneta = vehiculo(TipoVehiculo.FURGONETA, 3, 1000);
            assertThatCode(() -> reglas.validarNuevoAlquiler(furgoneta, 2, INICIO, INICIO.plusHours(2)))
                    .doesNotThrowAnyException();
            assertThatThrownBy(() -> reglas.validarNuevoAlquiler(furgoneta, 2, INICIO, INICIO.plusMinutes(119)))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("dos horas");
        }

        @Test
        @DisplayName("limitan a menos de seis horas las furgonetas de más de 150.000 km (R8)")
        void furgonetaAltaRotacion() {
            Vehiculo furgoneta = vehiculo(TipoVehiculo.FURGONETA, 3, 150_001);
            assertThatCode(() -> reglas.validarNuevoAlquiler(furgoneta, 2, INICIO, INICIO.plusHours(5).plusMinutes(59)))
                    .doesNotThrowAnyException();
            assertThatThrownBy(() -> reglas.validarNuevoAlquiler(furgoneta, 2, INICIO, INICIO.plusHours(6)))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("150.000");
        }

        @Test
        @DisplayName("no limitan la duración de una furgoneta con 150.000 km justos (R8)")
        void furgonetaEnElLimite() {
            Vehiculo furgoneta = vehiculo(TipoVehiculo.FURGONETA, 3, 150_000);
            assertThatCode(() -> reglas.validarNuevoAlquiler(furgoneta, 2, INICIO, INICIO.plusDays(2)))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("limitan al 50% de la capacidad los vehículos de más de 200.000 km (R8)")
        void limiteCargaPorDesgaste() {
            Vehiculo turismo = vehiculo(TipoVehiculo.TURISMO, 5, 200_001);
            assertThatCode(() -> reglas.validarNuevoAlquiler(turismo, 2, INICIO, INICIO.plusDays(1)))
                    .doesNotThrowAnyException();
            assertThatThrownBy(() -> reglas.validarNuevoAlquiler(turismo, 3, INICIO, INICIO.plusDays(1)))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("50%");
        }

        @Test
        @DisplayName("no limitan la carga de un vehículo con 200.000 km justos (R8)")
        void cargaEnElLimite() {
            Vehiculo turismo = vehiculo(TipoVehiculo.TURISMO, 5, 200_000);
            assertThatCode(() -> reglas.validarNuevoAlquiler(turismo, 5, INICIO, INICIO.plusDays(1)))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("al confirmar o buscar vehículos libres")
    class ConfirmacionYBusqueda {

        @Test
        @DisplayName("vuelven a comprobar las restricciones por kilometraje (R8)")
        void kilometrajeAlConfirmar() {
            Vehiculo turismo = vehiculo(TipoVehiculo.TURISMO, 4, 250_000);
            assertThatThrownBy(() -> reglas.validarKilometraje(turismo, 3, INICIO, INICIO.plusDays(1)))
                    .isInstanceOf(ReglaNegocioException.class);
            assertThatCode(() -> reglas.validarKilometraje(turismo, 2, INICIO, INICIO.plusDays(1)))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("indican si un vehículo admite el alquiler")
        void admiteAlquiler() {
            Vehiculo turismo = vehiculo(TipoVehiculo.TURISMO, 5, 1000);
            Vehiculo furgoneta = vehiculo(TipoVehiculo.FURGONETA, 3, 1000);
            assertThat(reglas.admiteAlquiler(turismo, 2, INICIO, INICIO.plusHours(3))).isFalse();
            assertThat(reglas.admiteAlquiler(furgoneta, 2, INICIO, INICIO.plusHours(3))).isTrue();
            assertThat(reglas.admiteAlquiler(furgoneta, 4, INICIO, INICIO.plusHours(3))).isFalse();
        }
    }
}
