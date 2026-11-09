package io.github.pazvzv24.aeropuerto;

import java.io.PrintStream;
import java.util.OptionalInt;
import java.util.Scanner;

/**
 * Punto de entrada: menú interactivo por consola.
 * Toda la entrada del usuario se valida: si escribe algo que no es un número,
 * el programa vuelve a preguntar en vez de cerrarse con un error.
 */
public class Main {

    private static final int OPCION_SALIR = 7;

    public static void main(String[] args) {
        Aeropuerto aeropuerto = new Aeropuerto("Aeropuerto Internacional");
        try (Scanner scanner = new Scanner(System.in)) {
            ejecutarMenu(aeropuerto, scanner, System.out);
        }
    }

    /** Muestra el menú y procesa opciones hasta que el usuario elige salir o termina la entrada. */
    static void ejecutarMenu(Aeropuerto aeropuerto, Scanner scanner, PrintStream salida) {
        boolean salir = false;
        while (!salir) {
            mostrarMenu(aeropuerto, salida);
            OptionalInt opcion = leerEntero(scanner, salida, "Seleccione una opción: ");
            if (opcion.isEmpty()) {
                salida.println("\nNo hay más entrada. Saliendo...");
                return;
            }

            switch (opcion.getAsInt()) {
                case 1 -> listarAviones(aeropuerto, salida);
                case 2 -> salida.println("\nTotal de pasajeros a bordo en el aeropuerto: "
                        + aeropuerto.calcularTotalPasajeros());
                case 3 -> salida.println("\nTotal de asientos disponibles: "
                        + aeropuerto.calcularTotalDisponibles());
                case 4 -> buscarAvion(aeropuerto, scanner, salida);
                case 5 -> moverPasajeros(aeropuerto, scanner, salida, true);
                case 6 -> moverPasajeros(aeropuerto, scanner, salida, false);
                case OPCION_SALIR -> {
                    salir = true;
                    salida.println("Saliendo...");
                }
                default -> salida.println("Opción inválida: elija un número del 1 al " + OPCION_SALIR + ".");
            }
        }
    }

    private static void mostrarMenu(Aeropuerto aeropuerto, PrintStream salida) {
        salida.println("\n--- Aeropuerto: " + aeropuerto.getNombre() + " ---");
        salida.println("1. Listar los aviones.");
        salida.println("2. Total de pasajeros a bordo.");
        salida.println("3. Total de asientos disponibles.");
        salida.println("4. Buscar avión por ID.");
        salida.println("5. Subir pasajeros.");
        salida.println("6. Bajar pasajeros.");
        salida.println(OPCION_SALIR + ". Salir.");
    }

    private static void listarAviones(Aeropuerto aeropuerto, PrintStream salida) {
        salida.println("\n--- Lista de aviones ---");
        for (Avion avion : aeropuerto.getAviones()) {
            salida.println("ID: " + avion.getId() + " | Estado: " + describirEstado(avion));
        }
    }

    private static void buscarAvion(Aeropuerto aeropuerto, Scanner scanner, PrintStream salida) {
        OptionalInt id = leerEntero(scanner, salida, "Ingrese el ID del avión: ");
        if (id.isEmpty()) {
            return;
        }
        Avion avion = aeropuerto.buscarAvion(id.getAsInt());
        if (avion == null) {
            salida.println(OperationResult.AIRPLANE_NOT_FOUND.getMessage());
            return;
        }
        salida.println("ID: " + avion.getId()
                + " | Capacidad: " + avion.getCantidadMaxima()
                + " | A bordo: " + avion.getPasajerosActuales()
                + " | Disponibles: " + avion.getAsientosDisponibles()
                + " | Estado: " + describirEstado(avion));
    }

    private static void moverPasajeros(Aeropuerto aeropuerto, Scanner scanner, PrintStream salida, boolean subir) {
        OptionalInt id = leerEntero(scanner, salida, "Ingrese el ID del avión: ");
        if (id.isEmpty()) {
            return;
        }
        if (aeropuerto.buscarAvion(id.getAsInt()) == null) {
            mostrarResultado(OperationResult.AIRPLANE_NOT_FOUND, subir ? "Subir pasajeros" : "Bajar pasajeros", salida);
            return;
        }
        OptionalInt cantidad = leerEntero(scanner, salida,
                subir ? "¿Cuántos pasajeros desea subir?: " : "¿Cuántos pasajeros desea bajar?: ");
        if (cantidad.isEmpty()) {
            return;
        }
        OperationResult resultado = subir
                ? aeropuerto.subirPasajeros(id.getAsInt(), cantidad.getAsInt())
                : aeropuerto.bajarPasajeros(id.getAsInt(), cantidad.getAsInt());
        mostrarResultado(resultado, subir ? "Subir pasajeros" : "Bajar pasajeros", salida);
    }

    private static String describirEstado(Avion avion) {
        return avion.getEstado() ? "Activo" : "Cancelado";
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
}
            