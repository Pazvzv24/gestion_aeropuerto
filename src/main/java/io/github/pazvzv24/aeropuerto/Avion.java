package io.github.pazvzv24.aeropuerto;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Avión de la flota, identificado por su matrícula chilena (formato CC-AMA).
 * Su capacidad la define el modelo, y su estado sigue las reglas de {@link EstadoAvion}.
 * Garantiza siempre que 0 &lt;= pasajerosActuales &lt;= capacidad, y que el avión
 * esté vacío en los estados que lo exigen (disponible, en mantenimiento o fuera de servicio).
 */
public class Avion {

    /** Matrícula chilena: prefijo de nacionalidad "CC-" seguido de 3 letras. */
    private static final Pattern FORMATO_MATRICULA = Pattern.compile("CC-[A-Z]{3}");

    private final String matricula;
    private final ModeloAvion modelo;
    private int pasajerosActuales;
    private EstadoAvion estado;

    /** Crea un avión nuevo: disponible y sin pasajeros. */
    public Avion(String matricula, ModeloAvion modelo) {
        this(matricula, modelo, EstadoAvion.DISPONIBLE, 0);
    }

    /**
     * Crea un avión con un estado y pasajeros conocidos (por ejemplo, al cargar la flota).
     *
     * @throws IllegalArgumentException si los datos no forman un avión válido
     */
    public Avion(String matricula, ModeloAvion modelo, EstadoAvion estado, int pasajerosActuales) {
        if (!esMatriculaValida(matricula)) {
            throw new IllegalArgumentException("Matrícula inválida: use el formato CC-XXX.");
        }
        if (modelo == null) {
            throw new IllegalArgumentException("El modelo del avión es obligatorio.");
        }
        if (estado == null) {
            throw new IllegalArgumentException("El estado del avión es obligatorio.");
        }
        if (pasajerosActuales < 0 || pasajerosActuales > modelo.getCapacidad()) {
            throw new IllegalArgumentException(
                    "Los pasajeros a bordo deben estar entre 0 y la capacidad del modelo (" + modelo.getCapacidad() + ").");
        }
        if (estado.requiereAvionVacio() && pasajerosActuales > 0) {
            throw new IllegalArgumentException("Un avión en estado " + estado + " no puede tener pasajeros a bordo.");
        }
        this.matricula = normalizarMatricula(matricula);
        this.modelo = modelo;
        this.estado = estado;
        this.pasajerosActuales = pasajerosActuales;
    }

    /** Acepta mayúsculas o minúsculas y espacios alrededor: " cc-ama " es válida. */
    public static boolean esMatriculaValida(String matricula) {
        return matricula != null && FORMATO_MATRICULA.matcher(normalizarMatricula(matricula)).matches();
    }

    /** Deja la matrícula en mayúsculas y sin espacios alrededor. */
    public static String normalizarMatricula(String matricula) {
        return matricula == null ? "" : matricula.trim().toUpperCase(Locale.ROOT);
    }

    public String getMatricula() {
        return matricula;
    }

    public ModeloAvion getModelo() {
        return modelo;
    }

    public EstadoAvion getEstado() {
        return estado;
    }

    public int getCantidadMaxima() {
        return modelo.getCapacidad();
    }

    public int getPasajerosActuales() {
        return pasajerosActuales;
    }

    public int getAsientosDisponibles() {
        return getCantidadMaxima() - pasajerosActuales;
    }

    /** Porcentaje de ocupación del avión, entre 0 y 100. */
    public double getPorcentajeOcupacion() {
        return pasajerosActuales * 100.0 / getCantidadMaxima();
    }

    /**
     * Sube pasajeros. Solo se permite durante el embarque y hasta completar la capacidad.
     * Si la operación falla, el avión no cambia.
     */
    public OperationResult subirPasajeros(int cantidad) {
        if (!estado.permiteSubirPasajeros()) {
            return OperationResult.OPERATION_NOT_ALLOWED;
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
     * Baja pasajeros. Se permite durante el embarque (si alguien desiste) y al desembarcar.
     * Si la operación falla, el avión no cambia.
     */
    public OperationResult bajarPasajeros(int cantidad) {
        if (!estado.permiteBajarPasajeros()) {
            return OperationResult.OPERATION_NOT_ALLOWED;
        }
        if (cantidad <= 0) {
            return OperationResult.INVALID_AMOUNT;
        }
        if (cantidad > pasajerosActuales) {
            return OperationResult.NOT_ENOUGH_PASSENGERS;
        }
        pasajerosActuales -= cantidad;
        return OperationResult.SUCCESS;
    }

    /**
     * Cambia el estado del avión respetando la máquina de estados.
     * Si el nuevo estado exige el avión vacío y aún hay pasajeros, se rechaza.
     */
    public OperationResult cambiarEstado(EstadoAvion nuevoEstado) {
        if (!estado.puedeCambiarA(nuevoEstado)) {
            return OperationResult.INVALID_TRANSITION;
        }
        if (nuevoEstado.requiereAvionVacio() && pasajerosActuales > 0) {
            return OperationResult.AIRPLANE_NOT_EMPTY;
        }
        estado = nuevoEstado;
        return OperationResult.SUCCESS;
    }

    /** Ejemplo: "CC-AMA · Airbus A320". */
    @Override
    public String toString() {
        return matricula + " · " + modelo;
    }
}