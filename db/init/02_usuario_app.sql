-- =====================================================================
-- Altamar Airlines · Usuario de la aplicación con mínimo privilegio
-- La aplicación NO usa el superusuario "postgres": usa "altamar_app",
-- que solo puede leer y escribir los datos que necesita.
-- La contraseña se lee de la variable ALTAMAR_DB_PASSWORD (archivo .env),
-- nunca se escribe en este archivo.
-- =====================================================================

\set ON_ERROR_STOP on

\getenv clave_app ALTAMAR_DB_PASSWORD
\if :{?clave_app}
\else
    DO $$ BEGIN RAISE EXCEPTION 'Falta la variable ALTAMAR_DB_PASSWORD'; END $$;
\endif

CREATE ROLE altamar_app LOGIN PASSWORD :'clave_app'
    NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION NOBYPASSRLS
    CONNECTION LIMIT 10;

-- Nadie más que los usuarios autorizados puede conectarse a esta base
REVOKE ALL ON DATABASE :"DBNAME" FROM PUBLIC;
GRANT CONNECT ON DATABASE :"DBNAME" TO altamar_app;

-- El usuario de la aplicación no puede crear objetos en ningún esquema
REVOKE ALL ON SCHEMA public FROM PUBLIC;
GRANT USAGE ON SCHEMA altamar TO altamar_app;

-- Aviones: leer, agregar y cambiar SOLO el estado y los pasajeros a bordo (no puede borrar)
GRANT SELECT, INSERT ON altamar.aviones TO altamar_app;
GRANT UPDATE (estado, pasajeros_actuales) ON altamar.aviones TO altamar_app;

-- Vuelos: leer, agregar y cambiar SOLO el estado (no puede borrar ni cambiar la ruta)
GRANT SELECT, INSERT ON altamar.vuelos TO altamar_app;
GRANT UPDATE (estado) ON altamar.vuelos TO altamar_app;

-- Pasajeros: leer, agregar y quitar (para anular un check-in); no puede modificar un registro
GRANT SELECT, INSERT, DELETE ON altamar.pasajeros_vuelo TO altamar_app;