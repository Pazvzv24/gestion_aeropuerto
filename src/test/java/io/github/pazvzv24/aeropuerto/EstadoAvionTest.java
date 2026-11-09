package io.github.pazvzv24.aeropuerto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EstadoAvionTest {

    @Test
    @DisplayName("Cada estado permite exactamente los cambios definidos, con la acción principal primero")
    void transiciones_SonLasDefinidas() {
        assertEquals(List.of(EstadoAvion.EMBARCANDO, EstadoAvion.EN_MANTENIMIENTO, EstadoAvion.FUERA_DE_SERVICIO),
                EstadoAvion.DISPONIBLE.getSiguientes());
        assertEquals(List.of(EstadoAvion.EN_VUELO, EstadoAvion.DISPONIBLE), EstadoAvion.EMBARCANDO.getSiguientes());
        assertEquals(List.of(EstadoAvion.DESEMBARCANDO), EstadoAvion.EN_VUELO.getSiguientes());
        assertEquals(List.of(EstadoAvion.DISPONIBLE), EstadoAvion.DESEMBARCANDO.getSiguientes());
        assertEquals(List.of(EstadoAvion.DISPONIBLE, EstadoAvion.FUERA_DE_SERVICIO),
                EstadoAvion.EN_MANTENIMIENTO.getSiguientes());
        assertTrue(EstadoAvion.FUERA_DE_SERVICIO.getSiguientes().isEmpty());
    }

    @Test
    @DisplayName("Un avión en vuelo no puede ir directo a mantenimiento")
    void enVuelo_NoPuedeIrAMantenimiento() {
        assertFalse(EstadoAvion.EN_VUELO.puedeCambiarA(EstadoAvion.EN_MANTENIMIENTO));
        assertFalse(EstadoAvion.EN_VUELO.puedeCambiarA(null));
    }

    @Test
    @DisplayName("Solo se suben pasajeros al embarcar, y se bajan al embarcar o desembarcar")
    void permisosDePasajeros_SegunElEstado() {
        for (EstadoAvion estado : EstadoAvion.values()) {
            assertEquals(estado == EstadoAvion.EMBARCANDO, estado.permiteSubirPasajeros(), estado.name());
            assertEquals(estado == EstadoAvion.EMBARCANDO || estado == EstadoAvion.DESEMBARCANDO,
                    estado.permiteBajarPasajeros(), estado.name());
        }
    }

    @Test
    @DisplayName("Las acciones tienen nombres claros según el estado de origen")
    void accionHacia_NombraLaAccion() {
        assertEquals("Iniciar embarque", EstadoAvion.DISPONIBLE.accionHacia(EstadoAvion.EMBARCANDO));
        assertEquals("Despegar", EstadoAvion.EMBARCANDO.accionHacia(EstadoAvion.EN_VUELO));
        assertEquals("Cancelar embarque", EstadoAvion.EMBARCANDO.accionHacia(EstadoAvion.DISPONIBLE));
        assertEquals("Aterrizar", EstadoAvion.EN_VUELO.accionHacia(EstadoAvion.DESEMBARCANDO));
        assertEquals("Finalizar desembarque", EstadoAvion.DESEMBARCANDO.accionHacia(EstadoAvion.DISPONIBLE));
        assertEquals("Liberar de mantenimiento", EstadoAvion.EN_MANTENIMIENTO.accionHacia(EstadoAvion.DISPONIBLE));
        assertEquals("Dar de baja", EstadoAvion.EN_MANTENIMIENTO.accionHacia(EstadoAvion.FUERA_DE_SERVICIO));
    }
}