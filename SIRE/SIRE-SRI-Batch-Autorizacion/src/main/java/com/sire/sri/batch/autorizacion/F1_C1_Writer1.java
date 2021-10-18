package com.sire.sri.batch.autorizacion;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import com.sire.soap.util.SoapUtil;
import ec.gob.sri.comprobantes.modelo.liquidacion.Liquidacion;
import ec.gob.sri.comprobantes.modelo.reportes.*;
import ec.gob.sri.ws.autorizacion.*;
import com.sire.event.MailEvent;
import com.sire.service.IMailService;
import com.sire.sri.batch.commons.CommonsItemWriter;
import com.sire.sri.batch.constant.Constant;
import ec.gob.sri.comprobantes.modelo.factura.Factura;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.inject.Named;
import ec.gob.sri.comprobantes.modelo.LoteXml;
import ec.gob.sri.comprobantes.modelo.guia.GuiaRemision;
import ec.gob.sri.comprobantes.modelo.notacredito.NotaCredito;
import ec.gob.sri.comprobantes.modelo.notadebito.NotaDebito;
import ec.gob.sri.comprobantes.modelo.rentencion.ComprobanteRetencion;
import ec.gob.sri.comprobantes.util.reportes.ReporteUtil;
import org.apache.logging.log4j.Level;
import com.sire.logger.LogManager;
import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Properties;
import javax.batch.runtime.BatchRuntime;
import javax.batch.runtime.context.JobContext;
import javax.inject.Inject;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

@Named
public class F1_C1_Writer1 extends CommonsItemWriter {

    @Inject
    private JobContext jobCtx;
    private IMailService mailService;
    private String urlReporte;
    private static final org.apache.logging.log4j.Logger log = LogManager.getLogger(F1_C1_Writer1.class);

    @Override
    public void open(Serializable checkpoint) {
        Properties runtimeParams = BatchRuntime.getJobOperator().getParameters(jobCtx.getExecutionId());
        urlReporte = runtimeParams.getProperty(Constant.URL_REPORTE);
    }

    @Override
    public void writeItems(List<Object> items) {
        Map duplaFinal = new HashMap<>();

        items.forEach((item) -> {
            try{
                RespuestaComprobante respuestaComprobante = (RespuestaComprobante) ((Map) item).get("respuestaComprobante");
                LoteXml lote = (LoteXml) ((Map) item).get(Constant.LOTE);
                for (Object comprobante : lote.getComprobantes()) {
                    String secuencial = null;
                    String claveAcceso = null;
                    String nombreTablaComprobante = null;
                    String nombreSecuencial = null;
                    if (comprobante instanceof Factura) {
                        Factura factura = (Factura) comprobante;
                        secuencial = factura.getInfoTributaria().getEstab() + "-" + factura.getInfoTributaria().getPtoEmi() + "-" + factura.getInfoTributaria().getSecuencial();
                        claveAcceso = factura.getInfoTributaria().getClaveAcceso();
                        nombreTablaComprobante = Constant.FAC_FACTURA_C;
                        nombreSecuencial = Constant.SECUENCIAL;
                    }  else if (comprobante instanceof Liquidacion) {
                        Liquidacion liquidacion = (Liquidacion) comprobante;
                        secuencial = liquidacion.getInfoTributaria().getEstab() + "-" + liquidacion.getInfoTributaria().getPtoEmi() + "-" + liquidacion.getInfoTributaria().getSecuencial();
                        claveAcceso = liquidacion.getInfoTributaria().getClaveAcceso();
                        nombreTablaComprobante = Constant.INV_MOVIMIENTO_CAB;
                        nombreSecuencial = Constant.NUM_SECUENCIAL;
                    } else if (comprobante instanceof NotaCredito) {
                        NotaCredito notaCredito = (NotaCredito) comprobante;
                        secuencial = notaCredito.getInfoTributaria().getEstab() + "-" + notaCredito.getInfoTributaria().getPtoEmi() + "-" + notaCredito.getInfoTributaria().getSecuencial();
                        claveAcceso = notaCredito.getInfoTributaria().getClaveAcceso();
                        nombreTablaComprobante = Constant.FAC_DEVOLUCION_C;
                        nombreSecuencial = Constant.NUM_SECUENCIAL;
                    } else if (comprobante instanceof NotaDebito) {
                        NotaDebito notaDebito = (NotaDebito) comprobante;
                        secuencial = notaDebito.getInfoTributaria().getEstab() + "-" + notaDebito.getInfoTributaria().getPtoEmi() + "-" + notaDebito.getInfoTributaria().getSecuencial();
                        claveAcceso = notaDebito.getInfoTributaria().getClaveAcceso();
                        nombreTablaComprobante = Constant.CXC_DOC_COBRAR;
                        nombreSecuencial = Constant.NUM_SECUENCIAL;
                    } else if (comprobante instanceof GuiaRemision) {
                        GuiaRemision guiaRemision = (GuiaRemision) comprobante;
                        secuencial = guiaRemision.getInfoTributaria().getEstab() + "-" + guiaRemision.getInfoTributaria().getPtoEmi() + "-" + guiaRemision.getInfoTributaria().getSecuencial();
                        claveAcceso = guiaRemision.getInfoTributaria().getClaveAcceso();
                        nombreTablaComprobante = Constant.PED_DESPACHO_C;
                        nombreSecuencial = Constant.NUM_SECUENCIAL;
                    } else if (comprobante instanceof ComprobanteRetencion) {
                        ComprobanteRetencion comprobanteRetencion = (ComprobanteRetencion) comprobante;
                        secuencial = comprobanteRetencion.getInfoTributaria().getEstab() + "-" + comprobanteRetencion.getInfoTributaria().getPtoEmi() + "-" + comprobanteRetencion.getInfoTributaria().getSecuencial();
                        claveAcceso = comprobanteRetencion.getInfoTributaria().getClaveAcceso();
                        nombreTablaComprobante = Constant.BAN_RETENCION_C;
                        nombreSecuencial = Constant.NUM_SECUENCIAL;
                    }

                    Connection connection = null;
                    Statement statement = null;
                    try{
                        connection = getConnection();
                        statement = connection.createStatement();
                        for (Autorizacion autorizacion : respuestaComprobante.getAutorizaciones().getAutorizacion()) {
                            if (claveAcceso.equals(autorizacion.getNumeroAutorizacion())) {
                                log.info("----------------------------------------------------------------");
                                String estado = autorizacion.getEstado();
                                String fechaAutorizacion = autorizacion.getFechaAutorizacion().toGregorianCalendar().getTime().toString();
                                String year = fechaAutorizacion.substring(24);
                                String date = fechaAutorizacion.substring(0, 19);
                                fechaAutorizacion = date + Constant.SPACE + year;

                                DateFormat oldFormat = new SimpleDateFormat(Constant.DATE_FORMAT_1, Locale.ENGLISH);
                                Date oldDate = oldFormat.parse(fechaAutorizacion);

                                DateFormat newFormat = new SimpleDateFormat(Constant.DATE_FORMAT_2);
                                fechaAutorizacion = newFormat.format(oldDate);
                                String claveAccesoConsultada = respuestaComprobante.getClaveAccesoConsultada();
                                String numeroComprobantes = respuestaComprobante.getNumeroComprobantes();

                                log.info("Secuencial: {}, Estado: {}, FechaAutorizacion: {}"
                                                + ", ClaveAccesoConsultada: {}, NumeroComprobantes: {}, Lote: {}"
                                        , secuencial, estado, fechaAutorizacion, claveAccesoConsultada
                                        , numeroComprobantes, lote.getClaveAcceso());

                                String identificador = null;
                                String tipo = null;
                                String mensaje = null;

                                Autorizacion.Mensajes mensajes = autorizacion.getMensajes();
                                if (mensajes != null)
                                    for (Mensaje m : mensajes.getMensaje()) {
                                        identificador = m.getIdentificador();
                                        String informacionAdicional = m.getInformacionAdicional();
                                        mensaje = m.getMensaje();
                                        tipo = m.getTipo();

                                        log.info("Identificador: {}, InformacionAdicional: {}"
                                                        + ", Mensaje: {}, Tipo: {}"
                                                , identificador, informacionAdicional, mensaje, tipo);
                                    }

                                StringBuffer motivo = new StringBuffer();
                                if (!estado.equals(Constant.AUTORIZADO) && !estado.equals(Constant.EN_PROCESAMIENTO)) {
                                    motivo.append(", MOTIVO_SRI = '").append(identificador).append(":").append(tipo)
                                            .append(":").append(mensaje).append("'");
                                    fechaAutorizacion = "";
                                } else if (estado.equals(Constant.AUTORIZADO)) {
                                    fechaAutorizacion = ", FECHA_AUTORIZACION = '" + fechaAutorizacion + "'";
                                    motivo.append(", MOTIVO_SRI = ''");
                                    duplaFinal.put(comprobante, autorizacion);
                                } else {
                                    fechaAutorizacion = "";
                                }

                                StringBuffer cabeceraSQL = new StringBuffer();
                                cabeceraSQL.append("UPDATE ").append(nombreTablaComprobante).append(" SET ")
                                        .append("ESTADO_SRI = '").append(estado).append("'").append(motivo)
                                        .append(fechaAutorizacion).append(" WHERE ")
                                        .append(nombreSecuencial).append(" = '").append(secuencial).append("'");

                                log.info("update " + nombreTablaComprobante + " -> " + cabeceraSQL);
                                statement.addBatch(cabeceraSQL.toString());
                            }
                        }
                        statement.executeBatch();
                    }catch (SQLException | NamingException e) {
                        log.log(Level.ERROR, e);
                    }finally {
                        closeConnections(connection, null, null, null, statement);
                    }
                }
                StringBuffer loteSQL = new StringBuffer();
                loteSQL.append("UPDATE CEL_LOTE_AUTORIZADO SET ESTADO_SRI = 'PROCESADA'")
                        .append(" WHERE CLAVE_ACCESO = ?");
                log.info("update CEL_LOTE_AUTORIZADO -> " + loteSQL);

                Connection connection = null;
                PreparedStatement preparedStatement = null;
                try {
                    connection = getConnection();
                    preparedStatement = connection.prepareStatement(loteSQL.toString());
                    preparedStatement.setString(1, lote.getClaveAcceso());
                    preparedStatement.executeUpdate();
                } catch (SQLException | NamingException e) {
                    log.log(Level.ERROR, e);
                } finally {
                    closeConnections(connection, preparedStatement, null, null, null);
                }
            } catch (ParseException ex) {
                log.log(Level.ERROR, ex);
            }
        });

        duplaFinal.keySet().forEach((key) -> {
            try {
                String nombreComprobante = null;
                String claveAcceso = null;
                String secuencial = null;
                String recipient = null;
                ReporteUtil reporteUtil = new ReporteUtil();
                byte[] pdfBytes = null;
                String razonSocialComprador = null;
                String nombreComercial = null;
                String ruc = null;
                Autorizacion autorizacion = ((Autorizacion) duplaFinal.get(key));
                String numAut = autorizacion.getNumeroAutorizacion();
                String fechaAut = autorizacion.getFechaAutorizacion().toString();
                if (key instanceof Factura) {
                    nombreComprobante = "Factura";
                    Factura factura = (Factura) key;
                    for (Factura.InfoAdicional.CampoAdicional campoAdicional : factura.getInfoAdicional().getCampoAdicional()) {
                        if (campoAdicional.getNombre().equals(Constant.EMAIL)) {
                            recipient = campoAdicional.getValue();
                        }
                    }
                    claveAcceso = factura.getInfoTributaria().getClaveAcceso();
                    secuencial = factura.getInfoTributaria().getSecuencial();
                    FacturaReporte fact = new FacturaReporte(factura);
                    pdfBytes = reporteUtil.generarReporte(urlReporte, fact, numAut, fechaAut);
                    razonSocialComprador = factura.getInfoFactura().getRazonSocialComprador();
                    nombreComercial = factura.getInfoTributaria().getNombreComercial();
                    ruc = factura.getInfoTributaria().getRuc();
                } else if (key instanceof Liquidacion) {
                    nombreComprobante = "Liquidación de Compra";
                    Liquidacion liquidacion = (Liquidacion) key;
                    for (Liquidacion.InfoAdicional.CampoAdicional campoAdicional : liquidacion.getInfoAdicional().getCampoAdicional()) {
                        if (campoAdicional.getNombre().equals(Constant.EMAIL)) {
                            recipient = campoAdicional.getValue();
                        }
                    }
                    claveAcceso = liquidacion.getInfoTributaria().getClaveAcceso();
                    secuencial = liquidacion.getInfoTributaria().getSecuencial();
                    LiquidacionReporte liquidacionReporte = new LiquidacionReporte(liquidacion);
                    pdfBytes = reporteUtil.generarReporte(urlReporte, liquidacionReporte, numAut, fechaAut);
                    razonSocialComprador = liquidacion.getInfoLiquidacionCompra().getRazonSocialProveedor();
                    nombreComercial = liquidacion.getInfoTributaria().getNombreComercial();
                    ruc = liquidacion.getInfoTributaria().getRuc();
                } else if (key instanceof NotaCredito) {
                    nombreComprobante = "Nota de Crédito";
                    NotaCredito notaCredito = (NotaCredito) key;
                    for (NotaCredito.InfoAdicional.CampoAdicional campoAdicional : notaCredito.getInfoAdicional().getCampoAdicional()) {
                        if (campoAdicional.getNombre().equals(Constant.EMAIL)) {
                            recipient = campoAdicional.getValue();
                        }
                    }
                    claveAcceso = notaCredito.getInfoTributaria().getClaveAcceso();
                    secuencial = notaCredito.getInfoTributaria().getSecuencial();
                    NotaCreditoReporte notaCreditoReporte = new NotaCreditoReporte(notaCredito);
                    pdfBytes = reporteUtil.generarReporte(urlReporte, notaCreditoReporte, numAut, fechaAut);
                    razonSocialComprador = notaCredito.getInfoNotaCredito().getRazonSocialComprador();
                    nombreComercial = notaCredito.getInfoTributaria().getNombreComercial();
                    ruc = notaCredito.getInfoTributaria().getRuc();
                } else if (key instanceof NotaDebito) {
                    nombreComprobante = "Nota de Débito";
                    NotaDebito notaDebito = (NotaDebito) key;
                    for (NotaDebito.InfoAdicional.CampoAdicional campoAdicional : notaDebito.getInfoAdicional().getCampoAdicional()) {
                        if (campoAdicional.getNombre().equals(Constant.EMAIL)) {
                            recipient = campoAdicional.getValue();
                        }
                    }
                    claveAcceso = notaDebito.getInfoTributaria().getClaveAcceso();
                    secuencial = notaDebito.getInfoTributaria().getSecuencial();
                    NotaDebitoReporte notaDebitoReporte = new NotaDebitoReporte(notaDebito);
                    pdfBytes = reporteUtil.generarReporte(urlReporte, notaDebitoReporte, numAut, fechaAut);
                    razonSocialComprador = notaDebito.getInfoNotaDebito().getRazonSocialComprador();
                    nombreComercial = notaDebito.getInfoTributaria().getNombreComercial();
                    ruc = notaDebito.getInfoTributaria().getRuc();
                } else if (key instanceof GuiaRemision) {
                    nombreComprobante = "Guía de Remisión";
                    GuiaRemision guiaRemision = (GuiaRemision) key;
                    for (GuiaRemision.InfoAdicional.CampoAdicional campoAdicional : guiaRemision.getInfoAdicional().getCampoAdicional()) {
                        if (campoAdicional.getNombre().equals(Constant.EMAIL)) {
                            recipient = campoAdicional.getValue();
                        }
                    }
                    claveAcceso = guiaRemision.getInfoTributaria().getClaveAcceso();
                    secuencial = guiaRemision.getInfoTributaria().getSecuencial();
                    GuiaRemisionReporte guiaRemisionReporte = new GuiaRemisionReporte(guiaRemision);
                    pdfBytes = reporteUtil.generarReporte(urlReporte, guiaRemisionReporte, numAut, fechaAut, guiaRemision);
                    razonSocialComprador = guiaRemision.getDestinatarios().getDestinatario().get(0).getRazonSocialDestinatario();
                    nombreComercial = guiaRemision.getInfoTributaria().getNombreComercial();
                    ruc = guiaRemision.getInfoTributaria().getRuc();
                } else if (key instanceof ComprobanteRetencion) {
                    nombreComprobante = "Retención";
                    ComprobanteRetencion comprobanteRetencion = (ComprobanteRetencion) key;
                    for (ComprobanteRetencion.InfoAdicional.CampoAdicional campoAdicional : comprobanteRetencion.getInfoAdicional().getCampoAdicional()) {
                        if (campoAdicional.getNombre().equals(Constant.EMAIL)) {
                            recipient = campoAdicional.getValue();
                        }
                    }
                    claveAcceso = comprobanteRetencion.getInfoTributaria().getClaveAcceso();
                    secuencial = comprobanteRetencion.getInfoTributaria().getSecuencial();
                    ComprobanteRetencionReporte comprobanteRetencionReporte = new ComprobanteRetencionReporte(comprobanteRetencion);
                    if(log.isDebugEnabled()) {
                        log.debug("Generando PDF - ({},{},{},{})", urlReporte, comprobanteRetencionReporte, numAut, fechaAut);
                    }
                    pdfBytes = reporteUtil.generarReporte(urlReporte, comprobanteRetencionReporte, numAut, fechaAut);
                    if(log.isDebugEnabled()) {
                        log.debug("PDF Generado: {}", pdfBytes);
                    }
                    razonSocialComprador = comprobanteRetencion.getInfoCompRetencion().getRazonSocialSujetoRetenido();
                    nombreComercial = comprobanteRetencion.getInfoTributaria().getNombreComercial();
                    ruc = comprobanteRetencion.getInfoTributaria().getRuc();
                }

                //construct the mime multi part
                MimeMultipart mimeMultipart = new MimeMultipart();
                String autorizacionXml = object2xmlUnicode(autorizacion, Autorizacion.class, "ec.gob.sri.ws.autorizacion",
                        "autorizacion");

                if(log.isTraceEnabled()) {
                    log.trace("AutorizacionXml Generada: {} ", autorizacionXml);
                }

                addBodyPart(pdfBytes, "application/pdf", claveAcceso + ".pdf", mimeMultipart);
                addBodyPart(autorizacionXml.getBytes(), "application/xml", claveAcceso + ".xml", mimeMultipart);

                BodyPart messageBodyPart = new MimeBodyPart();
                StringBuffer content = new StringBuffer();
                content.append("Estimado Cliente ")
                        .append(razonSocialComprador)
                        .append(",<br><br>Nos complace adjuntar su e-Comprobante con el siguiente detalle:<br>")
                        .append("<br>e - Comprobante No: <b>")
                        .append(numAut, 24, 39)
                        .append("</b><br><br>Fecha Emisión: <b>")
                        .append(fechaAut)
                        .append("</b><br><br>")
                        .append("El documento pdf y xml de su comprobante se encuentra adjunto a este correo.<br>")
                        .append("<br>Atentamente:<br>")
                        .append(nombreComercial)
                        .append("<br>RUC: ")
                        .append(ruc);

                messageBodyPart.setContent(content.toString(),"text/html; charset=utf-8");
                mimeMultipart.addBodyPart(messageBodyPart);

                MailEvent event = new MailEvent();
                event.setTo(recipient);
                event.setSubject("Comprobante - " + nombreComprobante);
                event.setMimeMultipart(mimeMultipart);
                Map properties = new HashMap<String, String>();
                properties.put("secuencial", secuencial);
                event.setProperties(properties);

                if (recipient != null && !recipient.isEmpty()) {
                    log.info("Secuencial de Comprobante a ser enviado por mail: " + secuencial);
                    getMailService().sendMail(event); //firing event!
                }
            } catch (NamingException | MessagingException | JAXBException | SQLException | ClassNotFoundException | IOException ex) {
                log.log(Level.ERROR, ex);
            }
        });
        urlReporte = null;
    }

    private void addBodyPart(byte[] bytes, String type, String fileName, MimeMultipart mimeMultipart) throws MessagingException {
        DataSource dataSource = new ByteArrayDataSource(bytes, type);
        MimeBodyPart xmlBodyPart = new MimeBodyPart();
        xmlBodyPart.setDataHandler(new DataHandler(dataSource));
        xmlBodyPart.setFileName(fileName);
        mimeMultipart.addBodyPart(xmlBodyPart);
    }

    private IMailService getMailService() throws NamingException {
        if (mailService == null) {
            InitialContext ic = new InitialContext();
            mailService = (IMailService) ic.lookup("java:global/SIRE-EE/SIRE-Services/MailService!com.sire.service.IMailService");
        }
        return mailService;
    }

    @Override
    public JAXBContext getContextInstance(Class objectClass) {
        return SoapUtil.getContextInstance(objectClass);
    }
}