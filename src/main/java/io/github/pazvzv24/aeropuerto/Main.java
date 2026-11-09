package io.github.pazvzv24.aeropuerto;

import io.github.pazvzv24.aeropuerto.ui.Tema;
import io.github.pazvzv24.aeropuerto.ui.VentanaPrincipal;
import java.awt.GraphicsEnvironment;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Scanner;
import javax.swing.SwingUtilities;

/**
 * Punto de entrada. Por defecto abre la interfaz gráfica; con el argumento
 * {@code --consola} (o en un equipo sin pantalla) usa el menú por consola.
 * Toda la entrada del usuario se valida: si escribe algo inválido,
 * el programa avisa y vuelve a preguntar en vez de cerrarse con un error.
 */
public class Main {

    private static final int OPCION_SALIR = 8;

    public static void main(String[] args) {
        Aerolinea aerolinea = Aerolinea.altamarAirlines();

        if (usarConsola(args)) {
            try (Scanner scanner = new Scanner(System.in)) {
                ejecutarMenu(aerolinea, scanner, System.out);
            }
            return;
        }

        SwingUtilities.invokeLater(() -> {
            Tema.iniciar();
            new VentanaPrincipal(aerolinea).setVisible(true);
        });
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
        salida.println(OPCION_SALIR + ". Salir.");
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