package io.github.pazvzv24.aeropuerto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AeropuertoTest {

    private Aeropuerto crearAeropuerto() {
        return new Aeropuerto("Prueba",
                new Avion(1, 100, 40, true),
                new Avion(2, 80, 80, true),
                new Avion(3, 60, 10, false));
    }

    @Test
    @DisplayName("Al iniciar se crean 5 aviones con IDs del 1 al 5")
    void constructor_CreaCincoAvionesConIdsCorrelativos() {
        Aeropuerto aeropuerto = new Aeropuerto("Aeropuerto Internacional", new Random(7));

        List<Avion> aviones = aeropuerto.getAviones();

        assertEquals(Aeropuerto.AVIONES_INICIALES, aviones.size());
        for (int id = 1; id <= Aeropuerto.AVIONES_INICIALES; id++) {
            assertNotNull(aeropuerto.buscarAvion(id));
        }
    }

    @Test
    @DisplayName("Los totales suman todos los aviones, activos y cancelados")
    void totales_SumanTodosLosAviones() {
        Aeropuerto aeropuerto = crearAeropuerto();

        assertEquals(40 + 80 + 10, aeropuerto.calcularTotalPasajeros());
        assertEquals(60 + 0 + 50, aeropuerto.calcularTotalDisponibles());
    }

    @Test
    @DisplayName("Buscar un ID inexistente devuelve null")
    void buscarAvion_IdInexistente_DevuelveNull() {
        assertNull(crearAeropuerto().buscarAvion(99));
    }

    @Test
    @DisplayName("Operar sobre un avión inexistente informa AIRPLANE_NOT_FOUND")
    void operaciones_AvionInexistente_InformanNoEncontrado() {
        Aeropuerto aeropuerto = crearAeropuerto();

        assertEquals(OperationResult.AIRPLANE_NOT_FOUND, aeropuerto.subirPasajeros(99, 5));
        assertEquals(OperationResult.AIRPLANE_NOT_FOUND, aeropuerto.bajarPasajeros(99, 5));
    }

    @Test
    @DisplayName("Subir pasajeros desde el aeropuerto actualiza el total")
    void subirPasajeros_ActualizaElTotal() {
        Aeropuerto aeropuerto = crearAeropuerto();

        assertEquals(OperationResult.SUCCESS, aeropuerto.subirPasajeros(1, 20));
        assertEquals(150, aeropuerto.calcularTotalPasajeros());
    }

    @Test
    @DisplayName("La lista de aviones es de solo lectura (encapsulamiento)")
    void getAviones_EsDeSoloLectura() {
        List<Avion> aviones = crearAeropuerto().getAviones();

        assertThrows(UnsupportedOperationException.class, () -> aviones.clear());
    }

    @Test
    @DisplayName("Se rechazan IDs repetidos, aviones nulos y nombres vacíos")
    void constructor_ConDatosInvalidos_LanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> new Aeropuerto("Prueba", new Avion(1, 10, 0, true), new Avion(1, 20, 0, true)));
        assertThrows(IllegalArgumentException.class,
                () -> new Aeropuerto("Prueba", new Avion(1, 10, 0, true), null));
        assertThrows(IllegalArgumentException.class, () -> new Aeropuerto("  "));
    }

    @Test
    @DisplayName("No se pueden registrar más de 100 aviones")
    void constructor_MasDeCienAviones_LanzaExcepcion() {
        Avion[] aviones = new Avion[Aeropuerto.CAPACIDAD_AVIONES + 1];
        for (int i = 0; i < aviones.length; i++) {
            aviones[i] = new Avion(i + 1, 10, 0, true);
        }

        assertThrows(IllegalArgumentException.class, () -> new Aeropuerto("Prueba", aviones));
    }
}