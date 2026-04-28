public enum OperationResult {
    SUCCESS("Operación exitosa", 200),
    CAPACITY_EXCEEDED("Error: El avión está lleno", 400),
    INVALID_AMOUNT("Error: Cantidad de pasajeros no válida", 422),
    PASSENGER_NOT_FOUND("Error: Pasajero no encontrado", 400);

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
