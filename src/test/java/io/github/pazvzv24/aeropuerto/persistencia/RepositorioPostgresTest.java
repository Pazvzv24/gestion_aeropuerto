package io.github.pazvzv24.aeropuerto.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.pazvzv24.aeropuerto.Aerolinea;
import io.github.pazvzv24.aeropuerto.CheckIn;
import io.github.pazvzv24.aeropuerto.EstadoAvion;
import io.github.pazvzv24.aeropuerto.EstadoPasajero;
import io.github.pazvzv24.aeropuerto.EstadoVuelo;
import io.github.pazvzv24.aeropuerto.OperationResult;
import io.github.pazvzv24.aeropuerto.TipoDocumento;
import io.github.pazvzv24.aeropuerto.Vuelo;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/**
 * Pruebas con una base de datos PostgreSQL real. Solo se ejecutan en GitHub Actions
 * (variable CI=true), donde se levanta una base temporal con Docker Compose;
 * en el computador de desarrollo se omiten para no tocar los datos locales.
 */
@EnabledIfEnvironmentVariable(named = "CI", matches = "true")
class RepositorioPostgresTest {

    private static final String NOMBRE = "Altamar Airlines";

    private static RepositorioPostgres repositorio() {
        ConfiguracionBD configuracion = ConfiguracionBD.desdeEntorno()
                .orElseThrow(() -> new IllegalStateException("Falta la configuración de la base de datos de prueba"));
        return new RepositorioPostgres(configuracion, NOMBRE);
    }

    private static Aerolinea cargarOCrear(RepositorioPostgres repositorio) {
        return repositorio.cargar().orElseGet(() -> {
            Aerolinea demostracion = Aerolinea.altamarAirlines();
            repositorio.guardar(demostracion);
            return demostracion;
        });
    }

    @Test
    @DisplayName("Lo que se guarda en PostgreSQL se vuelve a cargar igual")
    void guardarYCargar_ConservaLosDatos() {
        RepositorioPostgres repositorio = repositorio();
        repositorio.guardar(Aerolinea.altamarAirlines());

        Aerolinea cargada = repositorio.cargar().orElseThrow();

        assertEquals(15, cargada.getFlota().size());
        assertEquals(5, cargada.getVuelos().size());
        assertEquals(EstadoVuelo.EMBARCANDO, cargada.buscarVuelo("AM205").getEstado());
        assertEquals(EstadoAvion.EMBARCANDO, cargada.buscarAvion("CC-AMD").getEstado());
        assertEquals(2, cargada.buscarVuelo("AM205").contarPasajeros(EstadoPasajero.EMBARCADO));
        assertEquals(4, cargada.buscarVuelo("AM101").getCantidadPasajeros());
        assertEquals("3A", cargada.buscarVuelo("AM101").buscarPasajero(TipoDocumento.RUT, "11.111.111-1").getAsiento());
    }

    @Test
    @DisplayName("Un nombre con código SQL se guarda como texto y no se ejecuta (inyección SQL)")
    void guardar_NombreConSql_SeGuardaComoTexto() {
        RepositorioPostgres repositorio = repositorio();
        Aerolinea aerolinea = cargarOCrear(repositorio);
        String nombre = "Robert'); DROP TABLE altamar.aviones;--";
        String pasaporte = "SQ" + ThreadLocalRandom.current().nextInt(1_000_000, 9_999_999);
        assertEquals(OperationResult.SUCCESS,
                aerolinea.hacerCheckIn("AM101", nombre, TipoDocumento.PASAPORTE, pasaporte));

        repositorio.guardar(aerolinea);
        Aerolinea cargada = repositorio.cargar().orElseThrow();

        Vuelo vuelo = cargada.buscarVuelo("AM101");
        CheckIn registro = vuelo.buscarPasajero(TipoDocumento.PASAPORTE, pasaporte);
        assertNotNull(registro);
        assertEquals(nombre, registro.getPasajero().getNombre());
        assertEquals(15, cargada.getFlota().size());
    }

    @Test
    @DisplayName("El usuario de la aplicación no puede borrar aviones ni tablas (mínimo privilegio)")
    void usuarioDeLaAplicacion_TienePrivilegiosMinimos() throws SQLException {
        RepositorioPostgres repositorio = repositorio();
        cargarOCrear(repositorio);

        try (Connection conexion = repositorio.conectar(); Statement sentencia = conexion.createStatement()) {
            SQLException borrar = assertThrows(SQLException.class,
                    () -> sentencia.executeUpdate("DELETE FROM altamar.aviones"));
            assertEquals("42501", borrar.getSQLState());
            assertThrows(SQLException.class, () -> sentencia.execute("DROP TABLE altamar.vuelos"));
            assertThrows(SQLException.class, () -> sentencia.execute("CREATE TABLE altamar.prueba (id INT)"));
        }
    }
}