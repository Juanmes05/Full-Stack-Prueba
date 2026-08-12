package es.uma.alquiler.servicios.excepciones;

public class VehiculoInexistenteException extends RuntimeException{
	public VehiculoInexistenteException() {
        super("Vehículo no encontrado");
    }

    public VehiculoInexistenteException(String mensaje) {
        super(mensaje);
    }
}
