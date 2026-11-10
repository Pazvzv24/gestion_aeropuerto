package io.github.pazvzv24.aeropuerto;

import java.util.Locale;

/**
 * Pasajero con nombre y documento de identidad (RUT chileno o pasaporte).
 * El documento se valida al crearlo y, para mostrarlo en pantalla, se entrega
 * enmascarado (minimización de datos personales, Ley 21.719).
 */
public class Pasajero {

    private final String nombre;
    private final TipoDocumento tipoDocumento;
    private final String documento;

    /**
     * @throws IllegalArgumentException si el nombre está vacío o el documento no es válido
     */
    public Pasajero(String nombre, TipoDocumento tipoDocumento, String documento) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del pasajero es obligatorio.");
        }
        if (tipoDocumento == null) {
            throw new IllegalArgumentException("El tipo de documento es obligatorio.");
        }
        if (!esDocumentoValido(tipoDocumento, documento)) {
            throw new IllegalArgumentException(tipoDocumento == TipoDocumento.RUT
                    ? "RUT inválido: revise el dígito verificador."
                    : "Pasaporte inválido: use de 6 a 9 letras o números.");
        }
        this.nombre = nombre.trim().replaceAll("\\s+", " ");
        this.tipoDocumento = tipoDocumento;
        this.documento = normalizarDocumento(tipoDocumento, documento);
    }

    /** RUT: módulo 11. Pasaporte: de 6 a 9 letras o números. */
    public static boolean esDocumentoValido(TipoDocumento tipo, String documento) {
        if (tipo == null || documento == null) {
            return false;
        }
        if (tipo == TipoDocumento.RUT) {
            return Rut.esValido(documento);
        }
        return normalizarDocumento(tipo, documento).matches("[A-Z0-9]{6,9}");
    }

    /** Deja el documento en un formato único para poder compararlo: el RUT con puntos y guion. */
    public static String normalizarDocumento(TipoDocumento tipo, String documento) {
        if (tipo == TipoDocumento.RUT) {
            return Rut.esValido(documento) ? Rut.formatear(documento) : Rut.normalizar(documento);
        }
        return documento == null ? "" : documento.trim().toUpperCase(Locale.ROOT);
    }

    public String getNombre() {
        return nombre;
    }

    public TipoDocumento getTipoDocumento() {
        return tipoDocumento;
    }

    /** Documento completo. Úselo solo cuando sea necesario: para mostrar, prefiera el enmascarado. */
    public String getDocumento() {
        return documento;
    }

    /** RUT "12.345.***-5" o pasaporte "*****123". */
    public String getDocumentoEnmascarado() {
        if (tipoDocumento == TipoDocumento.RUT) {
            return Rut.enmascarar(documento);
        }
        int visibles = 3;
        return "*".repeat(documento.length() - visibles) + documento.substring(documento.length() - visibles);
    }

    /** Dos pasajeros son el mismo si tienen el mismo tipo y número de documento. */
    public boolean mismoDocumento(TipoDocumento tipo, String otroDocumento) {
        return tipoDocumento == tipo && documento.equals(normalizarDocumento(tipo, otroDocumento));
    }

    /** No incluye el documento completo, para que no aparezca por error en registros o mensajes. */
    @Override
    public String toString() {
        return nombre + " (" + tipoDocumento + " " + getDocumentoEnmascarado() + ")";
    }
}