package com.nttdata.cambioexpress.domain.model;

import com.nttdata.cambioexpress.domain.exception.ReglaDeNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TipoCambioTest {
    private final TipoCambio tipoCambio = new TipoCambio(
            Moneda.USD,
            new BigDecimal("3.712"),
            new BigDecimal("3.786"),
            LocalDate.now()
    );

    @Test
    void compraDeDolaresUsaTipoDeVentaDeLaCasa() {
        assertEquals(new BigDecimal("378.60"),
                tipoCambio.convertir(new BigDecimal("100"), Operacion.COMPRA));
    }

    @Test
    void ventaDeDolaresUsaTipoDeCompraDeLaCasa() {
        assertEquals(new BigDecimal("371.20"),
                tipoCambio.convertir(new BigDecimal("100"), Operacion.VENTA));
    }

    @Test
    void redondeaElResultadoADosDecimales() {
        BigDecimal resultado = tipoCambio.convertir(new BigDecimal("10.55"), Operacion.COMPRA);

        assertEquals(new BigDecimal("39.94"), resultado);
        assertEquals(2, resultado.scale());
    }

    @Test
    void rechazaMontoSuperiorAlLimitePorOperacion() {
        ReglaDeNegocioException exception = assertThrows(ReglaDeNegocioException.class,
                () -> tipoCambio.convertir(new BigDecimal("10000.01"), Operacion.COMPRA));

        assertEquals("El monto supera el límite de 10000.00 por operación", exception.getMessage());
    }

    @Test
    void rechazaMontoCero() {
        ReglaDeNegocioException exception = assertThrows(ReglaDeNegocioException.class,
                () -> tipoCambio.convertir(BigDecimal.ZERO, Operacion.VENTA));

        assertEquals("El monto debe ser mayor que cero", exception.getMessage());
    }

    @Test
    void rechazaTipoDeCompraNoPositivo() {
        assertThrows(IllegalArgumentException.class, () -> new TipoCambio(
                Moneda.USD, BigDecimal.ZERO, new BigDecimal("3.786"), LocalDate.now()));
    }

    @Test
    void rechazaTipoDeVentaMenorOIgualAlTipoDeCompra() {
        assertThrows(IllegalArgumentException.class, () -> new TipoCambio(
                Moneda.USD, new BigDecimal("3.786"), new BigDecimal("3.712"), LocalDate.now()));
        assertThrows(IllegalArgumentException.class, () -> new TipoCambio(
                Moneda.USD, new BigDecimal("3.712"), new BigDecimal("3.712"), LocalDate.now()));
    }
}
