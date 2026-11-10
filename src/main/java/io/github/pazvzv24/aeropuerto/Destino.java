package io.github.pazvzv24.aeropuerto;

/** Aeropuertos que opera la aerolínea, con su código IATA. */
public enum Destino {
    SCL("SCL", "Santiago"),
    ARI("ARI", "Arica"),
    IQQ("IQQ", "Iquique"),
    CJC("CJC", "Calama"),
    ANF("ANF", "Antofagasta"),
    LSC("LSC", "La Serena"),
    CCP("CCP", "Concepción"),
    PMC("PMC", "Puerto Montt"),
    PUQ("PUQ", "Punta Arenas"),
    LIM("LIM", "Lima"),
    EZE("EZE", "Buenos Aires"),
    GRU("GRU", "São Paulo"),
    BOG("BOG", "Bogotá");

    private final String codigo;
    private final String ciudad;

    Destino(String codigo, String ciudad) {
        this.codigo = codigo;
        this.ciudad = ciudad;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getCiudad() {
        return ciudad;
    }

    /** Ejemplo: "Santiago (SCL)". */
    @Override
    public String toString() {
        return ciudad + " (" + codigo + ")";
    }
}