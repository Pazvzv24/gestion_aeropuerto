package io.github.pazvzv24.aeropuerto.ui;

import io.github.pazvzv24.aeropuerto.Aeropuerto;
import io.github.pazvzv24.aeropuerto.Avion;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 * Adapta la lista de aviones del aeropuerto a una tabla de Swing.
 * Solo lee datos: los cambios se hacen siempre a través de Aeropuerto.
 */
public final class AvionesTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    public static final int COLUMNA_ID = 0;
    public static final int COLUMNA_CAPACIDAD = 1;
    public static final int COLUMNA_A_BORDO = 2;
    public static final int COLUMNA_DISPONIBLES = 3;
    public static final int COLUMNA_OCUPACION = 4;
    public static final int COLUMNA_ESTADO = 5;

    private static final String[] COLUMNAS = {
        "ID", "Capacidad", "A bordo", "Disponibles", "Ocupación", "Estado"
    };

    private final transient Aeropuerto aeropuerto;
    private transient List<Avion> aviones;

    public AvionesTableModel(Aeropuerto aeropuerto) {
        this.aeropuerto = aeropuerto;
        this.aviones = aeropuerto.getAviones();
    }

    /** Vuelve a leer los aviones y avisa a la tabla que los datos cambiaron. */
    public void actualizar() {
        aviones = aeropuerto.getAviones();
        fireTableDataChanged();
    }

    /** @return el avión de esa fila de la tabla. */
    public Avion getAvion(int fila) {
        return aviones.get(fila);
    }

    @Override
    public int getRowCount() {
        return aviones.size();
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
            case COLUMNA_OCUPACION:
                return Double.class;
            case COLUMNA_ESTADO:
                return String.class;
            default:
                return Integer.class;
        }
    }

    @Override
    public Object getValueAt(int fila, int columna) {
        Avion avion = aviones.get(fila);
        switch (columna) {
            case COLUMNA_ID:
                return avion.getId();
            case COLUMNA_CAPACIDAD:
                return avion.getCantidadMaxima();
            case COLUMNA_A_BORDO:
                return avion.getPasajerosActuales();
            case COLUMNA_DISPONIBLES:
                return avion.getAsientosDisponibles();
            case COLUMNA_OCUPACION:
                return avion.getPorcentajeOcupacion();
            case COLUMNA_ESTADO:
                return avion.getEstado() ? "Activo" : "Cancelado";
            default:
                throw new IllegalArgumentException("Columna inexistente: " + columna);
        }
    }
}