package io.github.pazvzv24.aeropuerto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AerolineaVuelosTest {

    private static final LocalDateTime SALIDA = LocalDateTime.of(2026, 12, 24, 14, 30);

    private Aerolinea crearAerolinea() {
        return new Aerolinea("Prueba",
                new Avion("CC-AAA", ModeloAvion.A320),
                new Avion("CC-AAB", ModeloAvion.A321),
                new Avion("CC-AAC", ModeloAvion.A319, EstadoAvion.FUERA_DE_SERVICIO, 0));
    }

    @Test
    @DisplayName("Altamar parte con 5 vuelos de demostración y pasajeros ficticios")
    void altamarAirlines_TieneVuelosDeDemostracion() {
        Aerolinea altamar = Aerolinea.altamarAirlines();

        assertEquals(5, altamar.getVuelos().size());
        assertEquals(EstadoVuelo.EMBARCANDO, altamar.buscarVuelo("AM205").getEstado());
        assertEquals(EstadoAvion.EMBARCANDO, altamar.buscarAvion("CC-AMD").getEstado());
        assertEquals(2, altamar.buscarAvion("CC-AMD").getPasajerosActuales());
        assertEquals(EstadoVuelo.RETRASADO, altamar.buscarVuelo("am330").getEstado());
        assertEquals(EstadoVuelo.CANCELADO, altamar.buscarVuelo("AM518").getEstado());
        assertEquals(4, altamar.buscarVuelo("AM101").getCantidadPasajeros());
    }

    @Test
    @DisplayName("Programar un vuelo valida el código, la ruta y el avión")
    void programarVuelo_ValidaLosDatos() {
        Aerolinea aerolinea = crearAerolinea();

        assertEquals(OperationResult.SUCCESS,
                aerolinea.programarVuelo("AM101", Destino.SCL, Destino.LIM, SALIDA, "cc-aaa"));
        assertEquals(OperationResult.DUPLICATE_FLIGHT,
                aerolinea.programarVuelo("am101", Destino.SCL, Destino.PUQ, SALIDA, "CC-AAB"));
        assertEquals(OperationResult.INVALID_FLIGHT_DATA,
                aerolinea.programarVuelo("X1", Destino.SCL, Destino.LIM, SALIDA, "CC-AAB"));
        assertEquals(OperationResult.INVALID_FLIGHT_DATA,
                aerolinea.programarVuelo("AM102", Destino.LIM, Destino.LIM, SALIDA, "CC-AAB"));
        assertEquals(OperationResult.AIRPLANE_NOT_FOUND,
                aerolinea.programarVuelo("AM103", Destino.SCL, Destino.LIM, SALIDA, "CC-ZZZ"));
        assertEquals(OperationResult.AIRPLANE_NOT_AVAILABLE,
                aerolinea.programarVuelo("AM104", Destino.SCL, Destino.LIM, SALIDA, "CC-AAC"));
        assertEquals(1, aerolinea.getVuelos().size());
    }

    @Test
    @DisplayName("Un avión opera un solo vuelo pendiente a la vez")
    void programarVuelo_AvionConVueloPendiente_DevuelveAsignado() {
        Aerolinea aerolinea = crearAerolinea();
        aerolinea.programarVuelo("AM101", Destino.SCL, Destino.LIM, SALIDA, "CC-AAA");

        assertEquals(OperationResult.AIRPLANE_ASSIGNED_TO_FLIGHT,
                aerolinea.programarVuelo("AM102", Destino.LIM, Destino.SCL, SALIDA.plusHours(5), "CC-AAA"));

        aerolinea.cambiarEstadoVuelo("AM101", EstadoVuelo.CANCELADO);
        assertEquals(OperationResult.SUCCESS,
                aerolinea.programarVuelo("AM102", Destino.LIM, Destino.SCL, SALIDA.plusHours(5), "CC-AAA"));
    }

    @Test
    @DisplayName("El check-in valida el RUT con módulo 11 antes de registrar al pasajero")
    void hacerCheckIn_ConRutInvalido_DevuelveDatosInvalidos() {
        Aerolinea aerolinea = crearAerolinea();
        aerolinea.programarVuelo("AM101", Destino.SCL, Destino.LIM, SALIDA, "CC-AAA");

        assertEquals(OperationResult.INVALID_PASSENGER_DATA,
                aerolinea.hacerCheckIn("AM101", "Ana Pérez", TipoDocumento.RUT, "12.345.678-9"));
        assertEquals(OperationResult.INVALID_PASSENGER_DATA,
                aerolinea.hacerCheckIn("AM101", " ", TipoDocumento.RUT, "12.345.678-5"));
        assertEquals(OperationResult.FLIGHT_NOT_FOUND,
                aerolinea.hacerCheckIn("AM999", "Ana Pérez", TipoDocumento.RUT, "12.345.678-5"));
        assertEquals(OperationResult.SUCCESS,
                aerolinea.hacerCheckIn("AM101", "Ana Pérez", TipoDocumento.RUT, "12.345.678-5"));
    }

    @Test
    @DisplayName("Mientras un vuelo embarca o vuela, su avión no se puede operar desde la flota")
    void avionControladoPorVuelo_RechazaOperacionesDeFlota() {
        Aerolinea aerolinea = crearAerolinea();
        aerolinea.programarVuelo("AM101", Destino.SCL, Destino.LIM, SALIDA, "CC-AAA");
        Avion avion = aerolinea.buscarAvion("CC-AAA");
        assertFalse(aerolinea.estaControladoPorVuelo(avion));

        aerolinea.cambiarEstadoVuelo("AM101", EstadoVuelo.EMBARCANDO);

        assertTrue(aerolinea.estaControladoPorVuelo(avion));
        assertEquals(OperationResult.AIRPLANE_ASSIGNED_TO_FLIGHT, aerolinea.subirPasajeros("CC-AAA", 10));
        assertEquals(OperationResult.AIRPLANE_ASSIGNED_TO_FLIGHT,
                aerolinea.cambiarEstado("CC-AAA", EstadoAvion.EN_VUELO));
        assertEquals(0, avion.getPasajerosActuales());
    }

    @Test
    @DisplayName("Un avión con vuelo programado no inicia el embarque desde la flota, pero sí puede ir a mantenimiento")
    void avionConVueloProgramado_EmbarqueSoloDesdeElVuelo() {
        Aerolinea aerolinea = crearAerolinea();
        aerolinea.programarVuelo("AM101", Destino.SCL, Destino.LIM, SALIDA, "CC-AAA");

        assertEquals(OperationResult.AIRPLANE_ASSIGNED_TO_FLIGHT,
                aerolinea.cambiarEstado("CC-AAA", EstadoAvion.EMBARCANDO));
        assertEquals(OperationResult.SUCCESS,
                aerolinea.cambiarEstado("CC-AAA", EstadoAvion.EN_MANTENIMIENTO));
        assertEquals(OperationResult.AIRPLANE_NOT_AVAILABLE,
                aerolinea.cambiarEstadoVuelo("AM101", EstadoVuelo.EMBARCANDO));
    }

    @Test
    @DisplayName("Al aterrizar, el vuelo termina y el avión queda libre para otro vuelo")
    void vueloCompleto_LiberaElAvion() {
        Aerolinea aerolinea = crearAerolinea();
        aerolinea.programarVuelo("AM101", Destino.SCL, Destino.LIM, SALIDA, "CC-AAA");
        aerolinea.hacerCheckIn("AM101", "Ana Pérez", TipoDocumento.PASAPORTE, "AB123456");
        aerolinea.cambiarEstadoVuelo("AM101", EstadoVuelo.EMBARCANDO);
        assertEquals(OperationResult.SUCCESS,
                aerolinea.embarcarPasajero("AM101", TipoDocumento.PASAPORTE, "ab123456"));
        aerolinea.cambiarEstadoVuelo("AM101", EstadoVuelo.EN_VUELO);
        assertEquals(1, aerolinea.calcularTotalPasajeros());

        assertEquals(OperationResult.SUCCESS, aerolinea.cambiarEstadoVuelo("AM101", EstadoVuelo.ATERRIZADO));

        Avion avion = aerolinea.buscarAvion("CC-AAA");
        assertNull(aerolinea.buscarVueloPendiente(avion));
        assertEquals(EstadoAvion.DISPONIBLE, avion.getEstado());
        assertEquals(0, aerolinea.calcularTotalPasajeros());
        assertEquals(1, aerolinea.contarVuelosPorEstado(EstadoVuelo.ATERRIZADO));
        assertNotNull(aerolinea.buscarVuelo("AM101"));
    }
}