package es.uma.alquiler;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
class BackendApplicationTests {

    private static final ParameterizedTypeReference<Map<String, String>> ERROR =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<List<VehiculoDTO>> LISTA_VEHICULOS =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<List<AlquilerDTO>> LISTA_ALQUILERES =
            new ParameterizedTypeReference<>() {};

    @Autowired
    private TestRestTemplate restTemplate;

    @Value(value="${local.server.port}")
    private int port;

    @Autowired
    private VehiculoRepository vehiculoRepo;

    @Autowired
    private AlquilerRepository alquilerRepo;

    // Cada prueba parte de una base de datos vacía sin tener que reiniciar el contexto de Spring
    @BeforeEach
    void vaciarBaseDatos() {
        alquilerRepo.deleteAll();
        vehiculoRepo.deleteAll();
    }

    private URI uri(String scheme, String host, int port, String ...paths) {
        UriBuilderFactory ubf = new DefaultUriBuilderFactory();
        UriBuilder ub = ubf.builder().scheme(scheme).host(host).port(port);
        for (String path: paths) {
            ub = ub.path(path);
        }
        return ub.build();
    }

    private URI uriConParametros(String path, Map<String, Object> parametros) {
        UriBuilder ub = new DefaultUriBuilderFactory().builder()
                .scheme("http").host("localhost").port(port).path(path);
        parametros.forEach(ub::queryParam);
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

    private <T> RequestEntity<T> put(String scheme, String host, int port, String path, T object) {
        return RequestEntity.put(uri(scheme, host, port, path))
                .contentType(MediaType.APPLICATION_JSON)
                .body(object);
    }

    private RequestEntity<Void> postAccion(String scheme, String host, int port, String path) {
        return RequestEntity.post(uri(scheme, host, port, path)).build();
    }

    private Vehiculo guardarVehiculo(String matricula, TipoVehiculo tipo, int capacidad, int kilometraje) {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setMatricula(matricula);
        vehiculo.setTipo(tipo);
        vehiculo.setCapacidadMaxima(capacidad);
        vehiculo.setKilometrajeActual(kilometraje);
        return vehiculoRepo.save(vehiculo);
    }

    private Alquiler guardarAlquiler(Long vehiculoId, int pasajeros, LocalDateTime inicio, LocalDateTime fin,
            EstadoAlquiler estado) {
        Alquiler alquiler = new Alquiler();
        alquiler.setVehiculo(vehiculoRepo.findById(vehiculoId).get());
        alquiler.setPasajerosPrevistos(pasajeros);
        alquiler.setFechaInicio(inicio);
        alquiler.setFechaFin(fin);
        alquiler.setEstado(estado);
        return alquilerRepo.save(alquiler);
    }

    private AlquilerDTO nuevoAlquiler(Long vehiculoId, int pasajeros, LocalDateTime inicio, LocalDateTime fin) {
        AlquilerDTO alquiler = new AlquilerDTO();
        alquiler.setVehiculoId(vehiculoId);
        alquiler.setPasajerosPrevistos(pasajeros);
        alquiler.setFechaInicio(inicio);
        alquiler.setFechaFin(fin);
        return alquiler;
    }


    @Nested
    @DisplayName("cuando la base de datos está vacía")
    class BaseDatosVacia {

        @Test
        @DisplayName("devuelve listas vacías de vehículos y alquileres")
        void listasVacias() {
            var vehiculos = restTemplate.exchange(get("http", "localhost", port, "/vehiculos"), LISTA_VEHICULOS);
            var alquileres = restTemplate.exchange(get("http", "localhost", port, "/alquileres"), LISTA_ALQUILERES);

            assertThat(vehiculos.getStatusCode().value()).isEqualTo(200);
            assertThat(vehiculos.getBody()).isEmpty();
            assertThat(alquileres.getStatusCode().value()).isEqualTo(200);
            assertThat(alquileres.getBody()).isEmpty();
        }

        @Test
        @DisplayName("da error 404 al obtener un vehículo concreto que no existe")
        void errorAlObtenerVehiculoInexistente() {
            var peticion = get("http", "localhost", port, "/vehiculos/1");
            var respuesta = restTemplate.exchange(peticion, ERROR);

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
            var respuesta = restTemplate.exchange(peticion, VehiculoDTO.class);

            assertThat(respuesta.getStatusCode().value()).isEqualTo(201);
            assertThat(respuesta.getHeaders().get("Location").get(0))
                    .startsWith("http://localhost:" + port + "/vehiculos");
            assertThat(respuesta.getBody().getId()).isNotNull();

            List<Vehiculo> vehiculosBD = vehiculoRepo.findAll();
            assertThat(vehiculosBD).hasSize(1);
            assertThat(vehiculosBD.get(0).getMatricula()).isEqualTo("1234ABC");
        }

        @Test
        @DisplayName("da error 400 al insertar un vehículo con datos no válidos")
        void errorVehiculoNoValido() {
            VehiculoDTO sinMatricula = new VehiculoDTO();
            sinMatricula.setTipo(TipoVehiculo.SUV);
            sinMatricula.setCapacidadMaxima(0);
            sinMatricula.setKilometrajeActual(10);

            var respuesta = restTemplate.exchange(post("http", "localhost", port, "/vehiculos", sinMatricula), ERROR);

            assertThat(respuesta.getStatusCode().value()).isEqualTo(400);
            assertThat(respuesta.getBody().get("error")).contains("matrícula").contains("capacidad");
            assertThat(vehiculoRepo.count()).isZero();
        }

        @Test
        @DisplayName("da error 400 al insertar un vehículo de un tipo que no existe")
        void errorTipoVehiculo() {
            var cuerpo = Map.of("matricula", "1234ABC", "tipo", "MOTO", "capacidadMaxima", 2, "kilometrajeActual", 0);

            var respuesta = restTemplate.exchange(post("http", "localhost", port, "/vehiculos", cuerpo), ERROR);

            assertThat(respuesta.getStatusCode().value()).isEqualTo(400);
            assertThat(respuesta.getBody()).containsKey("error");
        }

        @Test
        @DisplayName("da error 404 al intentar crear un alquiler de un vehículo inexistente")
        void errorCrearAlquilerVehiculoInexistente() {
            AlquilerDTO alquiler = nuevoAlquiler(99L, 2, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(3));

            var peticion = post("http", "localhost", port, "/alquileres", alquiler);
            var respuesta = restTemplate.exchange(peticion, ERROR);

            assertThat(respuesta.getStatusCode().value()).isEqualTo(404);
            assertThat(respuesta.getBody()).containsKey("error");
        }

        @Test
        @DisplayName("da error 404 al consultar, confirmar o cancelar un alquiler inexistente")
        void errorAlquilerInexistente() {
            var consulta = restTemplate.exchange(get("http", "localhost", port, "/alquileres/7"), ERROR);
            var confirmacion = restTemplate.exchange(postAccion("http", "localhost", port, "/alquileres/7/confirmar"), ERROR);
            var cancelacion = restTemplate.exchange(postAccion("http", "localhost", port, "/alquileres/7/cancelar"), ERROR);

            assertThat(consulta.getStatusCode().value()).isEqualTo(404);
            assertThat(confirmacion.getStatusCode().value()).isEqualTo(404);
            assertThat(cancelacion.getStatusCode().value()).isEqualTo(404);
            assertThat(confirmacion.getBody()).containsKey("error");
        }

        @Test
        @DisplayName("da error 404 al pedir los alquileres o cambiar el kilometraje de un vehículo inexistente")
        void errorVehiculoInexistente() {
            var alquileres = restTemplate.exchange(get("http", "localhost", port, "/vehiculos/3/alquileres"), ERROR);
            var kilometraje = restTemplate.exchange(
                    put("http", "localhost", port, "/vehiculos/3/kilometraje", Map.of("kilometraje", 10)), ERROR);

            assertThat(alquileres.getStatusCode().value()).isEqualTo(404);
            assertThat(kilometraje.getStatusCode().value()).isEqualTo(404);
        }

        @Test
        @DisplayName("da error 400 si se filtra por un estado que no existe")
        void errorEstadoDesconocido() {
            var peticion = RequestEntity.get(uriConParametros("/alquileres", Map.of("estado", "TERMINADO"))).build();
            var respuesta = restTemplate.exchange(peticion, ERROR);

            assertThat(respuesta.getStatusCode().value()).isEqualTo(400);
            assertThat(respuesta.getBody().get("error")).contains("estado");
        }

        @Test
        @DisplayName("da error 400 si faltan parámetros al buscar vehículos libres")
        void errorLibresSinParametros() {
            var peticion = RequestEntity.get(uriConParametros("/vehiculos/libres", Map.of("pasajeros", 2))).build();
            var respuesta = restTemplate.exchange(peticion, ERROR);

            assertThat(respuesta.getStatusCode().value()).isEqualTo(400);
            assertThat(respuesta.getBody()).containsKey("error");
        }
    }

    @Nested
    @DisplayName("cuando la base de datos tiene datos")
    class BaseDatosConDatos {

        private Long turismoId;
        private Long furgonetaId;
        private Long suvId;
        private LocalDateTime mañana;

        @BeforeEach
        void insertarDatos() {
            turismoId = guardarVehiculo("9876XYZ", TipoVehiculo.TURISMO, 5, 100000).getId();
            // Furgoneta de alta rotación (R8): más de 150.000 km
            furgonetaId = guardarVehiculo("5555FFF", TipoVehiculo.FURGONETA, 3, 160000).getId();
            // SUV desgastado (R8): más de 200.000 km, admite como mucho 3 de sus 7 plazas
            suvId = guardarVehiculo("7777SSS", TipoVehiculo.SUV, 7, 250000).getId();

            mañana = LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.MINUTES);
        }

        @Test
        @DisplayName("devuelve todos los vehículos y uno concreto")
        void consultaVehiculos() {
            var todos = restTemplate.exchange(get("http", "localhost", port, "/vehiculos"), LISTA_VEHICULOS);
            var uno = restTemplate.exchange(get("http", "localhost", port, "/vehiculos/" + furgonetaId), VehiculoDTO.class);

            assertThat(todos.getBody()).extracting(VehiculoDTO::getMatricula)
                    .containsExactly("9876XYZ", "5555FFF", "7777SSS");
            assertThat(uno.getStatusCode().value()).isEqualTo(200);
            assertThat(uno.getBody().getTipo()).isEqualTo(TipoVehiculo.FURGONETA);
            assertThat(uno.getBody().getKilometrajeActual()).isEqualTo(160000);
        }

        @Test
        @DisplayName("da error 409 al insertar un vehículo con una matrícula existente")
        void errorMatriculaDuplicada() {
            VehiculoDTO repetido = new VehiculoDTO();
            repetido.setMatricula("9876xyz");
            repetido.setTipo(TipoVehiculo.SUV);
            repetido.setCapacidadMaxima(7);
            repetido.setKilometrajeActual(0);

            var respuesta = restTemplate.exchange(post("http", "localhost", port, "/vehiculos", repetido), ERROR);

            assertThat(respuesta.getStatusCode().value()).isEqualTo(409);
            assertThat(respuesta.getBody().get("error")).contains("9876XYZ");
            assertThat(vehiculoRepo.count()).isEqualTo(3);
        }

        @Test
        @DisplayName("actualiza el kilometraje enviado en el cuerpo o como parámetro")
        void actualizaKilometraje() {
            var conCuerpo = restTemplate.exchange(
                    put("http", "localhost", port, "/vehiculos/" + turismoId + "/kilometraje", Map.of("kilometraje", 120000)),
                    VehiculoDTO.class);

            assertThat(conCuerpo.getStatusCode().value()).isEqualTo(200);
            assertThat(conCuerpo.getBody().getKilometrajeActual()).isEqualTo(120000);

            var conParametro = restTemplate.exchange(RequestEntity.put(uriConParametros(
                    "/vehiculos/" + turismoId + "/kilometraje", Map.of("kilometraje", 130000))).build(), VehiculoDTO.class);

            assertThat(conParametro.getStatusCode().value()).isEqualTo(200);
            assertThat(vehiculoRepo.findById(turismoId).get().getKilometrajeActual()).isEqualTo(130000);
        }

        @Test
        @DisplayName("da error 400 al poner un kilometraje negativo")
        void errorKilometrajeNegativo() {
            var respuesta = restTemplate.exchange(
                    put("http", "localhost", port, "/vehiculos/" + turismoId + "/kilometraje", Map.of("kilometraje", -1)),
                    ERROR);

            assertThat(respuesta.getStatusCode().value()).isEqualTo(400);
            assertThat(vehiculoRepo.findById(turismoId).get().getKilometrajeActual()).isEqualTo(100000);
        }

        @Test
        @DisplayName("inserta un alquiler válido y le asigna el estado PENDIENTE (R1)")
        void crearAlquilerValido() {
            AlquilerDTO alquiler = nuevoAlquiler(turismoId, 4, mañana, mañana.plusDays(2));
            alquiler.setEstado(EstadoAlquiler.CONFIRMADO);

            var peticion = post("http", "localhost", port, "/alquileres", alquiler);
            var respuesta = restTemplate.exchange(peticion, AlquilerDTO.class);

            assertThat(respuesta.getStatusCode().value()).isEqualTo(201);
            assertThat(respuesta.getHeaders().getLocation().toString())
                    .isEqualTo("http://localhost:" + port + "/alquileres/" + respuesta.getBody().getId());
            assertThat(respuesta.getBody().getEstado()).isEqualTo(EstadoAlquiler.PENDIENTE);
            assertThat(respuesta.getBody().getVehiculo().getMatricula()).isEqualTo("9876XYZ");

            List<Alquiler> alquileresBD = alquilerRepo.findAll();
            assertThat(alquileresBD).hasSize(1);
            assertThat(alquileresBD.get(0).getEstado()).isEqualTo(EstadoAlquiler.PENDIENTE);
        }

        @Test
        @DisplayName("acepta el vehículo del alquiler como objeto anidado")
        void crearAlquilerConVehiculoAnidado() {
            var cuerpo = Map.of(
                    "vehiculo", Map.of("id", turismoId),
                    "pasajerosPrevistos", 2,
                    "fechaInicio", mañana.toString(),
                    "fechaFin", mañana.plusDays(1).toString());

            var respuesta = restTemplate.exchange(post("http", "localhost", port, "/alquileres", cuerpo), AlquilerDTO.class);

            assertThat(respuesta.getStatusCode().value()).isEqualTo(201);
            assertThat(respuesta.getBody().getVehiculoId()).isEqualTo(turismoId);
        }

        @Test
        @DisplayName("da error 400 si al alquiler le faltan datos")
        void errorAlquilerIncompleto() {
            AlquilerDTO alquiler = new AlquilerDTO();
            alquiler.setVehiculoId(turismoId);

            var respuesta = restTemplate.exchange(post("http", "localhost", port, "/alquileres", alquiler), ERROR);

            assertThat(respuesta.getStatusCode().value()).isEqualTo(400);
            assertThat(respuesta.getBody().get("error")).contains("pasajeros");
        }

        @Test
        @DisplayName("rechaza un alquiler si los pasajeros superan la capacidad (R2)")
        void errorCapacidadSuperada() {
            AlquilerDTO alquiler = nuevoAlquiler(turismoId, 6, mañana, mañana.plusDays(2));

            var peticion = post("http", "localhost", port, "/alquileres", alquiler);
            var respuesta = restTemplate.exchange(peticion, ERROR);

            assertThat(respuesta.getStatusCode().value()).isEqualTo(400);
            assertThat(respuesta.getBody().get("error")).contains("capacidad");
            assertThat(alquilerRepo.count()).isEqualTo(0);
        }

        @Test
        @DisplayName("rechaza un alquiler cuya fecha de fin no es posterior a la de inicio (R3)")
        void errorFechasInvertidas() {
            AlquilerDTO alquiler = nuevoAlquiler(turismoId, 2, mañana, mañana.minusDays(1));

            var respuesta = restTemplate.exchange(post("http", "localhost", port, "/alquileres", alquiler), ERROR);

            assertThat(respuesta.getStatusCode().value()).isEqualTo(400);
            assertThat(respuesta.getBody().get("error")).contains("posterior");
        }

        @Test
        @DisplayName("rechaza un alquiler de turismo si no dura mínimo 24h (R4)")
        void errorDuracionMinimaTurismo() {
            AlquilerDTO alquiler = nuevoAlquiler(turismoId, 2, mañana, mañana.plusHours(12));

            var peticion = post("http", "localhost", port, "/alquileres", alquiler);
            var respuesta = restTemplate.exchange(peticion, ERROR);

            assertThat(respuesta.getStatusCode().value()).isEqualTo(400);
            assertThat(respuesta.getBody()).containsKey("error");
        }

        @Test
        @DisplayName("rechaza un alquiler de más de cinco días (R5)")
        void errorDuracionMaxima() {
            AlquilerDTO alquiler = nuevoAlquiler(turismoId, 2, mañana, mañana.plusDays(5).plusHours(1));

            var respuesta = restTemplate.exchange(post("http", "localhost", port, "/alquileres", alquiler), ERROR);

            assertThat(respuesta.getStatusCode().value()).isEqualTo(400);
            assertThat(respuesta.getBody().get("error")).contains("cinco días");
        }

        @Test
        @DisplayName("solo alquila furgonetas de más de 150.000 km para menos de seis horas (R8)")
        void furgonetaAltaRotacion() {
            var largo = restTemplate.exchange(post("http", "localhost", port, "/alquileres",
                    nuevoAlquiler(furgonetaId, 2, mañana, mañana.plusHours(7))), ERROR);
            var corto = restTemplate.exchange(post("http", "localhost", port, "/alquileres",
                    nuevoAlquiler(furgonetaId, 2, mañana, mañana.plusHours(3))), AlquilerDTO.class);

            assertThat(largo.getStatusCode().value()).isEqualTo(400);
            assertThat(largo.getBody().get("error")).contains("seis horas");
            assertThat(corto.getStatusCode().value()).isEqualTo(201);
        }

        @Test
        @DisplayName("limita los pasajeros al 50% de la capacidad en vehículos de más de 200.000 km (R8)")
        void limiteCargaPorDesgaste() {
            var excesivo = restTemplate.exchange(post("http", "localhost", port, "/alquileres",
                    nuevoAlquiler(suvId, 4, mañana, mañana.plusDays(1))), ERROR);
            var admitido = restTemplate.exchange(post("http", "localhost", port, "/alquileres",
                    nuevoAlquiler(suvId, 3, mañana, mañana.plusDays(1))), AlquilerDTO.class);

            assertThat(excesivo.getStatusCode().value()).isEqualTo(400);
            assertThat(excesivo.getBody().get("error")).contains("50%");
            assertThat(admitido.getStatusCode().value()).isEqualTo(201);
        }

        @Test
        @DisplayName("permite confirmar un alquiler pendiente (R7)")
        void confirmarAlquilerPendiente() {
            Alquiler alquiler = guardarAlquiler(turismoId, 2, mañana, mañana.plusDays(2), EstadoAlquiler.PENDIENTE);

            var peticion = postAccion("http", "localhost", port, "/alquileres/" + alquiler.getId() + "/confirmar");
            var respuesta = restTemplate.exchange(peticion, AlquilerDTO.class);

            assertThat(respuesta.getStatusCode().value()).isEqualTo(200);
            assertThat(respuesta.getBody().getEstado()).isEqualTo(EstadoAlquiler.CONFIRMADO);
            assertThat(alquilerRepo.findById(alquiler.getId()).get().getEstado()).isEqualTo(EstadoAlquiler.CONFIRMADO);
        }

        @Test
        @DisplayName("no permite volver a confirmar un alquiler confirmado ni confirmar uno cancelado (R7)")
        void errorConfirmarNoPendiente() {
            Alquiler confirmado = guardarAlquiler(turismoId, 2, mañana, mañana.plusDays(2), EstadoAlquiler.CONFIRMADO);
            Alquiler cancelado = guardarAlquiler(turismoId, 2, mañana, mañana.plusDays(2), EstadoAlquiler.CANCELADO);

            var respuesta1 = restTemplate.exchange(
                    postAccion("http", "localhost", port, "/alquileres/" + confirmado.getId() + "/confirmar"), ERROR);
            var respuesta2 = restTemplate.exchange(
                    postAccion("http", "localhost", port, "/alquileres/" + cancelado.getId() + "/confirmar"), ERROR);

            assertThat(respuesta1.getStatusCode().value()).isEqualTo(400);
            assertThat(respuesta2.getStatusCode().value()).isEqualTo(400);
            assertThat(alquilerRepo.findById(cancelado.getId()).get().getEstado()).isEqualTo(EstadoAlquiler.CANCELADO);
        }

        @Test
        @DisplayName("rechaza confirmar un alquiler si hay solapamiento (R6)")
        void errorSolapamientoAlConfirmar() {
            guardarAlquiler(turismoId, 2, mañana, mañana.plusDays(2), EstadoAlquiler.CONFIRMADO);
            Alquiler alq2 = guardarAlquiler(turismoId, 2, mañana.plusDays(1), mañana.plusDays(3), EstadoAlquiler.PENDIENTE);

            var peticion = postAccion("http", "localhost", port, "/alquileres/" + alq2.getId() + "/confirmar");
            var respuesta = restTemplate.exchange(peticion, ERROR);

            assertThat(respuesta.getStatusCode().value()).isEqualTo(400);
            assertThat(respuesta.getBody().get("error")).contains("solapa");
        }

        @Test
        @DisplayName("permite confirmar alquileres consecutivos que no se solapan (R6)")
        void confirmarConsecutivos() {
            guardarAlquiler(turismoId, 2, mañana, mañana.plusDays(2), EstadoAlquiler.CONFIRMADO);
            Alquiler siguiente = guardarAlquiler(turismoId, 2, mañana.plusDays(2), mañana.plusDays(3), EstadoAlquiler.PENDIENTE);

            var respuesta = restTemplate.exchange(
                    postAccion("http", "localhost", port, "/alquileres/" + siguiente.getId() + "/confirmar"), AlquilerDTO.class);

            assertThat(respuesta.getStatusCode().value()).isEqualTo(200);
        }

        @Test
        @DisplayName("vuelve a comprobar el kilometraje al confirmar (R8)")
        void errorKilometrajeAlConfirmar() {
            var creado = restTemplate.exchange(post("http", "localhost", port, "/alquileres",
                    nuevoAlquiler(turismoId, 4, mañana, mañana.plusDays(1))), AlquilerDTO.class);
            restTemplate.exchange(put("http", "localhost", port, "/vehiculos/" + turismoId + "/kilometraje",
                    Map.of("kilometraje", 210000)), VehiculoDTO.class);

            var respuesta = restTemplate.exchange(postAccion("http", "localhost", port,
                    "/alquileres/" + creado.getBody().getId() + "/confirmar"), ERROR);

            assertThat(respuesta.getStatusCode().value()).isEqualTo(400);
            assertThat(respuesta.getBody().get("error")).contains("200.000");
        }

        @Test
        @DisplayName("cancela alquileres pendientes y confirmados, pero no los ya cancelados (R7)")
        void cancelarAlquileres() {
            Alquiler pendiente = guardarAlquiler(turismoId, 2, mañana, mañana.plusDays(1), EstadoAlquiler.PENDIENTE);
            Alquiler confirmado = guardarAlquiler(suvId, 2, mañana, mañana.plusDays(1), EstadoAlquiler.CONFIRMADO);

            var r1 = restTemplate.exchange(
                    postAccion("http", "localhost", port, "/alquileres/" + pendiente.getId() + "/cancelar"), AlquilerDTO.class);
            var r2 = restTemplate.exchange(
                    postAccion("http", "localhost", port, "/alquileres/" + confirmado.getId() + "/cancelar"), AlquilerDTO.class);
            var r3 = restTemplate.exchange(
                    postAccion("http", "localhost", port, "/alquileres/" + pendiente.getId() + "/cancelar"), ERROR);

            assertThat(r1.getStatusCode().value()).isEqualTo(200);
            assertThat(r1.getBody().getEstado()).isEqualTo(EstadoAlquiler.CANCELADO);
            assertThat(r2.getBody().getEstado()).isEqualTo(EstadoAlquiler.CANCELADO);
            assertThat(r3.getStatusCode().value()).isEqualTo(400);
        }

        @Test
        @DisplayName("consulta los alquileres, los de un estado, los de un vehículo y uno concreto")
        void consultaAlquileres() {
            Alquiler a1 = guardarAlquiler(turismoId, 2, mañana, mañana.plusDays(1), EstadoAlquiler.PENDIENTE);
            guardarAlquiler(turismoId, 2, mañana.plusDays(3), mañana.plusDays(4), EstadoAlquiler.CONFIRMADO);
            guardarAlquiler(suvId, 2, mañana, mañana.plusDays(1), EstadoAlquiler.CANCELADO);

            var todos = restTemplate.exchange(get("http", "localhost", port, "/alquileres"), LISTA_ALQUILERES);
            var confirmados = restTemplate.exchange(RequestEntity.get(
                    uriConParametros("/alquileres", Map.of("estado", "CONFIRMADO"))).build(), LISTA_ALQUILERES);
            var delTurismo = restTemplate.exchange(
                    get("http", "localhost", port, "/vehiculos/" + turismoId + "/alquileres"), LISTA_ALQUILERES);
            var uno = restTemplate.exchange(get("http", "localhost", port, "/alquileres/" + a1.getId()), AlquilerDTO.class);

            assertThat(todos.getBody()).hasSize(3);
            assertThat(confirmados.getBody()).extracting(AlquilerDTO::getEstado).containsExactly(EstadoAlquiler.CONFIRMADO);
            assertThat(delTurismo.getBody()).hasSize(2).allMatch(a -> a.getVehiculoId().equals(turismoId));
            assertThat(uno.getBody().getVehiculo().getMatricula()).isEqualTo("9876XYZ");
        }

        @Test
        @DisplayName("devuelve los vehículos libres que cumplen las reglas y no tienen alquileres confirmados solapados")
        void vehiculosLibres() {
            var parametros = Map.<String, Object>of(
                    "inicio", mañana.toString(), "fin", mañana.plusDays(2).toString(), "pasajeros", 3);

            var antes = restTemplate.exchange(
                    RequestEntity.get(uriConParametros("/vehiculos/libres", parametros)).build(), LISTA_VEHICULOS);

            // La furgoneta queda fuera por R8 (más de 150.000 km y más de seis horas)
            assertThat(antes.getStatusCode().value()).isEqualTo(200);
            assertThat(antes.getBody()).extracting(VehiculoDTO::getId).containsExactly(turismoId, suvId);

            guardarAlquiler(turismoId, 2, mañana.plusDays(1), mañana.plusDays(3), EstadoAlquiler.CONFIRMADO);
            guardarAlquiler(suvId, 2, mañana, mañana.plusDays(2), EstadoAlquiler.PENDIENTE);

            var despues = restTemplate.exchange(
                    RequestEntity.get(uriConParametros("/vehiculos/libres", parametros)).build(), LISTA_VEHICULOS);

            // Un alquiler pendiente no bloquea el vehículo; uno confirmado sí
            assertThat(despues.getBody()).extracting(VehiculoDTO::getId).containsExactly(suvId);
        }

        @Test
        @DisplayName("da error 400 al buscar vehículos libres con un intervalo no válido")
        void errorLibresIntervaloNoValido() {
            var parametros = Map.<String, Object>of(
                    "inicio", mañana.toString(), "fin", mañana.minusHours(1).toString(), "pasajeros", 2);

            var respuesta = restTemplate.exchange(
                    RequestEntity.get(uriConParametros("/vehiculos/libres", parametros)).build(), ERROR);

            assertThat(respuesta.getStatusCode().value()).isEqualTo(400);
            assertThat(respuesta.getBody()).containsKey("error");
        }
    }
}
