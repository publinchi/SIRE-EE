package ec.gob.sri.comprobantes.util.reportes;

import ec.gob.sri.comprobantes.administracion.modelo.Emisor;
import ec.gob.sri.comprobantes.administracion.modelo.ImpuestoValor;
import ec.gob.sri.comprobantes.modelo.InfoTributaria;
import ec.gob.sri.comprobantes.modelo.factura.Factura;
import ec.gob.sri.comprobantes.modelo.factura.Impuesto;
import ec.gob.sri.comprobantes.modelo.guia.GuiaRemision;
import ec.gob.sri.comprobantes.modelo.liquidacion.Liquidacion;
import ec.gob.sri.comprobantes.modelo.notacredito.NotaCredito;
import ec.gob.sri.comprobantes.modelo.notadebito.NotaDebito;
import ec.gob.sri.comprobantes.modelo.rentencion.ComprobanteRetencion;
import ec.gob.sri.comprobantes.modelo.reportes.*;
import ec.gob.sri.comprobantes.sql.EmisorSQL;
import ec.gob.sri.comprobantes.sql.ImpuestoValorSQL;
import ec.gob.sri.comprobantes.util.StringUtil;
import ec.gob.sri.comprobantes.util.TipoAmbienteEnum;
import ec.gob.sri.comprobantes.util.TipoEmisionEnum;
import ec.gob.sri.comprobantes.util.TipoImpuestoEnum;
import ec.gob.sri.comprobantes.util.TipoImpuestoIvaEnum;
import ec.gob.sri.comprobantes.util.reportes.JasperViwerSRI;
import java.awt.Dimension;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import javax.swing.JFrame;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.view.JRSaveContributor;
import net.sf.jasperreports.view.save.JRPdfSaveContributor;
import com.sire.logger.LogManager;

public class ReporteUtil {

    private static final String DIR_SUCURSAL = "DIR_SUCURSAL";
    private static final String CONT_ESPECIAL = "CONT_ESPECIAL";
    private static final String LLEVA_CONTABILIDAD = "LLEVA_CONTABILIDAD";
    String tarifaIva;
    private static final org.apache.logging.log4j.Logger log = LogManager.getLogger(ReporteUtil.class);

    private static Emisor obtenerEmisor()
            throws SQLException, ClassNotFoundException {
        EmisorSQL emisSQL = new EmisorSQL();
        return emisSQL.obtenerDatosEmisor();
    }

    public byte[] generarReporte(String urlReporte, FacturaReporte fact, String numAut, String fechaAut)
            throws SQLException, ClassNotFoundException, IOException {
        FileInputStream is = null;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            JRDataSource dataSource = new JRBeanCollectionDataSource(fact.getDetallesAdiciones());
            is = new FileInputStream(urlReporte);
            JasperPrint reporteView = JasperFillManager.fillReport(is, obtenerMapaParametrosReportes(obtenerParametrosInfoTriobutaria(fact.getFactura().getInfoTributaria(), numAut, fechaAut), obtenerInfoFactura(fact.getFactura().getInfoFactura(), fact)), dataSource);
            JasperExportManager.exportReportToPdfStream(reporteView, outputStream);
            if("true".equals(System.getProperty("isTest")))
                showReport(reporteView);
            return outputStream.toByteArray();
        } catch (FileNotFoundException | JRException ex) {
            log.error(ex);
            return null;
        } finally {
            //clean off
            if (null != outputStream) {
                try {
                    outputStream.close();
                    outputStream = null;
                } catch (IOException ex) {
                }
            }
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ex) {
                log.error(ex);
            }
        }
    }

    public byte[] generarReporte(String urlReporte, LiquidacionReporte liquidacionReporte, String numAut, String fechaAut)
            throws SQLException, ClassNotFoundException, IOException {
        FileInputStream is = null;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            JRDataSource dataSource = new JRBeanCollectionDataSource(liquidacionReporte.getDetallesAdiciones());
            is = new FileInputStream(urlReporte);
            JasperPrint reporteView = JasperFillManager.fillReport(is,
                    obtenerMapaParametrosReportes(obtenerParametrosInfoTriobutaria(liquidacionReporte.getLiquidacion()
                                    .getInfoTributaria(), numAut, fechaAut),
                            obtenerInfoLiquidacion(liquidacionReporte.getLiquidacion().getInfoLiquidacionCompra(),
                                    liquidacionReporte)), dataSource);
            JasperExportManager.exportReportToPdfStream(reporteView, outputStream);
            if("true".equals(System.getProperty("isTest")))
                showReport(reporteView);
            return outputStream.toByteArray();
        } catch (FileNotFoundException | JRException ex) {
            log.error(ex);
            return null;
        } finally {
            //clean off
            if (null != outputStream) {
                try {
                    outputStream.close();
                    outputStream = null;
                } catch (IOException ex) {
                }
            }
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ex) {
                log.error(ex);
            }
        }
    }

    public byte[] generarReporte(String urlReporte, ComprobanteRetencionReporte comprobanteRetencionReporte, String numAut, String fechaAut)
            throws SQLException, ClassNotFoundException, IOException {
        FileInputStream is = null;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            JRDataSource dataSource = new JRBeanCollectionDataSource(comprobanteRetencionReporte.getDetallesAdiciones());
            is = new FileInputStream(urlReporte);
            JasperPrint reporteView = JasperFillManager.fillReport(is, obtenerMapaParametrosReportes(obtenerParametrosInfoTriobutaria(comprobanteRetencionReporte.getComprobanteRetencion().getInfoTributaria(), numAut, fechaAut), obtenerInfoCompRetencion(comprobanteRetencionReporte.getComprobanteRetencion().getInfoCompRetencion())), dataSource);
            JasperExportManager.exportReportToPdfStream(reporteView, outputStream);
//            showReport(reporteView);
            return outputStream.toByteArray();
        } catch (FileNotFoundException | JRException ex) {
            log.error(ex);
            return null;
        } finally {
            //clean off
            if (null != outputStream) {
                try {
                    outputStream.close();
                    outputStream = null;
                } catch (IOException ex) {
                }
            }
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ex) {
                log.error(ex);
            }
        }
    }

    //    public void generarReporte(String urlReporte, NotaDebitoReporte rep, String numAut, String fechaAut)
//            throws SQLException, ClassNotFoundException, IOException {
//        FileInputStream is = null;
//        try {
//            JRDataSource dataSource = new JRBeanCollectionDataSource(rep.getDetallesAdiciones());
//            is = new FileInputStream(urlReporte);
//            JasperPrint reporte_view = JasperFillManager.fillReport(is, obtenerMapaParametrosReportes(obtenerParametrosInfoTriobutaria(rep.getNotaDebito().getInfoTributaria(), numAut, fechaAut), obtenerInfoND(rep.getNotaDebito().getInfoNotaDebito())), dataSource);
//
//            showReport(reporte_view);
//        } catch (FileNotFoundException | JRException ex) {
//            Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            try {
//                if (is != null) {
//                    is.close();
//                }
//            } catch (IOException ex) {
//                Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//    }
    public byte[] generarReporte(String urlReporte, NotaDebitoReporte rep, String numAut, String fechaAut)
            throws SQLException, ClassNotFoundException, IOException {
        FileInputStream is = null;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            JRDataSource dataSource = new JRBeanCollectionDataSource(rep.getDetallesAdiciones());
            is = new FileInputStream(urlReporte);
            JasperPrint reporte_view = JasperFillManager.fillReport(is, obtenerMapaParametrosReportes(obtenerParametrosInfoTriobutaria(rep.getNotaDebito().getInfoTributaria(), numAut, fechaAut), obtenerInfoND(rep.getNotaDebito().getInfoNotaDebito())), dataSource);
            JasperExportManager.exportReportToPdfStream(reporte_view, outputStream);

//            showReport(reporte_view);
            return outputStream.toByteArray();
        } catch (FileNotFoundException | JRException ex) {
            log.error(ex);
            return null;
        } finally {
            //clean off
            if (null != outputStream) {
                try {
                    outputStream.close();
                    outputStream = null;
                } catch (IOException ex) {
                }
            }
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ex) {
                log.error(ex);
            }
        }
    }

    //    public void generarReporte(String urlReporte, NotaCreditoReporte rep, String numAut, String fechaAut)
//            throws SQLException, ClassNotFoundException, IOException {
//        FileInputStream is = null;
//        try {
//            JRDataSource dataSource = new JRBeanCollectionDataSource(rep.getDetallesAdiciones());
//            is = new FileInputStream(urlReporte);
//            JasperPrint reporte_view = JasperFillManager.fillReport(is, obtenerMapaParametrosReportes(obtenerParametrosInfoTriobutaria(rep.getNotaCredito().getInfoTributaria(), numAut, fechaAut), obtenerInfoNC(rep.getNotaCredito().getInfoNotaCredito(), rep)), dataSource);
//
//            showReport(reporte_view);
//        } catch (FileNotFoundException | JRException ex) {
//            Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            try {
//                if (is != null) {
//                    is.close();
//                }
//            } catch (IOException ex) {
//                Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//    }
    public byte[] generarReporte(String urlReporte, NotaCreditoReporte rep, String numAut, String fechaAut)
            throws SQLException, ClassNotFoundException, IOException {
        FileInputStream is = null;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            JRDataSource dataSource = new JRBeanCollectionDataSource(rep.getDetallesAdiciones());
            is = new FileInputStream(urlReporte);
            JasperPrint reporte_view = JasperFillManager.fillReport(is, obtenerMapaParametrosReportes(obtenerParametrosInfoTriobutaria(rep.getNotaCredito().getInfoTributaria(), numAut, fechaAut), obtenerInfoNC(rep.getNotaCredito().getInfoNotaCredito(), rep)), dataSource);
            JasperExportManager.exportReportToPdfStream(reporte_view, outputStream);
//            showReport(reporte_view);
            return outputStream.toByteArray();
        } catch (FileNotFoundException | JRException ex) {
            log.error(ex);
            return null;
        } finally {
            //clean off
            if (null != outputStream) {
                try {
                    outputStream.close();
                    outputStream = null;
                } catch (IOException ex) {
                }
            }
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ex) {
                log.error(ex);
            }
        }
    }

    //    public void generarReporte(String urlReporte, GuiaRemisionReporte rep, String numAut, String fechaAut, GuiaRemision guiaRemision)
//            throws SQLException, ClassNotFoundException, IOException {
//        FileInputStream is = null;
//        try {
//            JRDataSource dataSource = new JRBeanCollectionDataSource(rep.getGuiaRemisionList());
//            is = new FileInputStream(urlReporte);
//            JasperPrint reporte_view = JasperFillManager.fillReport(is, obtenerMapaParametrosReportes(obtenerParametrosInfoTriobutaria(rep.getGuiaRemision().getInfoTributaria(), numAut, fechaAut), obtenerInfoGR(rep.getGuiaRemision().getInfoGuiaRemision(), guiaRemision)), dataSource);
//
//            showReport(reporte_view);
//        } catch (FileNotFoundException | JRException ex) {
//            Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            try {
//                if (is != null) {
//                    is.close();
//                }
//            } catch (IOException ex) {
//                Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//    }
    public byte[] generarReporte(String urlReporte, GuiaRemisionReporte rep, String numAut, String fechaAut, GuiaRemision guiaRemision)
            throws SQLException, ClassNotFoundException, IOException {
        FileInputStream is = null;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            JRDataSource dataSource = new JRBeanCollectionDataSource(rep.getGuiaRemisionList());
            is = new FileInputStream(urlReporte);
            JasperPrint reporte_view = JasperFillManager.fillReport(is, obtenerMapaParametrosReportes(obtenerParametrosInfoTriobutaria(rep.getGuiaRemision().getInfoTributaria(), numAut, fechaAut), obtenerInfoGR(rep.getGuiaRemision().getInfoGuiaRemision(), guiaRemision)), dataSource);
            JasperExportManager.exportReportToPdfStream(reporte_view, outputStream);
//            showReport(reporte_view);
            return outputStream.toByteArray();
        } catch (FileNotFoundException | JRException ex) {
            log.error(ex);
            return null;
        } finally {
            //clean off
            if (null != outputStream) {
                try {
                    outputStream.close();
                    outputStream = null;
                } catch (IOException ex) {
                }
            }
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ex) {
                log.error(ex);
            }
        }
    }

    //    public void generarReporte(String urlReporte, ComprobanteRetencionReporte rep, String numAut, String fechaAut)
//            throws SQLException, ClassNotFoundException, IOException {
//        FileInputStream is = null;
//        try {
//            JRDataSource dataSource = new JRBeanCollectionDataSource(rep.getDetallesAdiciones());
//            is = new FileInputStream(urlReporte);
//            JasperPrint reporte_view = JasperFillManager.fillReport(is, obtenerMapaParametrosReportes(obtenerParametrosInfoTriobutaria(rep.getComprobanteRetencion().getInfoTributaria(), numAut, fechaAut), obtenerInfoCompRetencion(rep.getComprobanteRetencion().getInfoCompRetencion())), dataSource);
//
//            showReport(reporte_view);
//        } catch (FileNotFoundException | JRException ex) {
//            Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            try {
//                if (is != null) {
//                    is.close();
//                }
//            } catch (IOException ex) {
//                Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//    }
    private Map<String, Object> obtenerMapaParametrosReportes(Map<String, Object> mapa1, Map<String, Object> mapa2) {
        mapa1.putAll(mapa2);
        return mapa1;
    }

    private Map<String, Object> obtenerParametrosInfoTriobutaria(InfoTributaria infoTributaria, String numAut, String fechaAut)
            throws SQLException, ClassNotFoundException, FileNotFoundException, IOException {
        Map<String, Object> param = new HashMap();
        param.put("RUC", infoTributaria.getRuc());
        param.put("CLAVE_ACC", infoTributaria.getClaveAcceso());
        param.put("RAZON_SOCIAL", infoTributaria.getRazonSocial());
        param.put("DIR_MATRIZ", infoTributaria.getDirMatriz());
        try {
            if ((obtenerEmisor().getPathLogo() != null) && (!obtenerEmisor().getPathLogo().isEmpty())) {
                param.put("LOGO", new FileInputStream(obtenerEmisor().getPathLogo()));
            } else {
                param.put("LOGO", new FileInputStream("resources/images/logo.jpeg"));
            }
        } catch (FileNotFoundException ex) {
            try {
                param.put("LOGO", new FileInputStream("resources/images/logo.jpeg"));
                log.error(ex);
            } catch (FileNotFoundException ex1) {
                log.error(ex);
            }
        }
        String home = System.getProperty("sire.home");
        if (home == null) {
            home = System.getProperty("user.home");
        }
        Properties runtimeParameters = new Properties();
        runtimeParameters.load(new FileInputStream(home + "/configuration.properties"));
        String pathReports = runtimeParameters.getProperty("pathReports");
        param.put("SUBREPORT_DIR", pathReports);
        param.put("SUBREPORT_PAGOS", pathReports);
        param.put("SUBREPORT_TOTALES", pathReports);
        param.put("TIPO_EMISION", obtenerTipoEmision(infoTributaria));
        param.put("NUM_AUT", numAut);
        param.put("FECHA_AUT", fechaAut);
//        param.put("MARCA_AGUA", obtenerMarcaAgua(infoTributaria.getAmbiente()));
        param.put("NUM_FACT", infoTributaria.getEstab() + "-" + infoTributaria.getPtoEmi() + "-" + infoTributaria.getSecuencial());
        param.put("AMBIENTE", obtenerAmbiente(infoTributaria));
        param.put("NOM_COMERCIAL", infoTributaria.getNombreComercial());
        param.put("REGIMEN_MICROEMPRESAS", infoTributaria.getRegimenMicroempresas());
        param.put("AGENTE_RETENCION", infoTributaria.getAgenteRetencion());
        return param;
    }

    private String obtenerAmbiente(InfoTributaria infoTributaria) {
        if (infoTributaria.getAmbiente().equals("2")) {
            return TipoAmbienteEnum.PRODUCCION.toString();
        }
        return TipoAmbienteEnum.PRUEBAS.toString();
    }

    private String obtenerTipoEmision(InfoTributaria infoTributaria) {
        if (infoTributaria.getTipoEmision().equals("2")) {
            return TipoEmisionEnum.PREAUTORIZADA.getCode();
        }
        if (infoTributaria.getTipoEmision().equals("1")) {
            return TipoEmisionEnum.NORMAL.getCode();
        }
        return null;
    }

    private InputStream obtenerMarcaAgua(String ambiente) {
        try {
            if (ambiente.equals(TipoAmbienteEnum.PRODUCCION.getCode())) {
                return new BufferedInputStream(new FileInputStream("resources/images/produccion.jpeg"));
            }
            return new BufferedInputStream(new FileInputStream("resources/images/pruebas.jpeg"));
        } catch (FileNotFoundException fe) {
            log.error(fe);
        }
        return null;
    }

    private BigDecimal obtenerTotalSinSubsidio(FacturaReporte fact) {
        BigDecimal totalSinSubsidio = BigDecimal.ZERO;
        BigDecimal ivaDistintoCero = BigDecimal.ZERO;
        BigDecimal iva0 = BigDecimal.ZERO;
        double iva = 0.0D;
        List<Factura.Detalles.Detalle> detalles = fact.getFactura().getDetalles().getDetalle();
        for (int i = 0; i < detalles.size(); i++) {
            BigDecimal sinSubsidio = BigDecimal.ZERO.setScale(2, RoundingMode.UP);
            if (((Factura.Detalles.Detalle) detalles.get(i)).getPrecioSinSubsidio() != null) {
                sinSubsidio = BigDecimal.valueOf(Double.valueOf(((Factura.Detalles.Detalle) detalles.get(i)).getPrecioSinSubsidio().toString()).doubleValue());
            }
            BigDecimal cantidad = BigDecimal.valueOf(Double.valueOf(((Factura.Detalles.Detalle) detalles.get(i)).getCantidad().toString()).doubleValue());
            if (Double.valueOf(sinSubsidio.toString()).doubleValue() <= 0.0D) {
                sinSubsidio = BigDecimal.valueOf(Double.valueOf(((Factura.Detalles.Detalle) detalles.get(i)).getPrecioUnitario().toString()).doubleValue());
            }
            List<Impuesto> impuesto = ((Factura.Detalles.Detalle) detalles.get(i)).getImpuestos().getImpuesto();
            double iva1 = 0.0D;
            for (int c = 0; c < impuesto.size(); c++) {
                if ((((Impuesto) impuesto.get(c)).getCodigo().equals(String.valueOf(TipoImpuestoEnum.IVA.getCode()))) && (!((Impuesto) impuesto.get(c)).getTarifa().equals(BigDecimal.ZERO))) {
                    iva = Double.valueOf(((Impuesto) impuesto.get(c)).getTarifa().toString()).doubleValue();
                    iva1 = Double.valueOf(((Impuesto) impuesto.get(c)).getTarifa().toString()).doubleValue();
                }
            }
            if (iva1 > 0.0D) {
                ivaDistintoCero = ivaDistintoCero.add(sinSubsidio.multiply(cantidad));
            } else {
                iva0 = iva0.add(sinSubsidio.multiply(cantidad));
            }
        }
        if (iva > 0.0D) {
            iva = iva / 100.0D + 1.0D;
            ivaDistintoCero = ivaDistintoCero.multiply(BigDecimal.valueOf(iva));
        }
        totalSinSubsidio = totalSinSubsidio.add(ivaDistintoCero).add(iva0);
        return totalSinSubsidio.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal obtenerTotalSinDescuento(FacturaReporte fact) {
        BigDecimal totalConSubsidio = BigDecimal.ZERO;
        BigDecimal ivaDistintoCero = BigDecimal.ZERO;
        BigDecimal iva0 = BigDecimal.ZERO;
        double iva = 0.0D;
        List<Factura.Detalles.Detalle> detalles = fact.getFactura().getDetalles().getDetalle();
        for (int i = 0; i < detalles.size(); i++) {
            BigDecimal sinSubsidio = BigDecimal.valueOf(Double.valueOf(((Factura.Detalles.Detalle) detalles.get(i)).getPrecioUnitario().toString()).doubleValue());
            BigDecimal cantidad = BigDecimal.valueOf(Double.valueOf(((Factura.Detalles.Detalle) detalles.get(i)).getCantidad().toString()).doubleValue());
            List<Impuesto> impuesto = ((Factura.Detalles.Detalle) detalles.get(i)).getImpuestos().getImpuesto();
            double iva1 = 0.0D;
            for (int c = 0; c < impuesto.size(); c++) {
                if ((((Impuesto) impuesto.get(c)).getCodigo().equals(String.valueOf(TipoImpuestoEnum.IVA.getCode()))) && (!((Impuesto) impuesto.get(c)).getTarifa().equals(BigDecimal.ZERO))) {
                    iva = Double.valueOf(((Impuesto) impuesto.get(c)).getTarifa().toString()).doubleValue();
                    iva1 = Double.valueOf(((Impuesto) impuesto.get(c)).getTarifa().toString()).doubleValue();
                }
            }
            if (iva1 > 0.0D) {
                ivaDistintoCero = ivaDistintoCero.add(sinSubsidio.multiply(cantidad));
            } else {
                iva0 = iva0.add(sinSubsidio.multiply(cantidad));
            }
        }
        if (iva > 0.0D) {
            iva = iva / 100.0D + 1.0D;
            ivaDistintoCero = ivaDistintoCero.multiply(BigDecimal.valueOf(iva));
        }
        totalConSubsidio = totalConSubsidio.add(ivaDistintoCero).add(iva0);
        return totalConSubsidio.setScale(2, RoundingMode.HALF_UP);
    }

    private Map<String, Object> obtenerInfoFactura(Factura.InfoFactura infoFactura, FacturaReporte fact) {
        BigDecimal TotalSinSubsidio = BigDecimal.ZERO;
        BigDecimal TotalSinDescuento = BigDecimal.ZERO;
        BigDecimal TotalSubsidio = BigDecimal.ZERO;
        Map<String, Object> param = new HashMap();
        param.put("DIR_SUCURSAL", infoFactura.getDirEstablecimiento());
        param.put("CONT_ESPECIAL", infoFactura.getContribuyenteEspecial());
        param.put("LLEVA_CONTABILIDAD", infoFactura.getObligadoContabilidad());
        param.put("RS_COMPRADOR", infoFactura.getRazonSocialComprador());
        param.put("RUC_COMPRADOR", infoFactura.getIdentificacionComprador());
        param.put("DIRECCION_CLIENTE", infoFactura.getIDireccionComprador());
        param.put("FECHA_EMISION", infoFactura.getFechaEmision());
        param.put("GUIA", infoFactura.getGuiaRemision());
        param.put("PLACA", infoFactura.getPlaca());
        TotalComprobante tc = getTotales(infoFactura);
        if (infoFactura.getTotalSubsidio() != null) {
            TotalSinSubsidio = obtenerTotalSinSubsidio(fact);
            TotalSinDescuento = obtenerTotalSinDescuento(fact);
            TotalSubsidio = TotalSinSubsidio.subtract(TotalSinDescuento).setScale(2, RoundingMode.UP);
            if (Double.valueOf(tc.getTotalIRBPNR().toString()).doubleValue() < 0.0D) {
                TotalSinSubsidio = TotalSinSubsidio.add(tc.getTotalIRBPNR());
            }
            if (infoFactura.getPropina() != null) {
                TotalSinSubsidio = TotalSinSubsidio.add(infoFactura.getPropina());
            }
        }
        param.put("TOTAL_SIN_SUBSIDIO", TotalSinSubsidio.setScale(2, RoundingMode.UP));
        param.put("AHORRO_POR_SUBSIDIO", TotalSubsidio.setScale(2, RoundingMode.UP));

        return param;
    }

    private Map<String, Object> obtenerInfoLiquidacion(Liquidacion.InfoLiquidacionCompra infoLiquidacionCompra,
                                                       LiquidacionReporte liquidacionReporte) {
        BigDecimal TotalSinSubsidio = BigDecimal.ZERO;
        BigDecimal TotalSinDescuento = BigDecimal.ZERO;
        BigDecimal TotalSubsidio = BigDecimal.ZERO;
        Map<String, Object> param = new HashMap();
        param.put("DIR_SUCURSAL", infoLiquidacionCompra.getDirEstablecimiento());
        param.put("CONT_ESPECIAL", infoLiquidacionCompra.getContribuyenteEspecial());
        param.put("LLEVA_CONTABILIDAD", infoLiquidacionCompra.getObligadoContabilidad());
        param.put("RS_COMPRADOR", infoLiquidacionCompra.getRazonSocialProveedor());
        param.put("RUC_COMPRADOR", infoLiquidacionCompra.getIdentificacionProveedor());
        //param.put("DIRECCION_CLIENTE", infoLiquidacionCompra.getIDireccionComprador());
        param.put("FECHA_EMISION", infoLiquidacionCompra.getFechaEmision());
        param.put("GUIA", infoLiquidacionCompra.getGuiaRemision());
        //param.put("PLACA", infoLiquidacionCompra.getPlaca());
        /*TotalComprobante tc = getTotales(infoLiquidacionCompra);
        if (infoLiquidacionCompra.getTotalSubsidio() != null) {
            TotalSinSubsidio = obtenerTotalSinSubsidio(liquidacionReporte);
            TotalSinDescuento = obtenerTotalSinDescuento(liquidacionReporte);
            TotalSubsidio = TotalSinSubsidio.subtract(TotalSinDescuento).setScale(2, RoundingMode.UP);
            if (Double.valueOf(tc.getTotalIRBPNR().toString()).doubleValue() < 0.0D) {
                TotalSinSubsidio = TotalSinSubsidio.add(tc.getTotalIRBPNR());
            }
            if (infoLiquidacionCompra.getPropina() != null) {
                TotalSinSubsidio = TotalSinSubsidio.add(infoLiquidacionCompra.getPropina());
            }
        }
        param.put("TOTAL_SIN_SUBSIDIO", TotalSinSubsidio.setScale(2, RoundingMode.UP));
        param.put("AHORRO_POR_SUBSIDIO", TotalSubsidio.setScale(2, RoundingMode.UP));*/

        return param;
    }

    private String obtenerPorcentajeIvaVigente(Date fechaEmision) {
        try {
            ImpuestoValorSQL impvalorSQL = new ImpuestoValorSQL();
            BigDecimal porcentaje = BigDecimal.valueOf(((ImpuestoValor) impvalorSQL.obtenerDatosIvaVigente(fechaEmision).get(0)).getPorcentaje().doubleValue());
            return porcentaje.setScale(0).toString() + "%";
        } catch (SQLException ex) {
            log.error(ex);
            return "";
        } catch (ClassNotFoundException ex) {
            log.error(ex);
        }
        return "";
    }

    private String obtenerPorcentajeIvaVigente(String cod) {
        try {
            ImpuestoValorSQL impvalorSQL = new ImpuestoValorSQL();
            BigDecimal porcentaje = BigDecimal.valueOf(impvalorSQL.obtenerDatosIvaCodigoPorcentaje(cod).get(0)
                    .getPorcentaje().doubleValue());
            return porcentaje.setScale(0).toString() + "%";
        } catch (SQLException ex) {
            log.error(ex);
            return "";
        } catch (ClassNotFoundException ex) {
            log.error(ex);
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
            ivaDiferenteCero.add(LlenaIvaDiferenteCero(infoFactura));
        }
        tc.setIvaDistintoCero(ivaDiferenteCero);

        tc.setSubtotal0(totalIva0);
        tc.setTotalIce(totalICE);
        tc.setSubtotal(totalIva0.add(totalIva));
        tc.setSubtotalExentoIVA(totalExentoIVA);
        tc.setTotalIRBPNR(totalIRBPNR);
        tc.setSubtotalNoSujetoIva(totalSinImpuesto);
        return tc;
    }

    private IvaDiferenteCeroReporte LlenaIvaDiferenteCero(Factura.InfoFactura infoFactura) {
        BigDecimal valor = BigDecimal.ZERO.setScale(2);
        String porcentajeIva = obtenerPorcentajeIvaVigente(DeStringADate(infoFactura.getFechaEmision()));
        return new IvaDiferenteCeroReporte(valor, porcentajeIva, valor);
    }

    private Map<String, Object> obtenerInfoNC(NotaCredito.InfoNotaCredito infoNC, NotaCreditoReporte nc)
            throws SQLException, ClassNotFoundException {
        Map<String, Object> param = new HashMap();
        param.put("DIR_SUCURSAL", infoNC.getDirEstablecimiento());
        param.put("CONT_ESPECIAL", infoNC.getContribuyenteEspecial());
        param.put("LLEVA_CONTABILIDAD", infoNC.getObligadoContabilidad());
        param.put("RS_COMPRADOR", infoNC.getRazonSocialComprador());
        param.put("RUC_COMPRADOR", infoNC.getIdentificacionComprador());
        param.put("FECHA_EMISION", infoNC.getFechaEmision());
        param.put("NUM_DOC_MODIFICADO", infoNC.getNumDocModificado());
        param.put("FECHA_EMISION_DOC_SUSTENTO", infoNC.getFechaEmisionDocSustento());
        param.put("DOC_MODIFICADO", StringUtil.obtenerDocumentoModificado(infoNC.getCodDocModificado()));
        param.put("TOTAL_DESCUENTO", obtenerTotalDescuento(nc));
        param.put("RAZON_MODIF", infoNC.getMotivo());
        String porcentajeIva = ObtieneIvaRideNC(nc.getNotaCredito().getInfoNotaCredito().getTotalConImpuestos(), DeStringADate(infoNC.getFechaEmisionDocSustento()));
        param.put("PORCENTAJE_IVA", porcentajeIva);
        return param;
    }

    private String ObtieneIvaRideNC(ec.gob.sri.comprobantes.modelo.notacredito.TotalConImpuestos impuestos, Date fecha) {
        for (ec.gob.sri.comprobantes.modelo.notacredito.TotalConImpuestos.TotalImpuesto impuesto : impuestos.getTotalImpuesto()) {
            Integer cod = new Integer(impuesto.getCodigo());
            if ((TipoImpuestoEnum.IVA.getCode() == cod.intValue()) && (impuesto.getValor().doubleValue() > 0.0D)) {
                return obtenerPorcentajeIvaVigente(impuesto.getCodigoPorcentaje());
            }
        }
        return obtenerPorcentajeIvaVigente(fecha).toString();
    }

    private Map<String, Object> obtenerInfoGR(GuiaRemision.InfoGuiaRemision igr, GuiaRemision guiaRemision) {
        Map<String, Object> param = new HashMap();
        param.put("DIR_SUCURSAL", igr.getDirEstablecimiento());
        param.put("CONT_ESPECIAL", igr.getContribuyenteEspecial());
        param.put("LLEVA_CONTABILIDAD", igr.getObligadoContabilidad());
        param.put("FECHA_INI_TRANSPORTE", igr.getFechaIniTransporte());
        param.put("FECHA_FIN_TRANSPORTE", igr.getFechaFinTransporte());
        param.put("RUC_TRANSPORTISTA", igr.getRucTransportista());
        param.put("RS_TRANSPORTISTA", igr.getRazonSocialTransportista());
        param.put("PLACA", igr.getPlaca());
        param.put("PUNTO_PARTIDA", igr.getDirPartida());
        param.put("INFO_ADICIONAL", getInfoAdicional(guiaRemision));
        return param;
    }

    public List<InformacionAdicional> getInfoAdicional(GuiaRemision guiaRemision) {
        List<InformacionAdicional> infoAdicional = new ArrayList();
        if (guiaRemision.getInfoAdicional() != null) {
            guiaRemision.getInfoAdicional().getCampoAdicional().forEach((ca) -> {
                infoAdicional.add(new InformacionAdicional(ca.getValue(), ca.getNombre()));
            });
        }
        if ((infoAdicional != null) && (!infoAdicional.isEmpty())) {
            return infoAdicional;
        }
        return null;
    }

    private String obtenerTotalDescuento(NotaCreditoReporte nc)
            throws SQLException, ClassNotFoundException {
        BigDecimal descuento = new BigDecimal(0);
        for (DetallesAdicionalesReporte detalle : nc.getDetallesAdiciones()) {
            descuento = descuento.add(new BigDecimal(detalle.getDescuento()));
        }
        return descuento.toString();
    }

    private Map<String, Object> obtenerInfoND(NotaDebito.InfoNotaDebito notaDebito) {
        Map<String, Object> param = new HashMap();
        param.put("DIR_SUCURSAL", notaDebito.getDirEstablecimiento());
        param.put("CONT_ESPECIAL", notaDebito.getContribuyenteEspecial());
        param.put("LLEVA_CONTABILIDAD", notaDebito.getObligadoContabilidad());
        param.put("RS_COMPRADOR", notaDebito.getRazonSocialComprador());
        param.put("RUC_COMPRADOR", notaDebito.getIdentificacionComprador());
        param.put("FECHA_EMISION", notaDebito.getFechaEmision());
        param.put("NUM_DOC_MODIFICADO", notaDebito.getNumDocModificado());
        param.put("FECHA_EMISION_DOC_SUSTENTO", notaDebito.getFechaEmisionDocSustento());
        param.put("DOC_MODIFICADO", StringUtil.obtenerDocumentoModificado(notaDebito.getCodDocModificado()));
        return param;
    }

    private Map<String, Object> obtenerInfoCompRetencion(ComprobanteRetencion.InfoCompRetencion infoComp) {
        Map<String, Object> param = new HashMap();
        param.put("DIR_SUCURSAL", infoComp.getDirEstablecimiento());
        param.put("RS_COMPRADOR", infoComp.getRazonSocialSujetoRetenido());
        param.put("RUC_COMPRADOR", infoComp.getIdentificacionSujetoRetenido());
        param.put("FECHA_EMISION", infoComp.getFechaEmision());
        param.put("CONT_ESPECIAL", infoComp.getContribuyenteEspecial());
        param.put("LLEVA_CONTABILIDAD", infoComp.getObligadoContabilidad());
        param.put("EJERCICIO_FISCAL", infoComp.getPeriodoFiscal());
        return param;
    }

    public void showReport(JasperPrint jp) {
        JasperViwerSRI jv = new JasperViwerSRI(jp, Locale.getDefault());
        List<JRSaveContributor> newSaveContributors = new LinkedList();
        JRSaveContributor[] saveContributors = jv.getSaveContributors();
        for (JRSaveContributor saveContributor : saveContributors) {
            if (saveContributor instanceof JRPdfSaveContributor) {
                newSaveContributors.add(saveContributor);
            }
        }
        jv.setSaveContributors((JRSaveContributor[]) newSaveContributors.toArray(new JRSaveContributor[0]));

        JFrame jf = new JFrame();
        jf.setTitle("Generador de RIDE");
        jf.getContentPane().add(jv);
        jf.validate();
        jf.setVisible(true);
        jf.setSize(new Dimension(800, 650));
        jf.setLocation(300, 100);
        jf.setDefaultCloseOperation(1);
    }
}
