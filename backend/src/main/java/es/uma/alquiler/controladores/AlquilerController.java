package es.uma.alquiler.controladores;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import es.uma.alquiler.dtos.AlquilerDTO;
import es.uma.alquiler.entidades.Alquiler;
import es.uma.alquiler.entidades.EstadoAlquiler;
import es.uma.alquiler.servicios.LogicaAlquileres;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/alquileres")
public class AlquilerController {

    private final LogicaAlquileres servicio;

    public AlquilerController(LogicaAlquileres servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    public ResponseEntity<AlquilerDTO> crearAlquiler(@Validated @RequestBody AlquilerDTO dto, UriComponentsBuilder uriBuilder) {
        Alquiler guardado = servicio.aniadirAlquiler(dto.aEntidad(), dto.getVehiculoSolicitado());

        URI location = uriBuilder.path("/alquileres/{id}").buildAndExpand(guardado.getId()).toUri();

        return ResponseEntity.created(location).body(AlquilerDTO.convertirADTO(guardado));
    }

    @GetMapping
    public List<AlquilerDTO> consultarAlquileres(@RequestParam(required = false) EstadoAlquiler estado) {
        List<Alquiler> lista = estado == null ? servicio.obtenerAlquileres() : servicio.obtenerAlquileresPorEstado(estado);
        return lista.stream().map(AlquilerDTO::convertirADTO).toList();
    }

    @GetMapping("/{id}")
    public AlquilerDTO obtenerAlquiler(@PathVariable Long id) {
        return AlquilerDTO.convertirADTO(servicio.obtenerAlquiler(id));
    }

    @PostMapping("/{id}/confirmar")
    public AlquilerDTO confirmarAlquiler(@PathVariable Long id) {
        return AlquilerDTO.convertirADTO(servicio.confirmarAlquiler(id));
    }

    @PostMapping("/{id}/cancelar")
    public AlquilerDTO cancelarAlquiler(@PathVariable Long id) {
        return AlquilerDTO.convertirADTO(servicio.cancelarAlquiler(id));
    }
}
