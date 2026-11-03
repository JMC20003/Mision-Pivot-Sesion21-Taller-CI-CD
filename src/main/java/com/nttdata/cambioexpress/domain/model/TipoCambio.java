package com.nttdata.cambioexpress.domain.model;

import com.nttdata.cambioexpress.domain.exception.ReglaDeNegocioException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

public record TipoCambio(Moneda moneda, BigDecimal compra, BigDecimal venta, LocalDate fecha) {
    private static final BigDecimal MONTO_MAXIMO = new BigDecimal("10000.00");
    private static final int ESCALA_MONETARIA = 2;

    public TipoCambio {
        Objects.requireNonNull(moneda, "La moneda es obligatoria");
        Objects.requireNonNull(compra, "El tipo de compra es obligatorio");
        Objects.requireNonNull(venta, "El tipo de venta es obligatorio");
        Objects.requireNonNull(fecha, "La fecha es obligatoria");
        if (compra.signum() <= 0) {
            throw new IllegalArgumentException("El tipo de compra debe ser positivo");
        }
        if (venta.compareTo(compra) <= 0) {
            throw new IllegalArgumentException("El tipo de venta debe ser mayor que el tipo de compra");
        }
    }

    public BigDecimal convertir(BigDecimal monto, Operacion operacion) {
        Objects.requireNonNull(operacion, "La operación es obligatoria");
        if (monto == null || monto.signum() <= 0) {
            throw new ReglaDeNegocioException("El monto debe ser mayor que cero");
        }
        if (monto.compareTo(MONTO_MAXIMO) > 0) {
            throw new ReglaDeNegocioException(
                    "El monto supera el límite de 10000.00 por operación");
        }

        BigDecimal montoRedondeado = monto.setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP);
        BigDecimal tipoAplicado = operacion == Operacion.COMPRA ? venta : compra;
        return montoRedondeado.multiply(tipoAplicado)
                .setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP);
    }
}
