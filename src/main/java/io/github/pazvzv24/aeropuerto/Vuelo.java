package io.github.pazvzv24.aeropuerto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Vuelo de la aerolínea: ruta, hora de salida, avión asignado, estado y lista de pasajeros (manifiesto).
 * Mientras embarca y vuela, el vuelo maneja el estado y los pasajeros del avión:
 * iniciar el embarque pone el avión en embarque, cada pasajero que embarca ocupa un asiento,
 * despegar pone el avión en vuelo y aterrizar baja a todos los pasajeros y deja el avión disponible.
 */
public final class Vuelo {

    /** Código de vuelo de Altamar: "AM" seguido de 3 o 4 dígitos (por ejemplo, AM101). */
    private static final Pattern FORMATO_CODIGO = Pattern.compile("AM\\d{3,4}");

    private final String codigo;
    private final Destino origen;
    private final Destino destino;
    private final LocalDateTime salida;
    private final Avion avion;
    private EstadoVuelo estado = EstadoVuelo.PROGRAMADO;
    private final List<CheckIn> manifiesto = new ArrayList<>();

    /**
     * Crea un vuelo programado.
     *
     * @throws IllegalArgumentException si el código, la ruta, la hora o el avión no son válidos
     */
    public Vuelo(String codigo, Destino origen, Destino destino, LocalDateTime salida, Avion avion) {
        if (!esCodigoValido(codigo)) {
            throw new IllegalArgumentException("Código de vuelo inválido: use el formato AM123.");
        }
        if (origen == null || destino == null || origen == destino) {
            throw new IllegalArgumentException("El origen y el destino son obligatorios y deben ser distintos.");
        }
        if (salida == null) {
            throw new IllegalArgumentException("La hora de salida es obligatoria.");
        }
        if (avion == null) {
            throw new IllegalArgumentException("El avión es obligatorio.");
        }
        this.codigo = normalizarCodigo(codigo);
        this.origen = origen;
        this.destino = destino;
        this.salida = salida;
        this.avion = avion;
    }

    /**
     * Reconstruye un vuelo guardado, con su estado y su lista de pasajeros.
     * Revisa que todo sea coherente con el estado del vuelo y del avión.
     *
     * @throws IllegalArgumentException si los datos no son coherentes
     */
    public static Vuelo restaurar(String codigo, Destino origen, Destino destino, LocalDateTime salida,
                                  Avion avion, EstadoVuelo estado, List<CheckIn> pasajeros) {
        if (estado == null || pasajeros == null) {
            throw new IllegalArgumentException("El estado y la lista de pasajeros son obligatorios.");
        }
        Vuelo vuelo = new Vuelo(codigo, origen, destino, salida, avion);
        vuelo.estado = estado;
        for (CheckIn registro : pasajeros) {
            if (registro == null) {
                throw new IllegalArgumentException("La lista de pasajeros no puede contener valores nulos.");
            }
            Pasajero pasajero = registro.getPasajero();
            if (vuelo.buscarPasajero(pasajero.getTipoDocumento(), pasajero.getDocumento()) != null) {
                throw new IllegalArgumentException("Documento repetido en el vuelo " + vuelo.codigo + ".");
            }
            if (!estadoPasajeroPosible(estado, registro.getEstado())) {
                throw new IllegalArgumentException("Pasajero en estado " + registro.getEstado()
                        + " imposible para un vuelo " + estado + ".");
            }
            vuelo.manifiesto.add(new CheckIn(pasajero, registro.getEstado()));
        }
        if (vuelo.manifiesto.size() > avion.getCantidadMaxima()) {
            throw new IllegalArgumentException("El vuelo " + vuelo.codigo + " tiene más pasajeros que asientos.");
        }
        if (estado.controlaElAvion()) {
            EstadoAvion esperado = estado == EstadoVuelo.EMBARCANDO ? EstadoAvion.EMBARCANDO : EstadoAvion.EN_VUELO;
            if (avion.getEstado() != esperado
                    || avion.getPasajerosActuales() != vuelo.contarPasajeros(EstadoPasajero.EMBARCADO)) {
                throw new IllegalArgumentException("El avión del vuelo " + vuelo.codigo + " no coincide con su estado.");
            }
        }
        return vuelo;
    }

    /** Antes de embarcar solo hay check-in; al embarcar, embarcados; después del despegue, nadie queda en check-in. */
    private static boolean estadoPasajeroPosible(EstadoVuelo vuelo, EstadoPasajero pasajero) {
        switch (vuelo) {
            case EMBARCANDO:
                return pasajero != EstadoPasajero.NO_SE_PRESENTO;
            case EN_VUELO:
            case ATERRIZADO:
                return pasajero != EstadoPasajero.CHECK_IN;
            default:
                return pasajero == EstadoPasajero.CHECK_IN;
        }
    }

    public static boolean esCodigoValido(String codigo) {
        return codigo != null && FORMATO_CODIGO.matcher(normalizarCodigo(codigo)).matches();
    }

    public static String normalizarCodigo(String codigo) {
        return codigo == null ? "" : codigo.trim().toUpperCase(Locale.ROOT);
    }

    public String getCodigo() {
        return codigo;
    }

    public Destino getOrigen() {
        return origen;
    }

    public Destino getDestino() {
        return destino;
    }

    /** Ejemplo: "SCL → LIM". */
    public String getRuta() {
        return origen.getCodigo() + " → " + destino.getCodigo();
    }

    public LocalDateTime getSalida() {
        return salida;
    }

    public Avion getAvion() {
        return avion;
    }

    public EstadoVuelo getEstado() {
        return estado;
    }

    /** Copia de solo lectura de la lista de pasajeros. */
    public List<CheckIn> getManifiesto() {
        return Collections.unmodifiableList(new ArrayList<>(manifiesto));
    }

    public int getCantidadPasajeros() {
        return manifiesto.size();
    }

    public int contarPasajeros(EstadoPasajero estadoPasajero) {
        int total = 0;
        for (CheckIn registro : manifiesto) {
            if (registro.getEstado() == estadoPasajero) {
                total++;
            }
        }
        return total;
    }

    /** @return el registro del pasajero con ese documento, o null si no está en el vuelo. */
    public CheckIn buscarPasajero(TipoDocumento tipo, String documento) {
        for (CheckIn registro : manifiesto) {
            if (registro.getPasajero().mismoDocumento(tipo, documento)) {
                return registro;
            }
        }
        return null;
    }

    /**
     * Registra al pasajero en el vuelo. Se permite antes del despegue,
     * sin documentos repetidos y sin superar la capacidad del avión.
     */
    public OperationResult hacerCheckIn(Pasajero pasajero) {
        if (pasajero == null) {
            return OperationResult.INVALID_PASSENGER_DATA;
        }
        if (!estado.permiteCheckIn()) {
            return OperationResult.CHECK_IN_CLOSED;
        }
        if (buscarPasajero(pasajero.getTipoDocumento(), pasajero.getDocumento()) != null) {
            return OperationResult.DUPLICATE_PASSENGER;
        }
        if (manifiesto.size() >= avion.getCantidadMaxima()) {
            return OperationResult.CAPACITY_EXCEEDED;
        }
        manifiesto.add(new CheckIn(pasajero));
        return OperationResult.SUCCESS;
    }

    /** Quita del vuelo a un pasajero que aún no ha embarcado. */
    public OperationResult anularCheckIn(TipoDocumento tipo, String documento) {
        CheckIn registro = buscarPasajero(tipo, documento);
        if (registro == null) {
            return OperationResult.PASSENGER_NOT_FOUND;
        }
        if (!estado.permiteCheckIn()) {
            return OperationResult.CHECK_IN_CLOSED;
        }
        if (registro.getEstado() == EstadoPasajero.EMBARCADO) {
            return OperationResult.ALREADY_BOARDED;
        }
        manifiesto.remove(registro);
        return OperationResult.SUCCESS;
    }

    /** Embarca a un pasajero con check-in: ocupa un asiento en el avión. */
    public OperationResult embarcarPasajero(TipoDocumento tipo, String documento) {
        if (estado != EstadoVuelo.EMBARCANDO) {
            return OperationResult.OPERATION_NOT_ALLOWED;
        }
        CheckIn registro = buscarPasajero(tipo, documento);
        if (registro == null) {
            return OperationResult.PASSENGER_NOT_FOUND;
        }
        if (registro.getEstado() == EstadoPasajero.EMBARCADO) {
            return OperationResult.ALREADY_BOARDED;
        }
        OperationResult resultado = avion.subirPasajeros(1);
        if (resultado.isSuccess()) {
            registro.cambiarEstado(EstadoPasajero.EMBARCADO);
        }
        return resultado;
    }

    /**
     * Cambia el estado del vuelo y, cuando corresponde, el del avión:
     * <ul>
     *   <li>Iniciar embarque: el avión debe estar disponible y pasa a embarque.</li>
     *   <li>Despegar: el avión pasa a vuelo; quienes no embarcaron quedan como "No se presentó".</li>
     *   <li>Aterrizar: el avión baja a todos los pasajeros y queda disponible.</li>
     *   <li>Cancelar durante el embarque: los embarcados bajan y el avión queda disponible.</li>
     * </ul>
     * Si algo no se cumple, ni el vuelo ni el avión cambian.
     */
    public OperationResult cambiarEstado(EstadoVuelo nuevoEstado) {
        if (!estado.puedeCambiarA(nuevoEstado)) {
            return OperationResult.INVALID_TRANSITION;
        }
        int embarcados = contarPasajeros(EstadoPasajero.EMBARCADO);

        switch (nuevoEstado) {
            case EMBARCANDO:
                if (avion.getEstado() != EstadoAvion.DISPONIBLE || avion.getPasajerosActuales() > 0) {
                    return OperationResult.AIRPLANE_NOT_AVAILABLE;
                }
                avion.cambiarEstado(EstadoAvion.EMBARCANDO);
                break;
            case EN_VUELO:
                if (avion.getEstado() != EstadoAvion.EMBARCANDO) {
                    return OperationResult.AIRPLANE_NOT_AVAILABLE;
                }
                avion.cambiarEstado(EstadoAvion.EN_VUELO);
                for (CheckIn registro : manifiesto) {
                    if (registro.getEstado() == EstadoPasajero.CHECK_IN) {
                        registro.cambiarEstado(EstadoPasajero.NO_SE_PRESENTO);
                    }
                }
                break;
            case ATERRIZADO:
                if (avion.getEstado() != EstadoAvion.EN_VUELO || avion.getPasajerosActuales() != embarcados) {
                    return OperationResult.AIRPLANE_NOT_AVAILABLE;
                }
                avion.cambiarEstado(EstadoAvion.DESEMBARCANDO);
                if (embarcados > 0) {
                    avion.bajarPasajeros(embarcados);
                }
                avion.cambiarEstado(EstadoAvion.DISPONIBLE);
                break;
            case CANCELADO:
                if (estado == EstadoVuelo.EMBARCANDO) {
                    if (avion.getEstado() != EstadoAvion.EMBARCANDO || avion.getPasajerosActuales() != embarcados) {
                        return OperationResult.AIRPLANE_NOT_AVAILABLE;
                    }
                    if (embarcados > 0) {
                        avion.bajarPasajeros(embarcados);
                    }
                    avion.cambiarEstado(EstadoAvion.DISPONIBLE);
                    for (CheckIn registro : manifiesto) {
                        if (registro.getEstado() == EstadoPasajero.EMBARCADO) {
                            registro.cambiarEstado(EstadoPasajero.CHECK_IN);
                        }
                    }
                }
                break;
            default:
                break;
        }
        estado = nuevoEstado;
        return OperationResult.SUCCESS;
    }

    /** Ejemplo: "AM101 · SCL → LIM". */
    @Override
    public String toString() {
        return codigo + " · " + getRuta();
    }
}