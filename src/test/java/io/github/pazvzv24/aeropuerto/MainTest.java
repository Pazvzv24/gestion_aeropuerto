package io.github.pazvzv24.aeropuerto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.OptionalInt;
import java.util.Scanner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MainTest {

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final PrintStream salida = new PrintStream(buffer, true, StandardCharsets.UTF_8);

    private String textoSalida() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("Si el usuario escribe letras, se le vuelve a preguntar en vez de cerrarse")
    void leerEntero_ConTextoInvalido_VuelveAPreguntar() {
        Scanner scanner = new Scanner("abc\n\n12\n");

        OptionalInt numero = Main.leerEntero(scanner, salida, "> ");

        assertEquals(12, numero.getAsInt());
        assertTrue(textoSalida().contains("Entrada inválida"));
    }

    @Test
    @DisplayName("Si la entrada termina, la lectura devuelve vacío sin errores")
    void leerEntero_SinMasEntrada_DevuelveVacio() {
        OptionalInt numero = Main.leerEntero(new Scanner(""), salida, "> ");

        assertFalse(numero.isPresent());
    }

    @Test
    @DisplayName("El menú completo procesa una sesión y termina al elegir Salir")
    void ejecutarMenu_SesionCompleta_TerminaAlSalir() {
        Aeropuerto aeropuerto = new Aeropuerto("Prueba", new Avion(1, 100, 10, true));
        // Subir 5 al avión 1, intentar bajar 999, escribir una letra, mostrar total y salir
        Scanner scanner = new Scanner("5\n1\n5\n6\n1\n999\nx\n2\n7\n");

        Main.ejecutarMenu(aeropuerto, scanner, salida);

        String texto = textoSalida();
        assertEquals(15, aeropuerto.calcularTotalPasajeros());
        assertTrue(texto.contains("[OK] Subir pasajeros"));
        assertTrue(texto.contains(OperationResult.NOT_ENOUGH_PASSENGERS.getMessage()));
        assertTrue(texto.contains("Entrada inválida"));
        assertTrue(texto.contains("Total de pasajeros a bordo en el aeropuerto: 15"));
        assertTrue(texto.contains("Saliendo..."));
    }
}