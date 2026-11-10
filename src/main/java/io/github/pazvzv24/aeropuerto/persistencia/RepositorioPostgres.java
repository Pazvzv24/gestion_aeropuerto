package io.github.pazvzv24.aeropuerto.persistencia;

import io.github.pazvzv24.aeropuerto.Aerolinea;
import io.github.pazvzv24.aeropuerto.Avion;
import io.github.pazvzv24.aeropuerto.CheckIn;
import io.github.pazvzv24.aeropuerto.Destino;
import io.github.pazvzv24.aeropuerto.EstadoAvion;
import io.github.pazvzv24.aeropuerto.EstadoPasajero;
import io.github.pazvzv24.aeropuerto.EstadoVuelo;
import io.github.pazvzv24.aeropuerto.ModeloAvion;
import io.github.pazvzv24.aeropuerto.Pasajero;
import io.github.pazvzv24.aeropuerto.TipoDocumento;
import io.github.pazvzv24.aeropuerto.Vuelo;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * Guarda la aerolínea en PostgreSQL.
 *
 * <p>Seguridad:
 * <ul>
 *   <li>Todas las consultas usan {@link PreparedStatement}: los datos que escribe el usuario
 *       (nombres, documentos) nunca se pegan dentro del SQL, así no hay inyección SQL.</li>
 *   <li>Se conecta con un usuario de mínimo privilegio ({@code altamar_app}), que no puede
 *       crear ni borrar tablas, ni borrar aviones o vuelos.</li>
 *   <li>Cada guardado es una transacción: se guarda todo o nada.</li>
 *   <li>Al cargar, los datos se validan con las mismas reglas del modelo.</li>
 * </ul>
 */
public final class RepositorioPostgres implements RepositorioAerolinea {

    private static final int SEGUNDOS_ESPERA_CONEXION = 5;

    private static final String SELECT_AVIONES =
            "SELECT matricula, modelo, estado, pasajeros_actuales FROM altamar.aviones ORDER BY matricula";
    private static final String SELECT_VUELOS =
            "SELECT codigo, origen, destino, salida, matricula, estado FROM altamar.vuelos ORDER BY salida, codigo";
    private static final String SELECT_PASAJEROS =
            "SELECT codigo_vuelo, nombre, tipo_documento, documento, estado, asiento"
            + " FROM altamar.pasajeros_vuelo ORDER BY codigo_vuelo, orden";

    private static final String GUARDAR_AVION =
            "INSERT INTO altamar.aviones (matricula, modelo, estado, pasajeros_actuales) VALUES (?, ?, ?, ?)"
            + " ON CONFLICT (matricula) DO UPDATE"
            + " SET estado = EXCLUDED.estado, pasajeros_actuales = EXCLUDED.pasajeros_actuales";
    private static final String GUARDAR_VUELO =
            "INSERT INTO altamar.vuelos (codigo, origen, destino, salida, matricula, estado) VALUES (?, ?, ?, ?, ?, ?)"
            + " ON CONFLICT (codigo) DO UPDATE SET estado = EXCLUDED.estado";
    private static final String BORRAR_PASAJEROS =
            "DELETE FROM altamar.pasajeros_vuelo WHERE codigo_vuelo = ?";
    private static final String GUARDAR_PASAJERO =
            "INSERT INTO altamar.pasajeros_vuelo (codigo_vuelo, orden, nombre, tipo_documento, documento, estado, asiento)"
            + " VALUES (?, ?, ?, ?, ?, ?, ?)";

    private final ConfiguracionBD configuracion;
    private final String nombreAerolinea;

    public RepositorioPostgres(ConfiguracionBD configuracion, String nombreAerolinea) {
        this.configuracion = configuracion;
        this.nombreAerolinea = nombreAerolinea;
    }

    /** Abre una conexión nueva; la contraseña se entrega aparte, nunca dentro de la URL. */
    Connection conectar() throws SQLException {
        Properties propiedades = new Properties();
        propiedades.setProperty("user", configuracion.getUsuario());
        propiedades.setProperty("password", configuracion.getClave());
        propiedades.setProperty("connectTimeout", String.valueOf(SEGUNDOS_ESPERA_CONEXION));
        propiedades.setProperty("ApplicationName", "Altamar Airlines");
        return DriverManager.getConnection(configuracion.getUrl(), propiedades);
    }

    @Override
    public Optional<Aerolinea> cargar() {
        try (Connection conexion = conectar()) {
            Map<String, Avion> aviones = leerAviones(conexion);
            if (aviones.isEmpty()) {
                return Optional.empty();
            }
            Map<String, List<CheckIn>> pasajeros = leerPasajeros(conexion);
            List<Vuelo> vuelos = leerVuelos(conexion, aviones, pasajeros);
            return Optional.of(Aerolinea.desdeDatos(nombreAerolinea, new ArrayList<>(aviones.values()), vuelos));
        } catch (SQLException e) {
            throw new RepositorioException("No se pudo leer la base de datos. Revise que Docker esté encendido"
                    + " (docker compose up -d) y los datos del archivo .env.", e);
        } catch (IllegalArgumentException e) {
            throw new RepositorioException("Los datos guardados no son válidos: " + e.getMessage(), e);
        }
    }

    private static Map<String, Avion> leerAviones(Connection conexion) throws SQLException {
        Map<String, Avion> aviones = new LinkedHashMap<>();
        try (PreparedStatement consulta = conexion.prepareStatement(SELECT_AVIONES);
             ResultSet fila = consulta.executeQuery()) {
            while (fila.next()) {
                Avion avion = new Avion(fila.getString("matricula"),
                        ModeloAvion.valueOf(fila.getString("modelo")),
                        EstadoAvion.valueOf(fila.getString("estado")),
                        fila.getInt("pasajeros_actuales"));
                aviones.put(avion.getMatricula(), avion);
            }
        }
        return aviones;
    }

    private static Map<String, List<CheckIn>> leerPasajeros(Connection conexion) throws SQLException {
        Map<String, List<CheckIn>> pasajeros = new HashMap<>();
        try (PreparedStatement consulta = conexion.prepareStatement(SELECT_PASAJEROS);
             ResultSet fila = consulta.executeQuery()) {
            while (fila.next()) {
                Pasajero pasajero = new Pasajero(fila.getString("nombre"),
                        TipoDocumento.valueOf(fila.getString("tipo_documento")),
                        fila.getString("documento"));
                pasajeros.computeIfAbsent(fila.getString("codigo_vuelo"), codigo -> new ArrayList<>())
                        .add(new CheckIn(pasajero, EstadoPasajero.valueOf(fila.getString("estado")),
                                fila.getString("asiento")));
            }
        }
        return pasajeros;
    }

    private static List<Vuelo> leerVuelos(Connection conexion, Map<String, Avion> aviones,
                                          Map<String, List<CheckIn>> pasajeros) throws SQLException {
        List<Vuelo> vuelos = new ArrayList<>();
        try (PreparedStatement consulta = conexion.prepareStatement(SELECT_VUELOS);
             ResultSet fila = consulta.executeQuery()) {
            while (fila.next()) {
                String codigo = fila.getString("codigo");
                vuelos.add(Vuelo.restaurar(codigo,
                        Destino.valueOf(fila.getString("origen")),
                        Destino.valueOf(fila.getString("destino")),
                        fila.getObject("salida", LocalDateTime.class),
                        aviones.get(fila.getString("matricula")),
                        EstadoVuelo.valueOf(fila.getString("estado")),
                        pasajeros.getOrDefault(codigo, List.of())));
            }
        }
        return vuelos;
    }

    @Override
    public void guardar(Aerolinea aerolinea) {
        try (Connection conexion = conectar()) {
            conexion.setAutoCommit(false);
            try {
                guardarAviones(conexion, aerolinea.getFlota());
                guardarVuelos(conexion, aerolinea.getVuelos());
                guardarPasajeros(conexion, aerolinea.getVuelos());
                conexion.commit();
            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RepositorioException("No se pudieron guardar los cambios en la base de datos."
                    + " Revise que Docker esté encendido.", e);
        }
    }

    private static void guardarAviones(Connection conexion, List<Avion> aviones) throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(GUARDAR_AVION)) {
            for (Avion avion : aviones) {
                sentencia.setString(1, avion.getMatricula());
                sentencia.setString(2, avion.getModelo().name());
                sentencia.setString(3, avion.getEstado().name());
                sentencia.setInt(4, avion.getPasajerosActuales());
                sentencia.addBatch();
            }
            sentencia.executeBatch();
        }
    }

    private static void guardarVuelos(Connection conexion, List<Vuelo> vuelos) throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(GUARDAR_VUELO)) {
            for (Vuelo vuelo : vuelos) {
                sentencia.setString(1, vuelo.getCodigo());
                sentencia.setString(2, vuelo.getOrigen().name());
                sentencia.setString(3, vuelo.getDestino().name());
                sentencia.setObject(4, vuelo.getSalida());
                sentencia.setString(5, vuelo.getAvion().getMatricula());
                sentencia.setString(6, vuelo.getEstado().name());
                sentencia.addBatch();
            }
            sentencia.executeBatch();
        }
    }

    /** Reemplaza la lista de pasajeros de cada vuelo (así quedan también las anulaciones de check-in). */
    private static void guardarPasajeros(Connection conexion, List<Vuelo> vuelos) throws SQLException {
        try (PreparedStatement borrar = conexion.prepareStatement(BORRAR_PASAJEROS);
             PreparedStatement insertar = conexion.prepareStatement(GUARDAR_PASAJERO)) {
            for (Vuelo vuelo : vuelos) {
                borrar.setString(1, vuelo.getCodigo());
                borrar.addBatch();
                int orden = 0;
                for (CheckIn registro : vuelo.getManifiesto()) {
                    Pasajero pasajero = registro.getPasajero();
                    insertar.setString(1, vuelo.getCodigo());
                    insertar.setInt(2, ++orden);
                    insertar.setString(3, pasajero.getNombre());
                    insertar.setString(4, pasajero.getTipoDocumento().name());
                    insertar.setString(5, pasajero.getDocumento());
                    insertar.setString(6, registro.getEstado().name());
                    insertar.setString(7, registro.getAsiento());
                    insertar.addBatch();
                }
            }
            borrar.executeBatch();
            insertar.executeBatch();
        }
    }
}