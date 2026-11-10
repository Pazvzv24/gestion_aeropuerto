package io.github.pazvzv24.aeropuerto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PasajeroTest {

    @Test
    @DisplayName("El pasajero guarda el RUT con formato y lo muestra enmascarado")
    void pasajeroConRut_GuardaFormatoYEnmascara() {
        Pasajero pasajero = new Pasajero("  Camila   Rojas ", TipoDocumento.RUT, "123456785");

        assertEquals("Camila Rojas", pasajero.getNombre());
        assertEquals("12.345.678-5", pasajero.getDocumento());
        assertEquals("12.345.***-5", pasajero.getDocumentoEnmascarado());
    }

    @Test
    @DisplayName("El pasaporte se guarda en mayúsculas y solo se ven sus últimos 3 caracteres")
    void pasajeroConPasaporte_EnmascaraMenosLosUltimos3() {
        Pasajero pasajero = new Pasajero("Lucía Fernández", TipoDocumento.PASAPORTE, "pe4821930");

        assertEquals("PE4821930", pasajero.getDocumento());
        assertEquals("******930", pasajero.getDocumentoEnmascarado());
    }

    @Test
    @DisplayName("No se puede crear un pasajero sin nombre o con documento inválido")
    void constructor_ConDatosInvalidos_LanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new Pasajero(" ", TipoDocumento.RUT, "12.345.678-5"));
        assertThrows(IllegalArgumentException.class, () -> new Pasajero("Ana", TipoDocumento.RUT, "12.345.678-9"));
        assertThrows(IllegalArgumentException.class, () -> new Pasajero("Ana", TipoDocumento.PASAPORTE, "AB-1"));
        assertThrows(IllegalArgumentException.class, () -> new Pasajero("Ana", null, "12.345.678-5"));
    }

    @Test
    @DisplayName("El mismo RUT escrito de otra forma se reconoce como el mismo documento")
    void mismoDocumento_IgnoraElFormato() {
        Pasajero pasajero = new Pasajero("Ana", TipoDocumento.RUT, "12.345.678-5");

        assertTrue(pasajero.mismoDocumento(TipoDocumento.RUT, "123456785"));
        assertFalse(pasajero.mismoDocumento(TipoDocumento.PASAPORTE, "123456785"));
    }

    @Test
    @DisplayName("El texto del pasajero nunca muestra el documento completo")
    void toString_NoMuestraElDocumentoCompleto() {
        Pasajero pasajero = new Pasajero("Ana", TipoDocumento.RUT, "12.345.678-5");

        assertFalse(pasajero.toString().contains("678"));
    }
}