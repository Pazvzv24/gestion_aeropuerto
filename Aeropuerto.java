import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Aeropuerto con capacidad para 100 aviones, guardados en un arreglo.
 * El arreglo es privado: desde fuera solo se puede consultar una copia,
 * así nadie puede reemplazar o borrar aviones sin pasar por esta clase.
 */
public class Aeropuerto {

    /** Cantidad máxima de aviones que admite el aeropuerto. */
    public static final int CAPACIDAD_AVIONES = 100;

    /** Aviones que se crean al iniciar la simulación. */
    public static final int AVIONES_INICIALES = 5;

    private final String nombre;
    private final Avion[] aviones = new Avion[CAPACIDAD_AVIONES];

    /** Crea el aeropuerto con 5 aviones aleatorios (IDs 1 a 5). */
    public Aeropuerto(String nombre) {
        this(nombre, new Random());
    }

    /** Crea el aeropuerto con 5 aviones aleatorios usando el generador indicado. */
    public Aeropuerto(String nombre, Random random) {
        this.nombre = validarNombre(nombre);
        for (int i = 0; i < AVIONES_INICIALES; i++) {
            aviones[i] = new Avion(i + 1, random);
        }
    }

    /**
     * Crea el aeropuerto con aviones conocidos (útil para pruebas).
     *
     * @throws IllegalArgumentException si hay más de 100 aviones, alguno es nulo o hay IDs repetidos
     */
    public Aeropuerto(String nombre, Avion... avionesIniciales) {
        this.nombre = validarNombre(nombre);
        if (avionesIniciales.length > CAPACIDAD_AVIONES) {
            throw new IllegalArgumentException("El aeropuerto admite como máximo " + CAPACIDAD_AVIONES + " aviones.");
        }
        Set<Integer> ids = new HashSet<>();
        for (int i = 0; i < avionesIniciales.length; i++) {
            Avion avion = avionesIniciales[i];
            if (avion == null) {
                throw new IllegalArgumentException("La lista de aviones no puede contener valores nulos.");
            }
            if (!ids.add(avion.getId())) {
                throw new IllegalArgumentException("ID de avión repetido: " + avion.getId());
            }
            aviones[i] = avion;
        }
    }

    private static String validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del aeropuerto es obligatorio.");
        }
        return nombre.trim();
    }

    public String getNombre() {
        return nombre;
    }

    /** Copia de solo lectura de los aviones registrados (sin posiciones vacías). */
    public List<Avion> getAviones() {
        List<Avion> lista = new ArrayList<>();
        for (Avion avion : aviones) {
            if (avion != null) {
                lista.add(avion);
            }
        }
        return Collections.unmodifiableList(lista);
    }

    /** Total de pasajeros a bordo de todos los aviones (activos y cancelados). */
    public int calcularTotalPasajeros() {
        int total = 0;
        for (Avion avion : aviones) {
            if (avion != null) {
                total += avion.getPasajerosActuales();
            }
        }
        return total;
    }

    /** Total de asientos libres en todos los aviones (activos y cancelados). */
    public int calcularTotalDisponibles() {
        int total = 0;
        for (Avion avion : aviones) {
            if (avion != null) {
                total += avion.getAsientosDisponibles();
            }
        }
        return total;
    }

    /** @return el avión con ese ID, o null si no existe. */
    public Avion buscarAvion(int id) {
        for (Avion avion : aviones) {
            if (avion != null && avion.getId() == id) {
                return avion;
            }
        }
        return null;
    }

    /** Sube pasajeros al avión indicado; si no existe, devuelve AIRPLANE_NOT_FOUND. */
    public OperationResult subirPasajeros(int idAvion, int cantidad) {
        Avion avion = buscarAvion(idAvion);
        return avion == null ? OperationResult.AIRPLANE_NOT_FOUND : avion.subirPasajeros(cantidad);
    }

    /** Baja pasajeros del avión indicado; si no existe, devuelve AIRPLANE_NOT_FOUND. */
    public OperationResult bajarPasajeros(int idAvion, int cantidad) {
        Avion avion = buscarAvion(idAvion);
        return avion == null ? OperationResult.AIRPLANE_NOT_FOUND : avion.bajarPasajeros(cantidad);
    }
}