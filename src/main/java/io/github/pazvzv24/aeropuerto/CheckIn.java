package io.github.pazvzv24.aeropuerto;

/**
 * Registro de un pasajero en un vuelo (una línea del manifiesto): el pasajero,
 * su estado y su asiento. Solo el vuelo puede cambiar el estado o el asiento.
 */
public final class CheckIn {

    private final Pasajero pasajero;
    private EstadoPasajero estado;
    private String asiento;

    CheckIn(Pasajero pasajero) {
        this(pasajero, EstadoPasajero.CHECK_IN);
    }

    /** Registro con un estado conocido y sin asiento (el vuelo le asigna uno). */
    public CheckIn(Pasajero pasajero, EstadoPasajero estado) {
        this(pasajero, estado, null);
    }

    /** Registro con un estado y un asiento conocidos (por ejemplo, leído desde la base de datos). */
    public CheckIn(Pasajero pasajero, EstadoPasajero estado, String asiento) {
        if (pasajero == null || estado == null) {
            throw new IllegalArgumentException("El pasajero y su estado son obligatorios.");
        }
        this.pasajero = pasajero;
        this.estado = estado;
        this.asiento = asiento == null || asiento.isBlank() ? null : ModeloAvion.normalizarAsiento(asiento);
    }

    public Pasajero getPasajero() {
        return pasajero;
    }

    public EstadoPasajero getEstado() {
        return estado;
    }

    /** Asiento asignado, por ejemplo "12C". */
    public String getAsiento() {
        return asiento;
    }

    void cambiarEstado(EstadoPasajero nuevoEstado) {
        this.estado = nuevoEstado;
    }

    void cambiarAsiento(String nuevoAsiento) {
        this.asiento = nuevoAsiento;
    }
}