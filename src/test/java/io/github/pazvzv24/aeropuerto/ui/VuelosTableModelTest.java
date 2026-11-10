package io.github.pazvzv24.aeropuerto.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.pazvzv24.aeropuerto.Aerolinea;
import io.github.pazvzv24.aeropuerto.Avion;
import io.github.pazvzv24.aeropuerto.Destino;
import io.github.pazvzv24.aeropuerto.EstadoVuelo;
import io.github.pazvzv24.aeropuerto.ModeloAvion;
import io.github.pazvzv24.aeropuerto.TipoDocumento;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class VuelosTableModelTest {

    private static final LocalDateTime SALIDA = LocalDateTime.of(2026, 12, 24, 14, 30);

    @Test
    @DisplayName("La tabla de vuelos muestra código, ruta, avión, pasajeros y estado")
    void tabla_MuestraLosDatosDelVuelo() {
        Aerolinea aerolinea = new Aerolinea("Prueba", new Avion("CC-AAA", ModeloAvion.A320));
        aerolinea.programarVuelo("AM101", Destino.SCL, Destino.LIM, SALIDA, "CC-AAA");
        aerolinea.hacerCheckIn("AM101", "Ana Pérez", TipoDocumento.RUT, "12.345.678-5");
        VuelosTableModel modelo = new VuelosTableModel(aerolinea);

        assertEquals(1, modelo.getRowCount());
        assertEquals(7, modelo.getColumnCount());
        assertEquals("AM101", modelo.getValueAt(0, VuelosTableModel.COLUMNA_CODIGO));
        assertEquals("SCL → LIM", modelo.getValueAt(0, VuelosTableModel.COLUMNA_RUTA));
        assertEquals(SALIDA, modelo.getValueAt(0, VuelosTableModel.COLUMNA_SALIDA));
        assertEquals("CC-AAA", modelo.getValueAt(0, VuelosTableModel.COLUMNA_AVION));
        assertEquals(1, modelo.getValueAt(0, VuelosTableModel.COLUMNA_CHECK_IN));
        assertEquals(0, modelo.getValueAt(0, VuelosTableModel.COLUMNA_EMBARCADOS));
        assertEquals(EstadoVuelo.PROGRAMADO, modelo.getValueAt(0, VuelosTableModel.COLUMNA_ESTADO));
    }

    @Test
    @DisplayName("Después de actualizar, la tabla muestra los vuelos nuevos")
    void actualizar_LeeLosVuelosNuevos() {
        Aerolinea aerolinea = new Aerolinea("Prueba", new Avion("CC-AAA", ModeloAvion.A320));
        VuelosTableModel modelo = new VuelosTableModel(aerolinea);
        aerolinea.programarVuelo("AM101", Destino.SCL, Destino.LIM, SALIDA, "CC-AAA");

        modelo.actualizar();

        assertEquals(1, modelo.getRowCount());
        assertEquals(0, modelo.buscarFila("AM101"));
        assertEquals(-1, modelo.buscarFila("AM999"));
    }
}