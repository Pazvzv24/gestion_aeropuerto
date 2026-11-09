package io.github.pazvzv24.aeropuerto;

/**
 * Resultado de una operación sobre un avión (patrón Result Object).
 * Las operaciones devuelven uno de estos valores en vez de lanzar excepciones,
 * así el flujo normal del programa no depende de capturar errores.
 * Los códigos siguen la idea de los códigos HTTP para que sean fáciles de reconocer.
 */
public enum OperationResult {
    SUCCESS("Operación exitosa", 200),
    CAPACITY_EXCEEDED("Error: El avión no tiene asientos suficientes", 400),
    NOT_ENOUGH_PASSENGERS("Error: No hay tantos pasajeros a bordo", 409),
    INVALID_AMOUNT("Error: Cantidad de pasajeros inválida", 422),
    AIRPLANE_NOT_FOUND("Error: Avión no encontrado", 404),
    FLIGHT_CANCELLED("Error: El vuelo está cancelado", 503);

    private final String message;
    private final int statusCode;

    OperationResult(String message, int statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public boolean isSuccess() {
        return this == SUCCESS;
    }
}