package io.github.pazvzv24.aeropuerto;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Modelos de avión de la flota, con su capacidad de pasajeros y su distribución de asientos.
 * Son modelos que operan comúnmente en Chile; la capacidad es referencial
 * (corresponde a una configuración típica y varía según cada aerolínea).
 *
 * <p>Los asientos se numeran por fila y letra (12C). Los aviones de pasillo único usan
 * 6 asientos por fila (ABC DEF), el 767 usa 7 (AB CDE FG) y el 787 usa 9 (ABC DEF GHJ,
 * sin la I para no confundirla con el 1). Si la capacidad no completa la última fila,
 * esa fila queda parcial.
 */
public enum ModeloAvion {
    A319("Airbus", "A319", 144, "ABC DEF"),
    A320("Airbus", "A320", 174, "ABC DEF"),
    A320NEO("Airbus", "A320neo", 186, "ABC DEF"),
    A321("Airbus", "A321", 220, "ABC DEF"),
    A321NEO("Airbus", "A321neo", 238, "ABC DEF"),
    B767_300ER("Boeing", "767-300ER", 238, "AB CDE FG"),
    B787_8("Boeing", "787-8", 247, "ABC DEF GHJ"),
    B787_9("Boeing", "787-9", 300, "ABC DEF GHJ");

    /** Fila y letra, por ejemplo 12C. */
    private static final Pattern FORMATO_ASIENTO = Pattern.compile("([1-9][0-9]?)([A-HJK])");

    private final String fabricante;
    private final String nombre;
    private final int capacidad;
    /** Letras de una fila; los espacios marcan los pasillos. */
    private final String distribucion;
    private final String letras;

    ModeloAvion(String fabricante, String nombre, int capacidad, String distribucion) {
        this.fabricante = fabricante;
        this.nombre = nombre;
        this.capacidad = capacidad;
        this.distribucion = distribucion;
        this.letras = distribucion.replace(" ", "");
    }

    public String getFabricante() {
        return fabricante;
    }

    public String getNombre() {
        return nombre;
    }

    /** Cantidad de asientos de pasajeros. */
    public int getCapacidad() {
        return capacidad;
    }

    /** Letras de una fila con espacios en los pasillos, por ejemplo "ABC DEF". */
    public String getDistribucion() {
        return distribucion;
    }

    public int getAsientosPorFila() {
        return letras.length();
    }

    public int getFilas() {
        return (capacidad + letras.length() - 1) / letras.length();
    }

    /** Todos los asientos del avión en orden: 1A, 1B, ... */
    public List<String> getAsientos() {
        List<String> asientos = new ArrayList<>(capacidad);
        for (int i = 0; i < capacidad; i++) {
            asientos.add((i / letras.length() + 1) + String.valueOf(letras.charAt(i % letras.length())));
        }
        return asientos;
    }

    /** true si el asiento existe en este modelo (acepta minúsculas y espacios: " 12c "). */
    public boolean esAsientoValido(String asiento) {
        Matcher partes = FORMATO_ASIENTO.matcher(normalizarAsiento(asiento));
        if (!partes.matches()) {
            return false;
        }
        int fila = Integer.parseInt(partes.group(1));
        int columna = letras.indexOf(partes.group(2).charAt(0));
        return columna >= 0 && (fila - 1) * letras.length() + columna < capacidad;
    }

    public static String normalizarAsiento(String asiento) {
        return asiento == null ? "" : asiento.trim().toUpperCase(Locale.ROOT);
    }

    /** Ejemplo: "Airbus A320". */
    @Override
    public String toString() {
        return fabricante + " " + nombre;
    }
}