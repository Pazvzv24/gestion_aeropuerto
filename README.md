# SISTEMA DE GESTION DE AEROPUERTO

> Simulación orientada a objetos de un sistema de gestión de pasajeros, implemetado en Java.

🔗 **Repositorio:** [github.com/Pazvzv24/sistema-aeropuerto-java](https://github.com/Pazvzv24/sistema-aeropuerto-java)

---

## 📋 Tabla de Contenido

- [Descripción](#descripcion)
- [Arquitectura del Proyecto](#arquitectura-del-proyecto)
- [Clases y Responsabilidades](#clases-y-responsabilidades)
- [Instalación y Ejecución](#instalación-y-ejecución)
- [Ejemplos de Uso](#ejemplos-de-uso)
- [Manejo de Resultados](#manejo-de-resultados)
- [Requisitos](#requisitos)
- [Autora](#autora)




## Descripción

Sistema de gestión de aeropuerto en Java que implementa POO. Simula el embarque y desembarque de pasajeros en múltiples aviones, incorporando validaciones y un sistema unificado de resultados de operación.

---

## Arquitectura del Proyecto

```
src/
├── Aeropuerto.java       # Entidad principal que agrupa aviones
├── Avion.java            # Modelo de avión con lógica pasajeros
├── Pasajero.java         # Modelo de pasajero
├── OperationResult.java  # Enum de resultados operación
└── Main.java             # Punto de entrada y menú interactivo
```

---

## Clases y Responsabilidades

### `Aeropuerto`
Representa el aeropuerto. Gestiona un arreglo hasta de 100 aviones y expone métodos para calcular estadísticas globales.

 | Método  | Descripción  |
 |---|---|
 | `Aeropuerto(String nombre)` | Constructor. Inicializa 5 aviones con atributos aleatorios. |
| `getNombre()` | Retorna el nombre del aeropuerto. |
| `getA()` | Retorna el arreglo completo de aviones. |
| `buscarAvion(int id)` | Busca y retorna un avión por su ID. Retorna `null` si no existe. |
| `calcularTotalPasajeros()` | Retorna el total de pasajeros en todos los aviones activos. |
| `calcularTotalDisponibles()` | Retorna el total de asientos disponibles en todos los aviones. |

---

### `Avion`
Encapsula el estado y la lógica de cada avión.

| Método | Descripción |
|---|---|
| `Avion(int id, int max, int actuales, boolean estado)` | Constructor con valores explícitos. |
| `Avion(int id)` | Constructor con valores generados aleatoriamente. |
| `subirPasajeros(int cantidad)` | Intenta embarcar pasajeros. Retorna un `OperationResult`. |
| `bajarPasajeros(int cantidad)` | Intenta desembarcar pasajeros. Retorna un `OperationResult`. |
| `getId()` | Retorna el identificador del avión. |
| `getCantidadMaxima()` | Retorna la capacidad máxima del avión. |
| `getPasajerosActuales()` | Retorna la cantidad actual de pasajeros. |
| `getEstado()` | Retorna `true` si el vuelo está activo, `false` si está cancelado. |

---

### `Pasajero`
Modelo simple que representa a un pasajero con nombre e ID.

| Campo | Tipo | Descripción |
|---|---|---|
| `nombre` | `String` | Nombre del pasajero |
| `id` | `String` | Identificador único |

---

### `OperationResult`
Enum que estandariza los resultados de todas las operaciones de pasajeros, incluyendo un mensaje descriptivo y un código de referencia.

| Constante | Código | Mensaje |
|---|---|---|
| `SUCCESS` | 200 | Operación exitosa |
| `CAPACITY_EXCEEDED` | 400 | El avión está lleno |
| `INVALID_AMOUNT` | 422 | Cantidad de pasajeros inválida |
| `FLIGHT_CANCELLED` | 503 | El vuelo está cancelado |
| `PASSENGER_NOT_FOUND` | 404 | Pasajero no encontrado |

---

## Diagrama de Clases

```
┌─────────────────────┐                  ┌──────────────────────────┐
│     Aeropuerto      │                  │          Avion           │
├─────────────────────┤      1   *       ├──────────────────────────┤
│ - nombre: String    │─────────────────▶│ - id: int                │
│ - A: Avion[100]     │                  │ - cantidadMaxima: int    │
├─────────────────────┤                  │ - pasajerosActuales: int │
│ + getNombre()       │                  │ - estado: boolean        │
│ + getA()            │                  ├──────────────────────────┤
│ + buscarAvion()     │                  │ + subirPasajeros()       │
│ + calcularTotal...  │                  │ + bajarPasajeros()       │
│ + calcularDisp...   │                  └──────────────────────────┘
└─────────────────────┘                              │ returns
                                                     ▼
                                         ┌──────────────────────────┐
                                         │     OperationResult      │
                                         ├──────────────────────────┤
                                         │ SUCCESS                  │
                                         │ CAPACITY_EXCEEDED        │
                                         │ INVALID_AMOUNT           │
                                         │ FLIGHT_CANCELLED         │
                                         │ PASSENGER_NOT_FOUND      │
                                         └──────────────────────────┘
```
---

## Instalación y Ejecución

Requisitos previos: JDK 8 o superior.

```bash
# 1. Clonar el repositorio
git clone https://github.com/Pazvzv24/sistema-aeropuerto-java.git
cd sistema-aeropuerto-java

# 2. Compilar todos los archivos fuente
javac *.java

# 3. Ejecutar el programa
java Main
```

Al ejecutar, el programa presenta un menú interactivo:

```
--- Aeropuerto: Aeropuerto Internacional ---
1. Listar el ID de todos los aviones.
2. Total de pasajeros actuales en todo los aviones.
3. Total de asientos disponibles.
4. Buscar avión por ID.
5. Subir pasajeros (requiere cantidad).
6. Bajar pasajeros (requiere cantidad).
7. SALIR.
Seleccione una opción:
```
---

## Ejemplos de Uso

```java
// El aeropuerto se inicializa con 5 aviones aleatorios
Aeropuerto airport = new Aeropuerto("Aeropuerto Internacional");

// Listar todos los aviones
for (Avion a : airport.getA()) {
    if (a != null) {
        System.out.println("ID: " + a.getId() + " | Estado: " + (a.getEstado() ? "Activo" : "Inactivo"));
    }
}

// Buscar un avión por ID
Avion avion = airport.buscarAvion(3);

// Intentar embarcar pasajeros
OperationResult resultado = avion.subirPasajeros(10);
if (resultado.isSuccess()) {
    System.out.println("[OK] Embarque exitoso.");
} else {
    System.out.println("[ERROR] " + resultado.getMessage() + " (Cod: " + resultado.getStatusCode() + ")");
}

// Estadísticas globales
System.out.println("Total pasajeros: " + airport.calcularTotalPasajeros());
System.out.println("Asientos disponibles: " + airport.calcularTotalDisponibles());
```

---

## Manejo de Resultados

El sistema utiliza el patron "Result Object" mediante el enum `OperationResult`, evitando el uso de excepciones para flujo de control. Cada operación retorna un resultado que puede inspeccionarse de forma limpia:

```java
OperationResult result = avion.subirPasajeros(cantidad);

System.out.println(result.getMessage());    // Mensaje legible
System.out.println(result.getStatusCode()); // Código de referencia
System.out.println(result.isSuccess());     // true / false
```

---

## Requisitos

- **Java:** JDK 8+
- **Paradigma:** POO
- **Dependencias externas:** Ninguna

---

## 😎 Autora

**Marcela Luza Barraza**  
Técnico en Programación y Análisis de Sistemas — Instituto Profesional AIEP  
Año 2026













