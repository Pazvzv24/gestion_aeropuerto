public enum OperationResult {
    SUCCESS("Operación exitosa", 200),
    CAPACITY_EXCEEDED("Error: El avión está lleno", 400),
    INVALID_AMOUNT("Error: Cantidad de pasajeros inválida", 422),
    FLIGHT_CANCELLED("Error: El vuelo está cancelado", 503),
    PASSENGER_NOT_FOUND("Error: Pasajero no encontrado", 404);

    private final String message;
    private final int statusCode;

    OperationResult(String message, int statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }

    public String getMessage() { return message; }
    public int getStatusCode() { return statusCode; }
    public boolean isSuccess() { return this == SUCCESS; }
}
