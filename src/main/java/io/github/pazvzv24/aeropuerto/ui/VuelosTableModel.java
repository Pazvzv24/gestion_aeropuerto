package io.github.pazvzv24.aeropuerto.ui;

import io.github.pazvzv24.aeropuerto.Aerolinea;
import io.github.pazvzv24.aeropuerto.EstadoPasajero;
import io.github.pazvzv24.aeropuerto.EstadoVuelo;
import io.github.pazvzv24.aeropuerto.Vuelo;
import java.time.LocalDateTime;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 * Adapta los vuelos de la aerolínea a una tabla de Swing.
 * Solo lee datos: los cambios se hacen siempre a través de Aerolinea.
 */
public final class VuelosTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    public static final int COLUMNA_CODIGO = 0;
    public static final int COLUMNA_RUTA = 1;
    public static final int COLUMNA_SALIDA = 2;
    public static final int COLUMNA_AVION = 3;
    public static final int COLUMNA_CHECK_IN = 4;
    public static final int COLUMNA_EMBARCADOS = 5;
    public static final int COLUMNA_ESTADO = 6;

    private static final String[] COLUMNAS = {
        "Vuelo", "Ruta", "Salida", "Avión", "Check-in", "Embarcados", "Estado"
    };

    private final transient Aerolinea aerolinea;
    private transient List<Vuelo> vuelos;

    public VuelosTableModel(Aerolinea aerolinea) {
        this.aerolinea = aerolinea;
        this.vuelos = aerolinea.getVuelos();
    }

    /** Vuelve a leer los vuelos y avisa a la tabla que los datos cambiaron. */
    public void actualizar() {
        vuelos = aerolinea.getVuelos();
        fireTableDataChanged();
    }

    /** @return el vuelo de esa fila (índice del modelo, no de la vista ordenada). */
    public Vuelo getVuelo(int fila) {
        return vuelos.get(fila);
    }

    /** @return la fila del vuelo con ese código, o -1 si no está. */
    public int buscarFila(String codigo) {
        for (int i = 0; i < vuelos.size(); i++) {
            if (vuelos.get(i).getCodigo().equals(codigo)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getRowCount() {
        return vuelos.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNAS.length;
    }

    @Override
    public String getColumnName(int columna) {
        return COLUMNAS[columna];
    }

    @Override
    public Class<?> getColumnClass(int columna) {
        switch (columna) {
            case COLUMNA_SALIDA:
                return LocalDateTime.class;
            case COLUMNA_CHECK_IN:
            case COLUMNA_EMBARCADOS:
                return Integer.class;
            case COLUMNA_ESTADO:
                return EstadoVuelo.class;
            default:
                return String.class;
        }
    }

    @Override
    public Object getValueAt(int fila, int columna) {
        Vuelo vuelo = vuelos.get(fila);
        switch (columna) {
            case COLUMNA_CODIGO:
                return vuelo.getCodigo();
            case COLUMNA_RUTA:
                return vuelo.getRuta();
            case COLUMNA_SALIDA:
                return vuelo.getSalida();
            case COLUMNA_AVION:
                return vuelo.getAvion().getMatricula();
            case COLUMNA_CHECK_IN:
                return vuelo.getCantidadPasajeros();
            case COLUMNA_EMBARCADOS:
                return vuelo.contarPasajeros(EstadoPasajero.EMBARCADO);
            case COLUMNA_ESTADO:
                return vuelo.getEstado();
            default:
                throw new IllegalArgumentException("Columna inexistente: " + columna);
        }
    }
}