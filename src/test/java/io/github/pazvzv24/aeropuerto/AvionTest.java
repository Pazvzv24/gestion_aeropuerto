package io.github.pazvzv24.aeropuerto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AvionTest {

    private Avion embarcando(int pasajeros) {
        return new Avion("CC-AMA", ModeloAvion.A320, EstadoAvion.EMBARCANDO, pasajeros);
    }

    @Test
    @DisplayName("Un avión nuevo queda disponible, vacío y con la capacidad de su modelo")
    void avionNuevo_QuedaDisponibleYVacio() {
        Avion avion = new Avion("CC-AMA", ModeloAvion.A320);

        assertEquals(EstadoAvion.DISPONIBLE, avion.getEstado());
        assertEquals(0, avion.getPasajerosActuales());
        assertEquals(174, avion.getCantidadMaxima());
    }

    @Test
    @DisplayName("La matrícula sigue el formato chileno CC-XXX, sin importar mayúsculas ni espacios")
    void matricula_SigueElFormatoChileno() {
        assertTrue(Avion.esMatriculaValida("CC-AMA"));
        assertTrue(Avion.esMatriculaValida(" cc-ama "));
        assertFalse(Avion.esMatriculaValida("CC-AM1"));
        assertFalse(Avion.esMatriculaValida("LV-AMA"));
        assertFalse(Avion.esMatriculaValida("CCAMA"));
        assertFalse(Avion.esMatriculaValida(null));
        assertEquals("CC-AMA", new Avion(" cc-ama ", ModeloAvion.A320).getMatricula());
    }

    @Test
    @DisplayName("Durante el embarque se suben pasajeros hasta completar la capacidad")
    void subirPasajeros_DuranteElEmbarque_HastaLaCapacidad() {
        Avion avion = embarcando(170);

        assertEquals(OperationResult.SUCCESS, avion.subirPasajeros(4));
        assertEquals(0, avion.getAsientosDisponibles());
        assertEquals(OperationResult.CAPACITY_EXCEEDED, avion.subirPasajeros(1));
        assertEquals(174, avion.getPasajerosActuales());
    }

    @Test
    @DisplayName("No se suben pasajeros fuera del embarque")
    void subirPasajeros_FueraDelEmbarque_NoSePermite() {
        Avion disponible = new Avion("CC-AMA", ModeloAvion.A320);
        Avion enVuelo = new Avion("CC-AMB", ModeloAvion.A320, EstadoAvion.EN_VUELO, 100);

        assertEquals(OperationResult.OPERATION_NOT_ALLOWED, disponible.subirPasajeros(5));
        assertEquals(OperationResult.OPERATION_NOT_ALLOWED, enVuelo.subirPasajeros(5));
        assertEquals(OperationResult.OPERATION_NOT_ALLOWED, enVuelo.bajarPasajeros(5));
        assertEquals(100, enVuelo.getPasajerosActuales());
    }

    @Test
    @DisplayName("Al desembarcar se bajan pasajeros, pero no más de los que hay")
    void bajarPasajeros_AlDesembarcar() {
        Avion avion = new Avion("CC-AMA", ModeloAvion.A320, EstadoAvion.DESEMBARCANDO, 30);

        assertEquals(OperationResult.NOT_ENOUGH_PASSENGERS, avion.bajarPasajeros(31));
        assertEquals(OperationResult.SUCCESS, avion.bajarPasajeros(30));
        assertEquals(0, avion.getPasajerosActuales());
    }

    @Test
    @DisplayName("Cantidades cero o negativas son inválidas")
    void cantidadesNoPositivas_SonInvalidas() {
        Avion avion = embarcando(10);

        assertEquals(OperationResult.INVALID_AMOUNT, avion.subirPasajeros(0));
        assertEquals(OperationResult.INVALID_AMOUNT, avion.bajarPasajeros(-3));
        assertEquals(10, avion.getPasajerosActuales());
    }

    @Test
    @DisplayName("Un vuelo completo: embarque, despegue, aterrizaje y desembarque")
    void cicloCompleto_DeUnVuelo() {
        Avion avion = new Avion("CC-AMA", ModeloAvion.A320);

        assertEquals(OperationResult.SUCCESS, avion.cambiarEstado(EstadoAvion.EMBARCANDO));
        assertEquals(OperationResult.SUCCESS, avion.subirPasajeros(150));
        assertEquals(OperationResult.SUCCESS, avion.cambiarEstado(EstadoAvion.EN_VUELO));
        assertEquals(OperationResult.SUCCESS, avion.cambiarEstado(EstadoAvion.DESEMBARCANDO));
        assertEquals(OperationResult.SUCCESS, avion.bajarPasajeros(150));
        assertEquals(OperationResult.SUCCESS, avion.cambiarEstado(EstadoAvion.DISPONIBLE));
        assertEquals(EstadoAvion.DISPONIBLE, avion.getEstado());
    }

    @Test
    @DisplayName("Se rechazan cambios de estado imposibles, sin modificar el avión")
    void cambiarEstado_Imposible_EsRechazado() {
        Avion enVuelo = new Avion("CC-AMA", ModeloAvion.A320, EstadoAvion.EN_VUELO, 100);

        assertEquals(OperationResult.INVALID_TRANSITION, enVuelo.cambiarEstado(EstadoAvion.EN_MANTENIMIENTO));
        assertEquals(OperationResult.INVALID_TRANSITION, enVuelo.cambiarEstado(EstadoAvion.EMBARCANDO));
        assertEquals(OperationResult.INVALID_TRANSITION, enVuelo.cambiarEstado(null));
        assertEquals(EstadoAvion.EN_VUELO, enVuelo.getEstado());
    }

    @Test
    @DisplayName("No se libera un avión con pasajeros a bordo")
    void cambiarEstado_ConPasajeros_AEstadoQueExigeVacio_EsRechazado() {
        Avion avion = new Avion("CC-AMA", ModeloAvion.A320, EstadoAvion.DESEMBARCANDO, 5);

        assertEquals(OperationResult.AIRPLANE_NOT_EMPTY, avion.cambiarEstado(EstadoAvion.DISPONIBLE));
        assertEquals(EstadoAvion.DESEMBARCANDO, avion.getEstado());
    }

    @Test
    @DisplayName("Un avión fuera de servicio ya no admite cambios")
    void fueraDeServicio_EsEstadoFinal() {
        Avion avion = new Avion("CC-AMA", ModeloAvion.A320, EstadoAvion.FUERA_DE_SERVICIO, 0);

        for (EstadoAvion estado : EstadoAvion.values()) {
            assertEquals(OperationResult.INVALID_TRANSITION, avion.cambiarEstado(estado));
        }
    }

    @Test
    @DisplayName("El porcentaje de ocupación se calcula sobre la capacidad del modelo")
    void porcentajeOcupacion_SeCalculaSobreLaCapacidad() {
        assertEquals(0.0, new Avion("CC-AMA", ModeloAvion.B787_9).getPorcentajeOcupacion());
        assertEquals(50.0, new Avion("CC-AMA", ModeloAvion.B787_9, EstadoAvion.EN_VUELO, 150).getPorcentajeOcupacion());
    }

    @Test
    @DisplayName("El constructor rechaza datos que no forman un avión válido")
    void constructor_ConDatosInvalidos_LanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new Avion("AMA", ModeloAvion.A320));
        assertThrows(IllegalArgumentException.class, () -> new Avion("CC-AMA", null));
        assertThrows(IllegalArgumentException.class,
                () -> new Avion("CC-AMA", ModeloAvion.A320, null, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new Avion("CC-AMA", ModeloAvion.A320, EstadoAvion.EMBARCANDO, 175));
        assertThrows(IllegalArgumentException.class,
                () -> new Avion("CC-AMA", ModeloAvion.A320, EstadoAvion.EN_MANTENIMIENTO, 10));
    }
}