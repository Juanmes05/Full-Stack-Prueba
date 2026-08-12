package es.uma.alquiler.controladores;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import es.uma.alquiler.dtos.AlquilerDTO;
import es.uma.alquiler.dtos.ErrorDTO;
import es.uma.alquiler.dtos.VehiculoDTO;
import es.uma.alquiler.entidades.Vehiculo;
import es.uma.alquiler.servicios.LogicaAlquileres;
import es.uma.alquiler.servicios.excepciones.ReglaNegocioException;
import es.uma.alquiler.servicios.excepciones.VehiculoDuplicadoExcception;
import es.uma.alquiler.servicios.excepciones.VehiculoInexistenteException;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/vehiculos")
public class VehiculoController {

    private final LogicaAlquileres servicio;

    public VehiculoController(LogicaAlquileres servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    public ResponseEntity<VehiculoDTO> crearVehiculo(@RequestBody VehiculoDTO dto, UriComponentsBuilder uriBuilder) {
        Vehiculo guardado = servicio.aniadirVehiculo(dto.aEntidad());
        
        URI location = uriBuilder.path("/vehiculos/{id}").buildAndExpand(guardado.getId()).toUri();
        
        return ResponseEntity.created(location).body(VehiculoDTO.convertirADTO(guardado));
    }

    @GetMapping
    public List<VehiculoDTO> listarVehiculos() {
        return servicio.obtenerVehiculos().stream()
                .map(ve -> VehiculoDTO.convertirADTO(ve)).toList();
    }

    @GetMapping("/{id}")
    public VehiculoDTO obtenerVehiculo(@PathVariable Long id) {
        return VehiculoDTO.convertirADTO(servicio.obtenerVehiculo(id));
    }

    @GetMapping("/{id}/alquileres")
    public List<AlquilerDTO> listarAlquileresDeVehiculo(@PathVariable Long id) {
        return servicio.obtenerAlquileresPorVehiculo(id).stream()
                .map(al -> AlquilerDTO.convertirADTO(al)).toList();
    }

    @PutMapping("/{id}/kilometraje")
    public ResponseEntity<Void> actualizarKilometraje(@PathVariable Long id, @RequestParam Integer kilometraje) {
        servicio.actualizarKilometraje(id, kilometraje);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/libres")
    public List<VehiculoDTO> vehiculosLibres(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin,
            @RequestParam Integer pasajeros) {
        
        return servicio.obtenerVehiculosLibres(inicio, fin, pasajeros).stream()
                .map(ve -> VehiculoDTO.convertirADTO(ve)).toList();
    }


    @ExceptionHandler(ReglaNegocioException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ErrorDTO reglaNegocioViolada(ReglaNegocioException e) {
        return new ErrorDTO(e.getMessage());
    }
    
    @ExceptionHandler(VehiculoInexistenteException.class)
    @ResponseStatus(code = HttpStatus.NOT_FOUND)
    public ErrorDTO vehiculoNoEncontrado(VehiculoInexistenteException e) {
        String mensaje = e.getMessage() != null ? e.getMessage() : "Vehículo no encontrado";
        return new ErrorDTO(mensaje);
    }
    
    @ExceptionHandler(VehiculoDuplicadoExcception.class)
    @ResponseStatus(code = HttpStatus.CONFLICT)
    public ErrorDTO vehiculoNoEncontrado(VehiculoDuplicadoExcception e) {
        String mensaje = e.getMessage() != null ? e.getMessage() : "Vehículo no encontrado";
        return new ErrorDTO(mensaje);
    }
}
