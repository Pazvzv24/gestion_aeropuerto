package io.github.pazvzv24.aeropuerto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ModeloAvionTest {

    @Test
    @DisplayName("Cada modelo tiene exactamente tantos asientos como su capacidad, sin repetir")
    void getAsientos_CoincideConLaCapacidad() {
        for (ModeloAvion modelo : ModeloAvion.values()) {
            List<String> asientos = modelo.getAsientos();
            assertEquals(modelo.getCapacidad(), asientos.size());
            assertEquals(asientos.size(), new HashSet<>(asientos).size());
        }
    }

    @Test
    @DisplayName("El A320 tiene 6 asientos por fila (A a F) y 29 filas")
    void a320_SeisAsientosPorFila() {
        assertEquals(6, ModeloAvion.A320.getAsientosPorFila());
        assertEquals(29, ModeloAvion.A320.getFilas());
        assertEquals("1A", ModeloAvion.A320.getAsientos().get(0));
        assertEquals("29F", ModeloAvion.A320.getAsientos().get(173));
    }

    @Test
    @DisplayName("Un asiento existe solo si está dentro de la capacidad y la letra corresponde al modelo")
    void esAsientoValido_SegunElModelo() {
        assertTrue(ModeloAvion.A320.esAsientoValido(" 12c "));
        assertFalse(ModeloAvion.A320.esAsientoValido("12G"));
        assertFalse(ModeloAvion.A320.esAsientoValido("30A"));
        assertTrue(ModeloAvion.B787_9.esAsientoValido("10J"));
        assertFalse(ModeloAvion.B787_9.esAsientoValido("10I"));
        assertFalse(ModeloAvion.A320.esAsientoValido("0A"));
        assertFalse(ModeloAvion.A320.esAsientoValido(null));
    }

    @Test
    @DisplayName("Si la capacidad no completa la última fila, esa fila queda parcial")
    void ultimaFilaParcial() {
        // A321: 220 asientos = 36 filas completas (216) + 4 en la fila 37
        assertEquals(37, ModeloAvion.A321.getFilas());
        assertTrue(ModeloAvion.A321.esAsientoValido("37D"));
        assertFalse(ModeloAvion.A321.esAsientoValido("37E"));
    }
}