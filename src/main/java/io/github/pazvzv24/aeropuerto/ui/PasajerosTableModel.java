package io.github.pazvzv24.aeropuerto.ui;

import io.github.pazvzv24.aeropuerto.CheckIn;
import io.github.pazvzv24.aeropuerto.EstadoPasajero;
import io.github.pazvzv24.aeropuerto.Pasajero;
import io.github.pazvzv24.aeropuerto.Vuelo;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 * Lista de pasajeros (manifiesto) del vuelo seleccionado.
 * El documento se muestra siempre enmascarado: la tabla nunca recibe el número completo.
 */
public final class PasajerosTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    public static final int COLUMNA_NOMBRE = 0;
    public static final int COLUMNA_DOCUMENTO = 1;
    public static final int COLUMNA_ASIENTO = 2;
    public static final int COLUMNA_ESTADO = 3;

    private static final String[] COLUMNAS = {"Pasajero", "Documento", "Asiento", "Estado"};

    private transient Vuelo vuelo;
    private transient List<CheckIn> registros = List.of();

    /** Muestra los pasajeros de otro vuelo (o ninguno si es null). */
    public void setVuelo(Vuelo vuelo) {
        this.vuelo = vuelo;
        actualizar();
    }

    public Vuelo getVuelo() {
        return vuelo;
    }

    /** Vuelve a leer el manifiesto del vuelo y avisa a la tabla. */
    public void actualizar() {
        registros = vuelo == null ? List.of() : vuelo.getManifiesto();
        fireTableDataChanged();
    }

    /** @return el registro de esa fila (índice del modelo, no de la vista ordenada). */
    public CheckIn getCheckIn(int fila) {
        return registros.get(fila);
    }

    @Override
    public int getRowCount() {
        return registros.size();
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
        return columna == COLUMNA_ESTADO ? EstadoPasajero.class : String.class;
    }

    @Override
    public Object getValueAt(int fila, int columna) {
        CheckIn registro = registros.get(fila);
        Pasajero pasajero = registro.getPasajero();
        switch (columna) {
            case COLUMNA_NOMBRE:
                return pasajero.getNombre();
            case COLUMNA_DOCUMENTO:
                return pasajero.getTipoDocumento() + " " + pasajero.getDocumentoEnmascarado();
            case COLUMNA_ASIENTO:
                return registro.getAsiento();
            case COLUMNA_ESTADO:
                return registro.getEstado();
            default:
                throw new IllegalArgumentException("Columna inexistente: " + columna);
        }
    }
}