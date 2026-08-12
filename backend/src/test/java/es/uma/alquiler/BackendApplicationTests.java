package es.uma.alquiler;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriBuilderFactory;

import es.uma.alquiler.dtos.AlquilerDTO;
import es.uma.alquiler.dtos.VehiculoDTO;
import es.uma.alquiler.entidades.Alquiler;
import es.uma.alquiler.entidades.EstadoAlquiler;
import es.uma.alquiler.entidades.TipoVehiculo;
import es.uma.alquiler.entidades.Vehiculo;
import es.uma.alquiler.repositorios.AlquilerRepository;
import es.uma.alquiler.repositorios.VehiculoRepository;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DisplayName("En el servicio de gestión de alquileres de vehículos")
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class BackendApplicationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Value(value="${local.server.port}")
    private int port;

    @Autowired
    private VehiculoRepository vehiculoRepo;

    @Autowired
    private AlquilerRepository alquilerRepo;
    
    private URI uri(String scheme, String host, int port, String ...paths) {
        UriBuilderFactory ubf = new DefaultUriBuilderFactory();
        UriBuilder ub = ubf.builder().scheme(scheme).host(host).port(port);
        for (String path: paths) {
            ub = ub.path(path);
        }
        return ub.build();
    }

    private RequestEntity<Void> get(String scheme, String host, int port, String path) {
        return RequestEntity.get(uri(scheme, host, port, path))
                .accept(MediaType.APPLICATION_JSON)
                .build();
    }

    private <T> RequestEntity<T> post(String scheme, String host, int port, String path, T object) {
        return RequestEntity.post(uri(scheme, host, port, path))
                .contentType(MediaType.APPLICATION_JSON)
                .body(object);
    }
    
    private RequestEntity<Void> postAccion(String scheme, String host, int port, String path) {
        return RequestEntity.post(uri(scheme, host, port, path)).build();
    }


    @Nested
    @DisplayName("cuando la base de datos está vacía")
    class BaseDatosVacia {

        @Test
        @DisplayName("da error 404 al obtener un vehículo concreto que no existe")
        void errorAlObtenerVehiculoInexistente() {
            var peticion = get("http", "localhost", port, "/vehiculos/1");
            var respuesta = restTemplate.exchange(peticion, new ParameterizedTypeReference<Map<String, String>>() {});

            assertThat(respuesta.getStatusCode().value()).isEqualTo(404);
            assertThat(respuesta.getBody()).containsKey("error");
        }

        @Test
        @DisplayName("inserta correctamente un vehículo")
        void insertaVehiculo() {
            VehiculoDTO vehiculo = new VehiculoDTO();
            vehiculo.setMatricula("1234ABC");
            vehiculo.setTipo(TipoVehiculo.TURISMO);
            vehiculo.setCapacidadMaxima(5);
            vehiculo.setKilometrajeActual(50000);

            var peticion = post("http", "localhost", port, "/vehiculos", vehiculo);
            var respuesta = restTemplate.exchange(peticion, Void.class);

            assertThat(respuesta.getStatusCode().value()).isEqualTo(201);
            assertThat(respuesta.getHeaders().get("Location").get(0))
                    .startsWith("http://localhost:" + port + "/vehiculos");

            List<Vehiculo> vehiculosBD = vehiculoRepo.findAll();
            assertThat(vehiculosBD).hasSize(1);
            assertThat(vehiculosBD.get(0).getMatricula()).isEqualTo("1234ABC");
        }

        @Test
        @DisplayName("da error 404 al intentar crear un alquiler de un vehículo inexistente")
        void errorCrearAlquilerVehiculoInexistente() {
            AlquilerDTO alquiler = new AlquilerDTO();
            alquiler.setVehiculoId(99L);
            alquiler.setPasajerosPrevistos(2);
            alquiler.setFechaInicio(LocalDateTime.now().plusDays(1));
            alquiler.setFechaFin(LocalDateTime.now().plusDays(3));

            var peticion = post("http", "localhost", port, "/alquileres", alquiler);
            var respuesta = restTemplate.exchange(peticion, Void.class);

            assertThat(respuesta.getStatusCode().value()).isEqualTo(404);
        }
    }

    @Nested
    @DisplayName("cuando la base de datos tiene datos")
    class BaseDatosConDatos {

        private Long turismoId;
        private LocalDateTime mañana;

        @BeforeEach
        void insertarDatos() {
            Vehiculo vehiculo = new Vehiculo();
            vehiculo.setMatricula("9876XYZ");
            vehiculo.setTipo(TipoVehiculo.TURISMO);
            vehiculo.setCapacidadMaxima(5);
            vehiculo.setKilometrajeActual(100000);
            vehiculoRepo.save(vehiculo);
            
            turismoId = vehiculo.getId();
            mañana = LocalDateTime.now().plusDays(1);
        }

        @Test
        @DisplayName("inserta un alquiler válido y le asigna el estado PENDIENTE (R1)")
        void crearAlquilerValido() {
            AlquilerDTO alquiler = new AlquilerDTO();
            alquiler.setVehiculoId(turismoId);
            alquiler.setPasajerosPrevistos(4);
            alquiler.setFechaInicio(mañana);
            alquiler.setFechaFin(mañana.plusDays(2));

            var peticion = post("http", "localhost", port, "/alquileres", alquiler);
            var respuesta = restTemplate.exchange(peticion, Void.class);

            assertThat(respuesta.getStatusCode().value()).isEqualTo(201);
            
            List<Alquiler> alquileresBD = alquilerRepo.findAll();
            assertThat(alquileresBD).hasSize(1);
            assertThat(alquileresBD.get(0).getEstado()).isEqualTo(EstadoAlquiler.PENDIENTE);
        }

        @Test
        @DisplayName("rechaza un alquiler si los pasajeros superan la capacidad (R2)")
        void errorCapacidadSuperada() {
            AlquilerDTO alquiler = new AlquilerDTO();
            alquiler.setVehiculoId(turismoId);
            alquiler.setPasajerosPrevistos(6); 
            alquiler.setFechaInicio(mañana);
            alquiler.setFechaFin(mañana.plusDays(2));

            var peticion = post("http", "localhost", port, "/alquileres", alquiler);
            var respuesta = restTemplate.exchange(peticion, Map.class);

            assertThat(respuesta.getStatusCode().value()).isEqualTo(400);
            assertThat(alquilerRepo.count()).isEqualTo(0);
        }

        @Test
        @DisplayName("rechaza un alquiler de turismo si no dura mínimo 24h (R4)")
        void errorDuracionMinimaTurismo() {
            AlquilerDTO alquiler = new AlquilerDTO();
            alquiler.setVehiculoId(turismoId);
            alquiler.setPasajerosPrevistos(2);
            alquiler.setFechaInicio(mañana);
            alquiler.setFechaFin(mañana.plusHours(12));

            var peticion = post("http", "localhost", port, "/alquileres", alquiler);
            var respuesta = restTemplate.exchange(peticion, Map.class);

            assertThat(respuesta.getStatusCode().value()).isEqualTo(400);
        }

        @Test
        @DisplayName("permite confirmar un alquiler pendiente (R7)")
        void confirmarAlquilerPendiente() {
            Vehiculo vehiculo = vehiculoRepo.findById(turismoId).get();
            Alquiler alquiler = new Alquiler();
            alquiler.setVehiculo(vehiculo);
            alquiler.setPasajerosPrevistos(2);
            alquiler.setFechaInicio(mañana);
            alquiler.setFechaFin(mañana.plusDays(2));
            alquiler.setEstado(EstadoAlquiler.PENDIENTE);
            alquiler = alquilerRepo.save(alquiler);

            var peticion = postAccion("http", "localhost", port, "/alquileres/" + alquiler.getId() + "/confirmar");
            var respuesta = restTemplate.exchange(peticion, Void.class);

            assertThat(respuesta.getStatusCode().value()).isEqualTo(200);
            assertThat(alquilerRepo.findById(alquiler.getId()).get().getEstado()).isEqualTo(EstadoAlquiler.CONFIRMADO);
        }
        
        @Test
        @DisplayName("rechaza confirmar un alquiler si hay solapamiento (R6)")
        void errorSolapamientoAlConfirmar() {
            Vehiculo vehiculo = vehiculoRepo.findById(turismoId).get();
            
            Alquiler alq1 = new Alquiler();
            alq1.setVehiculo(vehiculo);
            alq1.setPasajerosPrevistos(2);
            alq1.setFechaInicio(mañana);
            alq1.setFechaFin(mañana.plusDays(2));
            alq1.setEstado(EstadoAlquiler.CONFIRMADO);
            alquilerRepo.save(alq1);
            
            Alquiler alq2 = new Alquiler();
            alq2.setVehiculo(vehiculo);
            alq2.setPasajerosPrevistos(2);
            alq2.setFechaInicio(mañana.plusDays(1)); 
            alq2.setFechaFin(mañana.plusDays(3));
            alq2.setEstado(EstadoAlquiler.PENDIENTE);
            alq2 = alquilerRepo.save(alq2);
            
            var peticion = postAccion("http", "localhost", port, "/alquileres/" + alq2.getId() + "/confirmar");
            var respuesta = restTemplate.exchange(peticion, Map.class);
            
            assertThat(respuesta.getStatusCode().value()).isEqualTo(400);
        }
    }
}