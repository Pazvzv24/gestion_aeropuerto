package io.github.pazvzv24.aeropuerto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Reglas para reconstruir la aerolínea desde datos guardados y avisar cambios para guardarlos. */
class AerolineaPersistenciaTest {

    private static final LocalDateTime SALIDA = LocalDateTime.of(2026, 12, 24, 14, 30);

    private static Pasajero pasajero(String rut) {
        return new Pasajero("Pasajero de prueba", TipoDocumento.RUT, rut);
    }

    @Test
    @DisplayName("Solo las operaciones exitosas avisan que hay cambios por guardar")
    void alCambiar_SoloConOperacionesExitosas() {
        Aerolinea aerolinea = new Aerolinea("Prueba", new Avion("CC-AAA", ModeloAvion.A320));
        AtomicInteger guardados = new AtomicInteger();
        aerolinea.setAlCambiar(guardados::incrementAndGet);

        aerolinea.programarVuelo("AM101", Destino.SCL, Destino.LIM, SALIDA, "CC-AAA");
        aerolinea.hacerCheckIn("AM101", "Ana Pérez", TipoDocumento.RUT, "12.345.678-9");
        aerolinea.subirPasajeros("CC-ZZZ", 5);
        aerolinea.hacerCheckIn("AM101", "Ana Pérez", TipoDocumento.RUT, "12.345.678-5");

        assertEquals(2, guardados.get());
    }

    @Test
    @DisplayName("Se puede reconstruir un vuelo embarcando con sus pasajeros y su avión")
    void restaurar_VueloEmbarcando() {
        Avion avion = new Avion("CC-AAA", ModeloAvion.A320, EstadoAvion.EMBARCANDO, 1);
        Vuelo vuelo = Vuelo.restaurar("AM101", Destino.SCL, Destino.LIM, SALIDA, avion, EstadoVuelo.EMBARCANDO,
                List.of(new CheckIn(pasajero("11.111.111-1"), EstadoPasajero.EMBARCADO),
                        new CheckIn(pasajero("22.222.222-2"), EstadoPasajero.CHECK_IN)));

        Aerolinea aerolinea = Aerolinea.desdeDatos("Prueba", List.of(avion), List.of(vuelo));

        assertEquals(EstadoVuelo.EMBARCANDO, aerolinea.buscarVuelo("AM101").getEstado());
        assertEquals(2, aerolinea.buscarVuelo("AM101").getCantidadPasajeros());
        assertEquals(OperationResult.SUCCESS, aerolinea.embarcarPasajero("AM101", TipoDocumento.RUT, "22.222.222-2"));
        assertEquals(2, avion.getPasajerosActuales());
    }

    @Test
    @DisplayName("Se rechazan datos guardados imposibles, aunque alguien los haya modificado en la base")
    void restaurar_DatosIncoherentes_LanzaExcepcion() {
        Avion disponible = new Avion("CC-AAA", ModeloAvion.A320);
        List<CheckIn> embarcado = List.of(new CheckIn(pasajero("11.111.111-1"), EstadoPasajero.EMBARCADO));

        // Un vuelo en el aire con un avión que está disponible
        assertThrows(IllegalArgumentException.class, () -> Vuelo.restaurar("AM101", Destino.SCL, Destino.LIM,
                SALIDA, disponible, EstadoVuelo.EN_VUELO, embarcado));
        // Un pasajero embarcado en un vuelo que todavía no embarca
        assertThrows(IllegalArgumentException.class, () -> Vuelo.restaurar("AM101", Destino.SCL, Destino.LIM,
                SALIDA, disponible, EstadoVuelo.PROGRAMADO, embarcado));
        // El mismo documento dos veces
        assertThrows(IllegalArgumentException.class, () -> Vuelo.restaurar("AM101", Destino.SCL, Destino.LIM,
                SALIDA, disponible, EstadoVuelo.PROGRAMADO, List.of(
                        new CheckIn(pasajero("11.111.111-1"), EstadoPasajero.CHECK_IN),
                        new CheckIn(pasajero("111111111"), EstadoPasajero.CHECK_IN))));
    }

    @Test
    @DisplayName("Se rechaza un avión con dos vuelos pendientes o un vuelo con un avión ajeno a la flota")
    void desdeDatos_VuelosIncoherentes_LanzaExcepcion() {
        Avion avion = new Avion("CC-AAA", ModeloAvion.A320);
        Avion ajeno = new Avion("CC-AAA", ModeloAvion.A320);
        Vuelo primero = Vuelo.restaurar("AM101", Destino.SCL, Destino.LIM, SALIDA, avion,
                EstadoVuelo.PROGRAMADO, List.of());
        Vuelo segundo = Vuelo.restaurar("AM102", Destino.LIM, Destino.SCL, SALIDA, avion,
                EstadoVuelo.PROGRAMADO, List.of());
        Vuelo conAvionAjeno = Vuelo.restaurar("AM103", Destino.SCL, Destino.LIM, SALIDA, ajeno,
                EstadoVuelo.PROGRAMADO, List.of());

        assertThrows(IllegalArgumentException.class,
                () -> Aerolinea.desdeDatos("Prueba", List.of(avion), List.of(primero, segundo)));
        assertThrows(IllegalArgumentException.class,
                () -> Aerolinea.desdeDatos("Prueba", List.of(avion), List.of(conAvionAjeno)));
    }

    @Test
    @DisplayName("Un nombre de más de 100 caracteres no se acepta en el check-in")
    void hacerCheckIn_NombreMuyLargo_DevuelveDatosInvalidos() {
        Aerolinea aerolinea = new Aerolinea("Prueba", new Avion("CC-AAA", ModeloAvion.A320));
        aerolinea.programarVuelo("AM101", Destino.SCL, Destino.LIM, SALIDA, "CC-AAA");

        assertEquals(OperationResult.INVALID_PASSENGER_DATA,
                aerolinea.hacerCheckIn("AM101", "A".repeat(101), TipoDocumento.RUT, "12.345.678-5"));
    }
}