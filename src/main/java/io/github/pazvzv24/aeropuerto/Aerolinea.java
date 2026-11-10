package io.github.pazvzv24.aeropuerto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aerolínea, su flota de aviones y sus vuelos.
 * La flota activa admite como máximo 100 aviones. Los aviones dados de baja
 * (fuera de servicio) se conservan en el registro como historial, pero ya no
 * cuentan como flota activa.
 * La lista es privada: desde fuera solo se puede consultar una copia de solo lectura,
 * así nadie puede agregar o quitar aviones sin pasar por las reglas de esta clase.
 * Un avión que está embarcando o volando con un vuelo asignado se maneja desde ese vuelo,
 * no desde la flota, para que el manifiesto y los pasajeros a bordo siempre coincidan.
 */
public final class Aerolinea {

    /** Cantidad máxima de aviones en la flota activa. */
    public static final int CAPACIDAD_FLOTA = 100;

    private final String nombre;
    private final List<Avion> registro = new ArrayList<>();
    private final List<Vuelo> vuelos = new ArrayList<>();
    private Runnable alCambiar = () -> { };

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
     * Altamar Airlines (aerolínea ficticia): flota inicial de 15 aviones y 5 vuelos de hoy.
     * Las matrículas, los pasajeros y sus documentos son inventados;
     * cualquier coincidencia con un avión o una persona real es casual.
     */
    public static Aerolinea altamarAirlines() {
        Aerolinea altamar = new Aerolinea("Altamar Airlines",
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
        altamar.cargarVuelosDeDemostracion(LocalDate.now());
        return altamar;
    }

    /** Vuelos y pasajeros ficticios para ver la aplicación funcionando desde el primer momento. */
    private void cargarVuelosDeDemostracion(LocalDate hoy) {
        programarVuelo("AM101", Destino.SCL, Destino.LIM, LocalDateTime.of(hoy, LocalTime.of(14, 30)), "CC-AMA");
        programarVuelo("AM205", Destino.SCL, Destino.PMC, LocalDateTime.of(hoy, LocalTime.of(12, 15)), "CC-AMD");
        programarVuelo("AM330", Destino.SCL, Destino.EZE, LocalDateTime.of(hoy, LocalTime.of(16, 45)), "CC-AMH");
        programarVuelo("AM412", Destino.SCL, Destino.GRU, LocalDateTime.of(hoy, LocalTime.of(21, 10)), "CC-AMK");
        programarVuelo("AM518", Destino.SCL, Destino.ANF, LocalDateTime.of(hoy, LocalTime.of(9, 0)), "CC-AMN");

        hacerCheckIn("AM101", "Camila Rojas Fuentes", TipoDocumento.RUT, "11.111.111-1");
        hacerCheckIn("AM101", "Matías Soto Pizarro", TipoDocumento.RUT, "22.222.222-2");
        hacerCheckIn("AM101", "Lucía Fernández Ortiz", TipoDocumento.PASAPORTE, "PE4821930");
        hacerCheckIn("AM101", "Diego Muñoz Castillo", TipoDocumento.RUT, "33.333.333-3");

        hacerCheckIn("AM205", "Valentina Araya Díaz", TipoDocumento.RUT, "44.444.444-4");
        hacerCheckIn("AM205", "Benjamín Torres Vega", TipoDocumento.RUT, "55.555.555-5");
        hacerCheckIn("AM205", "Isidora Contreras Lagos", TipoDocumento.RUT, "66.666.666-6");
        hacerCheckIn("AM205", "Tomás Herrera Molina", TipoDocumento.RUT, "77.777.777-7");
        cambiarEstadoVuelo("AM205", EstadoVuelo.EMBARCANDO);
        embarcarPasajero("AM205", TipoDocumento.RUT, "44.444.444-4");
        embarcarPasajero("AM205", TipoDocumento.RUT, "55.555.555-5");

        hacerCheckIn("AM330", "Martina Gómez Silva", TipoDocumento.PASAPORTE, "AR7395102");
        hacerCheckIn("AM330", "Joaquín Reyes Navarro", TipoDocumento.RUT, "88.888.888-8");
        cambiarEstadoVuelo("AM330", EstadoVuelo.RETRASADO);

        hacerCheckIn("AM412", "Sofía Ramírez Campos", TipoDocumento.RUT, "99.999.999-9");
        hacerCheckIn("AM412", "Gabriel Alves Costa", TipoDocumento.PASAPORTE, "BR2648017");

        cambiarEstadoVuelo("AM518", EstadoVuelo.CANCELADO);
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

    /**
     * Reconstruye una aerolínea con datos guardados (por ejemplo, desde PostgreSQL).
     * Revisa que los datos sean coherentes: si alguien modificó la base de datos a mano
     * y dejó algo imposible, se rechaza en vez de cargar un estado inválido.
     *
     * @throws IllegalArgumentException si los datos no son coherentes
     */
    public static Aerolinea desdeDatos(String nombre, List<Avion> aviones, List<Vuelo> vuelosGuardados) {
        Aerolinea aerolinea = new Aerolinea(nombre, aviones.toArray(new Avion[0]));
        for (Vuelo vuelo : vuelosGuardados) {
            if (vuelo == null || aerolinea.buscarVuelo(vuelo.getCodigo()) != null) {
                throw new IllegalArgumentException("Vuelo nulo o con código repetido.");
            }
            if (aerolinea.buscarAvion(vuelo.getAvion().getMatricula()) != vuelo.getAvion()) {
                throw new IllegalArgumentException("El vuelo " + vuelo.getCodigo() + " usa un avión que no es de la flota.");
            }
            if (!vuelo.getEstado().esFinal() && aerolinea.buscarVueloPendiente(vuelo.getAvion()) != null) {
                throw new IllegalArgumentException("El avión " + vuelo.getAvion().getMatricula()
                        + " tiene más de un vuelo pendiente.");
            }
            aerolinea.vuelos.add(vuelo);
        }
        return aerolinea;
    }

    /**
     * Acción que se ejecuta después de cada operación exitosa que cambia datos
     * (por ejemplo, guardar en la base de datos). Las operaciones fallidas no la ejecutan.
     */
    public void setAlCambiar(Runnable accion) {
        this.alCambiar = accion == null ? () -> { } : accion;
    }

    private OperationResult notificar(OperationResult resultado) {
        if (resultado.isSuccess()) {
            alCambiar.run();
        }
        return resultado;
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
        return notificar(incorporarAvionSinGuardar(matricula, modelo));
    }

    private OperationResult incorporarAvionSinGuardar(String matricula, ModeloAvion modelo) {
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

    /**
     * Sube pasajeros al avión indicado (sin manifiesto).
     * Si no existe devuelve AIRPLANE_NOT_FOUND; si lo maneja un vuelo, AIRPLANE_ASSIGNED_TO_FLIGHT.
     */
    public OperationResult subirPasajeros(String matricula, int cantidad) {
        return notificar(subirPasajerosSinGuardar(matricula, cantidad));
    }

    private OperationResult subirPasajerosSinGuardar(String matricula, int cantidad) {
        Avion avion = buscarAvion(matricula);
        if (avion == null) {
            return OperationResult.AIRPLANE_NOT_FOUND;
        }
        return estaControladoPorVuelo(avion) ? OperationResult.AIRPLANE_ASSIGNED_TO_FLIGHT : avion.subirPasajeros(cantidad);
    }

    /** Baja pasajeros del avión indicado, con las mismas reglas que {@link #subirPasajeros}. */
    public OperationResult bajarPasajeros(String matricula, int cantidad) {
        return notificar(bajarPasajerosSinGuardar(matricula, cantidad));
    }

    private OperationResult bajarPasajerosSinGuardar(String matricula, int cantidad) {
        Avion avion = buscarAvion(matricula);
        if (avion == null) {
            return OperationResult.AIRPLANE_NOT_FOUND;
        }
        return estaControladoPorVuelo(avion) ? OperationResult.AIRPLANE_ASSIGNED_TO_FLIGHT : avion.bajarPasajeros(cantidad);
    }

    /**
     * Cambia el estado del avión indicado, con las mismas reglas que {@link #subirPasajeros}.
     * Además, si el avión tiene un vuelo programado, el embarque se inicia desde ese vuelo
     * (así los pasajeros quedan en su manifiesto).
     */
    public OperationResult cambiarEstado(String matricula, EstadoAvion nuevoEstado) {
        return notificar(cambiarEstadoSinGuardar(matricula, nuevoEstado));
    }

    private OperationResult cambiarEstadoSinGuardar(String matricula, EstadoAvion nuevoEstado) {
        Avion avion = buscarAvion(matricula);
        if (avion == null) {
            return OperationResult.AIRPLANE_NOT_FOUND;
        }
        if (estaControladoPorVuelo(avion)
                || (nuevoEstado == EstadoAvion.EMBARCANDO && buscarVueloPendiente(avion) != null)) {
            return OperationResult.AIRPLANE_ASSIGNED_TO_FLIGHT;
        }
        return avion.cambiarEstado(nuevoEstado);
    }

    // ---------------------------------------------------------------- Vuelos

    /** Copia de solo lectura de todos los vuelos, incluidos los terminados y cancelados. */
    public List<Vuelo> getVuelos() {
        return Collections.unmodifiableList(new ArrayList<>(vuelos));
    }

    public int contarVuelosPorEstado(EstadoVuelo estado) {
        int total = 0;
        for (Vuelo vuelo : vuelos) {
            if (vuelo.getEstado() == estado) {
                total++;
            }
        }
        return total;
    }

    /** @return el vuelo con ese código (sin importar mayúsculas), o null si no existe. */
    public Vuelo buscarVuelo(String codigo) {
        String buscado = Vuelo.normalizarCodigo(codigo);
        for (Vuelo vuelo : vuelos) {
            if (vuelo.getCodigo().equals(buscado)) {
                return vuelo;
            }
        }
        return null;
    }

    /** @return el vuelo aún no terminado ni cancelado que tiene asignado el avión, o null si no tiene. */
    public Vuelo buscarVueloPendiente(Avion avion) {
        for (Vuelo vuelo : vuelos) {
            if (vuelo.getAvion() == avion && !vuelo.getEstado().esFinal()) {
                return vuelo;
            }
        }
        return null;
    }

    /** true si el avión está embarcando o volando con un vuelo asignado. */
    public boolean estaControladoPorVuelo(Avion avion) {
        Vuelo vuelo = buscarVueloPendiente(avion);
        return vuelo != null && vuelo.getEstado().controlaElAvion();
    }

    /**
     * Programa un vuelo nuevo. El avión debe existir, estar activo y no tener
     * otro vuelo pendiente (cada avión opera un vuelo a la vez).
     */
    public OperationResult programarVuelo(String codigo, Destino origen, Destino destino,
                                          LocalDateTime salida, String matricula) {
        return notificar(programarVueloSinGuardar(codigo, origen, destino, salida, matricula));
    }

    private OperationResult programarVueloSinGuardar(String codigo, Destino origen, Destino destino,
                                                     LocalDateTime salida, String matricula) {
        if (!Vuelo.esCodigoValido(codigo) || origen == null || destino == null
                || origen == destino || salida == null) {
            return OperationResult.INVALID_FLIGHT_DATA;
        }
        if (buscarVuelo(codigo) != null) {
            return OperationResult.DUPLICATE_FLIGHT;
        }
        Avion avion = buscarAvion(matricula);
        if (avion == null) {
            return OperationResult.AIRPLANE_NOT_FOUND;
        }
        if (!avion.getEstado().esActivo()) {
            return OperationResult.AIRPLANE_NOT_AVAILABLE;
        }
        if (buscarVueloPendiente(avion) != null) {
            return OperationResult.AIRPLANE_ASSIGNED_TO_FLIGHT;
        }
        vuelos.add(new Vuelo(codigo, origen, destino, salida, avion));
        return OperationResult.SUCCESS;
    }

    /** Registra un pasajero en el vuelo; valida el nombre y el documento (RUT con módulo 11). */
    public OperationResult hacerCheckIn(String codigoVuelo, String nombre, TipoDocumento tipo, String documento) {
        return notificar(hacerCheckInSinGuardar(codigoVuelo, nombre, tipo, documento));
    }

    private OperationResult hacerCheckInSinGuardar(String codigoVuelo, String nombre, TipoDocumento tipo, String documento) {
        Vuelo vuelo = buscarVuelo(codigoVuelo);
        if (vuelo == null) {
            return OperationResult.FLIGHT_NOT_FOUND;
        }
        if (!Pasajero.esNombreValido(nombre) || !Pasajero.esDocumentoValido(tipo, documento)) {
            return OperationResult.INVALID_PASSENGER_DATA;
        }
        return vuelo.hacerCheckIn(new Pasajero(nombre, tipo, documento));
    }

    public OperationResult anularCheckIn(String codigoVuelo, TipoDocumento tipo, String documento) {
        return notificar(anularCheckInSinGuardar(codigoVuelo, tipo, documento));
    }

    private OperationResult anularCheckInSinGuardar(String codigoVuelo, TipoDocumento tipo, String documento) {
        Vuelo vuelo = buscarVuelo(codigoVuelo);
        return vuelo == null ? OperationResult.FLIGHT_NOT_FOUND : vuelo.anularCheckIn(tipo, documento);
    }

    public OperationResult embarcarPasajero(String codigoVuelo, TipoDocumento tipo, String documento) {
        return notificar(embarcarPasajeroSinGuardar(codigoVuelo, tipo, documento));
    }

    private OperationResult embarcarPasajeroSinGuardar(String codigoVuelo, TipoDocumento tipo, String documento) {
        Vuelo vuelo = buscarVuelo(codigoVuelo);
        return vuelo == null ? OperationResult.FLIGHT_NOT_FOUND : vuelo.embarcarPasajero(tipo, documento);
    }

    public OperationResult cambiarEstadoVuelo(String codigoVuelo, EstadoVuelo nuevoEstado) {
        return notificar(cambiarEstadoVueloSinGuardar(codigoVuelo, nuevoEstado));
    }

    private OperationResult cambiarEstadoVueloSinGuardar(String codigoVuelo, EstadoVuelo nuevoEstado) {
        Vuelo vuelo = buscarVuelo(codigoVuelo);
        return vuelo == null ? OperationResult.FLIGHT_NOT_FOUND : vuelo.cambiarEstado(nuevoEstado);
    }
}