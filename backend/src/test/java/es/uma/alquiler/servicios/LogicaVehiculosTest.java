package es.uma.alquiler.servicios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import es.uma.alquiler.entidades.TipoVehiculo;
import es.uma.alquiler.entidades.Vehiculo;
import es.uma.alquiler.repositorios.VehiculoRepository;
import es.uma.alquiler.servicios.excepciones.ReglaNegocioException;
import es.uma.alquiler.servicios.excepciones.VehiculoDuplicadoException;
import es.uma.alquiler.servicios.excepciones.VehiculoInexistenteException;

@ExtendWith(MockitoExtension.class)
@DisplayName("El servicio de vehículos")
class LogicaVehiculosTest {

    @Mock
    private VehiculoRepository vehiculoRepo;

    private LogicaVehiculos logica;

    @BeforeEach
    void inicializar() {
        logica = new LogicaVehiculos(vehiculoRepo);
    }

    private static Vehiculo vehiculo(String matricula, TipoVehiculo tipo, Integer capacidad, Integer kilometraje) {
        Vehiculo ve = new Vehiculo();
        ve.setMatricula(matricula);
        ve.setTipo(tipo);
        ve.setCapacidadMaxima(capacidad);
        ve.setKilometrajeActual(kilometraje);
        return ve;
    }

    @Test
    @DisplayName("guarda un vehículo nuevo con la matrícula normalizada")
    void aniadeVehiculo() {
        when(vehiculoRepo.existsByMatricula("1234ABC")).thenReturn(false);
        when(vehiculoRepo.save(any(Vehiculo.class))).thenAnswer(inv -> inv.getArgument(0));
        Vehiculo nuevo = vehiculo(" 1234 abc", TipoVehiculo.SUV, 7, 0);
        nuevo.setId(55L);

        Vehiculo guardado = logica.aniadirVehiculo(nuevo);

        assertThat(guardado.getMatricula()).isEqualTo("1234ABC");
        assertThat(guardado.getId()).isNull();
    }

    @Test
    @DisplayName("rechaza una matrícula repetida")
    void matriculaDuplicada() {
        when(vehiculoRepo.existsByMatricula("1234ABC")).thenReturn(true);

        assertThatThrownBy(() -> logica.aniadirVehiculo(vehiculo("1234ABC", TipoVehiculo.TURISMO, 5, 0)))
                .isInstanceOf(VehiculoDuplicadoException.class);
        verify(vehiculoRepo, never()).save(any());
    }

    @Test
    @DisplayName("rechaza vehículos con datos incompletos o no válidos")
    void datosNoValidos() {
        assertThatThrownBy(() -> logica.aniadirVehiculo(vehiculo(" ", TipoVehiculo.TURISMO, 5, 0)))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> logica.aniadirVehiculo(vehiculo("1234ABC", null, 5, 0)))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> logica.aniadirVehiculo(vehiculo("1234ABC", TipoVehiculo.TURISMO, 0, 0)))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> logica.aniadirVehiculo(vehiculo("1234ABC", TipoVehiculo.TURISMO, 5, -1)))
                .isInstanceOf(ReglaNegocioException.class);
        verify(vehiculoRepo, never()).save(any());
    }

    @Test
    @DisplayName("devuelve todos los vehículos o uno concreto")
    void consultas() {
        Vehiculo ve = vehiculo("1234ABC", TipoVehiculo.TURISMO, 5, 0);
        when(vehiculoRepo.findAll(any(Sort.class))).thenReturn(List.of(ve));
        when(vehiculoRepo.findById(1L)).thenReturn(Optional.of(ve));

        assertThat(logica.obtenerVehiculos()).containsExactly(ve);
        assertThat(logica.obtenerVehiculo(1L)).isSameAs(ve);
    }

    @Test
    @DisplayName("falla al pedir un vehículo inexistente")
    void vehiculoInexistente() {
        when(vehiculoRepo.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> logica.obtenerVehiculo(9L)).isInstanceOf(VehiculoInexistenteException.class);
    }

    @Test
    @DisplayName("actualiza el kilometraje")
    void actualizaKilometraje() {
        Vehiculo ve = vehiculo("1234ABC", TipoVehiculo.TURISMO, 5, 1000);
        when(vehiculoRepo.findById(1L)).thenReturn(Optional.of(ve));
        when(vehiculoRepo.save(ve)).thenReturn(ve);

        assertThat(logica.actualizarKilometraje(1L, 180_000).getKilometrajeActual()).isEqualTo(180_000);
    }

    @Test
    @DisplayName("rechaza un kilometraje negativo o ausente")
    void kilometrajeNoValido() {
        assertThatThrownBy(() -> logica.actualizarKilometraje(1L, -5)).isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> logica.actualizarKilometraje(1L, null)).isInstanceOf(ReglaNegocioException.class);
        verify(vehiculoRepo, never()).save(any());
    }
}
