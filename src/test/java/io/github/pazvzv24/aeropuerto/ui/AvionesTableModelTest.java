package io.github.pazvzv24.aeropuerto.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.pazvzv24.aeropuerto.Aeropuerto;
import io.github.pazvzv24.aeropuerto.Avion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AvionesTableModelTest {

    private Aeropuerto crearAeropuerto() {
        return new Aeropuerto("Prueba",
                new Avion(1, 100, 40, true),
                new Avion(2, 80, 0, false));
    }

    @Test
    @DisplayName("La tabla tiene una fila por avión y las 6 columnas esperadas")
    void tabla_TieneUnaFilaPorAvion() {
        AvionesTableModel modelo = new AvionesTableModel(crearAeropuerto());

        assertEquals(2, modelo.getRowCount());
        assertEquals(6, modelo.getColumnCount());
        assertEquals("Ocupación", modelo.getColumnName(AvionesTableModel.COLUMNA_OCUPACION));
    }

    @Test
    @DisplayName("Cada columna muestra el dato correcto del avión")
    void tabla_MuestraLosDatosDelAvion() {
        AvionesTableModel modelo = new AvionesTableModel(crearAeropuerto());

        assertEquals(1, modelo.getValueAt(0, AvionesTableModel.COLUMNA_ID));
        assertEquals(100, modelo.getValueAt(0, AvionesTableModel.COLUMNA_CAPACIDAD));
        assertEquals(40, modelo.getValueAt(0, AvionesTableModel.COLUMNA_A_BORDO));
        assertEquals(60, modelo.getValueAt(0, AvionesTableModel.COLUMNA_DISPONIBLES));
        assertEquals(40.0, modelo.getValueAt(0, AvionesTableModel.COLUMNA_OCUPACION));
        assertEquals("Activo", modelo.getValueAt(0, AvionesTableModel.COLUMNA_ESTADO));
        assertEquals("Cancelado", modelo.getValueAt(1, AvionesTableModel.COLUMNA_ESTADO));
    }

    @Test
    @DisplayName("Al actualizar, la tabla refleja los cambios hechos en el aeropuerto")
    void actualizar_ReflejaLosCambios() {
        Aeropuerto aeropuerto = crearAeropuerto();
        AvionesTableModel modelo = new AvionesTableModel(aeropuerto);

        aeropuerto.subirPasajeros(1, 10);
        modelo.actualizar();

        assertEquals(50, modelo.getValueAt(0, AvionesTableModel.COLUMNA_A_BORDO));
        assertEquals(50.0, modelo.getValueAt(0, AvionesTableModel.COLUMNA_OCUPACION));
    }
}