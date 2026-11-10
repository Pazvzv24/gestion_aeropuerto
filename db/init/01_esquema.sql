-- =====================================================================
-- Altamar Airlines · Esquema de la base de datos
-- Docker lo ejecuta solo la primera vez, cuando el volumen está vacío.
-- Todos los datos que se guardan son ficticios.
-- =====================================================================

\set ON_ERROR_STOP on

-- Esquema propio: las tablas no quedan en "public"
CREATE SCHEMA altamar;

-- Aviones de la flota (los dados de baja se conservan como historial)
CREATE TABLE altamar.aviones (
    matricula          VARCHAR(6)  PRIMARY KEY
                       CHECK (matricula ~ '^CC-[A-Z]{3}$'),
    modelo             VARCHAR(12) NOT NULL
                       CHECK (modelo IN ('A319', 'A320', 'A320NEO', 'A321', 'A321NEO',
                                         'B767_300ER', 'B787_8', 'B787_9')),
    estado             VARCHAR(17) NOT NULL
                       CHECK (estado IN ('DISPONIBLE', 'EMBARCANDO', 'EN_VUELO', 'DESEMBARCANDO',
                                         'EN_MANTENIMIENTO', 'FUERA_DE_SERVICIO')),
    pasajeros_actuales INTEGER     NOT NULL DEFAULT 0
                       CHECK (pasajeros_actuales BETWEEN 0 AND 400)
);

-- Vuelos (los aterrizados y cancelados se conservan como historial)
CREATE TABLE altamar.vuelos (
    codigo    VARCHAR(6)  PRIMARY KEY
              CHECK (codigo ~ '^AM[0-9]{3,4}$'),
    origen    CHAR(3)     NOT NULL CHECK (origen ~ '^[A-Z]{3}$'),
    destino   CHAR(3)     NOT NULL CHECK (destino ~ '^[A-Z]{3}$'),
    salida    TIMESTAMP   NOT NULL,
    matricula VARCHAR(6)  NOT NULL REFERENCES altamar.aviones (matricula),
    estado    VARCHAR(10) NOT NULL
              CHECK (estado IN ('PROGRAMADO', 'RETRASADO', 'EMBARCANDO', 'EN_VUELO',
                                'ATERRIZADO', 'CANCELADO')),
    CONSTRAINT origen_distinto_de_destino CHECK (origen <> destino)
);

-- Regla del negocio también en la base: cada avión tiene a lo más un vuelo pendiente
CREATE UNIQUE INDEX un_vuelo_pendiente_por_avion
    ON altamar.vuelos (matricula)
    WHERE estado NOT IN ('ATERRIZADO', 'CANCELADO');

-- Pasajeros de cada vuelo (lista de pasajeros o manifiesto)
CREATE TABLE altamar.pasajeros_vuelo (
    id             BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo_vuelo   VARCHAR(6)   NOT NULL REFERENCES altamar.vuelos (codigo),
    orden          INTEGER      NOT NULL CHECK (orden > 0),
    nombre         VARCHAR(100) NOT NULL CHECK (length(trim(nombre)) > 0),
    tipo_documento VARCHAR(9)   NOT NULL CHECK (tipo_documento IN ('RUT', 'PASAPORTE')),
    documento      VARCHAR(12)  NOT NULL,
    estado         VARCHAR(14)  NOT NULL
                   CHECK (estado IN ('CHECK_IN', 'EMBARCADO', 'NO_SE_PRESENTO')),
    asiento        VARCHAR(3)   NOT NULL CHECK (asiento ~ '^[1-9][0-9]?[A-HJK]$'),
    -- Un documento no puede repetirse en el mismo vuelo
    CONSTRAINT documento_unico_por_vuelo UNIQUE (codigo_vuelo, tipo_documento, documento),
    -- Dos pasajeros no pueden tener el mismo asiento en el mismo vuelo
    CONSTRAINT asiento_unico_por_vuelo UNIQUE (codigo_vuelo, asiento),
    CONSTRAINT orden_unico_por_vuelo UNIQUE (codigo_vuelo, orden)
);

COMMENT ON COLUMN altamar.pasajeros_vuelo.nombre IS 'Dato personal (Ley 21.719): solo para el check-in.';
COMMENT ON COLUMN altamar.pasajeros_vuelo.documento IS 'Dato personal (Ley 21.719): la aplicación lo muestra enmascarado.';