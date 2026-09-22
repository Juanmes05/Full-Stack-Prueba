package es.uma.alquiler.controladores;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import es.uma.alquiler.dtos.AlquilerDTO;
import es.uma.alquiler.dtos.KilometrajeDTO;
import es.uma.alquiler.dtos.VehiculoDTO;
import es.uma.alquiler.entidades.Vehiculo;
import es.uma.alquiler.servicios.LogicaAlquileres;
import es.uma.alquiler.servicios.LogicaVehiculos;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/vehiculos")
public class VehiculoController {

    private final LogicaVehiculos logicaVehiculos;
    private final LogicaAlquileres logicaAlquileres;

    public VehiculoController(LogicaVehiculos logicaVehiculos, LogicaAlquileres logicaAlquileres) {
        this.logicaVehiculos = logicaVehiculos;
        this.logicaAlquileres = logicaAlquileres;
    }

    @PostMapping
    public ResponseEntity<VehiculoDTO> crearVehiculo(@Validated @RequestBody VehiculoDTO dto, UriComponentsBuilder uriBuilder) {
        Vehiculo guardado = logicaVehiculos.aniadirVehiculo(dto.aEntidad());

        URI location = uriBuilder.path("/vehiculos/{id}").buildAndExpand(guardado.getId()).toUri();

        return ResponseEntity.created(location).body(VehiculoDTO.convertirADTO(guardado));
    }

    @GetMapping
    public List<VehiculoDTO> listarVehiculos() {
        return logicaVehiculos.obtenerVehiculos().stream()
                .map(VehiculoDTO::convertirADTO).toList();
    }

    @GetMapping("/{id}")
    public VehiculoDTO obtenerVehiculo(@PathVariable Long id) {
        return VehiculoDTO.convertirADTO(logicaVehiculos.obtenerVehiculo(id));
    }

    @GetMapping("/{id}/alquileres")
    public List<AlquilerDTO> listarAlquileresDeVehiculo(@PathVariable Long id) {
        return logicaAlquileres.obtenerAlquileresPorVehiculo(id).stream()
                .map(AlquilerDTO::convertirADTO).toList();
    }

    /**
     * El nuevo kilometraje puede enviarse en el cuerpo ({"kilometraje": 123456})
     * o como parámetro (?kilometraje=123456).
     */
    @PutMapping("/{id}/kilometraje")
    public VehiculoDTO actualizarKilometraje(@PathVariable Long id,
            @RequestParam(name = "kilometraje", required = false) Integer kilometrajeParametro,
            @RequestBody(required = false) KilometrajeDTO cuerpo) {
        Integer kilometraje = kilometrajeParametro != null ? kilometrajeParametro
                : cuerpo != null ? cuerpo.getKilometraje() : null;
        return VehiculoDTO.convertirADTO(logicaVehiculos.actualizarKilometraje(id, kilometraje));
    }

    @GetMapping("/libres")
    public List<VehiculoDTO> vehiculosLibres(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin,
            @RequestParam Integer pasajeros) {

        return logicaAlquileres.obtenerVehiculosLibres(inicio, fin, pasajeros).stream()
                .map(VehiculoDTO::convertirADTO).toList();
    }
}
