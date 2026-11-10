package io.github.pazvzv24.aeropuerto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Pase de abordar de un pasajero. Muestra el documento enmascarado, nunca el número completo,
 * porque el pase se imprime o se muestra en pantalla y puede quedar a la vista de otras personas.
 */
public final class PaseAbordar {

    /** El embarque comienza 40 minutos antes de la salida. */
    public static final int MINUTOS_ANTES_DEL_EMBARQUE = 40;
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd MMM uuuu", Locale.forLanguageTag("es-CL"));
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final String aerolinea;
    private final String nombrePasajero;
    private final String documento;
    private final String vuelo;
    private final Destino origen;
    private final Destino destino;
    private final LocalDateTime salida;
    private final String asiento;
    private final int secuencia;

    PaseAbordar(String aerolinea, Vuelo vuelo, CheckIn registro) {
        Pasajero pasajero = registro.getPasajero();
        this.aerolinea = aerolinea;
        this.nombrePasajero = pasajero.getNombre();
        this.documento = pasajero.getTipoDocumento() + " " + pasajero.getDocumentoEnmascarado();
        this.vuelo = vuelo.getCodigo();
        this.origen = vuelo.getOrigen();
        this.destino = vuelo.getDestino();
        this.salida = vuelo.getSalida();
        this.asiento = registro.getAsiento();
        this.secuencia = vuelo.getManifiesto().indexOf(registro) + 1;
    }

    public String getAerolinea() {
        return aerolinea;
    }

    public String getNombrePasajero() {
        return nombrePasajero;
    }

    /** Documento enmascarado, por ejemplo "RUT 12.345.***-5". */
    public String getDocumento() {
        return documento;
    }

    public String getVuelo() {
        return vuelo;
    }

    public Destino getOrigen() {
        return origen;
    }

    public Destino getDestino() {
        return destino;
    }

    public LocalDateTime getSalida() {
        return salida;
    }

    public LocalDateTime getHoraEmbarque() {
        return salida.minusMinutes(MINUTOS_ANTES_DEL_EMBARQUE);
    }

    public String getAsiento() {
        return asiento;
    }

    /** Número de orden del pasajero en el vuelo, por ejemplo "004". */
    public String getSecuencia() {
        return String.format("%03d", secuencia);
    }

    /** Grupo de embarque según la fila: las filas de atrás embarcan primero. */
    public int getGrupoEmbarque() {
        int fila = Integer.parseInt(asiento.replaceAll("[^0-9]", ""));
        return fila >= 20 ? 1 : fila >= 10 ? 2 : 3;
    }

    public String getFecha() {
        return FORMATO_FECHA.format(salida);
    }

    public String getHoraSalidaTexto() {
        return FORMATO_HORA.format(salida);
    }

    public String getHoraEmbarqueTexto() {
        return FORMATO_HORA.format(getHoraEmbarque());
    }

    /** Pase en texto, para la consola. */
    public String aTexto() {
        String linea = "+" + "-".repeat(50) + "+";
        return String.join("\n",
                linea,
                fila(aerolinea.toUpperCase(Locale.ROOT) + " · PASE DE ABORDAR"),
                linea,
                fila("Pasajero:   " + nombrePasajero),
                fila("Documento:  " + documento),
                fila("Vuelo:      " + vuelo + "   Fecha: " + getFecha()),
                fila("Desde:      " + origen),
                fila("Hacia:      " + destino),
                fila("Embarque:   " + getHoraEmbarqueTexto() + "   Salida: " + getHoraSalidaTexto()),
                fila("Asiento:    " + asiento + "   Grupo: " + getGrupoEmbarque() + "   Sec.: " + getSecuencia()),
                linea);
    }

    private static String fila(String texto) {
        String recortado = texto.length() > 48 ? texto.substring(0, 48) : texto;
        return "| " + recortado + " ".repeat(48 - recortado.length()) + " |";
    }

    @Override
    public String toString() {
        return "Pase " + vuelo + " · " + nombrePasajero + " · " + asiento;
    }
}