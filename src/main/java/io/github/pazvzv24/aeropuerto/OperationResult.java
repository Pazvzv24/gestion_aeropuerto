package io.github.pazvzv24.aeropuerto;

/**
 * Resultado de una operación sobre la flota (patrón Result Object).
 * Las operaciones devuelven uno de estos valores en vez de lanzar excepciones,
 * así el flujo normal del programa no depende de capturar errores.
 * Los códigos siguen la idea de los códigos HTTP para que sean fáciles de reconocer.
 */
public enum OperationResult {
    SUCCESS("Operación exitosa", 200),
    CAPACITY_EXCEEDED("Error: El avión no tiene asientos suficientes", 400),
    AIRPLANE_NOT_FOUND("Error: Avión no encontrado", 404),
    NOT_ENOUGH_PASSENGERS("Error: No hay tantos pasajeros a bordo", 409),
    OPERATION_NOT_ALLOWED("Error: Operación no permitida en el estado actual del avión", 409),
    INVALID_TRANSITION("Error: Cambio de estado no permitido", 409),
    AIRPLANE_NOT_EMPTY("Error: El avión aún tiene pasajeros a bordo", 409),
    DUPLICATE_REGISTRATION("Error: Ya existe un avión con esa matrícula", 409),
    FLEET_FULL("Error: La flota alcanzó su capacidad máxima", 409),
    INVALID_AMOUNT("Error: Cantidad de pasajeros inválida", 422),
    INVALID_REGISTRATION("Error: Matrícula inválida, use el formato CC-XXX", 422);

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