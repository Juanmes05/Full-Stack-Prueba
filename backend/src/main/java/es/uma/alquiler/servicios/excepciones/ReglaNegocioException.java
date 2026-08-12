package es.uma.alquiler.servicios.excepciones;

public class ReglaNegocioException extends RuntimeException{
	public ReglaNegocioException(String message) {
        super(message);
    }
}
