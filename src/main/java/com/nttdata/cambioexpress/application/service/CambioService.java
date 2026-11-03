package com.nttdata.cambioexpress.application.service;

import com.nttdata.cambioexpress.application.exception.RecursoNoEncontradoException;
import com.nttdata.cambioexpress.domain.model.Cotizacion;
import com.nttdata.cambioexpress.domain.model.Moneda;
import com.nttdata.cambioexpress.domain.model.Operacion;
import com.nttdata.cambioexpress.domain.model.TipoCambio;
import com.nttdata.cambioexpress.domain.port.CotizacionRepository;
import com.nttdata.cambioexpress.domain.port.TipoCambioRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CambioService {
    private static final int ESCALA_MONETARIA = 2;
    private static final RoundingMode REDONDEO_MONETARIO = RoundingMode.HALF_UP;

    private final TipoCambioRepository tipoCambioRepository;
    private final CotizacionRepository cotizacionRepository;
    private final AtomicLong secuenciaCotizaciones = new AtomicLong();

    public CambioService(
            TipoCambioRepository tipoCambioRepository,
            CotizacionRepository cotizacionRepository
    ) {
        this.tipoCambioRepository = tipoCambioRepository;
        this.cotizacionRepository = cotizacionRepository;
    }

    public List<TipoCambio> listarTiposCambio() {
        return tipoCambioRepository.findAll();
    }

    public TipoCambio obtenerTipoCambio(Moneda moneda) {
        return tipoCambioRepository.findByMoneda(moneda)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe tipo de cambio para la moneda " + moneda));
    }

    public Cotizacion cotizar(Moneda moneda, Operacion operacion, BigDecimal monto) {
        TipoCambio tipoCambio = obtenerTipoCambio(moneda);
        BigDecimal tasaAplicada = operacion == Operacion.COMPRA
                ? tipoCambio.venta()
                : tipoCambio.compra();
        BigDecimal montoSoles = tipoCambio.convertir(monto, operacion);
        BigDecimal montoMoneda = monto.setScale(ESCALA_MONETARIA, REDONDEO_MONETARIO);

        Cotizacion cotizacion = new Cotizacion(
                "C-" + String.format(Locale.ROOT, "%04d", secuenciaCotizaciones.incrementAndGet()),
                moneda,
                operacion,
                montoMoneda,
                montoSoles,
                tasaAplicada,
                Instant.now()
        );
        return cotizacionRepository.save(cotizacion);
    }

    public Cotizacion obtenerCotizacion(String id) {
        return cotizacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe la cotización " + id));
    }
}
