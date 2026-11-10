package io.github.pazvzv24.aeropuerto.ui;

import io.github.pazvzv24.aeropuerto.Aerolinea;
import io.github.pazvzv24.aeropuerto.Avion;
import io.github.pazvzv24.aeropuerto.CheckIn;
import io.github.pazvzv24.aeropuerto.Destino;
import io.github.pazvzv24.aeropuerto.EstadoPasajero;
import io.github.pazvzv24.aeropuerto.EstadoVuelo;
import io.github.pazvzv24.aeropuerto.OperationResult;
import io.github.pazvzv24.aeropuerto.PaseAbordar;
import io.github.pazvzv24.aeropuerto.Pasajero;
import io.github.pazvzv24.aeropuerto.TipoDocumento;
import io.github.pazvzv24.aeropuerto.Vuelo;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;

/**
 * Pestaña de vuelos: tabla de vuelos, lista de pasajeros del vuelo seleccionado
 * y panel para check-in, embarque y cambios de estado.
 * Los documentos de los pasajeros se muestran siempre enmascarados.
 */
public final class PanelVuelos extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final String ESTILO_TARJETA = "arc: 16; background: $Table.background";
    private static final String TODOS_LOS_ESTADOS = "Todos los estados";
    private static final DateTimeFormatter FORMATO_SALIDA = DateTimeFormatter.ofPattern("dd-MM HH:mm");
    private static final int ANCHO_TEXTO = 215;

    private final transient Aerolinea aerolinea;
    private final transient Runnable alCambiarFlota;

    private final VuelosTableModel modeloVuelos;
    private final JTable tablaVuelos;
    private final transient TableRowSorter<VuelosTableModel> ordenadorVuelos;
    private final PasajerosTableModel modeloPasajeros = new PasajerosTableModel();
    private final JTable tablaPasajeros = new JTable(modeloPasajeros);

    private final JButton btnProgramar = new JButton("+ Programar vuelo");
    private final JComboBox<Object> cmbEstado = new JComboBox<>();
    private final JLabel lblManifiesto = new JLabel("Pasajeros");

    private final JLabel lblVuelo = new JLabel("Ninguno");
    private final JLabel lblEstadoVuelo = new JLabel(" ");
    private final JLabel lblDetalleVuelo = new JLabel(" ");
    private final JPanel panelAcciones = new JPanel();
    private final JTextField txtNombre = new JTextField();
    private final JComboBox<TipoDocumento> cmbTipo = new JComboBox<>(TipoDocumento.values());
    private final JTextField txtDocumento = new JTextField();
    private final JButton btnCheckIn = new JButton("Hacer check-in");
    private final JButton btnEmbarcar = new JButton("Embarcar");
    private final JButton btnAnular = new JButton("Anular check-in");
    private final JButton btnAsientoCheckIn = new JButton();
    private final JButton btnCambiarAsiento = new JButton("Cambiar asiento");
    private final JButton btnPase = new JButton("Pase de abordar");
    /** Asiento elegido para el próximo check-in; null = el primero libre. */
    private String asientoElegido;
    private final JLabel lblResultado = new JLabel(" ");

    /**
     * @param alCambiarFlota se ejecuta después de cada operación, para que la pestaña de flota
     *                       y las tarjetas de resumen muestren el estado actualizado de los aviones
     */
    public PanelVuelos(Aerolinea aerolinea, Runnable alCambiarFlota) {
        super(new BorderLayout(16, 0));
        this.aerolinea = aerolinea;
        this.alCambiarFlota = alCambiarFlota;
        this.modeloVuelos = new VuelosTableModel(aerolinea);
        this.tablaVuelos = new JTable(modeloVuelos);
        this.ordenadorVuelos = new TableRowSorter<>(modeloVuelos);

        setOpaque(false);
        configurarTablas();

        JPanel arriba = new JPanel(new BorderLayout(0, 10));
        arriba.setOpaque(false);
        arriba.add(crearBarraHerramientas(), BorderLayout.NORTH);
        arriba.add(new JScrollPane(tablaVuelos), BorderLayout.CENTER);

        JPanel abajo = new JPanel(new BorderLayout(0, 6));
        abajo.setOpaque(false);
        lblManifiesto.setFont(lblManifiesto.getFont().deriveFont(Font.BOLD, 14f));
        abajo.add(lblManifiesto, BorderLayout.NORTH);
        abajo.add(new JScrollPane(tablaPasajeros), BorderLayout.CENTER);

        JSplitPane division = new JSplitPane(JSplitPane.VERTICAL_SPLIT, arriba, abajo);
        division.setResizeWeight(0.5);
        division.setBorder(null);
        division.setOpaque(false);

        add(division, BorderLayout.CENTER);
        add(crearPanelOperaciones(), BorderLayout.EAST);

        if (tablaVuelos.getRowCount() > 0) {
            tablaVuelos.setRowSelectionInterval(0, 0);
        }
        actualizarSeleccion();
    }

    // ------------------------------------------------------------ Construcción

    private JPanel crearBarraHerramientas() {
        btnProgramar.addActionListener(e -> programarVuelo());

        DefaultComboBoxModel<Object> estados = new DefaultComboBoxModel<>();
        estados.addElement(TODOS_LOS_ESTADOS);
        for (EstadoVuelo estado : EstadoVuelo.values()) {
            estados.addElement(estado);
        }
        cmbEstado.setModel(estados);
        cmbEstado.addActionListener(e -> aplicarFiltro());

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        barra.setOpaque(false);
        barra.add(btnProgramar);
        barra.add(Box.createHorizontalStrut(10));
        barra.add(cmbEstado);
        return barra;
    }

    private void configurarTablas() {
        tablaVuelos.setRowSorter(ordenadorVuelos);
        ordenadorVuelos.setSortKeys(List.of(new RowSorter.SortKey(VuelosTableModel.COLUMNA_SALIDA, SortOrder.ASCENDING)));
        prepararTabla(tablaVuelos);
        DefaultTableCellRenderer centrado = new DefaultTableCellRenderer();
        centrado.setHorizontalAlignment(SwingConstants.CENTER);
        for (int c = 0; c < VuelosTableModel.COLUMNA_ESTADO; c++) {
            tablaVuelos.getColumnModel().getColumn(c).setCellRenderer(centrado);
        }
        tablaVuelos.getColumnModel().getColumn(VuelosTableModel.COLUMNA_SALIDA).setCellRenderer(new RenderSalida());
        tablaVuelos.getColumnModel().getColumn(VuelosTableModel.COLUMNA_ESTADO).setCellRenderer(new RenderEstado());
        tablaVuelos.getColumnModel().getColumn(VuelosTableModel.COLUMNA_ESTADO).setPreferredWidth(120);
        tablaVuelos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarSeleccion();
            }
        });

        tablaPasajeros.setAutoCreateRowSorter(true);
        prepararTabla(tablaPasajeros);
        tablaPasajeros.getColumnModel().getColumn(PasajerosTableModel.COLUMNA_NOMBRE).setPreferredWidth(220);
        tablaPasajeros.getColumnModel().getColumn(PasajerosTableModel.COLUMNA_ESTADO).setCellRenderer(new RenderEstado());
        DefaultTableCellRenderer asientoCentrado = new DefaultTableCellRenderer();
        asientoCentrado.setHorizontalAlignment(SwingConstants.CENTER);
        tablaPasajeros.getColumnModel().getColumn(PasajerosTableModel.COLUMNA_ASIENTO).setCellRenderer(asientoCentrado);
        tablaPasajeros.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarBotonesPasajero();
            }
        });
    }

    private static void prepararTabla(JTable tabla) {
        tabla.setRowHeight(32);
        tabla.setFillsViewportHeight(true);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getTableHeader().setReorderingAllowed(false);
    }

    private JPanel crearPanelOperaciones() {
        JLabel titulo = new JLabel("Vuelo");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 16f));
        lblVuelo.setFont(lblVuelo.getFont().deriveFont(Font.BOLD, 15f));
        lblEstadoVuelo.setFont(lblEstadoVuelo.getFont().deriveFont(Font.BOLD));
        panelAcciones.setLayout(new BoxLayout(panelAcciones, BoxLayout.Y_AXIS));
        panelAcciones.setOpaque(false);
        lblResultado.setVerticalAlignment(SwingConstants.TOP);

        txtNombre.putClientProperty("JTextField.placeholderText", "Nombre completo");
        txtDocumento.putClientProperty("JTextField.placeholderText", "RUT con dígito verificador");
        cmbTipo.addActionListener(e -> txtDocumento.putClientProperty("JTextField.placeholderText",
                cmbTipo.getSelectedItem() == TipoDocumento.RUT ? "RUT con dígito verificador" : "N.º de pasaporte"));
        btnCheckIn.addActionListener(e -> hacerCheckIn());
        txtDocumento.addActionListener(e -> hacerCheckIn());
        btnEmbarcar.addActionListener(e -> operarPasajero(true));
        btnAnular.addActionListener(e -> operarPasajero(false));
        btnAsientoCheckIn.addActionListener(e -> elegirAsientoCheckIn());
        btnCambiarAsiento.addActionListener(e -> cambiarAsiento());
        btnPase.addActionListener(e -> mostrarPase());
        actualizarBotonAsiento();

        JPanel panel = new JPanel(new GridBagLayout());
        panel.putClientProperty("FlatLaf.style", ESTILO_TARJETA);
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        panel.setPreferredSize(new Dimension(300, 0));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        g.anchor = GridBagConstraints.NORTHWEST;

        agregar(panel, g, titulo, 10);
        agregar(panel, g, lblVuelo, 2);
        agregar(panel, g, lblEstadoVuelo, 2);
        agregar(panel, g, lblDetalleVuelo, 10);
        agregar(panel, g, panelAcciones, 12);

        agregar(panel, g, etiquetaSecundaria("Check-in de pasajero"), 4);
        agregar(panel, g, txtNombre, 6);
        JPanel documento = new JPanel(new BorderLayout(6, 0));
        documento.setOpaque(false);
        documento.add(cmbTipo, BorderLayout.WEST);
        documento.add(txtDocumento, BorderLayout.CENTER);
        agregar(panel, g, documento, 6);
        JPanel botonesCheckIn = new JPanel(new GridLayout(1, 2, 8, 0));
        botonesCheckIn.setOpaque(false);
        botonesCheckIn.add(btnAsientoCheckIn);
        botonesCheckIn.add(btnCheckIn);
        agregar(panel, g, botonesCheckIn, 12);

        agregar(panel, g, etiquetaSecundaria("Pasajero seleccionado"), 4);
        JPanel botonesPasajero = new JPanel(new GridLayout(2, 2, 8, 6));
        botonesPasajero.setOpaque(false);
        botonesPasajero.add(btnEmbarcar);
        botonesPasajero.add(btnAnular);
        botonesPasajero.add(btnCambiarAsiento);
        botonesPasajero.add(btnPase);
        agregar(panel, g, botonesPasajero, 12);

        g.weighty = 1;
        g.fill = GridBagConstraints.BOTH;
        agregar(panel, g, lblResultado, 0);
        return panel;
    }

    private static void agregar(JPanel panel, GridBagConstraints g, Component componente, int espacioAbajo) {
        g.insets = new Insets(0, 0, espacioAbajo, 0);
        panel.add(componente, g);
    }

    private static JLabel etiquetaSecundaria(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.putClientProperty("esTextoSecundario", Boolean.TRUE);
        etiqueta.setForeground(Tema.textoSecundario());
        return etiqueta;
    }

    private static String html(String texto) {
        return "<html><div style='width:" + ANCHO_TEXTO + "px'>" + texto + "</div></html>";
    }

    // ------------------------------------------------------------ Comportamiento

    /** Vuelve a leer vuelos y pasajeros (por ejemplo, al volver a esta pestaña o cambiar el tema). */
    public void actualizar() {
        String codigo = vueloSeleccionado() == null ? null : vueloSeleccionado().getCodigo();
        modeloVuelos.actualizar();
        seleccionarVuelo(codigo);
        actualizarSeleccion();
        Object exito = lblResultado.getClientProperty("exito");
        if (exito != null) {
            lblResultado.setForeground(Boolean.TRUE.equals(exito) ? Tema.exito() : Tema.error());
        }
    }

    private void aplicarFiltro() {
        Object estado = cmbEstado.getSelectedItem();
        if (estado instanceof EstadoVuelo) {
            ordenadorVuelos.setRowFilter(new RowFilter<VuelosTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends VuelosTableModel, ? extends Integer> fila) {
                    return fila.getValue(VuelosTableModel.COLUMNA_ESTADO) == estado;
                }
            });
        } else {
            ordenadorVuelos.setRowFilter(null);
        }
    }

    private Vuelo vueloSeleccionado() {
        int filaVista = tablaVuelos.getSelectedRow();
        return filaVista < 0 ? null : modeloVuelos.getVuelo(tablaVuelos.convertRowIndexToModel(filaVista));
    }

    private CheckIn pasajeroSeleccionado() {
        int filaVista = tablaPasajeros.getSelectedRow();
        return filaVista < 0 ? null : modeloPasajeros.getCheckIn(tablaPasajeros.convertRowIndexToModel(filaVista));
    }

    private void seleccionarVuelo(String codigo) {
        int filaModelo = codigo == null ? -1 : modeloVuelos.buscarFila(codigo);
        int filaVista = filaModelo < 0 ? -1 : tablaVuelos.convertRowIndexToView(filaModelo);
        if (filaVista >= 0) {
            tablaVuelos.setRowSelectionInterval(filaVista, filaVista);
            tablaVuelos.scrollRectToVisible(tablaVuelos.getCellRect(filaVista, 0, true));
        }
    }

    /** Muestra los datos del vuelo seleccionado, su lista de pasajeros y solo las acciones permitidas. */
    private void actualizarSeleccion() {
        Vuelo vuelo = vueloSeleccionado();
        if (vuelo != modeloPasajeros.getVuelo()) {
            asientoElegido = null;
            actualizarBotonAsiento();
        }
        modeloPasajeros.setVuelo(vuelo);
        panelAcciones.removeAll();
        if (vuelo == null) {
            lblVuelo.setText("Ninguno");
            lblEstadoVuelo.setText(" ");
            lblDetalleVuelo.setText(html("Seleccione un vuelo de la tabla."));
            lblManifiesto.setText("Pasajeros");
            btnCheckIn.setEnabled(false);
            btnAsientoCheckIn.setEnabled(false);
        } else {
            EstadoVuelo estado = vuelo.getEstado();
            lblVuelo.setText(vuelo.toString());
            lblEstadoVuelo.setText("● " + estado);
            lblEstadoVuelo.setForeground(Tema.colorEstado(estado));
            Avion avion = vuelo.getAvion();
            lblDetalleVuelo.setText(html(vuelo.getOrigen() + " → " + vuelo.getDestino()
                    + "<br>Salida: " + FORMATO_SALIDA.format(vuelo.getSalida())
                    + "<br>Avión: " + avion.getMatricula() + " · " + avion.getModelo()
                    + "<br>Capacidad: " + avion.getCantidadMaxima() + " asientos"));
            lblDetalleVuelo.setForeground(Tema.textoSecundario());
            lblManifiesto.setText("Pasajeros del vuelo " + vuelo.getCodigo() + " · "
                    + vuelo.getCantidadPasajeros() + " con check-in, "
                    + vuelo.contarPasajeros(EstadoPasajero.EMBARCADO) + " embarcados");
            btnCheckIn.setEnabled(estado.permiteCheckIn());
            btnAsientoCheckIn.setEnabled(estado.permiteCheckIn());
            btnCheckIn.setToolTipText(estado.permiteCheckIn() ? null : "El check-in cierra al despegar");

            for (EstadoVuelo destino : estado.getSiguientes()) {
                JButton boton = new JButton(estado.accionHacia(destino));
                boton.setAlignmentX(Component.LEFT_ALIGNMENT);
                boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, boton.getPreferredSize().height));
                boton.addActionListener(e -> cambiarEstado(destino));
                panelAcciones.add(boton);
                panelAcciones.add(Box.createVerticalStrut(6));
            }
            if (estado.esFinal()) {
                panelAcciones.add(etiquetaSecundaria("Vuelo " + estado.getNombre().toLowerCase() + ": sin acciones."));
            }
        }
        panelAcciones.revalidate();
        panelAcciones.repaint();
        actualizarBotonesPasajero();
    }

    private void actualizarBotonesPasajero() {
        Vuelo vuelo = vueloSeleccionado();
        CheckIn registro = pasajeroSeleccionado();
        boolean conCheckIn = vuelo != null && registro != null && registro.getEstado() == EstadoPasajero.CHECK_IN;
        btnEmbarcar.setEnabled(conCheckIn && vuelo.getEstado() == EstadoVuelo.EMBARCANDO);
        btnAnular.setEnabled(conCheckIn && vuelo.getEstado().permiteCheckIn());
        btnEmbarcar.setToolTipText(btnEmbarcar.isEnabled() ? null
                : "Seleccione un pasajero con check-in de un vuelo que esté embarcando");
        btnCambiarAsiento.setEnabled(conCheckIn && vuelo.getEstado().permiteCheckIn());
        btnPase.setEnabled(vuelo != null && registro != null
                && vuelo.validarPaseAbordar(registro.getPasajero().getTipoDocumento(),
                        registro.getPasajero().getDocumento()).isSuccess());
    }

    private void actualizarBotonAsiento() {
        btnAsientoCheckIn.setText(asientoElegido == null ? "Asiento: automático" : "Asiento: " + asientoElegido);
        btnAsientoCheckIn.setToolTipText("Elegir el asiento en el mapa del avión");
    }

    private void elegirAsientoCheckIn() {
        Vuelo vuelo = vueloSeleccionado();
        if (vuelo == null) {
            return;
        }
        MapaAsientos.elegir(this, vuelo, asientoElegido).ifPresent(asiento -> {
            asientoElegido = asiento;
            actualizarBotonAsiento();
        });
    }

    private void cambiarAsiento() {
        Vuelo vuelo = vueloSeleccionado();
        CheckIn registro = pasajeroSeleccionado();
        if (vuelo == null || registro == null) {
            return;
        }
        Pasajero pasajero = registro.getPasajero();
        MapaAsientos.elegir(this, vuelo, registro.getAsiento())
                .filter(asiento -> !asiento.equals(registro.getAsiento()))
                .ifPresent(asiento -> terminarOperacion(vuelo.getCodigo(),
                        aerolinea.cambiarAsiento(vuelo.getCodigo(), pasajero.getTipoDocumento(),
                                pasajero.getDocumento(), asiento),
                        "Cambiar asiento · " + pasajero.getNombre() + " → " + asiento));
    }

    private void mostrarPase() {
        Vuelo vuelo = vueloSeleccionado();
        CheckIn registro = pasajeroSeleccionado();
        if (vuelo == null || registro == null) {
            return;
        }
        Pasajero pasajero = registro.getPasajero();
        PaseAbordar pase = aerolinea.emitirPaseAbordar(vuelo.getCodigo(), pasajero.getTipoDocumento(),
                pasajero.getDocumento());
        if (pase == null) {
            terminarOperacion(vuelo.getCodigo(), OperationResult.BOARDING_PASS_UNAVAILABLE, "Pase de abordar");
            return;
        }
        DialogoPaseAbordar.mostrar(this, pase);
    }

    private void cambiarEstado(EstadoVuelo destino) {
        Vuelo vuelo = vueloSeleccionado();
        if (vuelo == null) {
            return;
        }
        String accion = vuelo.getEstado().accionHacia(destino) + " · " + vuelo.getCodigo();
        if (destino == EstadoVuelo.CANCELADO) {
            int respuesta = JOptionPane.showConfirmDialog(this,
                    "¿Cancelar el vuelo " + vuelo + "?\nEsta acción no se puede deshacer.",
                    "Cancelar vuelo", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (respuesta != JOptionPane.YES_OPTION) {
                return;
            }
        }
        terminarOperacion(vuelo.getCodigo(), aerolinea.cambiarEstadoVuelo(vuelo.getCodigo(), destino), accion);
    }

    private void hacerCheckIn() {
        Vuelo vuelo = vueloSeleccionado();
        if (vuelo == null || !btnCheckIn.isEnabled()) {
            return;
        }
        String nombre = txtNombre.getText().trim();
        TipoDocumento tipo = (TipoDocumento) cmbTipo.getSelectedItem();
        OperationResult resultado = aerolinea.hacerCheckIn(vuelo.getCodigo(), nombre, tipo, txtDocumento.getText(),
                asientoElegido);
        if (resultado.isSuccess()) {
            txtNombre.setText("");
            txtDocumento.setText("");
            asientoElegido = null;
            actualizarBotonAsiento();
        }
        String accion = "Check-in · " + vuelo.getCodigo() + (nombre.isEmpty() ? "" : " · " + nombre);
        terminarOperacion(vuelo.getCodigo(), resultado, accion);
    }

    /** Embarca (embarcar = true) o anula el check-in (embarcar = false) del pasajero seleccionado. */
    private void operarPasajero(boolean embarcar) {
        Vuelo vuelo = vueloSeleccionado();
        CheckIn registro = pasajeroSeleccionado();
        if (vuelo == null || registro == null) {
            return;
        }
        Pasajero pasajero = registro.getPasajero();
        OperationResult resultado = embarcar
                ? aerolinea.embarcarPasajero(vuelo.getCodigo(), pasajero.getTipoDocumento(), pasajero.getDocumento())
                : aerolinea.anularCheckIn(vuelo.getCodigo(), pasajero.getTipoDocumento(), pasajero.getDocumento());
        String accion = (embarcar ? "Embarcar · " : "Anular check-in · ") + pasajero.getNombre();
        terminarOperacion(vuelo.getCodigo(), resultado, accion);
    }

    private void programarVuelo() {
        JTextField txtCodigo = new JTextField();
        txtCodigo.putClientProperty("JTextField.placeholderText", "AM123");
        JComboBox<Destino> cmbOrigen = new JComboBox<>(Destino.values());
        JComboBox<Destino> cmbDestino = new JComboBox<>(Destino.values());
        cmbDestino.setSelectedItem(Destino.LIM);

        LocalDateTime sugerida = LocalDateTime.now().plusHours(3).truncatedTo(ChronoUnit.HOURS);
        Date inicial = Date.from(sugerida.atZone(ZoneId.systemDefault()).toInstant());
        JSpinner spnSalida = new JSpinner(new SpinnerDateModel(inicial, null, null, Calendar.MINUTE));
        spnSalida.setEditor(new JSpinner.DateEditor(spnSalida, "dd-MM-yyyy HH:mm"));

        DefaultComboBoxModel<Avion> aviones = new DefaultComboBoxModel<>();
        for (Avion avion : aerolinea.getFlota()) {
            if (avion.getEstado().esActivo() && aerolinea.buscarVueloPendiente(avion) == null) {
                aviones.addElement(avion);
            }
        }
        if (aviones.getSize() == 0) {
            terminarOperacion(null, OperationResult.AIRPLANE_NOT_AVAILABLE, "Programar vuelo");
            return;
        }
        JComboBox<Avion> cmbAvion = new JComboBox<>(aviones);
        cmbAvion.setRenderer(new RenderAvion());

        JPanel formulario = new JPanel(new GridLayout(10, 1, 0, 4));
        formulario.add(new JLabel("Código del vuelo (AM y 3 o 4 dígitos):"));
        formulario.add(txtCodigo);
        formulario.add(new JLabel("Origen:"));
        formulario.add(cmbOrigen);
        formulario.add(new JLabel("Destino:"));
        formulario.add(cmbDestino);
        formulario.add(new JLabel("Salida:"));
        formulario.add(spnSalida);
        formulario.add(new JLabel("Avión (solo los que no tienen otro vuelo):"));
        formulario.add(cmbAvion);

        int respuesta = JOptionPane.showConfirmDialog(this, formulario, "Programar vuelo",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (respuesta != JOptionPane.OK_OPTION) {
            return;
        }
        String codigo = Vuelo.normalizarCodigo(txtCodigo.getText());
        LocalDateTime salida = LocalDateTime.ofInstant(((Date) spnSalida.getValue()).toInstant(), ZoneId.systemDefault());
        Avion avion = (Avion) cmbAvion.getSelectedItem();
        OperationResult resultado = aerolinea.programarVuelo(codigo, (Destino) cmbOrigen.getSelectedItem(),
                (Destino) cmbDestino.getSelectedItem(), salida, avion == null ? null : avion.getMatricula());
        terminarOperacion(codigo, resultado, "Programar vuelo " + codigo);
    }

    /** Actualiza tablas, mantiene seleccionado el vuelo, avisa a la flota y muestra el resultado. */
    private void terminarOperacion(String codigo, OperationResult resultado, String accion) {
        modeloVuelos.actualizar();
        seleccionarVuelo(codigo);
        actualizarSeleccion();
        alCambiarFlota.run();

        boolean exito = resultado.isSuccess();
        String detalle = exito ? accion + "<br>Operación exitosa."
                : accion + "<br>" + resultado.getMessage() + " (código " + resultado.getStatusCode() + ")";
        lblResultado.setText(html(detalle));
        lblResultado.setForeground(exito ? Tema.exito() : Tema.error());
        lblResultado.putClientProperty("exito", exito);
    }

    // ------------------------------------------------------------ Celdas especiales

    /** Muestra la hora de salida como "dd-MM HH:mm", pero ordena por la fecha real. */
    private static final class RenderSalida extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        RenderSalida() {
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        protected void setValue(Object value) {
            setText(value instanceof LocalDateTime ? FORMATO_SALIDA.format((LocalDateTime) value) : "");
        }
    }

    /** Muestra el estado del vuelo o del pasajero con su color. */
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
            if (!isSelected && value instanceof EstadoVuelo) {
                setForeground(Tema.colorEstado((EstadoVuelo) value));
            } else if (!isSelected && value instanceof EstadoPasajero) {
                setForeground(Tema.colorEstado((EstadoPasajero) value));
            }
            return this;
        }
    }

    /** Muestra cada avión del selector como "CC-AMA · Airbus A320 (174 asientos)". */
    private static final class RenderAvion extends DefaultListCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Avion) {
                Avion avion = (Avion) value;
                setText(avion.getMatricula() + " · " + avion.getModelo() + " (" + avion.getCantidadMaxima() + " asientos)");
            }
            return this;
        }
    }
}