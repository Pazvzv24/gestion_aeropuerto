package io.github.pazvzv24.aeropuerto;

import java.util.List;

/**
 * Estados operativos de un avión y los cambios permitidos entre ellos (máquina de estados).
 *
 * <pre>
 *   DISPONIBLE ──► EMBARCANDO ──► EN_VUELO ──► DESEMBARCANDO ──► DISPONIBLE
 *       │  ▲           │
 *       │  └───────────┘ (cancelar embarque, con el avión vacío)
 *       ├──► EN_MANTENIMIENTO ──► DISPONIBLE
 *       │          │
 *       └──────────┴──► FUERA_DE_SERVICIO (estado final)
 * </pre>
 */
public enum EstadoAvion {
    DISPONIBLE("Disponible", "En tierra y listo para operar"),
    EMBARCANDO("Embarcando", "Recibiendo pasajeros en la puerta de embarque"),
    EN_VUELO("En vuelo", "Volando: no se pueden subir ni bajar pasajeros"),
    DESEMBARCANDO("Desembarcando", "Aterrizó y está bajando a los pasajeros"),
    EN_MANTENIMIENTO("En mantenimiento", "En revisión técnica: no puede operar"),
    FUERA_DE_SERVICIO("Fuera de servicio", "Retirado de la flota");

    private final String nombre;
    private final String descripcion;

    EstadoAvion(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Estados a los que se puede pasar desde este, con la acción principal primero
     * (por ejemplo, al embarcar: primero "Despegar" y después "Cancelar embarque").
     */
    public List<EstadoAvion> getSiguientes() {
        switch (this) {
            case DISPONIBLE:
                return List.of(EMBARCANDO, EN_MANTENIMIENTO, FUERA_DE_SERVICIO);
            case EMBARCANDO:
                return List.of(EN_VUELO, DISPONIBLE);
            case EN_VUELO:
                return List.of(DESEMBARCANDO);
            case DESEMBARCANDO:
                return List.of(DISPONIBLE);
            case EN_MANTENIMIENTO:
                return List.of(DISPONIBLE, FUERA_DE_SERVICIO);
            default:
                return List.of();
        }
    }

    public boolean puedeCambiarA(EstadoAvion destino) {
        return destino != null && getSiguientes().contains(destino);
    }

    /** Solo se suben pasajeros durante el embarque. */
    public boolean permiteSubirPasajeros() {
        return this == EMBARCANDO;
    }

    /** Se bajan pasajeros durante el embarque (si alguien desiste) o al desembarcar. */
    public boolean permiteBajarPasajeros() {
        return this == EMBARCANDO || this == DESEMBARCANDO;
    }

    /** En estos estados el avión debe estar sin pasajeros. */
    public boolean requiereAvionVacio() {
        return this == DISPONIBLE || this == EN_MANTENIMIENTO || this == FUERA_DE_SERVICIO;
    }

    /** Un avión fuera de servicio ya no forma parte de la flota activa. */
    public boolean esActivo() {
        return this != FUERA_DE_SERVICIO;
    }

    /**
     * Nombre de la acción que lleva de este estado al destino, para mostrarla en botones y menús.
     * Ejemplo: de EMBARCANDO a EN_VUELO es "Despegar".
     */
    public String accionHacia(EstadoAvion destino) {
        if (destino == null) {
            return "";
        }
        switch (destino) {
            case EMBARCANDO:
                return "Iniciar embarque";
            case EN_VUELO:
                return "Despegar";
            case DESEMBARCANDO:
                return "Aterrizar";
            case EN_MANTENIMIENTO:
                return "Enviar a mantenimiento";
            case FUERA_DE_SERVICIO:
                return "Dar de baja";
            case DISPONIBLE:
                if (this == EMBARCANDO) {
                    return "Cancelar embarque";
                }
                if (this == DESEMBARCANDO) {
                    return "Finalizar desembarque";
                }
                return "Liberar de mantenimiento";
            default:
                return destino.getNombre();
        }
    }

    @Override
    public String toString() {
        return nombre;
    }
}