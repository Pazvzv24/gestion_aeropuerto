public class Aeropuerto {
    private String nombre;
    private Avion[] A = new Avion[100]; // Requisito 3b: Arreglo de 100 (se mantiene intacto)

    // 4a. Constructor que crea los aviones con atributos aleatorios
    public Aeropuerto(String nombre) {
        this.nombre = nombre;
        // AJUSTE PARA PRUEBA: Cambiamos A.length (100) por 5
        for (int i = 0; i < 5; i++) {
            // Pasamos (i + 1) para que el ID sea correlativo (1 al 5)
            A[i] = new Avion(i + 1);
        }
        // Las posiciones del arreglo A[5] hasta A[99] quedarán como null
    }

    // 4b. Método para total de pasajeros (se mantiene intacto)
    public int calcularTotalPasajeros() {
        int total = 0;
        for (Avion a : A) {
            if (a != null) {
                total += a.getPasajerosActuales();
            }
        }
        return total;
    }

    // 4c. Método para total de espacios disponibles (se mantiene intacto)
    public int calcularTotalDisponibles() {
        int total = 0;
        for (Avion a : A) {
            if (a != null) {
                total += (a.getCantidadMaxima() - a.getPasajerosActuales());
            }
        }
        return total;
    }

    // 4d. Método que retorna la información (el objeto) del avión buscado (se mantiene intacto)
    public Avion buscarAvion(int id) {
        for (Avion a : A) {
            if (a != null && a.getId() == id) {
                return a;
            }
        }
        return null;
    }

    // Getters adicionales (se mantienen intactos)
    public Avion[] getA() {
        return A;
    }

    public String getNombre() {
        return nombre;
    }
}
