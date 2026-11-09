/**
 * Pasajero con nombre e identificador.
 * Modelo base para una futura lista de pasajeros por vuelo.
 */
public class Pasajero {

    private final String nombre;
    private final String id;

    /**
     * @throws IllegalArgumentException si el nombre o el ID están vacíos
     */
    public Pasajero(String nombre, String id) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del pasajero es obligatorio.");
        }
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El ID del pasajero es obligatorio.");
        }
        this.nombre = nombre.trim();
        this.id = id.trim();
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }
}
