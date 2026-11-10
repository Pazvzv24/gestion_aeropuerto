package io.github.pazvzv24.aeropuerto.persistencia;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Datos de conexión a PostgreSQL. Se leen de variables de entorno o, si no están,
 * del archivo {@code .env} de la carpeta del proyecto. La contraseña nunca se escribe
 * en el código, nunca se muestra en pantalla y el archivo {@code .env} no se sube al repositorio.
 *
 * <p>Variables: {@code ALTAMAR_DB_URL}, {@code ALTAMAR_DB_USER} y {@code ALTAMAR_DB_PASSWORD}.
 */
public final class ConfiguracionBD {

    public static final String VARIABLE_URL = "ALTAMAR_DB_URL";
    public static final String VARIABLE_USUARIO = "ALTAMAR_DB_USER";
    public static final String VARIABLE_CLAVE = "ALTAMAR_DB_PASSWORD";
    private static final String PREFIJO_URL = "jdbc:postgresql://";

    /** Largo mínimo de la contraseña de la aplicación. */
    public static final int LARGO_MINIMO_CLAVE = 12;

    private final String url;
    private final String usuario;
    private final String clave;

    private ConfiguracionBD(String url, String usuario, String clave) {
        this.url = url;
        this.usuario = usuario;
        this.clave = clave;
    }

    /**
     * Lee la configuración del sistema: primero las variables de entorno y luego el archivo .env.
     *
     * @return la configuración, o vacío si falta algún dato (la aplicación funciona en memoria)
     * @throws IllegalArgumentException si la URL no es de PostgreSQL o la contraseña es débil
     */
    public static Optional<ConfiguracionBD> desdeEntorno() {
        List<String> lineas = List.of();
        Path archivo = Path.of(".env");
        if (Files.isRegularFile(archivo)) {
            try {
                lineas = Files.readAllLines(archivo, StandardCharsets.UTF_8);
            } catch (IOException e) {
                lineas = List.of();
            }
        }
        return desde(System.getenv(), lineas);
    }

    /**
     * Arma la configuración con las variables de entorno y las líneas de un archivo .env.
     * Las variables de entorno tienen prioridad sobre el archivo.
     */
    public static Optional<ConfiguracionBD> desde(Map<String, String> entorno, List<String> lineasEnv) {
        Map<String, String> valores = new HashMap<>(leerLineas(lineasEnv));
        for (String clave : List.of(VARIABLE_URL, VARIABLE_USUARIO, VARIABLE_CLAVE)) {
            String valor = entorno.get(clave);
            if (valor != null && !valor.isBlank()) {
                valores.put(clave, valor.trim());
            }
        }
        String url = valores.get(VARIABLE_URL);
        String usuario = valores.get(VARIABLE_USUARIO);
        String clave = valores.get(VARIABLE_CLAVE);
        if (vacio(url) || vacio(usuario) || vacio(clave)) {
            return Optional.empty();
        }
        if (!url.startsWith(PREFIJO_URL)) {
            throw new IllegalArgumentException(VARIABLE_URL + " debe comenzar con " + PREFIJO_URL);
        }
        if (clave.length() < LARGO_MINIMO_CLAVE || clave.toLowerCase(Locale.ROOT).contains("cambie")) {
            throw new IllegalArgumentException(VARIABLE_CLAVE + " es débil: use al menos " + LARGO_MINIMO_CLAVE
                    + " caracteres y no deje la contraseña de ejemplo.");
        }
        return Optional.of(new ConfiguracionBD(url, usuario, clave));
    }

    /** Lee líneas CLAVE=valor; ignora líneas vacías y comentarios (#). Acepta valores entre comillas. */
    static Map<String, String> leerLineas(List<String> lineas) {
        Map<String, String> valores = new HashMap<>();
        for (String linea : lineas) {
            String texto = linea.strip();
            int igual = texto.indexOf('=');
            if (texto.isEmpty() || texto.startsWith("#") || igual <= 0) {
                continue;
            }
            String clave = texto.substring(0, igual).strip();
            String valor = texto.substring(igual + 1).strip();
            if (valor.length() >= 2 && (valor.startsWith("\"") && valor.endsWith("\"")
                    || valor.startsWith("'") && valor.endsWith("'"))) {
                valor = valor.substring(1, valor.length() - 1);
            }
            valores.put(clave, valor);
        }
        return valores;
    }

    private static boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }

    public String getUrl() {
        return url;
    }

    public String getUsuario() {
        return usuario;
    }

    /** Solo para abrir la conexión. Nunca se debe mostrar ni guardar en registros. */
    String getClave() {
        return clave;
    }

    /** No incluye la contraseña, para que no aparezca por error en pantalla o en registros. */
    @Override
    public String toString() {
        return "ConfiguracionBD[url=" + url + ", usuario=" + usuario + ", clave=****]";
    }
}