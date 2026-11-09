package io.github.pazvzv24.aeropuerto.ui;

import io.github.pazvzv24.aeropuerto.Aeropuerto;
import io.github.pazvzv24.aeropuerto.Avion;
import io.github.pazvzv24.aeropuerto.OperationResult;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * Ventana principal: resumen del aeropuerto, tabla de aviones y panel de operaciones.
 * La ventana solo muestra datos y llama a Aeropuerto: las reglas del negocio
 * están en las clases del modelo y se prueban por separado.
 */
public final class VentanaPrincipal extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final String ESTILO_TARJETA = "arc: 16; background: $Table.background";

    private final transient Aeropuerto aeropuerto;
    private final AvionesTableModel modelo;
    private final JTable tabla;

    private final JLabel lblTitulo = new JLabel();
    private final JLabel lblSubtitulo = new JLabel("Panel de gestión de embarque");
    private final JButton btnTema = new JButton("Tema oscuro");

    private final JLabel valorAviones = new JLabel();
    private final JLabel valorPasajeros = new JLabel();
    private final JLabel valorDisponibles = new JLabel();
    private final JLabel valorCancelados = new JLabel();

    private final JLabel lblSeleccionado = new JLabel("Ninguno");
    private final JSpinner spnCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
    private final JButton btnSubir = new JButton("Subir pasajeros");
    private final JButton btnBajar = new JButton("Bajar pasajeros");
    private final JLabel lblResultado = new JLabel(" ");
    private final JLabel lblEstado = new JLabel("Seleccione un avión de la tabla para operar.");

    public VentanaPrincipal(Aeropuerto aeropuerto) {
        super("Gestión de Aeropuerto");
        this.aeropuerto = aeropuerto;
        this.modelo = new AvionesTableModel(aeropuerto);
        this.tabla = new JTable(modelo);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 580));
        setSize(1040, 640);
        setLocationRelativeTo(null);

        JPanel contenido = new JPanel(new BorderLayout(0, 16));
        contenido.setBorder(BorderFactory.createEmptyBorder(20, 24, 12, 24));
        contenido.add(crearEncabezado(), BorderLayout.NORTH);
        contenido.add(crearCuerpo(), BorderLayout.CENTER);
        contenido.add(crearBarraEstado(), BorderLayout.SOUTH);
        setContentPane(contenido);

        getRootPane().setDefaultButton(btnSubir);
        aplicarColores();
        actualizarResumen();
        actualizarSeleccion();
    }

    // ------------------------------------------------------------ Construcción

    private JPanel crearEncabezado() {
        lblTitulo.setText(aeropuerto.getNombre());
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 24f));
        lblSubtitulo.setFont(lblSubtitulo.getFont().deriveFont(14f));
        lblSubtitulo.putClientProperty("esTextoSecundario", Boolean.TRUE);

        JPanel titulos = new JPanel(new GridLayout(2, 1, 0, 2));
        titulos.setOpaque(false);
        titulos.add(lblTitulo);
        titulos.add(lblSubtitulo);

        btnTema.setFocusable(false);
        btnTema.addActionListener(e -> cambiarTema());

        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);
        barra.add(titulos, BorderLayout.WEST);
        barra.add(btnTema, BorderLayout.EAST);

        JPanel tarjetas = new JPanel(new GridLayout(1, 4, 12, 0));
        tarjetas.setOpaque(false);
        tarjetas.add(crearTarjeta("Aviones", valorAviones));
        tarjetas.add(crearTarjeta("Pasajeros a bordo", valorPasajeros));
        tarjetas.add(crearTarjeta("Asientos disponibles", valorDisponibles));
        tarjetas.add(crearTarjeta("Vuelos cancelados", valorCancelados));

        JPanel encabezado = new JPanel(new BorderLayout(0, 16));
        encabezado.setOpaque(false);
        encabezado.add(barra, BorderLayout.NORTH);
        encabezado.add(tarjetas, BorderLayout.CENTER);
        return encabezado;
    }

    private JPanel crearTarjeta(String titulo, JLabel valor) {
        JLabel lblTituloTarjeta = new JLabel(titulo);
        lblTituloTarjeta.setFont(lblTituloTarjeta.getFont().deriveFont(13f));
        lblTituloTarjeta.putClientProperty("esTextoSecundario", Boolean.TRUE);
        valor.setFont(valor.getFont().deriveFont(Font.BOLD, 26f));

        JPanel tarjeta = new JPanel(new GridLayout(2, 1, 0, 4));
        tarjeta.putClientProperty("FlatLaf.style", ESTILO_TARJETA);
        tarjeta.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        tarjeta.add(lblTituloTarjeta);
        tarjeta.add(valor);
        return tarjeta;
    }

    private JPanel crearCuerpo() {
        configurarTabla();
        JScrollPane scroll = new JScrollPane(tabla);

        JPanel cuerpo = new JPanel(new BorderLayout(16, 0));
        cuerpo.setOpaque(false);
        cuerpo.add(scroll, BorderLayout.CENTER);
        cuerpo.add(crearPanelOperaciones(), BorderLayout.EAST);
        return cuerpo;
    }

    private void configurarTabla() {
        tabla.setRowHeight(34);
        tabla.setFillsViewportHeight(true);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.setAutoCreateRowSorter(true);

        DefaultTableCellRenderer centrado = new DefaultTableCellRenderer();
        centrado.setHorizontalAlignment(SwingConstants.CENTER);
        for (int c = 0; c < AvionesTableModel.COLUMNA_OCUPACION; c++) {
            tabla.getColumnModel().getColumn(c).setCellRenderer(centrado);
        }
        tabla.getColumnModel().getColumn(AvionesTableModel.COLUMNA_OCUPACION).setCellRenderer(new RenderOcupacion());
        tabla.getColumnModel().getColumn(AvionesTableModel.COLUMNA_ESTADO).setCellRenderer(new RenderEstado());
        tabla.getColumnModel().getColumn(AvionesTableModel.COLUMNA_ID).setMaxWidth(70);
        tabla.getColumnModel().getColumn(AvionesTableModel.COLUMNA_OCUPACION).setPreferredWidth(170);

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarSeleccion();
            }
        });
    }

    private JPanel crearPanelOperaciones() {
        JLabel titulo = new JLabel("Operaciones");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 16f));
        lblSeleccionado.setFont(lblSeleccionado.getFont().deriveFont(Font.BOLD, 15f));

        btnSubir.addActionListener(e -> ejecutar(true));
        btnBajar.addActionListener(e -> ejecutar(false));
        lblResultado.setVerticalAlignment(SwingConstants.TOP);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.putClientProperty("FlatLaf.style", ESTILO_TARJETA);
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        panel.setPreferredSize(new Dimension(280, 0));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        g.anchor = GridBagConstraints.NORTHWEST;

        g.insets = new Insets(0, 0, 16, 0);
        panel.add(titulo, g);
        g.insets = new Insets(0, 0, 4, 0);
        panel.add(etiquetaSecundaria("Avión seleccionado"), g);
        g.insets = new Insets(0, 0, 16, 0);
        panel.add(lblSeleccionado, g);
        g.insets = new Insets(0, 0, 4, 0);
        panel.add(etiquetaSecundaria("Cantidad de pasajeros"), g);
        g.insets = new Insets(0, 0, 16, 0);
        panel.add(spnCantidad, g);
        g.insets = new Insets(0, 0, 8, 0);
        panel.add(btnSubir, g);
        g.insets = new Insets(0, 0, 16, 0);
        panel.add(btnBajar, g);
        g.weighty = 1;
        g.fill = GridBagConstraints.BOTH;
        g.insets = new Insets(0, 0, 0, 0);
        panel.add(lblResultado, g);
        return panel;
    }

    private JLabel etiquetaSecundaria(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.putClientProperty("esTextoSecundario", Boolean.TRUE);
        return etiqueta;
    }

    private JPanel crearBarraEstado() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);
        lblEstado.putClientProperty("esTextoSecundario", Boolean.TRUE);
        barra.add(lblEstado, BorderLayout.WEST);
        return barra;
    }

    // ------------------------------------------------------------ Comportamiento

    /** Sube o baja pasajeros del avión seleccionado y muestra el resultado. */
    private void ejecutar(boolean subir) {
        Avion avion = avionSeleccionado();
        if (avion == null) {
            mostrarResultado(null, "Seleccione un avión de la tabla.");
            return;
        }
        int cantidad = (Integer) spnCantidad.getValue();
        OperationResult resultado = subir
                ? aeropuerto.subirPasajeros(avion.getId(), cantidad)
                : aeropuerto.bajarPasajeros(avion.getId(), cantidad);

        int filaVista = tabla.getSelectedRow();
        modelo.actualizar();
        if (filaVista >= 0 && filaVista < tabla.getRowCount()) {
            tabla.setRowSelectionInterval(filaVista, filaVista);
        }
        actualizarResumen();

        String accion = (subir ? "Subir " : "Bajar ") + cantidad + " pasajero" + (cantidad == 1 ? "" : "s")
                + " · avión " + avion.getId();
        mostrarResultado(resultado, accion);
        lblEstado.setText(accion + ": " + (resultado.isSuccess() ? "operación exitosa." : resultado.getMessage()));
    }

    private Avion avionSeleccionado() {
        int filaVista = tabla.getSelectedRow();
        if (filaVista < 0) {
            return null;
        }
        return modelo.getAvion(tabla.convertRowIndexToModel(filaVista));
    }

    private void actualizarSeleccion() {
        Avion avion = avionSeleccionado();
        boolean hay = avion != null;
        btnSubir.setEnabled(hay && avion.getEstado());
        btnBajar.setEnabled(hay);
        if (!hay) {
            lblSeleccionado.setText("Ninguno");
        } else {
            lblSeleccionado.setText("Avión " + avion.getId() + " · " + (avion.getEstado() ? "Activo" : "Cancelado"));
            btnSubir.setToolTipText(avion.getEstado() ? null : "No se puede embarcar en un vuelo cancelado");
        }
    }

    private void actualizarResumen() {
        int cancelados = 0;
        for (Avion avion : aeropuerto.getAviones()) {
            if (!avion.getEstado()) {
                cancelados++;
            }
        }
        valorAviones.setText(String.valueOf(aeropuerto.getAviones().size()));
        valorPasajeros.setText(String.valueOf(aeropuerto.calcularTotalPasajeros()));
        valorDisponibles.setText(String.valueOf(aeropuerto.calcularTotalDisponibles()));
        valorCancelados.setText(String.valueOf(cancelados));
    }

    private void mostrarResultado(OperationResult resultado, String accion) {
        boolean exito = resultado != null && resultado.isSuccess();
        String detalle = resultado == null ? accion
                : exito ? accion + "<br>Operación exitosa."
                : accion + "<br>" + resultado.getMessage() + " (código " + resultado.getStatusCode() + ")";
        lblResultado.setText("<html><div style='width:220px'>" + detalle + "</div></html>");
        lblResultado.setForeground(exito ? Tema.exito() : Tema.error());
        lblResultado.putClientProperty("exito", exito);
    }

    private void cambiarTema() {
        Tema.alternar();
        btnTema.setText(Tema.esOscuro() ? "Tema claro" : "Tema oscuro");
        aplicarColores();
    }

    /** Colores que dependen del tema (se vuelven a aplicar al cambiarlo). */
    private void aplicarColores() {
        lblTitulo.setForeground(Tema.acento());
        valorAviones.setForeground(Tema.acento());
        valorPasajeros.setForeground(Tema.acento());
        valorDisponibles.setForeground(Tema.acento());
        valorCancelados.setForeground(Tema.error());
        aplicarTextoSecundario(getContentPane());
        Object exito = lblResultado.getClientProperty("exito");
        if (exito != null) {
            lblResultado.setForeground(Boolean.TRUE.equals(exito) ? Tema.exito() : Tema.error());
        }
        repaint();
    }

    private void aplicarTextoSecundario(Component componente) {
        if (componente instanceof JLabel && Boolean.TRUE.equals(((JLabel) componente).getClientProperty("esTextoSecundario"))) {
            componente.setForeground(Tema.textoSecundario());
        }
        if (componente instanceof java.awt.Container) {
            for (Component hijo : ((java.awt.Container) componente).getComponents()) {
                aplicarTextoSecundario(hijo);
            }
        }
    }

    // ------------------------------------------------------------ Celdas especiales

    /** Muestra la ocupación como barra de progreso con el porcentaje. */
    private static final class RenderOcupacion extends JProgressBar implements TableCellRenderer {
        private static final long serialVersionUID = 1L;

        RenderOcupacion() {
            super(0, 100);
            setStringPainted(true);
            setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            double porcentaje = value instanceof Double ? (Double) value : 0;
            setValue((int) Math.round(porcentaje));
            setString(Math.round(porcentaje) + " %");
            setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return this;
        }
    }

    /** Muestra el estado del vuelo en verde (activo) o rojo (cancelado). */
    private static final class RenderEstado extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        RenderEstado() {
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setFont(getFont().deriveFont(Font.BOLD));
            if (!isSelected) {
                setForeground("Activo".equals(value) ? Tema.exito() : Tema.error());
            }
            return this;
        }
    }
}