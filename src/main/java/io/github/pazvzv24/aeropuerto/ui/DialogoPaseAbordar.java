package io.github.pazvzv24.aeropuerto.ui;

import io.github.pazvzv24.aeropuerto.PaseAbordar;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/** Muestra un pase de abordar con aspecto de tarjeta. El documento aparece enmascarado. */
public final class DialogoPaseAbordar {

    private DialogoPaseAbordar() {
    }

    public static void mostrar(Component padre, PaseAbordar pase) {
        JOptionPane.showMessageDialog(padre, crearTarjeta(pase), "Pase de abordar · " + pase.getVuelo(),
                JOptionPane.PLAIN_MESSAGE);
    }

    static JPanel crearTarjeta(PaseAbordar pase) {
        JPanel tarjeta = new JPanel(new BorderLayout());
        tarjeta.setPreferredSize(new Dimension(520, 300));
        tarjeta.setBorder(BorderFactory.createLineBorder(Tema.acento(), 2));

        JLabel encabezado = new JLabel("  " + pase.getAerolinea().toUpperCase(Locale.ROOT) + "  ·  PASE DE ABORDAR");
        encabezado.setOpaque(true);
        encabezado.setBackground(Tema.acento());
        encabezado.setForeground(Color.WHITE);
        encabezado.setFont(encabezado.getFont().deriveFont(Font.BOLD, 15f));
        encabezado.setPreferredSize(new Dimension(0, 40));
        tarjeta.add(encabezado, BorderLayout.NORTH);

        JPanel ruta = new JPanel(new GridLayout(1, 3));
        ruta.setOpaque(false);
        ruta.add(codigoAeropuerto(pase.getOrigen().getCodigo(), pase.getOrigen().getCiudad()));
        JLabel avion = new JLabel("✈", SwingConstants.CENTER);
        avion.setFont(avion.getFont().deriveFont(28f));
        avion.setForeground(Tema.acento());
        ruta.add(avion);
        ruta.add(codigoAeropuerto(pase.getDestino().getCodigo(), pase.getDestino().getCiudad()));

        JPanel datos = new JPanel(new GridLayout(2, 4, 12, 4));
        datos.setOpaque(false);
        datos.add(dato("Vuelo", pase.getVuelo()));
        datos.add(dato("Fecha", pase.getFecha()));
        datos.add(dato("Embarque", pase.getHoraEmbarqueTexto()));
        datos.add(dato("Salida", pase.getHoraSalidaTexto()));
        datos.add(dato("Asiento", pase.getAsiento()));
        datos.add(dato("Grupo", String.valueOf(pase.getGrupoEmbarque())));
        datos.add(dato("Secuencia", pase.getSecuencia()));
        datos.add(dato("Puerta", "—"));

        JPanel pasajero = new JPanel(new GridLayout(1, 2, 12, 0));
        pasajero.setOpaque(false);
        pasajero.add(dato("Pasajero", pase.getNombrePasajero()));
        pasajero.add(dato("Documento", pase.getDocumento()));

        JPanel cuerpo = new JPanel(new BorderLayout(0, 14));
        cuerpo.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        cuerpo.add(pasajero, BorderLayout.NORTH);
        cuerpo.add(ruta, BorderLayout.CENTER);
        cuerpo.add(datos, BorderLayout.SOUTH);
        tarjeta.add(cuerpo, BorderLayout.CENTER);
        return tarjeta;
    }

    private static JPanel codigoAeropuerto(String codigo, String ciudad) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JLabel lblCodigo = new JLabel(codigo, SwingConstants.CENTER);
        lblCodigo.setFont(lblCodigo.getFont().deriveFont(Font.BOLD, 30f));
        JLabel lblCiudad = new JLabel(ciudad, SwingConstants.CENTER);
        lblCiudad.setForeground(Tema.textoSecundario());
        panel.add(lblCodigo, BorderLayout.CENTER);
        panel.add(lblCiudad, BorderLayout.SOUTH);
        return panel;
    }

    private static JPanel dato(String titulo, String valor) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JLabel lblTitulo = new JLabel(titulo.toUpperCase(Locale.ROOT));
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(10f));
        lblTitulo.setForeground(Tema.textoSecundario());
        JLabel lblValor = new JLabel(valor);
        lblValor.setFont(lblValor.getFont().deriveFont(Font.BOLD, 14f));
        panel.add(lblTitulo, BorderLayout.NORTH);
        panel.add(lblValor, BorderLayout.CENTER);
        return panel;
    }
}