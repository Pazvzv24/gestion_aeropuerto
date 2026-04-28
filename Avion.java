import java.util.Random;

public class Avion {
    private int id;
    private int cantidadMaxima;
    private int pasajerosActuales;
    private boolean estado;

    // a. Contructor con todos los valores como argumentos
        public Avion(int id, int cantidadMaxima, int pasajerosActuales, boolean estado) {
        this.id = id;
        this.cantidadMaxima = cantidadMaxima;
        this.pasajerosActuales = pasajerosActuales;
        this.estado = estado;
    }

}
