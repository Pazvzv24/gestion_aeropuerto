package io.github.pazvzv24.aeropuerto;

/** Situación de un pasajero dentro de un vuelo. */
public enum EstadoPasajero {
    CHECK_IN("Check-in realizado"),
    EMBARCADO("Embarcado"),
    NO_SE_PRESENTO("No se presentó");

    private final String nombre;

    EstadoPasajero(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }
}