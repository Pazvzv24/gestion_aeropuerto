package io.github.pazvzv24.aeropuerto;

/** Documento de identidad con el que viaja un pasajero. */
public enum TipoDocumento {
    RUT("RUT"),
    PASAPORTE("Pasaporte");

    private final String nombre;

    TipoDocumento(String nombre) {
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