package io.github.pazvzv24.aeropuerto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Asientos y pase de abordar. */
class VueloAsientosTest {

    private static final LocalDateTime SALIDA = LocalDateTime.of(2026, 12, 24, 14, 30);

    private final Aerolinea aerolinea = crearAerolinea();

    private static Aerolinea crearAerolinea() {
        Aerolinea aerolinea = new Aerolinea("Altamar Airlines", new Avion("CC-AAA", ModeloAvion.A320));
        aerolinea.programarVuelo("AM101", Destino.SCL, Destino.LIM, SALIDA, "CC-AAA");
        return aerolinea;
    }

    private Vuelo vuelo() {
        return aerolinea.buscarVuelo("AM101");
    }

    @Test
    @DisplayName("Sin elegir asiento, el check-in asigna el primero libre")
    void hacerCheckIn_SinAsiento_AsignaElPrimeroLibre() {
        aerolinea.hacerCheckIn("AM101", "Ana Pérez", TipoDocumento.RUT, "11.111.111-1");
        aerolinea.hacerCheckIn("AM101", "Luis Soto", TipoDocumento.RUT, "22.222.222-2");

        assertEquals("1A", vuelo().buscarPasajero(TipoDocumento.RUT, "11.111.111-1").getAsiento());
        assertEquals("1B", vuelo().buscarPasajero(TipoDocumento.RUT, "22.222.222-2").getAsiento());
    }

    @Test
    @DisplayName("No se puede elegir un asiento ocupado ni uno que no existe en el avión")
    void hacerCheckIn_AsientoOcupadoOInvalido() {
        assertEquals(OperationResult.SUCCESS,
                aerolinea.hacerCheckIn("AM101", "Ana Pérez", TipoDocumento.RUT, "11.111.111-1", "12c"));

        assertEquals(OperationResult.SEAT_TAKEN,
                aerolinea.hacerCheckIn("AM101", "Luis Soto", TipoDocumento.RUT, "22.222.222-2", "12C"));
        assertEquals(OperationResult.INVALID_SEAT,
                aerolinea.hacerCheckIn("AM101", "Luis Soto", TipoDocumento.RUT, "22.222.222-2", "45A"));
        assertEquals(1, vuelo().getCantidadPasajeros());
        assertTrue(vuelo().estaOcupado("12C"));
    }

    @Test
    @DisplayName("Se puede cambiar de asiento antes de embarcar, pero no después")
    void cambiarAsiento_AntesYDespuesDeEmbarcar() {
        aerolinea.hacerCheckIn("AM101", "Ana Pérez", TipoDocumento.RUT, "11.111.111-1", "12C");

        assertEquals(OperationResult.SUCCESS, aerolinea.cambiarAsiento("AM101", TipoDocumento.RUT, "11.111.111-1", "3A"));
        assertFalse(vuelo().estaOcupado("12C"));

        aerolinea.cambiarEstadoVuelo("AM101", EstadoVuelo.EMBARCANDO);
        aerolinea.embarcarPasajero("AM101", TipoDocumento.RUT, "11.111.111-1");
        assertEquals(OperationResult.ALREADY_BOARDED,
                aerolinea.cambiarAsiento("AM101", TipoDocumento.RUT, "11.111.111-1", "4A"));
    }

    @Test
    @DisplayName("El pase de abordar muestra el asiento, la hora de embarque y el documento enmascarado")
    void emitirPaseAbordar_ConDatosDelVuelo() {
        aerolinea.hacerCheckIn("AM101", "Ana Pérez", TipoDocumento.RUT, "12.345.678-5", "22A");

        PaseAbordar pase = aerolinea.emitirPaseAbordar("AM101", TipoDocumento.RUT, "123456785");

        assertNotNull(pase);
        assertEquals("22A", pase.getAsiento());
        assertEquals("13:50", pase.getHoraEmbarqueTexto());
        assertEquals("RUT 12.345.***-5", pase.getDocumento());
        assertEquals(1, pase.getGrupoEmbarque());
        assertEquals("001", pase.getSecuencia());
        assertFalse(pase.aTexto().contains("678"));
    }

    @Test
    @DisplayName("No hay pase para un vuelo cancelado ni para quien no está en el vuelo")
    void emitirPaseAbordar_NoDisponible() {
        aerolinea.hacerCheckIn("AM101", "Ana Pérez", TipoDocumento.RUT, "11.111.111-1");

        assertNull(aerolinea.emitirPaseAbordar("AM101", TipoDocumento.RUT, "22.222.222-2"));
        aerolinea.cambiarEstadoVuelo("AM101", EstadoVuelo.CANCELADO);
        assertNull(aerolinea.emitirPaseAbordar("AM101", TipoDocumento.RUT, "11.111.111-1"));
        assertEquals(OperationResult.BOARDING_PASS_UNAVAILABLE,
                vuelo().validarPaseAbordar(TipoDocumento.RUT, "11.111.111-1"));
    }

    @Test
    @DisplayName("Al restaurar datos se rechazan asientos repetidos o inexistentes")
    void restaurar_AsientosInvalidos() {
        Avion avion = new Avion("CC-BBB", ModeloAvion.A320);
        Pasajero ana = new Pasajero("Ana", TipoDocumento.RUT, "11.111.111-1");
        Pasajero luis = new Pasajero("Luis", TipoDocumento.RUT, "22.222.222-2");

        assertThrows(IllegalArgumentException.class, () -> Vuelo.restaurar("AM102", Destino.SCL, Destino.LIM, SALIDA, avion,
                EstadoVuelo.PROGRAMADO, List.of(new CheckIn(ana, EstadoPasajero.CHECK_IN, "5A"),
                        new CheckIn(luis, EstadoPasajero.CHECK_IN, "5A"))));
        assertThrows(IllegalArgumentException.class, () -> Vuelo.restaurar("AM102", Destino.SCL, Destino.LIM, SALIDA, avion,
                EstadoVuelo.PROGRAMADO, List.of(new CheckIn(ana, EstadoPasajero.CHECK_IN, "5K"))));
    }
}