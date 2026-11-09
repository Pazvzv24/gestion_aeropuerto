package io.github.pazvzv24.aeropuerto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aerolínea y su flota de aviones.
 * La flota activa admite como máximo 100 aviones. Los aviones dados de baja
 * (fuera de servicio) se conservan en el registro como historial, pero ya no
 * cuentan como flota activa.
 * La lista es privada: desde fuera solo se puede consultar una copia de solo lectura,
 * así nadie puede agregar o quitar aviones sin pasar por las reglas de esta clase.
 */
public final class Aerolinea {

    /** Cantidad máxima de aviones en la flota activa. */
    public static final int CAPACIDAD_FLOTA = 100;

    private final String nombre;
    private final List<Avion> registro = new ArrayList<>();

    /** Crea una aerolínea sin aviones. */
    public Aerolinea(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la aerolínea es obligatorio.");
        }
        this.nombre = nombre.trim();
    }

    /**
     * Crea una aerolínea con aviones conocidos (útil para pruebas).
     *
     * @throws IllegalArgumentException si hay aviones nulos, matrículas repetidas
     *                                  o más aviones activos que la capacidad de la flota
     */
    public Aerolinea(String nombre, Avion... aviones) {
        this(nombre);
        for (Avion avion : aviones) {
            registrar(avion);
        }
    }

    /**
     * Altamar Airlines (aerolínea ficticia): flota inicial de 15 aviones.
     * Las matrículas son inventadas; cualquier coincidencia con un avión real es casual.
     */
    public static Aerolinea altamarAirlines() {
        return new Aerolinea("Altamar Airlines",
                new Avion("CC-AMA", ModeloAvion.A320, EstadoAvion.DISPONIBLE, 0),
                new Avion("CC-AMB", ModeloAvion.A320, EstadoAvion.EMBARCANDO, 96),
                new Avion("CC-AMC", ModeloAvion.A320NEO, EstadoAvion.EN_VUELO, 171),
                new Avion("CC-AMD", ModeloAvion.A320NEO, EstadoAvion.DISPONIBLE, 0),
                new Avion("CC-AME", ModeloAvion.A321, EstadoAvion.EN_VUELO, 204),
                new Avion("CC-AMF", ModeloAvion.A321, EstadoAvion.EN_MANTENIMIENTO, 0),
                new Avion("CC-AMG", ModeloAvion.A321NEO, EstadoAvion.EMBARCANDO, 150),
                new Avion("CC-AMH", ModeloAvion.A319, EstadoAvion.DISPONIBLE, 0),
                new Avion("CC-AMI", ModeloAvion.A319, EstadoAvion.DESEMBARCANDO, 88),
                new Avion("CC-AMJ", ModeloAvion.B787_8, EstadoAvion.EN_VUELO, 231),
                new Avion("CC-AMK", ModeloAvion.B787_8, EstadoAvion.DISPONIBLE, 0),
                new Avion("CC-AML", ModeloAvion.B787_9, EstadoAvion.EN_VUELO, 276),
                new Avion("CC-AMM", ModeloAvion.B767_300ER, EstadoAvion.EN_MANTENIMIENTO, 0),
                new Avion("CC-AMN", ModeloAvion.A320, EstadoAvion.DISPONIBLE, 0),
                new Avion("CC-AMO", ModeloAvion.A321NEO, EstadoAvion.EN_VUELO, 219));
    }

    private void registrar(Avion avion) {
        if (avion == null) {
            throw new IllegalArgumentException("La lista de aviones no puede contener valores nulos.");
        }
        if (buscarAvion(avion.getMatricula()) != null) {
            throw new IllegalArgumentException("Matrícula repetida: " + avion.getMatricula());
        }
        if (avion.getEstado().esActivo() && getCantidadAvionesActivos() >= CAPACIDAD_FLOTA) {
            throw new IllegalArgumentException("La flota admite como máximo " + CAPACIDAD_FLOTA + " aviones activos.");
        }
        registro.add(avion);
    }

    public String getNombre() {
        return nombre;
    }

    /** Copia de solo lectura de todos los aviones registrados, incluidos los dados de baja. */
    public List<Avion> getFlota() {
        return Collections.unmodifiableList(new ArrayList<>(registro));
    }

    /** Aviones que no están fuera de servicio. */
    public int getCantidadAvionesActivos() {
        int activos = 0;
        for (Avion avion : registro) {
            if (avion.getEstado().esActivo()) {
                activos++;
            }
        }
        return activos;
    }

    /** Cuántos aviones más se pueden incorporar a la flota activa. */
    public int getEspacioDisponible() {
        return CAPACIDAD_FLOTA - getCantidadAvionesActivos();
    }

    public int contarPorEstado(EstadoAvion estado) {
        int total = 0;
        for (Avion avion : registro) {
            if (avion.getEstado() == estado) {
                total++;
            }
        }
        return total;
    }

    /** Total de pasajeros a bordo en toda la flota. */
    public int calcularTotalPasajeros() {
        int total = 0;
        for (Avion avion : registro) {
            total += avion.getPasajerosActuales();
        }
        return total;
    }

    /** Asientos libres en los aviones que están embarcando (los únicos que reciben pasajeros). */
    public int calcularAsientosEnEmbarque() {
        int total = 0;
        for (Avion avion : registro) {
            if (avion.getEstado() == EstadoAvion.EMBARCANDO) {
                total += avion.getAsientosDisponibles();
            }
        }
        return total;
    }

    /** @return el avión con esa matrícula (sin importar mayúsculas), o null si no existe. */
    public Avion buscarAvion(String matricula) {
        String buscada = Avion.normalizarMatricula(matricula);
        for (Avion avion : registro) {
            if (avion.getMatricula().equals(buscada)) {
                return avion;
            }
        }
        return null;
    }

    /** Incorpora un avión nuevo a la flota, disponible y sin pasajeros. */
    public OperationResult incorporarAvion(String matricula, ModeloAvion modelo) {
        if (!Avion.esMatriculaValida(matricula) || modelo == null) {
            return OperationResult.INVALID_REGISTRATION;
        }
        if (buscarAvion(matricula) != null) {
            return OperationResult.DUPLICATE_REGISTRATION;
        }
        if (getEspacioDisponible() <= 0) {
            return OperationResult.FLEET_FULL;
        }
        registro.add(new Avion(matricula, modelo));
        return OperationResult.SUCCESS;
    }

    /** Sube pasajeros al avión indicado; si no existe, devuelve AIRPLANE_NOT_FOUND. */
    public OperationResult subirPasajeros(String matricula, int cantidad) {
        Avion avion = buscarAvion(matricula);
        return avion == null ? OperationResult.AIRPLANE_NOT_FOUND : avion.subirPasajeros(cantidad);
    }

    /** Baja pasajeros del avión indicado; si no existe, devuelve AIRPLANE_NOT_FOUND. */
    public OperationResult bajarPasajeros(String matricula, int cantidad) {
        Avion avion = buscarAvion(matricula);
        return avion == null ? OperationResult.AIRPLANE_NOT_FOUND : avion.bajarPasajeros(cantidad);
    }

    /** Cambia el estado del avión indicado; si no existe, devuelve AIRPLANE_NOT_FOUND. */
    public OperationResult cambiarEstado(String matricula, EstadoAvion nuevoEstado) {
        Avion avion = buscarAvion(matricula);
        return avion == null ? OperationResult.AIRPLANE_NOT_FOUND : avion.cambiarEstado(nuevoEstado);
    }
}