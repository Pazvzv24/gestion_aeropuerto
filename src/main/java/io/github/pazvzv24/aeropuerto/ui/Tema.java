package io.github.pazvzv24.aeropuerto.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import io.github.pazvzv24.aeropuerto.EstadoAvion;
import io.github.pazvzv24.aeropuerto.EstadoPasajero;
import io.github.pazvzv24.aeropuerto.EstadoVuelo;
import java.awt.Color;
import javax.swing.UIManager;

/**
 * Apariencia de la aplicación con FlatLaf (tema claro u oscuro, estilo moderno).
 * Los colores de estado se eligen según el tema activo para mantener buen contraste.
 */
public final class Tema {

    private static boolean oscuro;

    private Tema() {
    }

    /** Aplica el tema claro al iniciar (antes de crear las ventanas). */
    public static void iniciar() {
        UIManager.put("Component.arc", 10);
        UIManager.put("Button.arc", 10);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("ScrollBar.showButtons", false);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.intercellSpacing", new java.awt.Dimension(0, 1));
        FlatLightLaf.setup();
        oscuro = false;
    }

    /** Cambia entre tema claro y oscuro, actualizando las ventanas abiertas. */
    public static void alternar() {
        oscuro = !oscuro;
        if (oscuro) {
            FlatDarkLaf.setup();
        } else {
            FlatLightLaf.setup();
        }
        FlatLaf.updateUI();
    }

    public static boolean esOscuro() {
        return oscuro;
    }

    /** Verde para resultados correctos y vuelos activos. */
    public static Color exito() {
        return oscuro ? new Color(0x4ADE80) : new Color(0x15803D);
    }

    /** Rojo para errores y vuelos cancelados. */
    public static Color error() {
        return oscuro ? new Color(0xF87171) : new Color(0xB91C1C);
    }

    /** Color de acento (títulos y cifras). */
    public static Color acento() {
        return oscuro ? new Color(0x60A5FA) : new Color(0x1D4ED8);
    }

    /** Color de cada estado del avión, para la tabla y el panel de operaciones. */
    public static Color colorEstado(EstadoAvion estado) {
        switch (estado) {
            case DISPONIBLE:
                return exito();
            case EMBARCANDO:
                return acento();
            case EN_VUELO:
                return oscuro ? new Color(0xC084FC) : new Color(0x7E22CE);
            case DESEMBARCANDO:
                return oscuro ? new Color(0x2DD4BF) : new Color(0x0F766E);
            case EN_MANTENIMIENTO:
                return oscuro ? new Color(0xFBBF24) : new Color(0xB45309);
            default:
                return textoSecundario();
        }
    }

    /** Color de cada estado del vuelo. */
    public static Color colorEstado(EstadoVuelo estado) {
        switch (estado) {
            case PROGRAMADO:
                return exito();
            case RETRASADO:
                return oscuro ? new Color(0xFBBF24) : new Color(0xB45309);
            case EMBARCANDO:
                return acento();
            case EN_VUELO:
                return oscuro ? new Color(0xC084FC) : new Color(0x7E22CE);
            case CANCELADO:
                return error();
            default:
                return textoSecundario();
        }
    }

    /** Color de cada estado del pasajero dentro del vuelo. */
    public static Color colorEstado(EstadoPasajero estado) {
        switch (estado) {
            case EMBARCADO:
                return exito();
            case NO_SE_PRESENTO:
                return error();
            default:
                return acento();
        }
    }

    /** Color de texto secundario. */
    public static Color textoSecundario() {
        Color color = UIManager.getColor("Label.disabledForeground");
        return color != null ? color : Color.GRAY;
    }
}