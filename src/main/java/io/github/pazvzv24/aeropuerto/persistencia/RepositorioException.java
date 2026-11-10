package io.github.pazvzv24.aeropuerto.persistencia;

/**
 * Error al leer o guardar datos. El mensaje está pensado para mostrarse al usuario:
 * nunca incluye contraseñas ni detalles internos de la base de datos.
 */
public class RepositorioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RepositorioException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}