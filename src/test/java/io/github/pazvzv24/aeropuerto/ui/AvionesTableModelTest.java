package io.github.pazvzv24.aeropuerto.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.pazvzv24.aeropuerto.Aerolinea;
import io.github.pazvzv24.aeropuerto.Avion;
import io.github.pazvzv24.aeropuerto.EstadoAvion;
import io.github.pazvzv24.aeropuerto.ModeloAvion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AvionesTableModelTest {

    private Aerolinea crearAerolinea() {
        return new Aerolinea("Prueba",
                new Avion("CC-AAA", ModeloAvion.B787_9, EstadoAvion.EMBARCANDO, 150),
                new Avion("CC-AAB", ModeloAvion.A320, EstadoAvion.EN_MANTENIMIENTO, 0));
    }

    @Test
    @DisplayName("La tabla tiene una fila por avión y las 6 columnas esperadas")
    void tabla_TieneUnaFilaPorAvion() {
        AvionesTableModel modelo = new AvionesTableModel(crearAerolinea());

        assertEquals(2, modelo.getRowCount());
        assertEquals(6, modelo.getColumnCount());
        assertEquals("Matrícula", modelo.getColumnName(AvionesTableModel.COLUMNA_MATRICULA));
    }

    @Test
    @DisplayName("Cada columna muestra el dato correcto del avión")
    void tabla_MuestraLosDatosDelAvion() {
        AvionesTableModel modelo = new AvionesTableModel(crearAerolinea());

        assertEquals("CC-AAA", modelo.getValueAt(0, AvionesTableModel.COLUMNA_MATRICULA));
        assertEquals(ModeloAvion.B787_9, modelo.getValueAt(0, AvionesTableModel.COLUMNA_MODELO));
        assertEquals(300, modelo.getValueAt(0, AvionesTableModel.COLUMNA_CAPACIDAD));
        assertEquals(150, modelo.getValueAt(0, AvionesTableModel.COLUMNA_A_BORDO));
        assertEquals(50.0, modelo.getValueAt(0, AvionesTableModel.COLUMNA_OCUPACION));
        assertEquals(EstadoAvion.EN_MANTENIMIENTO, modelo.getValueAt(1, AvionesTableModel.COLUMNA_ESTADO));
    }

    @Test
    @DisplayName("Al actualizar, la tabla refleja los cambios y los aviones incorporados")
    void actualizar_ReflejaLosCambios() {
        Aerolinea aerolinea = crearAerolinea();
        AvionesTableModel modelo = new AvionesTableModel(aerolinea);

        aerolinea.subirPasajeros("CC-AAA", 30);
        aerolinea.incorporarAvion("CC-AAC", ModeloAvion.A321);
        modelo.actualizar();

        assertEquals(180, modelo.getValueAt(0, AvionesTableModel.COLUMNA_A_BORDO));
        assertEquals(3, modelo.getRowCount());
        assertEquals(2, modelo.buscarFila("CC-AAC"));
    }
}