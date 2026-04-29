public class Pasajero {
    private String nombre;
    private String id;

    public Pasajero(String nombre, String ID) {
        this.nombre = nombre;
        this.id = ID;
    }

    public String getId () {
        return id;
    }
    public String getNombre() {
        return nombre;
    }
    
}
