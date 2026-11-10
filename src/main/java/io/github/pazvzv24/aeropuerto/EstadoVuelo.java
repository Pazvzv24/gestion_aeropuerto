package io.github.pazvzv24.aeropuerto;

import java.util.List;

/**
 * Estados de un vuelo y los cambios permitidos entre ellos.
 *
 * <pre>
 *   PROGRAMADO ──► EMBARCANDO ──► EN_VUELO ──► ATERRIZADO
 *      │   ▲           │
 *      ▼   │           └──► CANCELADO
 *   RETRASADO ──► CANCELADO
 * </pre>
 */
public enum EstadoVuelo {
    PROGRAMADO("Programado"),
    RETRASADO("Retrasado"),
    EMBARCANDO("Embarcando"),
    EN_VUELO("En vuelo"),
    ATERRIZADO("Aterrizado"),
    CANCELADO("Cancelado");

    private final String nombre;

    EstadoVuelo(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    /** Estados a los que se puede pasar desde este, con la acción principal primero. */
    public List<EstadoVuelo> getSiguientes() {
        switch (this) {
            case PROGRAMADO:
                return List.of(EMBARCANDO, RETRASADO, CANCELADO);
            case RETRASADO:
                return List.of(EMBARCANDO, CANCELADO);
            case EMBARCANDO:
                return List.of(EN_VUELO, CANCELADO);
            case EN_VUELO:
                return List.of(ATERRIZADO);
            default:
                return List.of();
        }
    }

    public boolean puedeCambiarA(EstadoVuelo destino) {
        return destino != null && getSiguientes().contains(destino);
    }

    /** El check-in está abierto antes del despegue (también si el vuelo está retrasado). */
    public boolean permiteCheckIn() {
        return this == PROGRAMADO || this == RETRASADO || this == EMBARCANDO;
    }

    /** Un vuelo terminado o cancelado ya no usa su avión. */
    public boolean esFinal() {
        return this == ATERRIZADO || this == CANCELADO;
    }

    /** Mientras embarca o vuela, el avión lo maneja el vuelo y no se puede operar desde la flota. */
    public boolean controlaElAvion() {
        return this == EMBARCANDO || this == EN_VUELO;
    }

    /** Nombre de la acción que lleva de este estado al destino, para botones y menús. */
    public String accionHacia(EstadoVuelo destino) {
        if (destino == null) {
            return "";
        }
        switch (destino) {
            case EMBARCANDO:
                return "Iniciar embarque";
            case RETRASADO:
                return "Marcar retraso";
            case EN_VUELO:
                return "Despegar";
            case ATERRIZADO:
                return "Aterrizar";
            case CANCELADO:
                return "Cancelar vuelo";
            default:
                return destino.getNombre();
        }
    }

    @Override
    public String toString() {
        return nombre;
    }
}