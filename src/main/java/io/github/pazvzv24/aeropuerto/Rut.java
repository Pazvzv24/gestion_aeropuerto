package io.github.pazvzv24.aeropuerto;

import java.util.Locale;

/**
 * Validación, formato y enmascaramiento del RUT chileno.
 * El dígito verificador se calcula con el algoritmo módulo 11.
 */
public final class Rut {

    private Rut() {
    }

    /**
     * Deja el RUT sin puntos ni guion, en mayúsculas: "12.345.678-5" queda "123456785".
     * Devuelve una cadena vacía si es nulo.
     */
    public static String normalizar(String rut) {
        if (rut == null) {
            return "";
        }
        return rut.replace(".", "").replace("-", "").replace(" ", "").trim().toUpperCase(Locale.ROOT);
    }

    /** @return true si el RUT tiene entre 7 y 8 dígitos y su dígito verificador es correcto. */
    public static boolean esValido(String rut) {
        String limpio = normalizar(rut);
        if (!limpio.matches("\\d{7,8}[0-9K]")) {
            return false;
        }
        String cuerpo = limpio.substring(0, limpio.length() - 1);
        char verificador = limpio.charAt(limpio.length() - 1);
        return calcularDigitoVerificador(cuerpo) == verificador;
    }

    /** Calcula el dígito verificador (0-9 o K) de un cuerpo numérico con módulo 11. */
    public static char calcularDigitoVerificador(String cuerpo) {
        int suma = 0;
        int multiplicador = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(cuerpo.charAt(i)) * multiplicador;
            multiplicador = multiplicador == 7 ? 2 : multiplicador + 1;
        }
        int resultado = 11 - (suma % 11);
        if (resultado == 11) {
            return '0';
        }
        if (resultado == 10) {
            return 'K';
        }
        return (char) ('0' + resultado);
    }

    /**
     * Formato con puntos y guion: "123456785" queda "12.345.678-5".
     *
     * @throws IllegalArgumentException si el RUT no es válido
     */
    public static String formatear(String rut) {
        if (!esValido(rut)) {
            throw new IllegalArgumentException("RUT inválido.");
        }
        String limpio = normalizar(rut);
        String cuerpo = limpio.substring(0, limpio.length() - 1);
        char verificador = limpio.charAt(limpio.length() - 1);
        StringBuilder conPuntos = new StringBuilder();
        int contador = 0;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            conPuntos.insert(0, cuerpo.charAt(i));
            contador++;
            if (contador % 3 == 0 && i > 0) {
                conPuntos.insert(0, '.');
            }
        }
        return conPuntos + "-" + verificador;
    }

    /**
     * Oculta los últimos 3 dígitos del cuerpo (minimización de datos personales):
     * "12.345.678-5" se muestra como "12.345.***-5".
     */
    public static String enmascarar(String rut) {
        String formateado = formatear(rut);
        int guion = formateado.indexOf('-');
        return formateado.substring(0, guion - 3) + "***" + formateado.substring(guion);
    }
}