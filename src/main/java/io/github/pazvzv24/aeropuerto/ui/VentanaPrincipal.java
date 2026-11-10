package io.github.pazvzv24.aeropuerto.ui;

import io.github.pazvzv24.aeropuerto.Aerolinea;
import io.github.pazvzv24.aeropuerto.Avion;
import io.github.pazvzv24.aeropuerto.EstadoAvion;
import io.github.pazvzv24.aeropuerto.ModeloAvion;
import io.github.pazvzv24.aeropuerto.OperationResult;
import io.github.pazvzv24.aeropuerto.Vuelo;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

/**
 * Ventana principal: resumen de la flota y dos pestañas, "Flota" (tabla de aviones con filtros
 * y panel de operaciones) y "Vuelos" (vuelos, pasajeros, check-in y embarque).
 * La ventana solo muestra datos y llama a Aerolinea: las reglas del negocio
 * están en las clases del modelo y se prueban por separado.
 */
public final class VentanaPrincipal extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final String ESTILO_TARJETA = "arc: 16; background: $Table.background";
    private static final String TODOS_LOS_ESTADOS = "Todos los estados";

    private final transient Aerolinea aerolinea;
    private final AvionesTableModel modelo;
    private final JTable tabla;
    private final transient TableRowSorter<AvionesTableModel> ordenador;
    private final PanelVuelos panelVuelos;
    private final JTabbedPane pestanas = new JTabbedPane();

    private final JLabel lblTitulo = new JLabel();
    private final JButton btnIncorporar = new JButton("+ Incorporar avión");
    private final JButton btnTema = new JButton("Tema oscuro");

    private final JLabel valorFlota = new JLabel();
    private final JLabel valorEnVuelo = new JLabel();
    private final JLabel valorPasajeros = new JLabel();
    private final JLabel valorMantenimiento = new JLabel();

    private final JTextField txtBuscar = new JTextField();
    private final JComboBox<Object> cmbEstado = new JComboBox<>();

    private final JLabel lblSeleccionado = new JLabel("Ninguno");
    private final JLabel lblEstadoAvion = new JLabel(" ");
    private final JLabel lblDescripcionEstado = new JLabel(" ");
    private final JSpinner spnCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
    private final JButton btnSubir = new JButton("Subir pasajeros");
    private final JButton btnBajar = new JButton("Bajar pasajeros");
    private final JPanel panelAcciones = new JPanel();
    private final JLabel lblResultado = new JLabel(" ");
    private final JLabel lblBarraEstado = new JLabel("Seleccione un avión de la tabla para operar.");

    public VentanaPrincipal(Aerolinea aerolinea) {
        super(aerolinea.getNombre() + " · Gestión de flota y vuelos");
        this.aerolinea = aerolinea;
        this.modelo = new AvionesTableModel(aerolinea);
        this.tabla = new JTable(modelo);
        this.ordenador = new TableRowSorter<>(modelo);
        this.panelVuelos = new PanelVuelos(aerolinea, this::refrescarFlota);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 640));
        setSize(1180, 720);
        setLocationRelativeTo(null);

        JPanel contenido = new JPanel(new BorderLayout(0, 16));
        contenido.setBorder(BorderFactory.createEmptyBorder(20, 24, 12, 24));
        contenido.add(crearEncabezado(), BorderLayout.NORTH);
        contenido.add(crearPestanas(), BorderLayout.CENTER);
        contenido.add(crearBarraEstado(), BorderLayout.SOUTH);
        setContentPane(contenido);

        aplicarColores();
        actualizarResumen();
        actualizarSeleccion();
    }

    // ------------------------------------------------------------ Construcción

    private JPanel crearEncabezado() {
        lblTitulo.setText(aerolinea.getNombre());
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 24f));
        JLabel lblSubtitulo = etiquetaSecundaria("Gestión de flota y vuelos");
        lblSubtitulo.setFont(lblSubtitulo.getFont().deriveFont(14f));

        JPanel titulos = new JPanel(new GridLayout(2, 1, 0, 2));
        titulos.setOpaque(false);
        titulos.add(lblTitulo);
        titulos.add(lblSubtitulo);

        btnIncorporar.addActionListener(e -> incorporarAvion());
        btnTema.setFocusable(false);
        btnTema.addActionListener(e -> cambiarTema());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setOpaque(false);
        botones.add(btnIncorporar);
        botones.add(btnTema);

        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);
        barra.add(titulos, BorderLayout.WEST);
        barra.add(botones, BorderLayout.EAST);

        JPanel tarjetas = new JPanel(new GridLayout(1, 4, 12, 0));
        tarjetas.setOpaque(false);
        tarjetas.add(crearTarjeta("Flota activa", valorFlota));
        tarjetas.add(crearTarjeta("En vuelo", valorEnVuelo));
        tarjetas.add(crearTarjeta("Pasajeros a bordo", valorPasajeros));
        tarjetas.add(crearTarjeta("En mantenimiento", valorMantenimiento));

        JPanel encabezado = new JPanel(new BorderLayout(0, 16));
        encabezado.setOpaque(false);
        encabezado.add(barra, BorderLayout.NORTH);
        encabezado.add(tarjetas, BorderLayout.CENTER);
        return encabezado;
    }

    private JPanel crearTarjeta(String titulo, JLabel valor) {
        JLabel lblTituloTarjeta = etiquetaSecundaria(titulo);
        lblTituloTarjeta.setFont(lblTituloTarjeta.getFont().deriveFont(13f));
        valor.setFont(valor.getFont().deriveFont(Font.BOLD, 26f));

        JPanel tarjeta = new JPanel(new GridLayout(2, 1, 0, 4));
        tarjeta.putClientProperty("FlatLaf.style", ESTILO_TARJETA);
        tarjeta.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        tarjeta.add(lblTituloTarjeta);
        tarjeta.add(valor);
        return tarjeta;
    }

    private JTabbedPane crearPestanas() {
        pestanas.putClientProperty("JTabbedPane.tabHeight", 36);
        pestanas.addTab("Flota", crearCuerpo());
        pestanas.addTab("Vuelos y pasajeros", panelVuelos);
        pestanas.addChangeListener(e -> {
            if (pestanas.getSelectedComponent() == panelVuelos) {
                panelVuelos.actualizar();
            } else {
                refrescarFlota();
            }
        });
        return pestanas;
    }

    private JPanel crearCuerpo() {
        configurarTabla();

        JPanel izquierda = new JPanel(new BorderLayout(0, 10));
        izquierda.setOpaque(false);
        izquierda.add(crearFiltros(), BorderLayout.NORTH);
        izquierda.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel cuerpo = new JPanel(new BorderLayout(16, 0));
        cuerpo.setOpaque(false);
        cuerpo.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        cuerpo.add(izquierda, BorderLayout.CENTER);
        cuerpo.add(crearPanelOperaciones(), BorderLayout.EAST);
        return cuerpo;
    }

    private JPanel crearFiltros() {
        txtBuscar.putClientProperty("JTextField.placeholderText", "Buscar por matrícula (ej. CC-AMA)");
        txtBuscar.putClientProperty("JTextField.showClearButton", true);
        txtBuscar.setPreferredSize(new Dimension(280, txtBuscar.getPreferredSize().height));
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                aplicarFiltros();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                aplicarFiltros();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                aplicarFiltros();
            }
        });

        DefaultComboBoxModel<Object> estados = new DefaultComboBoxModel<>();
        estados.addElement(TODOS_LOS_ESTADOS);
        for (EstadoAvion estado : EstadoAvion.values()) {
            estados.addElement(estado);
        }
        cmbEstado.setModel(estados);
        cmbEstado.addActionListener(e -> aplicarFiltros());

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        filtros.setOpaque(false);
        filtros.add(txtBuscar);
        filtros.add(javax.swing.Box.createHorizontalStrut(10));
        filtros.add(cmbEstado);
        return filtros;
    }

    private void configurarTabla() {
        tabla.setRowSorter(ordenador);
        tabla.setRowHeight(34);
        tabla.setFillsViewportHeight(true);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer centrado = new DefaultTableCellRenderer();
        centrado.setHorizontalAlignment(SwingConstants.CENTER);
        for (int c = 0; c < AvionesTableModel.COLUMNA_OCUPACION; c++) {
            tabla.getColumnModel().getColumn(c).setCellRenderer(centrado);
        }
        tabla.getColumnModel().getColumn(AvionesTableModel.COLUMNA_OCUPACION).setCellRenderer(new RenderOcupacion());
        tabla.getColumnModel().getColumn(AvionesTableModel.COLUMNA_ESTADO).setCellRenderer(new RenderEstado());
        tabla.getColumnModel().getColumn(AvionesTableModel.COLUMNA_MODELO).setPreferredWidth(150);
        tabla.getColumnModel().getColumn(AvionesTableModel.COLUMNA_OCUPACION).setPreferredWidth(150);
        tabla.getColumnModel().getColumn(AvionesTableModel.COLUMNA_ESTADO).setPreferredWidth(130);

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
        lblEstadoAvion.setFont(lblEstadoAvion.getFont().deriveFont(Font.BOLD));

        btnSubir.addActionListener(e -> moverPasajeros(true));
        btnBajar.addActionListener(e -> moverPasajeros(false));
        panelAcciones.setLayout(new BoxLayout(panelAcciones, BoxLayout.Y_AXIS));
        panelAcciones.setOpaque(false);
        lblResultado.setVerticalAlignment(SwingConstants.TOP);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.putClientProperty("FlatLaf.style", ESTILO_TARJETA);
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        panel.setPreferredSize(new Dimension(300, 0));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        g.anchor = GridBagConstraints.NORTHWEST;

        agregar(panel, g, titulo, 14);
        agregar(panel, g, etiquetaSecundaria("Avión seleccionado"), 2);
        agregar(panel, g, lblSeleccionado, 2);
        agregar(panel, g, lblEstadoAvion, 2);
        agregar(panel, g, lblDescripcionEstado, 16);
        agregar(panel, g, etiquetaSecundaria("Pasajeros"), 4);
        agregar(panel, g, spnCantidad, 8);
        JPanel botonesPasajeros = new JPanel(new GridLayout(1, 2, 8, 0));
        botonesPasajeros.setOpaque(false);
        botonesPasajeros.add(btnSubir);
        botonesPasajeros.add(btnBajar);
        agregar(panel, g, botonesPasajeros, 16);
        agregar(panel, g, etiquetaSecundaria("Cambiar estado"), 6);
        agregar(panel, g, panelAcciones, 16);
        g.weighty = 1;
        g.fill = GridBagConstraints.BOTH;
        agregar(panel, g, lblResultado, 0);
        return panel;
    }

    private static void agregar(JPanel panel, GridBagConstraints g, Component componente, int espacioAbajo) {
        g.insets = new Insets(0, 0, espacioAbajo, 0);
        panel.add(componente, g);
    }

    private JLabel etiquetaSecundaria(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.putClientProperty("esTextoSecundario", Boolean.TRUE);
        return etiqueta;
    }

    private JPanel crearBarraEstado() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);
        lblBarraEstado.putClientProperty("esTextoSecundario", Boolean.TRUE);
        barra.add(lblBarraEstado, BorderLayout.WEST);
        return barra;
    }

    // ------------------------------------------------------------ Comportamiento

    private void aplicarFiltros() {
        String texto = txtBuscar.getText().trim().toUpperCase(Locale.ROOT);
        Object estado = cmbEstado.getSelectedItem();
        List<RowFilter<AvionesTableModel, Integer>> filtros = new ArrayList<>();
        if (!texto.isEmpty()) {
            filtros.add(new RowFilter<AvionesTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends AvionesTableModel, ? extends Integer> fila) {
                    return fila.getStringValue(AvionesTableModel.COLUMNA_MATRICULA).contains(texto);
                }
            });
        }
        if (estado instanceof EstadoAvion) {
            filtros.add(new RowFilter<AvionesTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends AvionesTableModel, ? extends Integer> fila) {
                    return fila.getValue(AvionesTableModel.COLUMNA_ESTADO) == estado;
                }
            });
        }
        ordenador.setRowFilter(filtros.isEmpty() ? null : RowFilter.andFilter(filtros));
        lblBarraEstado.setText("Mostrando " + tabla.getRowCount() + " de " + modelo.getRowCount() + " aviones.");
    }

    private void moverPasajeros(boolean subir) {
        Avion avion = avionSeleccionado();
        if (avion == null) {
            return;
        }
        int cantidad = (Integer) spnCantidad.getValue();
        OperationResult resultado = subir
                ? aerolinea.subirPasajeros(avion.getMatricula(), cantidad)
                : aerolinea.bajarPasajeros(avion.getMatricula(), cantidad);
        String accion = (subir ? "Subir " : "Bajar ") + cantidad + " pasajero" + (cantidad == 1 ? "" : "s")
                + " · " + avion.getMatricula();
        terminarOperacion(avion.getMatricula(), resultado, accion);
    }

    private void cambiarEstado(EstadoAvion destino) {
        Avion avion = avionSeleccionado();
        if (avion == null) {
            return;
        }
        String accion = avion.getEstado().accionHacia(destino) + " · " + avion.getMatricula();
        if (destino == EstadoAvion.FUERA_DE_SERVICIO) {
            int respuesta = JOptionPane.showConfirmDialog(this,
                    "¿Dar de baja el avión " + avion.getMatricula() + "?\nEsta acción no se puede deshacer.",
                    "Dar de baja", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (respuesta != JOptionPane.YES_OPTION) {
                return;
            }
        }
        terminarOperacion(avion.getMatricula(), aerolinea.cambiarEstado(avion.getMatricula(), destino), accion);
    }

    private void incorporarAvion() {
        JTextField txtMatricula = new JTextField();
        txtMatricula.putClientProperty("JTextField.placeholderText", "CC-XXX");
        JComboBox<ModeloAvion> cmbModelo = new JComboBox<>(ModeloAvion.values());

        JPanel formulario = new JPanel(new GridLayout(4, 1, 0, 4));
        formulario.add(new JLabel("Matrícula (formato chileno CC-XXX):"));
        formulario.add(txtMatricula);
        formulario.add(new JLabel("Modelo:"));
        formulario.add(cmbModelo);

        int respuesta = JOptionPane.showConfirmDialog(this, formulario, "Incorporar avión",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (respuesta != JOptionPane.OK_OPTION) {
            return;
        }
        String matricula = Avion.normalizarMatricula(txtMatricula.getText());
        ModeloAvion modeloAvion = (ModeloAvion) cmbModelo.getSelectedItem();
        OperationResult resultado = aerolinea.incorporarAvion(matricula, modeloAvion);
        terminarOperacion(matricula, resultado, "Incorporar " + matricula + " · " + modeloAvion);
    }

    /** Actualiza la tabla y el resumen, mantiene seleccionado el avión y muestra el resultado. */
    private void terminarOperacion(String matricula, OperationResult resultado, String accion) {
        modelo.actualizar();
        int filaModelo = modelo.buscarFila(matricula);
        if (filaModelo >= 0) {
            int filaVista = tabla.convertRowIndexToView(filaModelo);
            if (filaVista >= 0) {
                tabla.setRowSelectionInterval(filaVista, filaVista);
                tabla.scrollRectToVisible(tabla.getCellRect(filaVista, 0, true));
            }
        }
        actualizarResumen();
        actualizarSeleccion();
        mostrarResultado(resultado, accion);
        lblBarraEstado.setText(accion + ": " + (resultado.isSuccess() ? "operación exitosa." : resultado.getMessage()));
    }

    /** Vuelve a leer la flota manteniendo el avión seleccionado (lo usa la pestaña de vuelos). */
    private void refrescarFlota() {
        Avion avion = avionSeleccionado();
        modelo.actualizar();
        if (avion != null) {
            int filaVista = tabla.convertRowIndexToView(modelo.buscarFila(avion.getMatricula()));
            if (filaVista >= 0) {
                tabla.setRowSelectionInterval(filaVista, filaVista);
            }
        }
        actualizarResumen();
        actualizarSeleccion();
    }

    private Avion avionSeleccionado() {
        int filaVista = tabla.getSelectedRow();
        if (filaVista < 0) {
            return null;
        }
        return modelo.getAvion(tabla.convertRowIndexToModel(filaVista));
    }

    /** Muestra los datos del avión seleccionado y solo las acciones que su estado permite. */
    private void actualizarSeleccion() {
        Avion avion = avionSeleccionado();
        panelAcciones.removeAll();
        if (avion == null) {
            lblSeleccionado.setText("Ninguno");
            lblEstadoAvion.setText(" ");
            lblDescripcionEstado.setText("Seleccione un avión de la tabla.");
            btnSubir.setEnabled(false);
            btnBajar.setEnabled(false);
        } else {
            EstadoAvion estado = avion.getEstado();
            lblSeleccionado.setText(avion.getMatricula() + " · " + avion.getModelo());
            lblEstadoAvion.setText("● " + estado + " (" + avion.getPasajerosActuales() + "/"
                    + avion.getCantidadMaxima() + " pasajeros)");
            lblEstadoAvion.setForeground(Tema.colorEstado(estado));
            lblDescripcionEstado.setText("<html><div style='width:215px'>" + estado.getDescripcion() + "</div></html>");
            Vuelo vuelo = aerolinea.buscarVueloPendiente(avion);
            boolean porVuelo = aerolinea.estaControladoPorVuelo(avion);
            if (vuelo != null) {
                lblDescripcionEstado.setText("<html><div style='width:215px'>" + estado.getDescripcion()
                        + "<br><b>Vuelo " + vuelo + "</b> (" + vuelo.getEstado() + ")"
                        + (porVuelo ? ": se opera desde la pestaña Vuelos y pasajeros." : ".") + "</div></html>");
            }
            btnSubir.setEnabled(!porVuelo && estado.permiteSubirPasajeros());
            btnBajar.setEnabled(!porVuelo && estado.permiteBajarPasajeros());
            btnSubir.setToolTipText(estado.permiteSubirPasajeros() ? null : "Solo se suben pasajeros durante el embarque");
            btnBajar.setToolTipText(estado.permiteBajarPasajeros() ? null : "Solo se bajan pasajeros en embarque o desembarque");

            for (EstadoAvion destino : porVuelo ? List.<EstadoAvion>of() : estado.getSiguientes()) {
                if (vuelo != null && destino == EstadoAvion.EMBARCANDO) {
                    continue; // el embarque de un avión con vuelo se inicia desde ese vuelo
                }
                JButton boton = new JButton(estado.accionHacia(destino));
                boton.setAlignmentX(Component.LEFT_ALIGNMENT);
                boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, boton.getPreferredSize().height));
                boton.addActionListener(e -> cambiarEstado(destino));
                panelAcciones.add(boton);
                panelAcciones.add(javax.swing.Box.createVerticalStrut(6));
            }
            if (porVuelo) {
                panelAcciones.add(etiquetaSecundaria("<html><div style='width:215px'>Embarque, despegue y aterrizaje"
                        + " se hacen desde el vuelo " + vuelo.getCodigo() + ".</div></html>"));
            } else if (estado.getSiguientes().isEmpty()) {
                panelAcciones.add(etiquetaSecundaria("Sin acciones: el avión fue dado de baja."));
            }
        }
        aplicarTextoSecundario(panelAcciones);
        panelAcciones.revalidate();
        panelAcciones.repaint();
    }

    private void actualizarResumen() {
        valorFlota.setText(aerolinea.getCantidadAvionesActivos() + " / " + Aerolinea.CAPACIDAD_FLOTA);
        valorEnVuelo.setText(String.valueOf(aerolinea.contarPorEstado(EstadoAvion.EN_VUELO)));
        valorPasajeros.setText(String.valueOf(aerolinea.calcularTotalPasajeros()));
        valorMantenimiento.setText(String.valueOf(aerolinea.contarPorEstado(EstadoAvion.EN_MANTENIMIENTO)));
    }

    private void mostrarResultado(OperationResult resultado, String accion) {
        boolean exito = resultado.isSuccess();
        String detalle = exito ? accion + "<br>Operación exitosa."
                : accion + "<br>" + resultado.getMessage() + " (código " + resultado.getStatusCode() + ")";
        lblResultado.setText("<html><div style='width:215px'>" + detalle + "</div></html>");
        lblResultado.setForeground(exito ? Tema.exito() : Tema.error());
        lblResultado.putClientProperty("exito", exito);
    }

    private void cambiarTema() {
        Tema.alternar();
        btnTema.setText(Tema.esOscuro() ? "Tema claro" : "Tema oscuro");
        aplicarColores();
        actualizarSeleccion();
        panelVuelos.actualizar();
    }

    /** Colores que dependen del tema (se vuelven a aplicar al cambiarlo). */
    private void aplicarColores() {
        lblTitulo.setForeground(Tema.acento());
        valorFlota.setForeground(Tema.acento());
        valorEnVuelo.setForeground(Tema.colorEstado(EstadoAvion.EN_VUELO));
        valorPasajeros.setForeground(Tema.acento());
        valorMantenimiento.setForeground(Tema.colorEstado(EstadoAvion.EN_MANTENIMIENTO));
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
        if (componente instanceof Container) {
            for (Component hijo : ((Container) componente).getComponents()) {
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

    /** Muestra el estado del avión con su color. */
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
            if (!isSelected && value instanceof EstadoAvion) {
                setForeground(Tema.colorEstado((EstadoAvion) value));
            }
            return this;
        }
    }
}