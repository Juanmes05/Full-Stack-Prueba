package es.uma.alquiler.controladores;

import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import es.uma.alquiler.dtos.ErrorDTO;
import es.uma.alquiler.servicios.excepciones.AlquilerInexistenteException;
import es.uma.alquiler.servicios.excepciones.ReglaNegocioException;
import es.uma.alquiler.servicios.excepciones.VehiculoDuplicadoException;
import es.uma.alquiler.servicios.excepciones.VehiculoInexistenteException;

/**
 * Traduce las excepciones de la aplicación a respuestas HTTP con cuerpo JSON
 * {"error": "descripción"}, comunes a todos los controladores.
 */
@RestControllerAdvice
public class GestorExcepciones {

    @ExceptionHandler(ReglaNegocioException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDTO reglaNegocioViolada(ReglaNegocioException e) {
        return new ErrorDTO(e.getMessage());
    }

    @ExceptionHandler(VehiculoInexistenteException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorDTO vehiculoNoEncontrado(VehiculoInexistenteException e) {
        return new ErrorDTO(e.getMessage());
    }

    @ExceptionHandler(AlquilerInexistenteException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorDTO alquilerNoEncontrado(AlquilerInexistenteException e) {
        return new ErrorDTO(e.getMessage());
    }

    @ExceptionHandler(VehiculoDuplicadoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorDTO vehiculoDuplicado(VehiculoDuplicadoException e) {
        return new ErrorDTO(e.getMessage());
    }

    // Dos altas simultáneas con la misma matrícula: la segunda la frena la restricción UNIQUE
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorDTO violacionIntegridad(DataIntegrityViolationException e) {
        return new ErrorDTO("La operación incumple una restricción de integridad de los datos (¿matrícula duplicada?).");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDTO datosNoValidos(MethodArgumentNotValidException e) {
        String mensaje = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining(" "));
        return new ErrorDTO(mensaje.isEmpty() ? "Los datos enviados no son válidos." : mensaje);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDTO cuerpoIlegible(HttpMessageNotReadableException e) {
        return new ErrorDTO("El cuerpo de la petición no es un JSON válido o tiene valores con formato incorrecto "
                + "(las fechas van como 2026-10-01T09:00 y el tipo debe ser TURISMO, SUV o FURGONETA).");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDTO parametroNoValido(MethodArgumentTypeMismatchException e) {
        return new ErrorDTO("El valor '" + e.getValue() + "' no es válido para el parámetro '" + e.getName() + "'.");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDTO parametroAusente(MissingServletRequestParameterException e) {
        return new ErrorDTO("Falta el parámetro obligatorio '" + e.getParameterName() + "'.");
    }
}
