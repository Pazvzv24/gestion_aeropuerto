import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        // 4a. Creamos el aeropuerto (el constructor crea los 100 aviones automáticamente)
        Aeropuerto airport = new Aeropuerto("Aeropuerto Internacional");

        boolean salir = false;

        while (!salir) {
            System.out.println("\n--- Aeropuerto: " + airport.getNombre() + " ---");
            // 1. Listar ID de todos los aviones
            System.out.println("1. Listar el ID de todos los aviones.");
            // 2. Total pasajeros en todos los aviones (aeropuerto)
            System.out.println("2. Total de pasajeros actuales en todo los aviones.");
            // 3. Cantidad de asientos disponibles en todos los aviones
            System.out.println("3. Total de asientos disponibles.");
            // 4. Buscar y mostrar datos de aviones a traves de ID
            System.out.println("4. Buscar avión por ID.");
            // 5. Subir Pasajeros
            System.out.println("5. Subir pasajeros (requiere cantidad).");
            // 6. Bajar Pasajeros
            System.out.println("6. Bajar pasajeros (requiere cantidad).");
            // 7. Salir
            System.out.println("7. SALIR.");
            // 8. Seleccionar opcion (1 - 7)
            System.out.print("Seleccione una opción: ");

            int opcion = scanner.nextInt();

            switch (opcion) {
                case 1:
                    System.out.println("\n--- Lista de Aviones ---");
                    for (Avion a : airport.getA()) {
                        if (a != null) {
                            System.out.println("ID: " + a.getId() + " | Estado: " + (a.getEstado() ? "Activo" : "Inactivo"));
                        }
                    }
                    break;

                case 2:
                    // 4b. Usamos el nuevo método de cálculo
                    System.out.println("\nTotal de pasajeros actuales en todo el aeropuerto: " + airport.calcularTotalPasajeros());
                    break;

                case 3:
                    // 4c. Usamos el nuevo método de cálculo
                    System.out.println("\nTotal de asientos disponibles: " + airport.calcularTotalDisponibles());
                    break;

                case 4:
                    System.out.print("Ingrese ID del avión: ");
                    int idBusqueda = scanner.nextInt();
                    Avion enc = airport.buscarAvion(idBusqueda);
                    if (enc != null) {
                        System.out.println("Datos: ID=" + enc.getId() + 
                                           ", Max=" + enc.getCantidadMaxima() + 
                                           ", Actual=" + enc.getPasajerosActuales() + 
                                           ", Estado=" + (enc.getEstado() ? "Activo" : "Inactivo"));
                    } else {
                        System.out.println("Error: Avión no encontrado.");
                    }
                    break;

                case 5:
                    System.out.print("Ingrese ID del avión: ");
                    int idSubir = scanner.nextInt();
                    Avion avSubir = airport.buscarAvion(idSubir);
                    if (avSubir != null) {
                        System.out.print("¿Cuántos pasajeros desea subir?: ");
                        int cant = scanner.nextInt();
                        procesar(avSubir.subirPasajeros(cant), "Subir Pasajeros");
                    } else {
                        System.out.println("Error: Avión no encontrado.");
                    }
                    break;

                case 6:
                    System.out.print("Ingrese ID del avión: ");
                    int idBajar = scanner.nextInt();
                    Avion avBajar = airport.buscarAvion(idBajar);
                    if (avBajar != null) {
                        System.out.print("¿Cuántos pasajeros desea bajar?: ");
                        int cant = scanner.nextInt();
                        procesar(avBajar.bajarPasajeros(cant), "Bajar Pasajeros");
                    } else {
                        System.out.println("Error: Avión no encontrado.");
                    }
                    break;

                case 7:
                    salir = true;
                    System.out.println("Saliendo...");
                    break;

                default:
                    System.out.println("Opción inválida.");
            }
        }
        scanner.close();
    }

    private static void procesar(OperationResult res, String accion) {
        if (res.isSuccess()) {
            System.out.println("[OK] " + accion + ": Éxito.");
        } else {
            System.out.println("[ERROR] " + accion + ": " + res.getMessage() + " (Cod: " + res.getStatusCode() + ")");
        }
    }
}

            