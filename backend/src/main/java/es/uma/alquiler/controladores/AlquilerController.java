package es.uma.alquiler.controladores;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import es.uma.alquiler.dtos.AlquilerDTO;
import es.uma.alquiler.dtos.ErrorDTO;
import es.uma.alquiler.entidades.Alquiler;
import es.uma.alquiler.entidades.EstadoAlquiler;
import es.uma.alquiler.servicios.LogicaAlquileres;
import es.uma.alquiler.servicios.excepciones.ReglaNegocioException;
import es.uma.alquiler.servicios.excepciones.VehiculoInexistenteException;
import es.uma.alquiler.servicios.excepciones.AlquilerInexistenteException;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/alquileres")
public class AlquilerController {

    private final LogicaAlquileres servicio;

    public AlquilerController(LogicaAlquileres servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    public ResponseEntity<AlquilerDTO> crearAlquiler(@RequestBody AlquilerDTO dto, UriComponentsBuilder uriBuilder) {
        Alquiler entidad = dto.aEntidad();
        
        Alquiler guardado = servicio.aniadirAlquiler(entidad, dto.getVehiculoId());
        
        URI location = uriBuilder.path("/alquileres/{id}").buildAndExpand(guardado.getId()).toUri();
        
        return ResponseEntity.created(location).body(AlquilerDTO.convertirADTO(guardado));
    }

    @GetMapping
    public List<AlquilerDTO> consultarAlquileres(@RequestParam(required = false) EstadoAlquiler estado) {
        List<Alquiler> lista = estado == null ? servicio.obtenerAlquileres() : servicio.obtenerAlquileresPorEstado(estado);
        return lista.stream().map(al -> AlquilerDTO.convertirADTO(al)).toList();
    }

    @PostMapping("/{id}/confirmar")
    public void confirmarAlquiler(@PathVariable Long id) {
        servicio.confirmarAlquiler(id);
    }

    @PostMapping("/{id}/cancelar")
    public void cancelarAlquiler(@PathVariable Long id) {
        servicio.cancelarAlquiler(id);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ErrorDTO reglaNegocioViolada(ReglaNegocioException e) {
        return new ErrorDTO(e.getMessage());
    }
    
    @ExceptionHandler(AlquilerInexistenteException.class)
    @ResponseStatus(code = HttpStatus.NOT_FOUND)
    public ErrorDTO alquilerInexistente(AlquilerInexistenteException e) {
        String mensaje = e.getMessage() != null ? e.getMessage() : "Alquiler no encontrado";
        return new ErrorDTO(mensaje);
    }
    
    @ExceptionHandler(VehiculoInexistenteException.class)
    @ResponseStatus(code = HttpStatus.NOT_FOUND)
    public ErrorDTO vehiculoNoEncontrado(VehiculoInexistenteException e) {
        String mensaje = e.getMessage() != null ? e.getMessage() : "Vehículo no encontrado";
        return new ErrorDTO(mensaje);
    }
}
