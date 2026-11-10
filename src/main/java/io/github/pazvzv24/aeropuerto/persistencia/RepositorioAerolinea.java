package io.github.pazvzv24.aeropuerto.persistencia;

import io.github.pazvzv24.aeropuerto.Aerolinea;
import java.util.Optional;

/**
 * Lugar donde se guardan los datos de la aerolínea.
 * La aplicación no sabe si detrás hay PostgreSQL u otra cosa: solo usa esta interfaz.
 */
public interface RepositorioAerolinea {

    /**
     * @return la aerolínea guardada, o vacío si todavía no hay datos (primer inicio)
     * @throws RepositorioException si no se pudo leer o los datos guardados no son coherentes
     */
    Optional<Aerolinea> cargar();

    /**
     * Guarda el estado completo de la aerolínea en una sola transacción:
     * se guarda todo o no se guarda nada.
     *
     * @throws RepositorioException si no se pudo guardar
     */
    void guardar(Aerolinea aerolinea);
}