package io.github.pazvzv24.aeropuerto.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConfiguracionBDTest {

    private static final String CLAVE_SEGURA = "Xk29fLq81mZp";

    private static List<String> archivoEnv(String clave) {
        return List.of(
                "# Comentario que se ignora",
                "",
                "ALTAMAR_DB_URL=jdbc:postgresql://127.0.0.1:5433/altamar",
                "ALTAMAR_DB_USER = altamar_app",
                "ALTAMAR_DB_PASSWORD=\"" + clave + "\"\r");
    }

    @Test
    @DisplayName("Lee el archivo .env ignorando comentarios, espacios, comillas y saltos de línea de Windows")
    void desde_LeeElArchivoEnv() {
        ConfiguracionBD configuracion = ConfiguracionBD.desde(Map.of(), archivoEnv(CLAVE_SEGURA)).orElseThrow();

        assertEquals("jdbc:postgresql://127.0.0.1:5433/altamar", configuracion.getUrl());
        assertEquals("altamar_app", configuracion.getUsuario());
        assertEquals(CLAVE_SEGURA, configuracion.getClave());
    }

    @Test
    @DisplayName("Las variables de entorno tienen prioridad sobre el archivo .env")
    void desde_VariablesDeEntornoTienenPrioridad() {
        ConfiguracionBD configuracion = ConfiguracionBD.desde(
                Map.of(ConfiguracionBD.VARIABLE_USUARIO, "otro_usuario"), archivoEnv(CLAVE_SEGURA)).orElseThrow();

        assertEquals("otro_usuario", configuracion.getUsuario());
    }

    @Test
    @DisplayName("Sin datos de conexión, la aplicación funciona en memoria")
    void desde_SinDatos_DevuelveVacio() {
        assertFalse(ConfiguracionBD.desde(Map.of(), List.of()).isPresent());
    }

    @Test
    @DisplayName("Se rechazan las contraseñas cortas y la contraseña de ejemplo")
    void desde_ConClaveDebil_LanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> ConfiguracionBD.desde(Map.of(), archivoEnv("1234")));
        assertThrows(IllegalArgumentException.class,
                () -> ConfiguracionBD.desde(Map.of(), archivoEnv("cambie-esta-otra-clave")));
    }

    @Test
    @DisplayName("Solo se aceptan URL de PostgreSQL")
    void desde_ConUrlDeOtraBase_LanzaExcepcion() {
        List<String> lineas = List.of("ALTAMAR_DB_URL=jdbc:mysql://127.0.0.1/altamar",
                "ALTAMAR_DB_USER=altamar_app", "ALTAMAR_DB_PASSWORD=" + CLAVE_SEGURA);

        assertThrows(IllegalArgumentException.class, () -> ConfiguracionBD.desde(Map.of(), lineas));
    }

    @Test
    @DisplayName("La contraseña nunca aparece al mostrar la configuración")
    void toString_NoMuestraLaClave() {
        ConfiguracionBD configuracion = ConfiguracionBD.desde(Map.of(), archivoEnv(CLAVE_SEGURA)).orElseThrow();

        assertFalse(configuracion.toString().contains(CLAVE_SEGURA));
        assertTrue(configuracion.toString().contains("****"));
    }
}