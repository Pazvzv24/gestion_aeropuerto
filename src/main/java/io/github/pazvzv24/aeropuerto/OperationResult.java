package io.github.pazvzv24.aeropuerto;

/**
 * Resultado de una operación sobre la flota o los vuelos (patrón Result Object).
 * Las operaciones devuelven uno de estos valores en vez de lanzar excepciones,
 * así el flujo normal del programa no depende de capturar errores.
 * Los códigos siguen la idea de los códigos HTTP para que sean fáciles de reconocer.
 */
public enum OperationResult {
    SUCCESS("Operación exitosa", 200),
    CAPACITY_EXCEEDED("Error: El avión no tiene asientos suficientes", 400),
    AIRPLANE_NOT_FOUND("Error: Avión no encontrado", 404),
    FLIGHT_NOT_FOUND("Error: Vuelo no encontrado", 404),
    PASSENGER_NOT_FOUND("Error: El pasajero no está en este vuelo", 404),
    NOT_ENOUGH_PASSENGERS("Error: No hay tantos pasajeros a bordo", 409),
    OPERATION_NOT_ALLOWED("Error: Operación no permitida en el estado actual", 409),
    INVALID_TRANSITION("Error: Cambio de estado no permitido", 409),
    AIRPLANE_NOT_EMPTY("Error: El avión aún tiene pasajeros a bordo", 409),
    AIRPLANE_NOT_AVAILABLE("Error: El avión no está disponible para este vuelo", 409),
    AIRPLANE_ASSIGNED_TO_FLIGHT("Error: El avión está operando un vuelo; gestiónelo desde Vuelos", 409),
    DUPLICATE_REGISTRATION("Error: Ya existe un avión con esa matrícula", 409),
    DUPLICATE_FLIGHT("Error: Ya existe un vuelo con ese código", 409),
    DUPLICATE_PASSENGER("Error: El pasajero ya tiene check-in en este vuelo", 409),
    ALREADY_BOARDED("Error: El pasajero ya embarcó", 409),
    CHECK_IN_CLOSED("Error: El check-in de este vuelo está cerrado", 409),
    SEAT_TAKEN("Error: El asiento ya está ocupado", 409),
    BOARDING_PASS_UNAVAILABLE("Error: No se puede emitir el pase de abordar en el estado actual", 409),
    FLEET_FULL("Error: La flota alcanzó su capacidad máxima", 409),
    INVALID_AMOUNT("Error: Cantidad de pasajeros inválida", 422),
    INVALID_REGISTRATION("Error: Matrícula inválida, use el formato CC-XXX", 422),
    INVALID_SEAT("Error: Asiento inválido para este avión (ejemplo: 12C)", 422),
    INVALID_FLIGHT_DATA("Error: Datos del vuelo inválidos; revise el código (AM123), la ruta y la hora", 422),
    INVALID_PASSENGER_DATA("Error: Datos del pasajero inválidos; revise el nombre y el documento", 422);

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