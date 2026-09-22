package es.uma.alquiler.servicios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import es.uma.alquiler.entidades.Alquiler;
import es.uma.alquiler.entidades.EstadoAlquiler;
import es.uma.alquiler.entidades.TipoVehiculo;
import es.uma.alquiler.entidades.Vehiculo;
import es.uma.alquiler.repositorios.AlquilerRepository;
import es.uma.alquiler.repositorios.VehiculoRepository;
import es.uma.alquiler.servicios.excepciones.AlquilerInexistenteException;
import es.uma.alquiler.servicios.excepciones.ReglaNegocioException;
import es.uma.alquiler.servicios.excepciones.VehiculoInexistenteException;

@ExtendWith(MockitoExtension.class)
@DisplayName("El servicio de alquileres")
class LogicaAlquileresTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2030, 1, 10, 9, 0);

    @Mock
    private AlquilerRepository alquilerRepo;

    @Mock
    private VehiculoRepository vehiculoRepo;

    private LogicaAlquileres logica;

    private Vehiculo turismo;

    @BeforeEach
    void inicializar() {
        logica = new LogicaAlquileres(alquilerRepo, vehiculoRepo, new ReglasAlquiler());
        turismo = vehiculo(1L, "1111AAA", TipoVehiculo.TURISMO, 5, 50_000);
    }

    private static Vehiculo vehiculo(Long id, String matricula, TipoVehiculo tipo, int capacidad, int kilometraje) {
        Vehiculo ve = new Vehiculo();
        ve.setId(id);
        ve.setMatricula(matricula);
        ve.setTipo(tipo);
        ve.setCapacidadMaxima(capacidad);
        ve.setKilometrajeActual(kilometraje);
        return ve;
    }

    private static Alquiler alquiler(Long id, Vehiculo ve, int pasajeros, EstadoAlquiler estado) {
        Alquiler al = new Alquiler();
        al.setId(id);
        al.setVehiculo(ve);
        al.setPasajerosPrevistos(pasajeros);
        al.setFechaInicio(INICIO);
        al.setFechaFin(INICIO.plusDays(2));
        al.setEstado(estado);
        return al;
    }

    private void guardarDevuelveArgumento() {
        when(alquilerRepo.save(any(Alquiler.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Nested
    @DisplayName("al crear un alquiler")
    class Crear {

        @Test
        @DisplayName("lo guarda en estado PENDIENTE aunque se pida otro estado (R1)")
        void estadoInicialPendiente() {
            when(vehiculoRepo.findById(1L)).thenReturn(Optional.of(turismo));
            guardarDevuelveArgumento();
            Alquiler nuevo = alquiler(99L, null, 2, EstadoAlquiler.CONFIRMADO);

            Alquiler guardado = logica.aniadirAlquiler(nuevo, 1L);

            assertThat(guardado.getEstado()).isEqualTo(EstadoAlquiler.PENDIENTE);
            assertThat(guardado.getId()).isNull();
            assertThat(guardado.getVehiculo()).isSameAs(turismo);
        }

        @Test
        @DisplayName("falla si el vehículo no existe")
        void vehiculoInexistente() {
            when(vehiculoRepo.findById(7L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> logica.aniadirAlquiler(alquiler(null, null, 2, null), 7L))
                    .isInstanceOf(VehiculoInexistenteException.class);
        }

        @Test
        @DisplayName("falla si no se indica el vehículo")
        void sinVehiculo() {
            assertThatThrownBy(() -> logica.aniadirAlquiler(alquiler(null, null, 2, null), null))
                    .isInstanceOf(ReglaNegocioException.class);
        }

        @Test
        @DisplayName("no guarda nada si se incumple una regla de negocio")
        void reglaIncumplida() {
            when(vehiculoRepo.findById(1L)).thenReturn(Optional.of(turismo));

            assertThatThrownBy(() -> logica.aniadirAlquiler(alquiler(null, null, 6, null), 1L))
                    .isInstanceOf(ReglaNegocioException.class);
            verify(alquilerRepo, never()).save(any());
        }
    }

    @Nested
    @DisplayName("al confirmar un alquiler")
    class Confirmar {

        @Test
        @DisplayName("pasa de PENDIENTE a CONFIRMADO si no hay solapamientos")
        void confirmaPendiente() {
            when(alquilerRepo.findById(10L)).thenReturn(Optional.of(alquiler(10L, turismo, 2, EstadoAlquiler.PENDIENTE)));
            when(vehiculoRepo.findByIdParaActualizar(1L)).thenReturn(Optional.of(turismo));
            when(alquilerRepo.existeSolapamiento(eq(1L), eq(EstadoAlquiler.CONFIRMADO), any(), any())).thenReturn(false);
            guardarDevuelveArgumento();

            assertThat(logica.confirmarAlquiler(10L).getEstado()).isEqualTo(EstadoAlquiler.CONFIRMADO);
        }

        @Test
        @DisplayName("falla si hay otro alquiler confirmado que se solapa (R6)")
        void solapamiento() {
            when(alquilerRepo.findById(10L)).thenReturn(Optional.of(alquiler(10L, turismo, 2, EstadoAlquiler.PENDIENTE)));
            when(vehiculoRepo.findByIdParaActualizar(1L)).thenReturn(Optional.of(turismo));
            when(alquilerRepo.existeSolapamiento(eq(1L), eq(EstadoAlquiler.CONFIRMADO), any(), any())).thenReturn(true);

            assertThatThrownBy(() -> logica.confirmarAlquiler(10L))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("solapa");
            verify(alquilerRepo, never()).save(any());
        }

        @Test
        @DisplayName("falla si el vehículo ha superado los 200.000 km desde la creación (R8)")
        void kilometrajeActualizado() {
            turismo.setKilometrajeActual(210_000);
            when(alquilerRepo.findById(10L)).thenReturn(Optional.of(alquiler(10L, turismo, 4, EstadoAlquiler.PENDIENTE)));
            when(vehiculoRepo.findByIdParaActualizar(1L)).thenReturn(Optional.of(turismo));

            assertThatThrownBy(() -> logica.confirmarAlquiler(10L))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("200.000");
        }

        @Test
        @DisplayName("falla si el alquiler no está PENDIENTE (R7)")
        void noPendiente() {
            when(alquilerRepo.findById(10L)).thenReturn(Optional.of(alquiler(10L, turismo, 2, EstadoAlquiler.CONFIRMADO)));
            when(alquilerRepo.findById(11L)).thenReturn(Optional.of(alquiler(11L, turismo, 2, EstadoAlquiler.CANCELADO)));

            assertThatThrownBy(() -> logica.confirmarAlquiler(10L)).isInstanceOf(ReglaNegocioException.class);
            assertThatThrownBy(() -> logica.confirmarAlquiler(11L)).isInstanceOf(ReglaNegocioException.class);
        }

        @Test
        @DisplayName("falla si el alquiler no existe")
        void inexistente() {
            when(alquilerRepo.findById(anyLong())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> logica.confirmarAlquiler(5L)).isInstanceOf(AlquilerInexistenteException.class);
        }
    }

    @Nested
    @DisplayName("al cancelar un alquiler")
    class Cancelar {

        @Test
        @DisplayName("admite alquileres PENDIENTES y CONFIRMADOS (R7)")
        void cancelaPendienteYConfirmado() {
            when(alquilerRepo.findById(10L)).thenReturn(Optional.of(alquiler(10L, turismo, 2, EstadoAlquiler.PENDIENTE)));
            when(alquilerRepo.findById(11L)).thenReturn(Optional.of(alquiler(11L, turismo, 2, EstadoAlquiler.CONFIRMADO)));
            guardarDevuelveArgumento();

            assertThat(logica.cancelarAlquiler(10L).getEstado()).isEqualTo(EstadoAlquiler.CANCELADO);
            assertThat(logica.cancelarAlquiler(11L).getEstado()).isEqualTo(EstadoAlquiler.CANCELADO);
        }

        @Test
        @DisplayName("no permite cambiar un alquiler ya CANCELADO (R7)")
        void yaCancelado() {
            when(alquilerRepo.findById(10L)).thenReturn(Optional.of(alquiler(10L, turismo, 2, EstadoAlquiler.CANCELADO)));

            assertThatThrownBy(() -> logica.cancelarAlquiler(10L)).isInstanceOf(ReglaNegocioException.class);
            verify(alquilerRepo, never()).save(any());
        }
    }

    @Nested
    @DisplayName("al consultar")
    class Consultar {

        @Test
        @DisplayName("devuelve todos los alquileres o los de un estado")
        void listados() {
            List<Alquiler> todos = List.of(alquiler(1L, turismo, 2, EstadoAlquiler.PENDIENTE));
            when(alquilerRepo.findAll(any(Sort.class))).thenReturn(todos);
            when(alquilerRepo.findByEstadoOrderByIdAsc(EstadoAlquiler.PENDIENTE)).thenReturn(todos);

            assertThat(logica.obtenerAlquileres()).isEqualTo(todos);
            assertThat(logica.obtenerAlquileresPorEstado(EstadoAlquiler.PENDIENTE)).isEqualTo(todos);
        }

        @Test
        @DisplayName("devuelve los alquileres de un vehículo existente")
        void alquileresDeVehiculo() {
            List<Alquiler> delTurismo = List.of(alquiler(1L, turismo, 2, EstadoAlquiler.PENDIENTE));
            when(vehiculoRepo.existsById(1L)).thenReturn(true);
            when(alquilerRepo.findByVehiculoIdOrderByIdAsc(1L)).thenReturn(delTurismo);

            assertThat(logica.obtenerAlquileresPorVehiculo(1L)).isEqualTo(delTurismo);
        }

        @Test
        @DisplayName("falla al pedir los alquileres de un vehículo inexistente")
        void alquileresDeVehiculoInexistente() {
            when(vehiculoRepo.existsById(9L)).thenReturn(false);

            assertThatThrownBy(() -> logica.obtenerAlquileresPorVehiculo(9L))
                    .isInstanceOf(VehiculoInexistenteException.class);
        }
    }

    @Nested
    @DisplayName("al buscar vehículos libres")
    class Libres {

        @Test
        @DisplayName("descarta los vehículos que incumplen alguna regla")
        void filtraPorReglas() {
            LocalDateTime fin = INICIO.plusDays(1);
            Vehiculo furgonetaGastada = vehiculo(2L, "2222BBB", TipoVehiculo.FURGONETA, 3, 160_000);
            Vehiculo suvGastado = vehiculo(3L, "3333CCC", TipoVehiculo.SUV, 3, 250_000);
            when(vehiculoRepo.findVehiculosLibres(INICIO, fin, 2, EstadoAlquiler.CONFIRMADO))
                    .thenReturn(List.of(turismo, furgonetaGastada, suvGastado));

            assertThat(logica.obtenerVehiculosLibres(INICIO, fin, 2)).containsExactly(turismo);
        }

        @Test
        @DisplayName("rechaza intervalos o pasajeros no válidos")
        void parametrosNoValidos() {
            assertThatThrownBy(() -> logica.obtenerVehiculosLibres(INICIO, INICIO.minusHours(1), 2))
                    .isInstanceOf(ReglaNegocioException.class);
            assertThatThrownBy(() -> logica.obtenerVehiculosLibres(INICIO, INICIO.plusDays(1), 0))
                    .isInstanceOf(ReglaNegocioException.class);
        }
    }
}
