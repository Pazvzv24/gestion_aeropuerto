package io.github.pazvzv24.aeropuerto;

/**
 * Registro de un pasajero en un vuelo (una línea del manifiesto).
 * Solo el vuelo puede cambiar su estado.
 */
public final class CheckIn {

    private final Pasajero pasajero;
    private EstadoPasajero estado = EstadoPasajero.CHECK_IN;

    CheckIn(Pasajero pasajero) {
        this.pasajero = pasajero;
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