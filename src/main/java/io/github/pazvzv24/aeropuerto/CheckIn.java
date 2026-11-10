package io.github.pazvzv24.aeropuerto;

/**
 * Registro de un pasajero en un vuelo (una línea del manifiesto).
 * Solo el vuelo puede cambiar su estado.
 */
public final class CheckIn {

    private final Pasajero pasajero;
    private EstadoPasajero estado = EstadoPasajero.CHECK_IN;

    CheckIn(Pasajero pasajero) {
        this(pasajero, EstadoPasajero.CHECK_IN);
    }

    /** Registro con un estado conocido (por ejemplo, leído desde la base de datos). */
    public CheckIn(Pasajero pasajero, EstadoPasajero estado) {
        if (pasajero == null || estado == null) {
            throw new IllegalArgumentException("El pasajero y su estado son obligatorios.");
        }
        this.pasajero = pasajero;
        this.estado = estado;
    }

    public Pasajero getPasajero() {
        return pasajero;
    }

    public EstadoPasajero getEstado() {
        return estado;
    }

    void cambiarEstado(EstadoPasajero nuevoEstado) {
        this.estado = nuevoEstado;
    }
}