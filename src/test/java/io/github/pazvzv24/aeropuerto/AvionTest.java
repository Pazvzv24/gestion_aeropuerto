package io.github.pazvzv24.aeropuerto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AvionTest {

    @Test
    @DisplayName("Subir pasajeros con asientos disponibles los agrega")
    void subirPasajeros_ConAsientosDisponibles_LosAgrega() {
        Avion avion = new Avion(1, 100, 40, true);

        OperationResult resultado = avion.subirPasajeros(10);

        assertEquals(OperationResult.SUCCESS, resultado);
        assertEquals(50, avion.getPasajerosActuales());
    }

    @Test
    @DisplayName("Se puede llenar el avión exactamente hasta su capacidad")
    void subirPasajeros_HastaLaCapacidadExacta_EsAceptado() {
        Avion avion = new Avion(1, 100, 90, true);

        assertEquals(OperationResult.SUCCESS, avion.subirPasajeros(10));
        assertEquals(0, avion.getAsientosDisponibles());
    }

    @Test
    @DisplayName("Subir más pasajeros que los asientos libres es rechazado sin cambios")
    void subirPasajeros_QueSuperanLaCapacidad_EsRechazadoSinCambios() {
        Avion avion = new Avion(1, 100, 95, true);

        assertEquals(OperationResult.CAPACITY_EXCEEDED, avion.subirPasajeros(6));
        assertEquals(95, avion.getPasajerosActuales());
    }

    @Test
    @DisplayName("No se puede subir pasajeros a un vuelo cancelado")
    void subirPasajeros_VueloCancelado_EsRechazado() {
        Avion avion = new Avion(1, 100, 10, false);

        assertEquals(OperationResult.FLIGHT_CANCELLED, avion.subirPasajeros(5));
        assertEquals(10, avion.getPasajerosActuales());
    }

    @Test
    @DisplayName("Cantidades cero o negativas son inválidas al subir y al bajar")
    void cantidadesNoPositivas_SonInvalidas() {
        Avion avion = new Avion(1, 100, 10, true);

        assertEquals(OperationResult.INVALID_AMOUNT, avion.subirPasajeros(0));
        assertEquals(OperationResult.INVALID_AMOUNT, avion.subirPasajeros(-3));
        assertEquals(OperationResult.INVALID_AMOUNT, avion.bajarPasajeros(0));
        assertEquals(OperationResult.INVALID_AMOUNT, avion.bajarPasajeros(-3));
        assertEquals(10, avion.getPasajerosActuales());
    }

    @Test
    @DisplayName("Bajar pasajeros los descuenta")
    void bajarPasajeros_ConPasajerosSuficientes_LosDescuenta() {
        Avion avion = new Avion(1, 100, 30, true);

        assertEquals(OperationResult.SUCCESS, avion.bajarPasajeros(30));
        assertEquals(0, avion.getPasajerosActuales());
    }

    @Test
    @DisplayName("Bajar más pasajeros de los que hay a bordo informa el error correcto")
    void bajarPasajeros_MasDeLosQueHay_InformaPasajerosInsuficientes() {
        Avion avion = new Avion(1, 100, 5, true);

        assertEquals(OperationResult.NOT_ENOUGH_PASSENGERS, avion.bajarPasajeros(6));
        assertEquals(5, avion.getPasajerosActuales());
    }

    @Test
    @DisplayName("En un vuelo cancelado los pasajeros igual pueden bajar")
    void bajarPasajeros_VueloCancelado_EsPermitido() {
        Avion avion = new Avion(1, 100, 20, false);

        assertEquals(OperationResult.SUCCESS, avion.bajarPasajeros(20));
        assertEquals(0, avion.getPasajerosActuales());
    }

    @Test
    @DisplayName("El constructor rechaza valores que no forman un avión válido")
    void constructor_ConValoresInvalidos_LanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new Avion(0, 100, 10, true));
        assertThrows(IllegalArgumentException.class, () -> new Avion(1, 0, 0, true));
        assertThrows(IllegalArgumentException.class, () -> new Avion(1, 100, -1, true));
        assertThrows(IllegalArgumentException.class, () -> new Avion(1, 100, 101, true));
    }

    @Test
    @DisplayName("Los aviones aleatorios siempre quedan en un estado válido")
    void constructorAleatorio_GeneraSiempreValoresValidos() {
        Random random = new Random(42); // semilla fija: la prueba es reproducible
        for (int i = 1; i <= 1000; i++) {
            Avion avion = new Avion(i, random);

            assertTrue(avion.getCantidadMaxima() >= Avion.CAPACIDAD_MINIMA_ALEATORIA);
            assertTrue(avion.getCantidadMaxima() <= Avion.CAPACIDAD_MAXIMA_ALEATORIA);
            assertTrue(avion.getPasajerosActuales() >= 0);
            assertTrue(avion.getPasajerosActuales() <= avion.getCantidadMaxima());
        }
    }
}