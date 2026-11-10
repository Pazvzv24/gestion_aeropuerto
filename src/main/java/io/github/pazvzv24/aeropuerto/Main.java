package io.github.pazvzv24.aeropuerto;

import io.github.pazvzv24.aeropuerto.persistencia.ConfiguracionBD;
import io.github.pazvzv24.aeropuerto.persistencia.RepositorioAerolinea;
import io.github.pazvzv24.aeropuerto.persistencia.RepositorioException;
import io.github.pazvzv24.aeropuerto.persistencia.RepositorioPostgres;
import io.github.pazvzv24.aeropuerto.ui.Tema;
import io.github.pazvzv24.aeropuerto.ui.VentanaPrincipal;
import java.awt.GraphicsEnvironment;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Scanner;
import java.util.function.Consumer;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Punto de entrada. Por defecto abre la interfaz gráfica; con el argumento
 * {@code --consola} (o en un equipo sin pantalla) usa el menú por consola.
 * Toda la entrada del usuario se valida: si escribe algo inválido,
 * el programa avisa y vuelve a preguntar en vez de cerrarse con un error.
 */
public class Main {

    private static final String NOMBRE_AEROLINEA = "Altamar Airlines";
    private static final int OPCION_VUELOS = 8;
    private static final int OPCION_SALIR = 9;
    private static final int VOLVER_DE_VUELOS = 8;

    /** Formato de fecha y hora para la consola, por ejemplo 24-12-2026 14:30. */
    static final DateTimeFormatter FORMATO_FECHA_HORA =
            DateTimeFormatter.ofPattern("dd-MM-uuuu HH:mm").withResolverStyle(ResolverStyle.STRICT);

    public static void main(String[] args) {
        Inicio inicio = iniciar();
        Aerolinea aerolinea = inicio.aerolinea();

        if (usarConsola(args)) {
            System.out.println(inicio.mensaje());
            guardarDespuesDeCadaCambio(inicio, mensaje -> System.out.println("[ERROR] " + mensaje));
            try (Scanner scanner = new Scanner(System.in)) {
                ejecutarMenu(aerolinea, scanner, System.out);
            }
            return;
        }

        SwingUtilities.invokeLater(() -> {
            Tema.iniciar();
            VentanaPrincipal ventana = new VentanaPrincipal(aerolinea, inicio.mensaje());
            guardarDespuesDeCadaCambio(inicio, mensaje -> JOptionPane.showMessageDialog(ventana, mensaje,
                    "Error al guardar", JOptionPane.ERROR_MESSAGE));
            ventana.setVisible(true);
        });
    }

    /**
     * Cómo partió la aplicación: la aerolínea, dónde se guardan sus datos (null = solo en memoria)
     * y un mensaje para el usuario.
     */
    record Inicio(Aerolinea aerolinea, RepositorioAerolinea repositorio, String mensaje) {
    }

    /**
     * Si hay un archivo .env (o variables de entorno) con los datos de PostgreSQL, carga la aerolínea
     * desde la base de datos; la primera vez guarda los datos de demostración.
     * Si no hay configuración o la base no responde, funciona en memoria y lo avisa.
     */
    static Inicio iniciar() {
        Optional<ConfiguracionBD> configuracion;
        try {
            configuracion = ConfiguracionBD.desdeEntorno();
        } catch (IllegalArgumentException e) {
            return enMemoria("Configuración de la base de datos inválida: " + e.getMessage()
                    + " Se usa el modo memoria.");
        }
        if (configuracion.isEmpty()) {
            return enMemoria("Modo memoria: los datos no se guardan al cerrar.");
        }
        RepositorioAerolinea repositorio = new RepositorioPostgres(configuracion.get(), NOMBRE_AEROLINEA);
        try {
            Optional<Aerolinea> guardada = repositorio.cargar();
            if (guardada.isPresent()) {
                return new Inicio(guardada.get(), repositorio, "Datos guardados en PostgreSQL.");
            }
            Aerolinea demostracion = Aerolinea.altamarAirlines();
            repositorio.guardar(demostracion);
            return new Inicio(demostracion, repositorio, "PostgreSQL: se guardaron los datos de demostración.");
        } catch (RepositorioException e) {
            avisarDetalle(e);
            return enMemoria(e.getMessage() + " Se usa el modo memoria: los cambios no se guardarán.");
        }
    }

    private static Inicio enMemoria(String mensaje) {
        return new Inicio(Aerolinea.altamarAirlines(), null, mensaje);
    }

    /** Después de cada operación exitosa guarda en la base de datos; si falla, avisa al usuario. */
    private static void guardarDespuesDeCadaCambio(Inicio inicio, Consumer<String> avisoError) {
        RepositorioAerolinea repositorio = inicio.repositorio();
        if (repositorio == null) {
            return;
        }
        Aerolinea aerolinea = inicio.aerolinea();
        aerolinea.setAlCambiar(() -> {
            try {
                repositorio.guardar(aerolinea);
            } catch (RepositorioException e) {
                avisarDetalle(e);
                avisoError.accept(e.getMessage());
            }
        });
    }

    /** El detalle técnico va a la consola de errores (nunca incluye contraseñas). */
    private static void avisarDetalle(RepositorioException e) {
        if (e.getCause() != null) {
            System.err.println("Detalle: " + e.getCause().getMessage());
        }
    }

    /** Consola si se pide con --consola o si el equipo no tiene pantalla (por ejemplo, un servidor). */
    static boolean usarConsola(String[] args) {
        return Arrays.asList(args).contains("--consola") || GraphicsEnvironment.isHeadless();
    }

    /** Muestra el menú y procesa opciones hasta que el usuario elige salir o termina la entrada. */
    static void ejecutarMenu(Aerolinea aerolinea, Scanner scanner, PrintStream salida) {
        boolean salir = false;
        while (!salir) {
            mostrarMenu(aerolinea, salida);
            OptionalInt opcion = leerEntero(scanner, salida, "Seleccione una opción: ");
            if (opcion.isEmpty()) {
                salida.println("\nNo hay más entrada. Saliendo...");
                return;
            }

            switch (opcion.getAsInt()) {
                case 1 -> listarFlota(aerolinea, salida);
                case 2 -> mostrarResumen(aerolinea, salida);
                case 3 -> buscarAvion(aerolinea, scanner, salida);
                case 4 -> moverPasajeros(aerolinea, scanner, salida, true);
                case 5 -> moverPasajeros(aerolinea, scanner, salida, false);
                case 6 -> cambiarEstado(aerolinea, scanner, salida);
                case 7 -> incorporarAvion(aerolinea, scanner, salida);
                case OPCION_VUELOS -> salir = !ejecutarMenuVuelos(aerolinea, scanner, salida);
                case OPCION_SALIR -> {
                    salir = true;
                    salida.println("Saliendo...");
                }
                default -> salida.println("Opción inválida: elija un número del 1 al " + OPCION_SALIR + ".");
            }
        }
    }

    private static void mostrarMenu(Aerolinea aerolinea, PrintStream salida) {
        salida.println("\n--- " + aerolinea.getNombre() + " · Gestión de flota ---");
        salida.println("1. Listar la flota.");
        salida.println("2. Resumen de la flota.");
        salida.println("3. Buscar avión por matrícula.");
        salida.println("4. Subir pasajeros.");
        salida.println("5. Bajar pasajeros.");
        salida.println("6. Cambiar estado de un avión.");
        salida.println("7. Incorporar avión a la flota.");
        salida.println(OPCION_VUELOS + ". Vuelos y pasajeros.");
        salida.println(OPCION_SALIR + ". Salir.");
    }

    // ------------------------------------------------------------ Vuelos y pasajeros

    /**
     * Submenú de vuelos. Vuelve al menú principal con la opción 8.
     *
     * @return false si la entrada terminó (para que el menú principal también termine)
     */
    static boolean ejecutarMenuVuelos(Aerolinea aerolinea, Scanner scanner, PrintStream salida) {
        while (true) {
            salida.println("\n--- Vuelos y pasajeros ---");
            salida.println("1. Listar vuelos.");
            salida.println("2. Ver pasajeros de un vuelo.");
            salida.println("3. Programar vuelo.");
            salida.println("4. Hacer check-in.");
            salida.println("5. Embarcar pasajero.");
            salida.println("6. Anular check-in.");
            salida.println("7. Cambiar estado de un vuelo.");
            salida.println(VOLVER_DE_VUELOS + ". Volver al menú principal.");
            OptionalInt opcion = leerEntero(scanner, salida, "Seleccione una opción: ");
            if (opcion.isEmpty()) {
                salida.println("\nNo hay más entrada. Saliendo...");
                return false;
            }
            switch (opcion.getAsInt()) {
                case 1 -> listarVuelos(aerolinea, salida);
                case 2 -> mostrarManifiesto(aerolinea, scanner, salida);
                case 3 -> programarVuelo(aerolinea, scanner, salida);
                case 4 -> hacerCheckIn(aerolinea, scanner, salida);
                case 5 -> operarPasajero(aerolinea, scanner, salida, true);
                case 6 -> operarPasajero(aerolinea, scanner, salida, false);
                case 7 -> cambiarEstadoVuelo(aerolinea, scanner, salida);
                case VOLVER_DE_VUELOS -> {
                    return true;
                }
                default -> salida.println("Opción inválida: elija un número del 1 al " + VOLVER_DE_VUELOS + ".");
            }
        }
    }

    private static void listarVuelos(Aerolinea aerolinea, PrintStream salida) {
        salida.println("\n--- Vuelos ---");
        for (Vuelo vuelo : aerolinea.getVuelos()) {
            salida.println(describir(vuelo));
        }
    }

    private static String describir(Vuelo vuelo) {
        return vuelo.getCodigo() + " | " + vuelo.getRuta()
                + " | " + FORMATO_FECHA_HORA.format(vuelo.getSalida())
                + " | " + vuelo.getAvion().getMatricula()
                + " | " + vuelo.getCantidadPasajeros() + " con check-in"
                + " | " + vuelo.getEstado();
    }

    /** Lista de pasajeros con el documento enmascarado: nunca se muestra completo. */
    private static void mostrarManifiesto(Aerolinea aerolinea, Scanner scanner, PrintStream salida) {
        Optional<Vuelo> vuelo = leerVuelo(aerolinea, scanner, salida);
        if (vuelo.isEmpty()) {
            return;
        }
        salida.println(describir(vuelo.get()));
        List<CheckIn> manifiesto = vuelo.get().getManifiesto();
        if (manifiesto.isEmpty()) {
            salida.println("  Sin pasajeros registrados.");
        }
        for (CheckIn registro : manifiesto) {
            Pasajero pasajero = registro.getPasajero();
            salida.println("  " + pasajero.getNombre() + " | " + pasajero.getTipoDocumento()
                    + " " + pasajero.getDocumentoEnmascarado() + " | " + registro.getEstado());
        }
    }

    private static void programarVuelo(Aerolinea aerolinea, Scanner scanner, PrintStream salida) {
        Optional<String> codigo = leerTexto(scanner, salida, "Código del vuelo (formato AM123): ");
        if (codigo.isEmpty()) {
            return;
        }
        Optional<Destino> origen = elegir(scanner, salida, "Origen", Destino.values());
        if (origen.isEmpty()) {
            return;
        }
        Optional<Destino> destino = elegir(scanner, salida, "Destino", Destino.values());
        if (destino.isEmpty()) {
            return;
        }
        Optional<String> fecha = leerTexto(scanner, salida, "Fecha y hora de salida (DD-MM-AAAA HH:MM): ");
        if (fecha.isEmpty()) {
            return;
        }
        LocalDateTime salidaVuelo;
        try {
            salidaVuelo = LocalDateTime.parse(fecha.get(), FORMATO_FECHA_HORA);
        } catch (DateTimeParseException e) {
            salida.println("Fecha inválida: use el formato DD-MM-AAAA HH:MM, por ejemplo 24-12-2026 14:30.");
            return;
        }
        Optional<Avion> avion = leerAvion(aerolinea, scanner, salida);
        if (avion.isEmpty()) {
            return;
        }
        mostrarResultado(aerolinea.programarVuelo(codigo.get(), origen.get(), destino.get(),
                salidaVuelo, avion.get().getMatricula()), "Programar vuelo", salida);
    }

    private static void hacerCheckIn(Aerolinea aerolinea, Scanner scanner, PrintStream salida) {
        Optional<Vuelo> vuelo = leerVuelo(aerolinea, scanner, salida);
        if (vuelo.isEmpty()) {
            return;
        }
        Optional<String> nombre = leerTexto(scanner, salida, "Nombre completo del pasajero: ");
        if (nombre.isEmpty()) {
            return;
        }
        Optional<TipoDocumento> tipo = elegir(scanner, salida, "Tipo de documento", TipoDocumento.values());
        if (tipo.isEmpty()) {
            return;
        }
        Optional<String> documento = leerTexto(scanner, salida,
                tipo.get() == TipoDocumento.RUT ? "RUT (ej. 12.345.678-5): " : "Número de pasaporte: ");
        if (documento.isEmpty()) {
            return;
        }
        mostrarResultado(aerolinea.hacerCheckIn(vuelo.get().getCodigo(), nombre.get(), tipo.get(), documento.get()),
                "Check-in", salida);
    }

    /** Embarca (embarcar = true) o anula el check-in (embarcar = false) de un pasajero. */
    private static void operarPasajero(Aerolinea aerolinea, Scanner scanner, PrintStream salida, boolean embarcar) {
        Optional<Vuelo> vuelo = leerVuelo(aerolinea, scanner, salida);
        if (vuelo.isEmpty()) {
            return;
        }
        Optional<TipoDocumento> tipo = elegir(scanner, salida, "Tipo de documento", TipoDocumento.values());
        if (tipo.isEmpty()) {
            return;
        }
        Optional<String> documento = leerTexto(scanner, salida, "Documento del pasajero: ");
        if (documento.isEmpty()) {
            return;
        }
        String codigo = vuelo.get().getCodigo();
        OperationResult resultado = embarcar
                ? aerolinea.embarcarPasajero(codigo, tipo.get(), documento.get())
                : aerolinea.anularCheckIn(codigo, tipo.get(), documento.get());
        mostrarResultado(resultado, embarcar ? "Embarcar pasajero" : "Anular check-in", salida);
    }

    private static void cambiarEstadoVuelo(Aerolinea aerolinea, Scanner scanner, PrintStream salida) {
        Optional<Vuelo> encontrado = leerVuelo(aerolinea, scanner, salida);
        if (encontrado.isEmpty()) {
            return;
        }
        Vuelo vuelo = encontrado.get();
        List<EstadoVuelo> opciones = vuelo.getEstado().getSiguientes();
        if (opciones.isEmpty()) {
            salida.println("El vuelo " + vuelo.getCodigo() + " está " + vuelo.getEstado() + ": no admite más cambios.");
            return;
        }
        salida.println("Estado actual: " + vuelo.getEstado() + ". Acciones posibles:");
        for (int i = 0; i < opciones.size(); i++) {
            salida.println("  " + (i + 1) + ". " + vuelo.getEstado().accionHacia(opciones.get(i))
                    + " (pasa a " + opciones.get(i) + ")");
        }
        OptionalInt eleccion = leerEntero(scanner, salida, "Elija una acción: ");
        if (eleccion.isEmpty()) {
            return;
        }
        int indice = eleccion.getAsInt() - 1;
        if (indice < 0 || indice >= opciones.size()) {
            salida.println("Opción inválida.");
            return;
        }
        EstadoVuelo destino = opciones.get(indice);
        String accion = vuelo.getEstado().accionHacia(destino);
        mostrarResultado(aerolinea.cambiarEstadoVuelo(vuelo.getCodigo(), destino), accion, salida);
    }

    /** Pide un código de vuelo y lo busca; avisa si no existe. */
    private static Optional<Vuelo> leerVuelo(Aerolinea aerolinea, Scanner scanner, PrintStream salida) {
        Optional<String> codigo = leerTexto(scanner, salida, "Código del vuelo (ej. AM101): ");
        if (codigo.isEmpty()) {
            return Optional.empty();
        }
        Vuelo vuelo = aerolinea.buscarVuelo(codigo.get());
        if (vuelo == null) {
            salida.println(OperationResult.FLIGHT_NOT_FOUND.getMessage());
        }
        return Optional.ofNullable(vuelo);
    }

    /** Muestra una lista numerada y devuelve la opción elegida (vacío si la elección no es válida). */
    private static <T> Optional<T> elegir(Scanner scanner, PrintStream salida, String titulo, T[] opciones) {
        salida.println(titulo + ":");
        for (int i = 0; i < opciones.length; i++) {
            salida.println("  " + (i + 1) + ". " + opciones[i]);
        }
        OptionalInt eleccion = leerEntero(scanner, salida, "Elija una opción: ");
        if (eleccion.isEmpty()) {
            return Optional.empty();
        }
        int indice = eleccion.getAsInt() - 1;
        if (indice < 0 || indice >= opciones.length) {
            salida.println("Opción inválida.");
            return Optional.empty();
        }
        return Optional.of(opciones[indice]);
    }

    private static void listarFlota(Aerolinea aerolinea, PrintStream salida) {
        salida.println("\n--- Flota ---");
        for (Avion avion : aerolinea.getFlota()) {
            salida.println(describir(avion));
        }
    }

    private static void mostrarResumen(Aerolinea aerolinea, PrintStream salida) {
        salida.println("\nFlota activa: " + aerolinea.getCantidadAvionesActivos() + " de " + Aerolinea.CAPACIDAD_FLOTA);
        salida.println("Pasajeros a bordo: " + aerolinea.calcularTotalPasajeros());
        salida.println("Asientos libres en embarque: " + aerolinea.calcularAsientosEnEmbarque());
        for (EstadoAvion estado : EstadoAvion.values()) {
            salida.println("  " + estado + ": " + aerolinea.contarPorEstado(estado));
        }
    }

    private static void buscarAvion(Aerolinea aerolinea, Scanner scanner, PrintStream salida) {
        Optional<Avion> avion = leerAvion(aerolinea, scanner, salida);
        avion.ifPresent(a -> salida.println(describir(a) + " · " + a.getEstado().getDescripcion()));
    }

    private static void moverPasajeros(Aerolinea aerolinea, Scanner scanner, PrintStream salida, boolean subir) {
        String accion = subir ? "Subir pasajeros" : "Bajar pasajeros";
        Optional<Avion> avion = leerAvion(aerolinea, scanner, salida);
        if (avion.isEmpty()) {
            return;
        }
        OptionalInt cantidad = leerEntero(scanner, salida,
                subir ? "¿Cuántos pasajeros desea subir?: " : "¿Cuántos pasajeros desea bajar?: ");
        if (cantidad.isEmpty()) {
            return;
        }
        String matricula = avion.get().getMatricula();
        OperationResult resultado = subir
                ? aerolinea.subirPasajeros(matricula, cantidad.getAsInt())
                : aerolinea.bajarPasajeros(matricula, cantidad.getAsInt());
        mostrarResultado(resultado, accion, salida);
    }

    private static void cambiarEstado(Aerolinea aerolinea, Scanner scanner, PrintStream salida) {
        Optional<Avion> encontrado = leerAvion(aerolinea, scanner, salida);
        if (encontrado.isEmpty()) {
            return;
        }
        Avion avion = encontrado.get();
        List<EstadoAvion> opciones = new ArrayList<>(avion.getEstado().getSiguientes());
        if (opciones.isEmpty()) {
            salida.println("El avión " + avion.getMatricula() + " está " + avion.getEstado() + ": no admite más cambios.");
            return;
        }
        salida.println("Estado actual: " + avion.getEstado() + ". Acciones posibles:");
        for (int i = 0; i < opciones.size(); i++) {
            salida.println("  " + (i + 1) + ". " + avion.getEstado().accionHacia(opciones.get(i))
                    + " (pasa a " + opciones.get(i) + ")");
        }
        OptionalInt eleccion = leerEntero(scanner, salida, "Elija una acción: ");
        if (eleccion.isEmpty()) {
            return;
        }
        int indice = eleccion.getAsInt() - 1;
        if (indice < 0 || indice >= opciones.size()) {
            salida.println("Opción inválida.");
            return;
        }
        EstadoAvion destino = opciones.get(indice);
        mostrarResultado(aerolinea.cambiarEstado(avion.getMatricula(), destino),
                avion.getEstado().accionHacia(destino), salida);
    }

    private static void incorporarAvion(Aerolinea aerolinea, Scanner scanner, PrintStream salida) {
        Optional<String> matricula = leerTexto(scanner, salida, "Matrícula del nuevo avión (formato CC-XXX): ");
        if (matricula.isEmpty()) {
            return;
        }
        ModeloAvion[] modelos = ModeloAvion.values();
        salida.println("Modelos disponibles:");
        for (int i = 0; i < modelos.length; i++) {
            salida.println("  " + (i + 1) + ". " + modelos[i] + " (" + modelos[i].getCapacidad() + " asientos)");
        }
        OptionalInt eleccion = leerEntero(scanner, salida, "Elija un modelo: ");
        if (eleccion.isEmpty()) {
            return;
        }
        int indice = eleccion.getAsInt() - 1;
        if (indice < 0 || indice >= modelos.length) {
            salida.println("Opción inválida.");
            return;
        }
        mostrarResultado(aerolinea.incorporarAvion(matricula.get(), modelos[indice]), "Incorporar avión", salida);
    }

    private static String describir(Avion avion) {
        return avion.getMatricula() + " | " + avion.getModelo()
                + " | " + avion.getPasajerosActuales() + "/" + avion.getCantidadMaxima() + " pasajeros"
                + " | " + avion.getEstado();
    }

    /** Pide una matrícula y busca el avión; avisa si no existe. */
    private static Optional<Avion> leerAvion(Aerolinea aerolinea, Scanner scanner, PrintStream salida) {
        Optional<String> matricula = leerTexto(scanner, salida, "Ingrese la matrícula (ej. CC-AMA): ");
        if (matricula.isEmpty()) {
            return Optional.empty();
        }
        Avion avion = aerolinea.buscarAvion(matricula.get());
        if (avion == null) {
            salida.println(OperationResult.AIRPLANE_NOT_FOUND.getMessage());
        }
        return Optional.ofNullable(avion);
    }

    static void mostrarResultado(OperationResult resultado, String accion, PrintStream salida) {
        if (resultado.isSuccess()) {
            salida.println("[OK] " + accion + ": operación exitosa.");
        } else {
            salida.println("[ERROR] " + accion + ": " + resultado.getMessage()
                    + " (código " + resultado.getStatusCode() + ")");
        }
    }

    /**
     * Lee un número entero. Si el usuario escribe otra cosa, avisa y vuelve a preguntar.
     *
     * @return el número leído, o vacío si la entrada terminó (por ejemplo, Ctrl+Z / Ctrl+D)
     */
    static OptionalInt leerEntero(Scanner scanner, PrintStream salida, String mensaje) {
        while (true) {
            salida.print(mensaje);
            if (!scanner.hasNextLine()) {
                return OptionalInt.empty();
            }
            String texto = scanner.nextLine().trim();
            try {
                return OptionalInt.of(Integer.parseInt(texto));
            } catch (NumberFormatException e) {
                salida.println("Entrada inválida: escriba un número entero.");
            }
        }
    }

    /**
     * Lee un texto no vacío. Si el usuario solo presiona Enter, vuelve a preguntar.
     *
     * @return el texto leído (sin espacios alrededor), o vacío si la entrada terminó
     */
    static Optional<String> leerTexto(Scanner scanner, PrintStream salida, String mensaje) {
        while (true) {
            salida.print(mensaje);
            if (!scanner.hasNextLine()) {
                return Optional.empty();
            }
            String texto = scanner.nextLine().trim();
            if (!texto.isEmpty()) {
                return Optional.of(texto);
            }
            salida.println("Entrada inválida: el valor no puede estar vacío.");
        }
    }
}