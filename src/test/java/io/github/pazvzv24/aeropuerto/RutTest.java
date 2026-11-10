package io.github.pazvzv24.aeropuerto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RutTest {

    @Test
    @DisplayName("El dígito verificador se calcula con el módulo 11 (incluye los casos K y 0)")
    void calcularDigitoVerificador_ConModulo11() {
        assertEquals('5', Rut.calcularDigitoVerificador("12345678"));
        assertEquals('K', Rut.calcularDigitoVerificador("10000013"));
        assertEquals('0', Rut.calcularDigitoVerificador("10000004"));
    }

    @Test
    @DisplayName("Un RUT es válido con o sin puntos y guion, y con la K en minúscula")
    void esValido_AceptaDistintosFormatos() {
        assertTrue(Rut.esValido("12.345.678-5"));
        assertTrue(Rut.esValido("123456785"));
        assertTrue(Rut.esValido("10.000.013-k"));
        assertTrue(Rut.esValido(" 11.111.111-1 "));
    }

    @Test
    @DisplayName("Un RUT con dígito verificador incorrecto o con letras no es válido")
    void esValido_RechazaRutIncorrecto() {
        assertFalse(Rut.esValido("12.345.678-9"));
        assertFalse(Rut.esValido("12.34A.678-5"));
        assertFalse(Rut.esValido(""));
        assertFalse(Rut.esValido(null));
    }

    @Test
    @DisplayName("El RUT se formatea con puntos y guion, y se enmascara ocultando 3 dígitos")
    void formatearYEnmascarar() {
        assertEquals("12.345.678-5", Rut.formatear("123456785"));
        assertEquals("12.345.***-5", Rut.enmascarar("12.345.678-5"));
        assertEquals("10.000.***-K", Rut.enmascarar("10000013k"));
    }
}