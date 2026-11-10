package io.github.pazvzv24.aeropuerto.ui;

import io.github.pazvzv24.aeropuerto.ModeloAvion;
import io.github.pazvzv24.aeropuerto.Vuelo;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Optional;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

/**
 * Mapa de asientos de un vuelo para elegir uno. Los asientos ocupados se ven deshabilitados,
 * los pasillos quedan como espacios y cada fila lleva su número a la izquierda.
 */
public final class MapaAsientos extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final int TAMANO_ASIENTO = 38;

    private final ButtonGroup grupo = new ButtonGroup();
    private String seleccionado;

    private MapaAsientos(ModeloAvion modelo, Set<String> ocupados, String actual) {
        super(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel cabina = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(2, 2, 2, 2);
        String distribucion = modelo.getDistribucion();
        int columnas = distribucion.length();

        // Fila de encabezado con las letras
        g.gridy = 0;
        for (int c = 0; c < columnas; c++) {
            g.gridx = c + 1;
            JLabel letra = new JLabel(String.valueOf(distribucion.charAt(c)).trim(), SwingConstants.CENTER);
            letra.setForeground(Tema.textoSecundario());
            cabina.add(letra, g);
        }

        int asientosPorFila = modelo.getAsientosPorFila();
        for (int fila = 1; fila <= modelo.getFilas(); fila++) {
            g.gridy = fila;
            g.gridx = 0;
            JLabel numero = new JLabel(String.valueOf(fila), SwingConstants.RIGHT);
            numero.setForeground(Tema.textoSecundario());
            numero.setPreferredSize(new Dimension(24, TAMANO_ASIENTO));
            cabina.add(numero, g);
            int indiceLetra = 0;
            for (int c = 0; c < columnas; c++) {
                char letra = distribucion.charAt(c);
                g.gridx = c + 1;
                if (letra == ' ') {
                    cabina.add(Box.createHorizontalStrut(14), g);
                    continue;
                }
                indiceLetra++;
                if ((fila - 1) * asientosPorFila + indiceLetra > modelo.getCapacidad()) {
                    continue;
                }
                String codigo = fila + String.valueOf(letra);
                cabina.add(crearAsiento(codigo, ocupados.contains(codigo) && !codigo.equals(actual),
                        codigo.equals(actual)), g);
            }
        }

        JScrollPane desplazamiento = new JScrollPane(cabina);
        desplazamiento.setBorder(BorderFactory.createEmptyBorder());
        desplazamiento.getVerticalScrollBar().setUnitIncrement(16);
        desplazamiento.setPreferredSize(new Dimension(
                Math.max(320, cabina.getPreferredSize().width + 30), 440));
        add(crearLeyenda(modelo), BorderLayout.NORTH);
        add(desplazamiento, BorderLayout.CENTER);
    }

    private JToggleButton crearAsiento(String codigo, boolean ocupado, boolean actual) {
        // El botón muestra solo la letra: el número de la fila ya está a la izquierda
        JToggleButton boton = new JToggleButton(codigo.substring(codigo.length() - 1));
        boton.setMargin(new Insets(0, 0, 0, 0));
        boton.putClientProperty("JButton.buttonType", "roundRect");
        boton.setPreferredSize(new Dimension(TAMANO_ASIENTO, TAMANO_ASIENTO - 6));
        boton.setFocusable(false);
        if (ocupado) {
            boton.setEnabled(false);
            boton.setToolTipText("Ocupado");
        } else {
            boton.setToolTipText("Asiento " + codigo);
            boton.addActionListener(e -> seleccionado = codigo);
        }
        if (actual) {
            boton.setSelected(true);
            seleccionado = codigo;
        }
        grupo.add(boton);
        return boton;
    }

    private static JPanel crearLeyenda(ModeloAvion modelo) {
        JPanel leyenda = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        JLabel titulo = new JLabel(modelo + " · " + modelo.getCapacidad() + " asientos");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD));
        leyenda.add(titulo);
        leyenda.add(etiquetaColor("En gris: ocupado · Marcado: elegido", Tema.textoSecundario()));
        return leyenda;
    }

    private static JLabel etiquetaColor(String texto, Color color) {
        JLabel etiqueta = new JLabel("■ " + texto);
        etiqueta.setForeground(color);
        return etiqueta;
    }

    /**
     * Muestra el mapa del vuelo y devuelve el asiento elegido.
     *
     * @param actual asiento que se muestra seleccionado al abrir (puede ser null)
     * @return el asiento elegido, o vacío si se canceló o no se eligió ninguno
     */
    public static Optional<String> elegir(Component padre, Vuelo vuelo, String actual) {
        MapaAsientos mapa = new MapaAsientos(vuelo.getAvion().getModelo(), vuelo.getAsientosOcupados(), actual);
        int respuesta = JOptionPane.showConfirmDialog(padre, mapa, "Elegir asiento · " + vuelo,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (respuesta != JOptionPane.OK_OPTION) {
            return Optional.empty();
        }
        return Optional.ofNullable(mapa.seleccionado);
    }
}