package io.github.pazvzv24.aeropuerto;

/**
 * Modelos de avión de la flota, con su capacidad de pasajeros.
 * Son modelos que operan comúnmente en Chile; la capacidad es referencial
 * (corresponde a una configuración típica y varía según cada aerolínea).
 */
public enum ModeloAvion {
    A319("Airbus", "A319", 144),
    A320("Airbus", "A320", 174),
    A320NEO("Airbus", "A320neo", 186),
    A321("Airbus", "A321", 220),
    A321NEO("Airbus", "A321neo", 238),
    B767_300ER("Boeing", "767-300ER", 238),
    B787_8("Boeing", "787-8", 247),
    B787_9("Boeing", "787-9", 300);

    private final String fabricante;
    private final String nombre;
    private final int capacidad;

    ModeloAvion(String fabricante, String nombre, int capacidad) {
        this.fabricante = fabricante;
        this.nombre = nombre;
        this.capacidad = capacidad;
    }

    public String getFabricante() {
        return fabricante;
    }

    public String getNombre() {
        return nombre;
    }

    /** Cantidad de asientos de pasajeros. */
    public int getCapacidad() {
        return capacidad;
    }

    /** Ejemplo: "Airbus A320". */
    @Override
    public String toString() {
        return fabricante + " " + nombre;
    }
}