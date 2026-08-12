package es.uma.alquiler.servicios.excepciones;

public class VehiculoDuplicadoExcception extends RuntimeException {
    public VehiculoDuplicadoExcception(String message) {
        super(message);
    }
}