package io.github.pazvzv24.aeropuerto.ui;

import io.github.pazvzv24.aeropuerto.Aerolinea;
import io.github.pazvzv24.aeropuerto.Avion;
import io.github.pazvzv24.aeropuerto.EstadoAvion;
import io.github.pazvzv24.aeropuerto.ModeloAvion;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 * Adapta la flota de la aerolínea a una tabla de Swing.
 * Solo lee datos: los cambios se hacen siempre a través de Aerolinea.
 */
public final class AvionesTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    public static final int COLUMNA_MATRICULA = 0;
    public static final int COLUMNA_MODELO = 1;
    public static final int COLUMNA_CAPACIDAD = 2;
    public static final int COLUMNA_A_BORDO = 3;
    public static final int COLUMNA_OCUPACION = 4;
    public static final int COLUMNA_ESTADO = 5;

    private static final String[] COLUMNAS = {
        "Matrícula", "Modelo", "Capacidad", "A bordo", "Ocupación", "Estado"
    };

    private final transient Aerolinea aerolinea;
    private transient List<Avion> aviones;

    public AvionesTableModel(Aerolinea aerolinea) {
        this.aerolinea = aerolinea;
        this.aviones = aerolinea.getFlota();
    }

    /** Vuelve a leer la flota y avisa a la tabla que los datos cambiaron. */
    public void actualizar() {
        aviones = aerolinea.getFlota();
        fireTableDataChanged();
    }

    /** @return el avión de esa fila (índice del modelo, no de la vista ordenada). */
    public Avion getAvion(int fila) {
        return aviones.get(fila);
    }

    /** @return la fila del avión con esa matrícula, o -1 si no está. */
    public int buscarFila(String matricula) {
        for (int i = 0; i < aviones.size(); i++) {
            if (aviones.get(i).getMatricula().equals(matricula)) {
                return i;
            }
        }
        return -1;
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
            case COLUMNA_MATRICULA:
                return String.class;
            case COLUMNA_MODELO:
                return ModeloAvion.class;
            case COLUMNA_OCUPACION:
                return Double.class;
            case COLUMNA_ESTADO:
                return EstadoAvion.class;
            default:
                return Integer.class;
        }
    }

    @Override
    public Object getValueAt(int fila, int columna) {
        Avion avion = aviones.get(fila);
        switch (columna) {
            case COLUMNA_MATRICULA:
                return avion.getMatricula();
            case COLUMNA_MODELO:
                return avion.getModelo();
            case COLUMNA_CAPACIDAD:
                return avion.getCantidadMaxima();
            case COLUMNA_A_BORDO:
                return avion.getPasajerosActuales();
            case COLUMNA_OCUPACION:
                return avion.getPorcentajeOcupacion();
            case COLUMNA_ESTADO:
                return avion.getEstado();
            default:
                throw new IllegalArgumentException("Columna inexistente: " + columna);
        }
    }
}