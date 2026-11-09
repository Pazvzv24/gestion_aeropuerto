package io.github.pazvzv24.aeropuerto;

import java.util.Random;

/**
 * Avión con capacidad máxima, pasajeros a bordo y estado del vuelo (activo o cancelado).
 * Garantiza siempre que 0 &lt;= pasajerosActuales &lt;= cantidadMaxima.
 */
public class Avion {

    /** Capacidad mínima y máxima de los aviones generados al azar. */
    public static final int CAPACIDAD_MINIMA_ALEATORIA = 50;
    public static final int CAPACIDAD_MAXIMA_ALEATORIA = 150;

    private final int id;
    private final int cantidadMaxima;
    private int pasajerosActuales;
    private final boolean estado;

    /**
     * Crea un avión con valores explícitos.
     *
     * @throws IllegalArgumentException si los valores no forman un avión válido
     */
    public Avion(int id, int cantidadMaxima, int pasajerosActuales, boolean estado) {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID del avión debe ser mayor que 0.");
        }
        if (cantidadMaxima <= 0) {
            throw new IllegalArgumentException("La capacidad máxima debe ser mayor que 0.");
        }
        if (pasajerosActuales < 0 || pasajerosActuales > cantidadMaxima) {
            throw new IllegalArgumentException(
                    "Los pasajeros a bordo deben estar entre 0 y la capacidad máxima (" + cantidadMaxima + ").");
        }
        this.id = id;
        this.cantidadMaxima = cantidadMaxima;
        this.pasajerosActuales = pasajerosActuales;
        this.estado = estado;
    }

    /** Crea un avión con capacidad, pasajeros y estado aleatorios. */
    public Avion(int id) {
        this(id, new Random());
    }

    /**
     * Crea un avión aleatorio usando el generador indicado.
     * Recibir el generador permite reproducir los mismos aviones en las pruebas.
     */
    public Avion(int id, Random random) {
        this(id, capacidadAleatoria(random), random);
    }

    private Avion(int id, int capacidad, Random random) {
        this(id, capacidad, random.nextInt(capacidad + 1), random.nextBoolean());
    }

    private static int capacidadAleatoria(Random random) {
        // Entre 50 y 150, ambos incluidos
        return CAPACIDAD_MINIMA_ALEATORIA
                + random.nextInt(CAPACIDAD_MAXIMA_ALEATORIA - CAPACIDAD_MINIMA_ALEATORIA + 1);
    }

    public int getId() {
        return id;
    }

    public int getCantidadMaxima() {
        return cantidadMaxima;
    }

    public int getPasajerosActuales() {
        return pasajerosActuales;
    }

    public int getAsientosDisponibles() {
        return cantidadMaxima - pasajerosActuales;
    }

    /** @return true si el vuelo está activo; false si está cancelado. */
    public boolean getEstado() {
        return estado;
    }

    /**
     * Sube pasajeros al avión. Solo se permite si el vuelo está activo
     * y hay asientos suficientes. Si la operación falla, el avión no cambia.
     */
    public OperationResult subirPasajeros(int cantidad) {
        if (!estado) {
            return OperationResult.FLIGHT_CANCELLED;
        }
        if (cantidad <= 0) {
            return OperationResult.INVALID_AMOUNT;
        }
        if (cantidad > getAsientosDisponibles()) {
            return OperationResult.CAPACITY_EXCEEDED;
        }
        pasajerosActuales += cantidad;
        return OperationResult.SUCCESS;
    }

    /**
     * Baja pasajeros del avión. Se permite aunque el vuelo esté cancelado
     * (los pasajeros siempre deben poder desembarcar). Si la operación falla, el avión no cambia.
     */
    public OperationResult bajarPasajeros(int cantidad) {
        if (cantidad <= 0) {
            return OperationResult.INVALID_AMOUNT;
        }
        if (cantidad > pasajerosActuales) {
            return OperationResult.NOT_ENOUGH_PASSENGERS;
        }
        pasajerosActuales -= cantidad;
        return OperationResult.SUCCESS;
    }
}

