package es.uma.alquiler.servicios.excepciones;

public class AlquilerInexistenteException extends RuntimeException {
	public AlquilerInexistenteException() {
        super("Alquiler no encontrado");
    }

    public AlquilerInexistenteException(String mensaje) {
        super(mensaje);
    }
}
