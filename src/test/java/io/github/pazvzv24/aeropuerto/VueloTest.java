package io.github.pazvzv24.aeropuerto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class VueloTest {

    private static final LocalDateTime SALIDA = LocalDateTime.of(2026, 12, 24, 14, 30);

    private final Avion avion = new Avion("CC-AAA", ModeloAvion.A319);
    private final Vuelo vuelo = new Vuelo("am101", Destino.SCL, Destino.LIM, SALIDA, avion);

    private static Pasajero pasajero(String rut) {
        return new Pasajero("Pasajero de prueba", TipoDocumento.RUT, rut);
    }

    @Test
    @DisplayName("El vuelo se crea programado, con el código en mayúsculas y su ruta")
    void constructor_CreaVueloProgramado() {
        assertEquals("AM101", vuelo.getCodigo());
        assertEquals("SCL → LIM", vuelo.getRuta());
        assertEquals(EstadoVuelo.PROGRAMADO, vuelo.getEstado());
    }

    @Test
    @DisplayName("No se puede crear un vuelo con código inválido o con origen igual al destino")
    void constructor_ConDatosInvalidos_LanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new Vuelo("LA101", Destino.SCL, Destino.LIM, SALIDA, avion));
        assertThrows(IllegalArgumentException.class, () -> new Vuelo("AM101", Destino.SCL, Destino.SCL, SALIDA, avion));
        assertThrows(IllegalArgumentException.class, () -> new Vuelo("AM101", Destino.SCL, Destino.LIM, null, avion));
    }

    @Test
    @DisplayName("No se permite hacer check-in dos veces con el mismo documento")
    void hacerCheckIn_ConDocumentoRepetido_DevuelveDuplicado() {
        assertEquals(OperationResult.SUCCESS, vuelo.hacerCheckIn(pasajero("11.111.111-1")));
        assertEquals(OperationResult.DUPLICATE_PASSENGER, vuelo.hacerCheckIn(pasajero("111111111")));
        assertEquals(1, vuelo.getCantidadPasajeros());
    }

    @Test
    @DisplayName("No se aceptan más check-in que asientos tiene el avión")
    void hacerCheckIn_SinAsientos_DevuelveCapacidadExcedida() {
        for (int i = 0; i < avion.getCantidadMaxima(); i++) {
            String cuerpo = String.valueOf(10_000_000 + i);
            String rut = cuerpo + "-" + Rut.calcularDigitoVerificador(cuerpo);
            assertEquals(OperationResult.SUCCESS, vuelo.hacerCheckIn(pasajero(rut)));
        }
        assertEquals(OperationResult.CAPACITY_EXCEEDED, vuelo.hacerCheckIn(pasajero("22.222.222-2")));
    }

    @Test
    @DisplayName("Iniciar embarque pone el avión en embarque y cada pasajero embarcado ocupa un asiento")
    void embarque_MueveElAvionYOcupaAsientos() {
        vuelo.hacerCheckIn(pasajero("11.111.111-1"));
        vuelo.hacerCheckIn(pasajero("22.222.222-2"));

        assertEquals(OperationResult.OPERATION_NOT_ALLOWED, vuelo.embarcarPasajero(TipoDocumento.RUT, "11.111.111-1"));
        assertEquals(OperationResult.SUCCESS, vuelo.cambiarEstado(EstadoVuelo.EMBARCANDO));
        assertEquals(EstadoAvion.EMBARCANDO, avion.getEstado());
        assertEquals(OperationResult.SUCCESS, vuelo.embarcarPasajero(TipoDocumento.RUT, "11.111.111-1"));
        assertEquals(OperationResult.ALREADY_BOARDED, vuelo.embarcarPasajero(TipoDocumento.RUT, "11.111.111-1"));
        assertEquals(OperationResult.PASSENGER_NOT_FOUND, vuelo.embarcarPasajero(TipoDocumento.RUT, "33.333.333-3"));
        assertEquals(1, avion.getPasajerosActuales());
    }

    @Test
    @DisplayName("No se puede anular el check-in de un pasajero que ya embarcó")
    void anularCheckIn_PasajeroEmbarcado_DevuelveYaEmbarcado() {
        vuelo.hacerCheckIn(pasajero("11.111.111-1"));
        vuelo.hacerCheckIn(pasajero("22.222.222-2"));
        vuelo.cambiarEstado(EstadoVuelo.EMBARCANDO);
        vuelo.embarcarPasajero(TipoDocumento.RUT, "11.111.111-1");

        assertEquals(OperationResult.ALREADY_BOARDED, vuelo.anularCheckIn(TipoDocumento.RUT, "11.111.111-1"));
        assertEquals(OperationResult.SUCCESS, vuelo.anularCheckIn(TipoDocumento.RUT, "22.222.222-2"));
        assertEquals(1, vuelo.getCantidadPasajeros());
    }

    @Test
    @DisplayName("Al despegar, quien no embarcó queda como 'No se presentó' y se cierra el check-in")
    void despegar_MarcaNoShowYCierraCheckIn() {
        vuelo.hacerCheckIn(pasajero("11.111.111-1"));
        vuelo.hacerCheckIn(pasajero("22.222.222-2"));
        vuelo.cambiarEstado(EstadoVuelo.EMBARCANDO);
        vuelo.embarcarPasajero(TipoDocumento.RUT, "11.111.111-1");

        assertEquals(OperationResult.SUCCESS, vuelo.cambiarEstado(EstadoVuelo.EN_VUELO));
        assertEquals(EstadoAvion.EN_VUELO, avion.getEstado());
        assertEquals(1, vuelo.contarPasajeros(EstadoPasajero.NO_SE_PRESENTO));
        assertEquals(OperationResult.CHECK_IN_CLOSED, vuelo.hacerCheckIn(pasajero("33.333.333-3")));
    }

    @Test
    @DisplayName("Al aterrizar, los pasajeros bajan y el avión queda disponible y vacío")
    void aterrizar_DejaElAvionDisponibleYVacio() {
        vuelo.hacerCheckIn(pasajero("11.111.111-1"));
        vuelo.cambiarEstado(EstadoVuelo.EMBARCANDO);
        vuelo.embarcarPasajero(TipoDocumento.RUT, "11.111.111-1");
        vuelo.cambiarEstado(EstadoVuelo.EN_VUELO);

        assertEquals(OperationResult.SUCCESS, vuelo.cambiarEstado(EstadoVuelo.ATERRIZADO));
        assertEquals(EstadoAvion.DISPONIBLE, avion.getEstado());
        assertEquals(0, avion.getPasajerosActuales());
        assertEquals(1, vuelo.contarPasajeros(EstadoPasajero.EMBARCADO));
    }

    @Test
    @DisplayName("Cancelar durante el embarque baja a los pasajeros y libera el avión")
    void cancelarEnEmbarque_LiberaElAvion() {
        vuelo.hacerCheckIn(pasajero("11.111.111-1"));
        vuelo.cambiarEstado(EstadoVuelo.EMBARCANDO);
        vuelo.embarcarPasajero(TipoDocumento.RUT, "11.111.111-1");

        assertEquals(OperationResult.SUCCESS, vuelo.cambiarEstado(EstadoVuelo.CANCELADO));
        assertEquals(EstadoAvion.DISPONIBLE, avion.getEstado());
        assertEquals(0, avion.getPasajerosActuales());
        assertEquals(OperationResult.INVALID_TRANSITION, vuelo.cambiarEstado(EstadoVuelo.EMBARCANDO));
    }

    @Test
    @DisplayName("Si el avión está en mantenimiento, el vuelo no puede iniciar el embarque")
    void iniciarEmbarque_AvionEnMantenimiento_NoCambiaNada() {
        avion.cambiarEstado(EstadoAvion.EN_MANTENIMIENTO);

        assertEquals(OperationResult.AIRPLANE_NOT_AVAILABLE, vuelo.cambiarEstado(EstadoVuelo.EMBARCANDO));
        assertEquals(EstadoVuelo.PROGRAMADO, vuelo.getEstado());
        assertEquals(EstadoAvion.EN_MANTENIMIENTO, avion.getEstado());
    }
}