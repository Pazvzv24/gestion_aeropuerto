import java.util.Random;

public class Avion {
    private int id;
    private int cantidadMaxima;
    private int pasajerosActuales;
    private boolean estado;

    // a. Constructor con todos los valores como argumentos
    public Avion(int id, int cantidadMaxima, int pasajerosActuales, boolean estado) {
        this.id = id;
        this.cantidadMaxima = cantidadMaxima;
        this.pasajerosActuales = pasajerosActuales;
        this.estado = estado;
    }

    // b. Constructor con valores aleatorios
    public Avion(int id) {
        Random rand = new Random();
        this.id = id;
        this.cantidadMaxima = rand.nextInt(100) + 50; // Max entre 50 y 150
        this.pasajerosActuales = rand.nextInt(this.cantidadMaxima); // Entre 0 y max
        this.estado = rand.nextBoolean(); // True o False aleatorio
    }

    // c. Retorna ID
    public int getId() { return id; }

    // d. Retorna capacidad máxima
    public int getCantidadMaxima() { return cantidadMaxima; }

    // e. Retorna cantidad actual
    public int getPasajerosActuales() { return pasajerosActuales; }

    // f. Retorna estado
    public boolean getEstado() { return estado; }

    // g. Método para bajar pasajeros
    public OperationResult bajarPasajeros(int cantidad) {
        if (cantidad <= 0) return OperationResult.INVALID_AMOUNT;
        if (pasajerosActuales - cantidad < 0) return OperationResult.CAPACITY_EXCEEDED; 
    
        pasajerosActuales -= cantidad;
        return OperationResult.SUCCESS; // <--- ESTO TAMBIÉN
    
    }
    // h. Método para subir  pasajeros
    public OperationResult subirPasajeros(int cantidad) {
        if (!estado) return OperationResult.FLIGHT_CANCELLED;
        if (cantidad <= 0) return OperationResult.INVALID_AMOUNT;
        if (pasajerosActuales + cantidad > cantidadMaxima) return OperationResult.CAPACITY_EXCEEDED;
    
        pasajerosActuales += cantidad;
        return OperationResult.SUCCESS; // <--- ESTO ES LO QUE EL MAIN ESTÁ ESPERANDO
    }
}


