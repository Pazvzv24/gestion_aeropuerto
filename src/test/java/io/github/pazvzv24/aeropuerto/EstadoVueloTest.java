package io.github.pazvzv24.aeropuerto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EstadoVueloTest {

    @Test
    @DisplayName("Un vuelo programado puede embarcar, retrasarse o cancelarse (embarque primero)")
    void programado_TieneTresAcciones() {
        assertEquals(List.of(EstadoVuelo.EMBARCANDO, EstadoVuelo.RETRASADO, EstadoVuelo.CANCELADO),
                EstadoVuelo.PROGRAMADO.getSiguientes());
    }

    @Test
    @DisplayName("Un vuelo en el aire solo puede aterrizar: no se cancela ni vuelve atrás")
    void enVuelo_SoloPuedeAterrizar() {
        assertTrue(EstadoVuelo.EN_VUELO.puedeCambiarA(EstadoVuelo.ATERRIZADO));
        assertFalse(EstadoVuelo.EN_VUELO.puedeCambiarA(EstadoVuelo.CANCELADO));
        assertFalse(EstadoVuelo.EMBARCANDO.puedeCambiarA(EstadoVuelo.PROGRAMADO));
    }

    @Test
    @DisplayName("Aterrizado y cancelado son estados finales sin más cambios")
    void estadosFinales_NoTienenSiguientes() {
        assertTrue(EstadoVuelo.ATERRIZADO.esFinal());
        assertTrue(EstadoVuelo.CANCELADO.getSiguientes().isEmpty());
        assertFalse(EstadoVuelo.RETRASADO.esFinal());
    }

    @Test
    @DisplayName("El check-in está abierto hasta el despegue, también si el vuelo está retrasado")
    void permiteCheckIn_HastaElDespegue() {
        assertTrue(EstadoVuelo.RETRASADO.permiteCheckIn());
        assertTrue(EstadoVuelo.EMBARCANDO.permiteCheckIn());
        assertFalse(EstadoVuelo.EN_VUELO.permiteCheckIn());
        assertEquals("Despegar", EstadoVuelo.EMBARCANDO.accionHacia(EstadoVuelo.EN_VUELO));
    }
}