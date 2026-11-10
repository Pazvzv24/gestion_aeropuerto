package io.github.pazvzv24.aeropuerto.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.pazvzv24.aeropuerto.Aerolinea;
import io.github.pazvzv24.aeropuerto.Avion;
import io.github.pazvzv24.aeropuerto.Destino;
import io.github.pazvzv24.aeropuerto.EstadoPasajero;
import io.github.pazvzv24.aeropuerto.ModeloAvion;
import io.github.pazvzv24.aeropuerto.TipoDocumento;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PasajerosTableModelTest {

    @Test
    @DisplayName("La lista de pasajeros muestra el documento enmascarado, nunca completo")
    void tabla_MuestraDocumentoEnmascarado() {
        Aerolinea aerolinea = new Aerolinea("Prueba", new Avion("CC-AAA", ModeloAvion.A320));
        aerolinea.programarVuelo("AM101", Destino.SCL, Destino.LIM, LocalDateTime.of(2026, 12, 24, 14, 30), "CC-AAA");
        aerolinea.hacerCheckIn("AM101", "Ana Pérez", TipoDocumento.RUT, "12.345.678-5");
        aerolinea.hacerCheckIn("AM101", "John Smith", TipoDocumento.PASAPORTE, "XK1234567");
        PasajerosTableModel modelo = new PasajerosTableModel();

        modelo.setVuelo(aerolinea.buscarVuelo("AM101"));

        assertEquals(2, modelo.getRowCount());
        assertEquals("Ana Pérez", modelo.getValueAt(0, PasajerosTableModel.COLUMNA_NOMBRE));
        assertEquals("RUT 12.345.***-5", modelo.getValueAt(0, PasajerosTableModel.COLUMNA_DOCUMENTO));
        assertEquals("Pasaporte ******567", modelo.getValueAt(1, PasajerosTableModel.COLUMNA_DOCUMENTO));
        assertEquals(EstadoPasajero.CHECK_IN, modelo.getValueAt(1, PasajerosTableModel.COLUMNA_ESTADO));
        assertEquals("1A", modelo.getValueAt(0, PasajerosTableModel.COLUMNA_ASIENTO));
        assertEquals("1B", modelo.getValueAt(1, PasajerosTableModel.COLUMNA_ASIENTO));
    }

    @Test
    @DisplayName("Sin vuelo seleccionado la lista queda vacía")
    void sinVuelo_ListaVacia() {
        PasajerosTableModel modelo = new PasajerosTableModel();

        modelo.setVuelo(null);

        assertEquals(0, modelo.getRowCount());
    }
}