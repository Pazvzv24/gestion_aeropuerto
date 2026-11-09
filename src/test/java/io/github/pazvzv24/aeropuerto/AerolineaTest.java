package io.github.pazvzv24.aeropuerto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AerolineaTest {

    private Aerolinea crearAerolinea() {
        return new Aerolinea("Prueba",
                new Avion("CC-AAA", ModeloAvion.A320, EstadoAvion.EMBARCANDO, 100),
                new Avion("CC-AAB", ModeloAvion.A321, EstadoAvion.EN_VUELO, 200),
                new Avion("CC-AAC", ModeloAvion.A319, EstadoAvion.EN_MANTENIMIENTO, 0));
    }

    @Test
    @DisplayName("Altamar Airlines parte con 15 aviones activos, todos válidos y sin matrículas repetidas")
    void altamarAirlines_TieneQuinceAviones() {
        Aerolinea altamar = Aerolinea.altamarAirlines();

        assertEquals("Altamar Airlines", altamar.getNombre());
        assertEquals(15, altamar.getCantidadAvionesActivos());
        assertEquals(Aerolinea.CAPACIDAD_FLOTA - 15, altamar.getEspacioDisponible());
        Set<String> matriculas = new HashSet<>();
        for (Avion avion : altamar.getFlota()) {
            matriculas.add(avion.getMatricula());
        }
        assertEquals(15, matriculas.size());
    }

    @Test
    @DisplayName("Los totales y conteos consideran toda la flota")
    void totales_YConteos() {
        Aerolinea aerolinea = crearAerolinea();

        assertEquals(300, aerolinea.calcularTotalPasajeros());
        assertEquals(74, aerolinea.calcularAsientosEnEmbarque());
        assertEquals(1, aerolinea.contarPorEstado(EstadoAvion.EN_VUELO));
        assertEquals(0, aerolinea.contarPorEstado(EstadoAvion.DISPONIBLE));
    }

    @Test
    @DisplayName("Se busca por matrícula sin importar mayúsculas; si no existe devuelve null")
    void buscarAvion_PorMatricula() {
        Aerolinea aerolinea = crearAerolinea();

        assertNotNull(aerolinea.buscarAvion("cc-aab"));
        assertNull(aerolinea.buscarAvion("CC-ZZZ"));
    }

    @Test
    @DisplayName("Operar sobre un avión inexistente informa AIRPLANE_NOT_FOUND")
    void operaciones_AvionInexistente() {
        Aerolinea aerolinea = crearAerolinea();

        assertEquals(OperationResult.AIRPLANE_NOT_FOUND, aerolinea.subirPasajeros("CC-ZZZ", 5));
        assertEquals(OperationResult.AIRPLANE_NOT_FOUND, aerolinea.bajarPasajeros("CC-ZZZ", 5));
        assertEquals(OperationResult.AIRPLANE_NOT_FOUND, aerolinea.cambiarEstado("CC-ZZZ", EstadoAvion.DISPONIBLE));
    }

    @Test
    @DisplayName("Incorporar un avión valida la matrícula y que no esté repetida")
    void incorporarAvion_ValidaMatricula() {
        Aerolinea aerolinea = crearAerolinea();

        assertEquals(OperationResult.SUCCESS, aerolinea.incorporarAvion("cc-aad", ModeloAvion.A320NEO));
        assertEquals(EstadoAvion.DISPONIBLE, aerolinea.buscarAvion("CC-AAD").getEstado());
        assertEquals(OperationResult.DUPLICATE_REGISTRATION, aerolinea.incorporarAvion("CC-AAA", ModeloAvion.A320));
        assertEquals(OperationResult.INVALID_REGISTRATION, aerolinea.incorporarAvion("AB-123", ModeloAvion.A320));
        assertEquals(4, aerolinea.getCantidadAvionesActivos());
    }

    @Test
    @DisplayName("La flota activa no supera los 100 aviones")
    void incorporarAvion_ConFlotaLlena_EsRechazado() {
        Aerolinea aerolinea = new Aerolinea("Prueba");
        for (int i = 0; i < Aerolinea.CAPACIDAD_FLOTA; i++) {
            assertEquals(OperationResult.SUCCESS, aerolinea.incorporarAvion(matricula(i), ModeloAvion.A320));
        }

        assertEquals(OperationResult.FLEET_FULL, aerolinea.incorporarAvion("CC-ZZZ", ModeloAvion.A320));
        assertEquals(Aerolinea.CAPACIDAD_FLOTA, aerolinea.getCantidadAvionesActivos());
    }

    @Test
    @DisplayName("Un avión dado de baja se conserva en el registro, pero libera espacio en la flota")
    void darDeBaja_LiberaEspacio() {
        Aerolinea aerolinea = crearAerolinea();

        assertEquals(OperationResult.SUCCESS, aerolinea.cambiarEstado("CC-AAC", EstadoAvion.FUERA_DE_SERVICIO));
        assertEquals(2, aerolinea.getCantidadAvionesActivos());
        assertEquals(3, aerolinea.getFlota().size());
    }

    @Test
    @DisplayName("La lista de la flota es de solo lectura (encapsulamiento)")
    void getFlota_EsDeSoloLectura() {
        List<Avion> flota = crearAerolinea().getFlota();

        assertThrows(UnsupportedOperationException.class, () -> flota.clear());
    }

    @Test
    @DisplayName("Se rechazan matrículas repetidas, aviones nulos y nombres vacíos")
    void constructor_ConDatosInvalidos_LanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new Aerolinea("Prueba",
                new Avion("CC-AAA", ModeloAvion.A320), new Avion("CC-AAA", ModeloAvion.A321)));
        assertThrows(IllegalArgumentException.class,
                () -> new Aerolinea("Prueba", new Avion("CC-AAA", ModeloAvion.A320), null));
        assertThrows(IllegalArgumentException.class, () -> new Aerolinea("  "));
    }

    /** Genera matrículas distintas CC-AAA, CC-AAB, ... para llenar la flota. */
    private static String matricula(int i) {
        char segunda = (char) ('A' + i / 26);
        char tercera = (char) ('A' + i % 26);
        return "CC-A" + segunda + tercera;
    }
}