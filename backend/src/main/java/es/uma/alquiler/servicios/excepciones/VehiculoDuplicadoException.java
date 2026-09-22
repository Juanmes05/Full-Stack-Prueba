package es.uma.alquiler.servicios.excepciones;

public class VehiculoDuplicadoException extends RuntimeException {
    public VehiculoDuplicadoException(String message) {
        super(message);
    }
}
