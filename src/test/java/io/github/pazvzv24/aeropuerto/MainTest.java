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
    @DisplayName("Si el usuario escribe letras donde va un número, se le vuelve a preguntar")
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
    @DisplayName("El argumento --consola abre el menú de texto en vez de la ventana")
    void usarConsola_ConArgumentoConsola_EsVerdadero() {
        assertTrue(Main.usarConsola(new String[] {"--consola"}));
    }

    @Test
    @DisplayName("El menú completo procesa una sesión: embarque, error de capacidad, despegue y salida")
    void ejecutarMenu_SesionCompleta_TerminaAlSalir() {
        Aerolinea aerolinea = new Aerolinea("Prueba", new Avion("CC-AAA", ModeloAvion.A319));
        // 6: cambiar estado de CC-AAA -> 1 (Iniciar embarque)
        // 4: subir 100 a cc-aaa | 4: subir 999 (no caben) | x: entrada inválida
        // 6: cambiar estado -> 1 (Despegar) | 2: resumen | 8: salir
        String entrada = String.join("\n",
                "6", "CC-AAA", "1",
                "4", "cc-aaa", "100",
                "4", "CC-AAA", "999",
                "x",
                "6", "CC-AAA", "1",
                "2",
                "8") + "\n";

        Main.ejecutarMenu(aerolinea, new Scanner(entrada), salida);

        String texto = textoSalida();
        assertEquals(EstadoAvion.EN_VUELO, aerolinea.buscarAvion("CC-AAA").getEstado());
        assertEquals(100, aerolinea.calcularTotalPasajeros());
        assertTrue(texto.contains("[OK] Iniciar embarque"));
        assertTrue(texto.contains("[OK] Subir pasajeros"));
        assertTrue(texto.contains(OperationResult.CAPACITY_EXCEEDED.getMessage()));
        assertTrue(texto.contains("Entrada inválida"));
        assertTrue(texto.contains("[OK] Despegar"));
        assertTrue(texto.contains("Pasajeros a bordo: 100"));
        assertTrue(texto.contains("Saliendo..."));
    }
}