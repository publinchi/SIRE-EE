package ec.gob.sri.comprobantes.modelo.reportes;

import ec.gob.sri.comprobantes.modelo.reportes.TotalesComprobante;
import ec.gob.sri.comprobantes.administracion.modelo.Compensaciones;
import ec.gob.sri.comprobantes.administracion.modelo.ImpuestoValor;
import ec.gob.sri.comprobantes.modelo.factura.Factura;
import ec.gob.sri.comprobantes.modelo.factura.Factura.Detalles;
import ec.gob.sri.comprobantes.modelo.factura.Factura.Detalles.Detalle;
import ec.gob.sri.comprobantes.modelo.factura.Factura.Detalles.Detalle.DetallesAdicionales;
import ec.gob.sri.comprobantes.modelo.factura.Factura.Detalles.Detalle.DetallesAdicionales.DetAdicional;
import ec.gob.sri.comprobantes.modelo.factura.Factura.InfoAdicional;
import ec.gob.sri.comprobantes.modelo.factura.Factura.InfoAdicional.CampoAdicional;
import ec.gob.sri.comprobantes.modelo.factura.Factura.InfoFactura;
import ec.gob.sri.comprobantes.modelo.factura.Factura.InfoFactura.Pago;
import ec.gob.sri.comprobantes.modelo.factura.Factura.InfoFactura.Pago.DetallePago;
import ec.gob.sri.comprobantes.modelo.factura.Factura.InfoFactura.TotalConImpuestos;
import ec.gob.sri.comprobantes.modelo.factura.Factura.InfoFactura.TotalConImpuestos.TotalImpuesto;
import ec.gob.sri.comprobantes.modelo.factura.Factura.InfoFactura.compensacion;
import ec.gob.sri.comprobantes.modelo.factura.Factura.InfoFactura.compensacion.detalleCompensaciones;
import ec.gob.sri.comprobantes.sql.CompensacionSQL;
import ec.gob.sri.comprobantes.sql.FormasPagoSQL;
import ec.gob.sri.comprobantes.sql.ImpuestoValorSQL;
import ec.gob.sri.comprobantes.util.TipoImpuestoEnum;
import ec.gob.sri.comprobantes.util.TipoImpuestoIvaEnum;
import ec.gob.sri.comprobantes.util.reportes.ReporteUtil;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FacturaReporte {

    private Factura factura;
    private String detalle1;
    private String detalle2;
    private String detalle3;
    //private List<DetallesAdicionalesReporte> detallesAdiciones;
    //private List<InformacionAdicional> infoAdicional;
    //private List<FormasPago> formasPago;
    //private List<TotalesComprobante> totalesComprobante;

    public FacturaReporte(Factura factura) {
        this.factura = factura;
    }

    public Factura getFactura() {
        return this.factura;
    }

    public void setFactura(Factura factura) {
        this.factura = factura;
    }

    public String getDetalle1() {
        return this.detalle1;
    }

    public void setDetalle1(String detalle1) {
        this.detalle1 = detalle1;
    }

    public String getDetalle2() {
        return this.detalle2;
    }

    public void setDetalle2(String detalle2) {
        this.detalle2 = detalle2;
    }

    public String getDetalle3() {
        return this.detalle3;
    }

    public void setDetalle3(String detalle3) {
        this.detalle3 = detalle3;
    }

    public List<DetallesAdicionalesReporte> getDetallesAdiciones()
            throws SQLException, ClassNotFoundException {
        List<DetallesAdicionalesReporte> detallesAdiciones = new ArrayList();
        for (Factura.Detalles.Detalle det : getFactura().getDetalles().getDetalle()) {
            DetallesAdicionalesReporte detAd = new DetallesAdicionalesReporte();
            detAd.setCodigoPrincipal(det.getCodigoPrincipal());
            detAd.setCodigoAuxiliar(det.getCodigoAuxiliar());
            detAd.setDescripcion(det.getDescripcion());
            detAd.setCodigoBarras(det.getCodigoBarras());
            detAd.setCantidad(det.getCantidad().toPlainString());
            detAd.setPrecioTotalSinImpuesto(det.getPrecioTotalSinImpuesto().setScale(2).toString());
            detAd.setPrecioUnitario(det.getPrecioUnitario());
            detAd.setPrecioSinSubsidio(det.getPrecioSinSubsidio());
            if (det.getDescuento() != null) {
                detAd.setDescuento(det.getDescuento().setScale(2).toString());
            } else {
                detAd.setDescuento(BigDecimal.ZERO.setScale(2).toString());
            }
            int i = 0;
            if ((det.getDetallesAdicionales() != null) && (det.getDetallesAdicionales().getDetAdicional() != null) && (!det.getDetallesAdicionales().getDetAdicional().isEmpty())) {
                for (Factura.Detalles.Detalle.DetallesAdicionales.DetAdicional detAdicional : det.getDetallesAdicionales().getDetAdicional()) {
                    if (i == 0) {
                        detAd.setDetalle1(detAdicional.getValor());
                    }
                    if (i == 1) {
                        detAd.setDetalle2(detAdicional.getValor());
                    }
                    if (i == 2) {
                        detAd.setDetalle3(detAdicional.getValor());
                    }
                    i++;
                }
            }
            detAd.setInfoAdicional(getInfoAdicional());
            if (getFormasPago() != null) {
                detAd.setFormasPago(getFormasPago());
            }
            detAd.setTotalesComprobante(getTotalesComprobante());
            detallesAdiciones.add(detAd);
        }
        return detallesAdiciones;
    }

//    public void setDetallesAdiciones(List<DetallesAdicionalesReporte> detallesAdiciones) {
//        this.detallesAdiciones = detallesAdiciones;
//    }

    public List<InformacionAdicional> getInfoAdicional() {
        List<InformacionAdicional> infoAdicional = new ArrayList();
        if (getFactura().getInfoAdicional() != null) {
            if ((getFactura().getInfoAdicional().getCampoAdicional() != null) && (!this.factura.getInfoAdicional().getCampoAdicional().isEmpty())) {
                for (Factura.InfoAdicional.CampoAdicional ca : getFactura().getInfoAdicional().getCampoAdicional()) {
                    infoAdicional.add(new InformacionAdicional(ca.getValue(), ca.getNombre()));
                }
            }
        }
        return infoAdicional;
    }

//    public void setInfoAdicional(List<InformacionAdicional> infoAdicional) {
//        this.infoAdicional = infoAdicional;
//    }

    public List<FormasPago> getFormasPago() {
        List<FormasPago> formasPago = new ArrayList();
        if (getFactura().getInfoFactura().getPagos() != null) {
            if ((getFactura().getInfoFactura().getPagos().getPagos() != null) && (!this.factura.getInfoFactura().getPagos().getPagos().isEmpty())) {
                for (Factura.InfoFactura.Pago.DetallePago pa : getFactura().getInfoFactura().getPagos().getPagos()) {
                    formasPago.add(new FormasPago(obtenerDetalleFormaPago(pa.getFormaPago()), pa.getTotal().setScale(2).toString()));
                }
            }
        }
        return formasPago;
    }

    private String obtenerDetalleFormaPago(String codigo) {
        FormasPagoSQL formasPagoSQL = new FormasPagoSQL();
        List<ec.gob.sri.comprobantes.administracion.modelo.FormasPago> formaPago = new ArrayList();
        try {
            formaPago = formasPagoSQL.obtenerDescripcionFormasPago(codigo);
            if (formaPago != null) {
                return ((ec.gob.sri.comprobantes.administracion.modelo.FormasPago) formaPago.get(0)).getDescripcion();
            }
            return "";
        } catch (SQLException ex) {
            Logger.getLogger(FacturaReporte.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(FacturaReporte.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }

    //public void setFormasPago(List<FormasPago> formasPago) {
    //    this.formasPago = formasPago;
    //}

    public List<TotalesComprobante> getTotalesComprobante()
            throws SQLException, ClassNotFoundException {
        List<TotalesComprobante> totalesComprobante = new ArrayList();
        BigDecimal importeTotal = BigDecimal.ZERO.setScale(2);
        BigDecimal compensaciones = BigDecimal.ZERO.setScale(2);
        TotalComprobante tc = getTotales(this.factura.getInfoFactura());
        for (IvaDiferenteCeroReporte iva : tc.getIvaDistintoCero()) {
            totalesComprobante.add(new TotalesComprobante("SUBTOTAL " + iva.getTarifa() + "%", iva.getSubtotal(), false));
        }
        totalesComprobante.add(new TotalesComprobante("SUBTOTAL IVA 0%", tc.getSubtotal0(), false));
        totalesComprobante.add(new TotalesComprobante("SUBTOTAL NO OBJETO IVA", tc.getSubtotalNoSujetoIva(), false));
        totalesComprobante.add(new TotalesComprobante("SUBTOTAL EXENTO IVA", tc.getSubtotalExentoIVA(), false));
        totalesComprobante.add(new TotalesComprobante("SUBTOTAL SIN IMPUESTOS", this.factura.getInfoFactura().getTotalSinImpuestos(), false));
        totalesComprobante.add(new TotalesComprobante("DESCUENTO", this.factura.getInfoFactura().getTotalDescuento(), false));
        totalesComprobante.add(new TotalesComprobante("ICE", tc.getTotalIce(), false));
        for (IvaDiferenteCeroReporte iva : tc.getIvaDistintoCero()) {
            totalesComprobante.add(new TotalesComprobante("IVA " + iva.getTarifa() + "%", iva.getValor(), false));
        }
        totalesComprobante.add(new TotalesComprobante("IRBPNR", tc.getTotalIRBPNR(), false));
        totalesComprobante.add(new TotalesComprobante("PROPINA", this.factura.getInfoFactura().getPropina(), false));
        if (this.factura.getInfoFactura().getCompensaciones() != null) {
            for (Factura.InfoFactura.compensacion.detalleCompensaciones compensacion : this.factura.getInfoFactura().getCompensaciones().getCompensaciones()) {
                compensaciones = compensaciones.add(compensacion.getValor());
            }
            importeTotal = this.factura.getInfoFactura().getImporteTotal().add(compensaciones);
        }
        if (!compensaciones.equals(BigDecimal.ZERO.setScale(2))) {
            totalesComprobante.add(new TotalesComprobante("VALOR TOTAL", importeTotal, false));
            for (Factura.InfoFactura.compensacion.detalleCompensaciones compensacion : this.factura.getInfoFactura().getCompensaciones().getCompensaciones()) {
                if (!compensacion.getValor().equals(BigDecimal.ZERO.setScale(2))) {
                    CompensacionSQL compensacionSQL = new CompensacionSQL();
                    String detalleCompensacion = ((Compensaciones) compensacionSQL.obtenerCompensacionesPorCodigo(compensacion.getCodigo()).get(0)).getTipoCompensacion();
                    totalesComprobante.add(new TotalesComprobante("(-) " + detalleCompensacion, compensacion.getValor(), true));
                }
            }
            totalesComprobante.add(new TotalesComprobante("VALOR A PAGAR", this.factura.getInfoFactura().getImporteTotal(), false));
        } else {
            totalesComprobante.add(new TotalesComprobante("VALOR TOTAL", this.factura.getInfoFactura().getImporteTotal(), false));
        }
        return totalesComprobante;
    }

    //public void setTotalesComprobante(List<TotalesComprobante> totalesComprobante) {
    //    this.totalesComprobante = totalesComprobante;
    //}

    private TotalComprobante getTotales(Factura.InfoFactura infoFactura) {
        List<IvaDiferenteCeroReporte> ivaDiferenteCero = new ArrayList();

        BigDecimal totalIva = new BigDecimal(0.0D);
        BigDecimal totalIva0 = new BigDecimal(0.0D);
        BigDecimal totalExentoIVA = new BigDecimal(0.0D);

        BigDecimal totalICE = new BigDecimal(0.0D);
        BigDecimal totalIRBPNR = new BigDecimal(0.0D);
        BigDecimal totalSinImpuesto = new BigDecimal(0.0D);
        TotalComprobante tc = new TotalComprobante();
        for (Factura.InfoFactura.TotalConImpuestos.TotalImpuesto ti : infoFactura.getTotalConImpuestos().getTotalImpuesto()) {
            Integer cod = new Integer(ti.getCodigo());
            if ((TipoImpuestoEnum.IVA.getCode() == cod.intValue()) && (ti.getValor().doubleValue() > 0.0D)) {
                String codigoPorcentaje = obtenerPorcentajeIvaVigente(ti.getCodigoPorcentaje());
                IvaDiferenteCeroReporte iva = new IvaDiferenteCeroReporte(ti.getBaseImponible(), codigoPorcentaje, ti.getValor());
                ivaDiferenteCero.add(iva);
            }
            if ((TipoImpuestoEnum.IVA.getCode() == cod.intValue()) && (TipoImpuestoIvaEnum.IVA_VENTA_0.getCode().equals(ti.getCodigoPorcentaje()))) {
                totalIva0 = totalIva0.add(ti.getBaseImponible());
            }
            if ((TipoImpuestoEnum.IVA.getCode() == cod.intValue()) && (TipoImpuestoIvaEnum.IVA_NO_OBJETO.getCode().equals(ti.getCodigoPorcentaje()))) {
                totalSinImpuesto = totalSinImpuesto.add(ti.getBaseImponible());
            }
            if ((TipoImpuestoEnum.IVA.getCode() == cod.intValue()) && (TipoImpuestoIvaEnum.IVA_EXCENTO.getCode().equals(ti.getCodigoPorcentaje()))) {
                totalExentoIVA = totalExentoIVA.add(ti.getBaseImponible());
            }
            if (TipoImpuestoEnum.ICE.getCode() == cod.intValue()) {
                totalICE = totalICE.add(ti.getValor());
            }
            if (TipoImpuestoEnum.IRBPNR.getCode() == cod.intValue()) {
                totalIRBPNR = totalIRBPNR.add(ti.getValor());
            }
        }
        if (ivaDiferenteCero.isEmpty()) {
            ivaDiferenteCero.add(LlenaIvaDiferenteCero());
        }
        tc.setIvaDistintoCero(ivaDiferenteCero);
        tc.setSubtotal0(totalIva0);
        tc.setTotalIce(totalICE);
        tc.setSubtotal(totalIva0.add(totalIva).add(totalSinImpuesto).add(totalExentoIVA));
        tc.setSubtotalExentoIVA(totalExentoIVA);
        tc.setTotalIRBPNR(totalIRBPNR);
        tc.setSubtotalNoSujetoIva(totalSinImpuesto);
        return tc;
    }

    private IvaDiferenteCeroReporte LlenaIvaDiferenteCero() {
        BigDecimal valor = BigDecimal.ZERO.setScale(2);
        String porcentajeIva = ObtieneIvaRideFactura(this.factura.getInfoFactura().getTotalConImpuestos(), DeStringADate(this.factura.getInfoFactura().getFechaEmision()));
        return new IvaDiferenteCeroReporte(valor, porcentajeIva, valor);
    }

    private String ObtieneIvaRideFactura(Factura.InfoFactura.TotalConImpuestos impuestos, Date fecha) {
        for (Factura.InfoFactura.TotalConImpuestos.TotalImpuesto impuesto : impuestos.getTotalImpuesto()) {
            Integer cod = new Integer(impuesto.getCodigo());
            if ((TipoImpuestoEnum.IVA.getCode() == cod.intValue()) && (impuesto.getValor().doubleValue() > 0.0D)) {
                return obtenerPorcentajeIvaVigente(impuesto.getCodigoPorcentaje());
            }
        }
        return obtenerPorcentajeIvaVigente(fecha).toString();
    }

    private String obtenerPorcentajeIvaVigente(Date fechaEmision) {
        try {
            ImpuestoValorSQL impvalorSQL = new ImpuestoValorSQL();
            BigDecimal porcentaje = BigDecimal.valueOf(((ImpuestoValor) impvalorSQL.obtenerDatosIvaVigente(fechaEmision).get(0)).getPorcentaje().doubleValue());
            return porcentaje.setScale(0).toString();
        } catch (SQLException ex) {
            Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }

    private String obtenerPorcentajeIvaVigente(String cod) {
        try {
            ImpuestoValorSQL impvalorSQL = new ImpuestoValorSQL();
            BigDecimal porcentaje = BigDecimal.valueOf(((ImpuestoValor) impvalorSQL.obtenerDatosIvaCodigoPorcentaje(cod).get(0)).getPorcentaje().doubleValue());
            return porcentaje.setScale(0).toString();
        } catch (SQLException ex) {
            Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }

    public Date DeStringADate(String fecha) {
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        String strFecha = fecha;
        Date fechaDate = null;
        try {
            return formato.parse(strFecha);
        } catch (ParseException ex) {
        }
        return fechaDate;
    }
}