package com.sire.sri.batch.recepcion;

import com.sire.signature.GenericXMLSignature;
import com.sire.signature.XAdESBESSignature;
import com.sire.soap.util.SoapUtil;
import com.sire.sri.batch.commons.CommonsItemWriter;
import com.sire.sri.batch.constant.Constant;
import com.sun.xml.messaging.saaj.soap.impl.ElementImpl;
import ec.gob.sri.comprobantes.modelo.Lote;
import ec.gob.sri.comprobantes.modelo.factura.Factura;
import ec.gob.sri.comprobantes.modelo.guia.GuiaRemision;
import ec.gob.sri.comprobantes.modelo.liquidacion.Liquidacion;
import ec.gob.sri.comprobantes.modelo.notacredito.NotaCredito;
import ec.gob.sri.comprobantes.modelo.notadebito.NotaDebito;
import ec.gob.sri.comprobantes.modelo.rentencion.ComprobanteRetencion;

import java.io.*;

import org.apache.logging.log4j.Level;
import com.sire.logger.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.CDATA;
import org.dom4j.DocumentHelper;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.batch.runtime.BatchRuntime;
import javax.batch.runtime.context.JobContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.NamingException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import ec.gob.sri.ws.recepcion.*;

@Named
public class F1_C1_Writer1 extends CommonsItemWriter {

    @Inject
    private JobContext jobCtx;
    private StringBuffer pathSignature;
    private String passSignature;
    private String urlRecepcion;
    private String codEmpresa;
    private static final Logger log = LogManager.getLogger(F1_C1_Writer1.class);

    @Override
    public void open(Serializable checkpoint) throws Exception {
        String home = System.getProperty(Constant.SIRE_HOME);
        if (home == null) {
            log.warn("SIRE HOME NOT FOUND.");
            return;
        }
        Properties runtimeParams = BatchRuntime.getJobOperator().getParameters(jobCtx.getExecutionId());
        codEmpresa = runtimeParams.getProperty(Constant.COD_EMPRESA);
        pathSignature = new StringBuffer();
        pathSignature.append(home).append(File.separator).append(runtimeParams.getProperty(Constant.PATH_SIGNATURE));
        passSignature = runtimeParams.getProperty(Constant.PASS_SIGNATURE);
        urlRecepcion = runtimeParams.getProperty(Constant.URL_RECEPCION);
    }

    @Override
    public void writeItems(List<Object> items) {
        if(Objects.isNull(items)) {
            return;
        }
        try {
            Lote lote = new Lote();
            for (Object item : items) {
                if (lote.getClaveAcceso() == null) {
                    if (((Map) item).get(Constant.COMPROBANTE) instanceof Factura) {
                        lote.setClaveAcceso(getClaveAccesoLote(Constant.CERO_UNO));
                        log.log(Level.INFO, "{}: {}", Constant.TIPO, Constant.CERO_UNO);
                    } else if (((Map) item).get(Constant.COMPROBANTE) instanceof Liquidacion) {
                        lote.setClaveAcceso(getClaveAccesoLote(Constant.CERO_TRES));
                        log.log(Level.INFO, "{}: {}", Constant.TIPO, Constant.CERO_TRES);
                    } else if (((Map) item).get(Constant.COMPROBANTE) instanceof NotaCredito) {
                        lote.setClaveAcceso(getClaveAccesoLote(Constant.CERO_CUATRO));
                        log.log(Level.INFO, "{}: {}", Constant.TIPO, Constant.CERO_CUATRO);
                    } else if (((Map) item).get(Constant.COMPROBANTE) instanceof NotaDebito) {
                        lote.setClaveAcceso(getClaveAccesoLote(Constant.CERO_CINCO));
                        log.log(Level.INFO, "{}: {}", Constant.TIPO, Constant.CERO_CINCO);
                    } else if (((Map) item).get(Constant.COMPROBANTE) instanceof GuiaRemision) {
                        lote.setClaveAcceso(getClaveAccesoLote(Constant.CERO_SEIS));
                        log.log(Level.INFO, "{}: {}", Constant.TIPO, Constant.CERO_SEIS);
                    } else if (((Map) item).get(Constant.COMPROBANTE) instanceof ComprobanteRetencion) {
                        lote.setClaveAcceso(getClaveAccesoLote(Constant.CERO_SIETE));
                        log.log(Level.INFO, "{}: {}", Constant.TIPO, Constant.CERO_SIETE);
                    }
                }
                GenericXMLSignature genericXMLSignature = XAdESBESSignature.firmar(xml2document(SoapUtil.object2xml(((Map) item)
                        .get(Constant.COMPROBANTE))), pathSignature.toString(), passSignature);
                String signedXml = genericXMLSignature.toSignedXml();
                if(Objects.isNull(signedXml)) {
                    log.log(Level.ERROR, "No se pudo firmar lote {}.", lote.getClaveAcceso());
                    return;
                }
                lote.getComprobantes().getComprobante().add(appendCdata(signedXml));
            }

            log.log(Level.INFO, "# Items: {}", items.size());
            log.log(Level.INFO, "Lote Clave Acceso: {}", lote.getClaveAcceso());

            if (lote.getClaveAcceso() != null) {
                lote.setRuc(lote.getClaveAcceso().substring(10, 23));
                lote.setVersion("1.0.0");

                String loteXml = object2xmlUnicode(lote, null,null,null);

                if(log.isTraceEnabled()) {
                    log.trace("loteXml: {}", loteXml);
                }

                Map mapCall = SoapUtil.call(
                        createSOAPMessage(new String(Base64.getEncoder().encode(doc2bytes(xml2document(loteXml))))),
                        new URL(urlRecepcion),
                        null);
                SOAPMessage soapMessage = (SOAPMessage) mapCall.get(Constant.SOAP_MESSAGE);

                if(log.isTraceEnabled()) {
                    log.trace("Soap Recepcion Response:");
                    log.trace(SoapUtil.getStringFromSoapMessage(soapMessage));
                }

                ValidarComprobanteResponse validarComprobanteResponse = toValidarComprobanteResponse(soapMessage);

                String estadoLote = validarComprobanteResponse.getRespuestaRecepcionComprobante().getEstado();

                if(estadoLote != null && !estadoLote.isEmpty()) {

                    items.forEach((item) -> processResponse(item, validarComprobanteResponse, lote.getClaveAcceso()));
                    String secuencial = lote.getClaveAcceso().substring(30, 39);

                    String fechaEstado = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(Calendar.getInstance()
                            .getTime());

                    StringBuffer insertSQL = new StringBuffer();
                    insertSQL.append("INSERT INTO CEL_LOTE_AUTORIZADO ")
                            .append("VALUES ('").append(codEmpresa).append("',")
                            .append(secuencial).append(",'01','").append(lote.getClaveAcceso())
                            .append("','").append(estadoLote).append("','").append(fechaEstado)
                            .append("')");

                    if(log.isTraceEnabled()) {
                        log.trace("insertSQL -> {}", insertSQL.toString());
                    }

                    Connection connection = null;
                    PreparedStatement preparedStatement = null;
                    try {
                        connection = getConnection();
                        preparedStatement = connection.prepareStatement(insertSQL.toString());
                        preparedStatement.executeUpdate();
                    } catch (SQLException | NamingException e) {
                        log.log(Level.ERROR, e);
                    } finally {
                        closeConnections(connection, preparedStatement, null, null, null);
                    }
                }
            }
        } catch (SOAPException | XPathExpressionException | MalformedURLException | JAXBException ex) {
            log.log(Level.ERROR, ex);
        } catch (TransformerException e) {
            log.log(Level.ERROR, e);
        }
    }

    private Document xml2document(String xml) throws JAXBException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8.name()));
            return db.parse(stream);
        } catch (ParserConfigurationException | SAXException | IOException | IllegalArgumentException ex) {
            log.log(Level.ERROR,"Error al parsear el documento: ",ex);
            return null;
        }
    }

    private SOAPMessage createSOAPMessage(String xmlBase64) {
        try {
            MessageFactory factory = MessageFactory.newInstance();
            SOAPMessage soapMsg = factory.createMessage();
            SOAPPart part = soapMsg.getSOAPPart();

            SOAPEnvelope envelope = part.getEnvelope();
            envelope.addNamespaceDeclaration("ec", "http://ec.gob.sri.ws.recepcion");
            SOAPBody body = envelope.getBody();
            MimeHeaders headers = soapMsg.getMimeHeaders();
            headers.addHeader("SOAPAction", "");
            SOAPBodyElement element = body.addBodyElement(
                    envelope.createName("ec:validarComprobante"));
            element.addChildElement("xml").addTextNode(xmlBase64);

            soapMsg.saveChanges();
            return soapMsg;
        } catch (SOAPException ex) {
            log.log(Level.ERROR, ex);
        }
        return null;
    }

    private ValidarComprobanteResponse toValidarComprobanteResponse(SOAPMessage soapMessage) throws SOAPException,
            XPathExpressionException {
        ValidarComprobanteResponse validarComprobanteResponse = new ValidarComprobanteResponse();
        RespuestaSolicitud respuestaRecepcionComprobante = new RespuestaSolicitud();
        validarComprobanteResponse.setRespuestaRecepcionComprobante(respuestaRecepcionComprobante);
        RespuestaSolicitud.Comprobantes comprobantes = new RespuestaSolicitud.Comprobantes();
        respuestaRecepcionComprobante.setComprobantes(comprobantes);

        XPath xpath = XPathFactory.newInstance().newXPath();

        Node estadoNode = (Node) xpath.evaluate("//estado", soapMessage.getSOAPBody(), XPathConstants.NODE);
        if (estadoNode != null) {
            respuestaRecepcionComprobante.setEstado(estadoNode.getValue());
        }

        NodeList comprobantesNodeList = (NodeList) xpath.evaluate("//comprobantes/comprobante", soapMessage.getSOAPBody(), XPathConstants.NODESET);

        for (int i = 0; i < comprobantesNodeList.getLength(); i++) {
            Comprobante comprobante = new Comprobante();
            comprobantes.getComprobante().add(comprobante);
            Node comprobanteNode = (Node) comprobantesNodeList.item(i);

            NodeList hijosComprobante = comprobanteNode.getChildNodes();

            for (int j = 0; j < hijosComprobante.getLength(); j++) {
                if (hijosComprobante.item(j).getNodeName().equals(Constant.CLAVE_ACCESO_CC)) {
                    comprobante.setClaveAcceso(hijosComprobante.item(j).getTextContent());
                } else if (hijosComprobante.item(j).getNodeName().equals(Constant.MENSAJES)) {
                    NodeList mensajesNodeList;
                    if(hijosComprobante.item(j) instanceof ElementImpl) {
                        ElementImpl elementImpl = (ElementImpl) hijosComprobante.item(j);
                        mensajesNodeList = elementImpl.getChildNodes();
                    }else {
                        mensajesNodeList = (NodeList) hijosComprobante.item(j);
                    }
                    Comprobante.Mensajes mensajes = new Comprobante.Mensajes();
                    comprobante.setMensajes(mensajes);
                    Mensaje mensaje = new Mensaje();
                    for (int k = 0; k < mensajesNodeList.getLength(); k++) {
                        Node mensajeNode = (Node) mensajesNodeList.item(k);
                        NodeList hijos = mensajeNode.getChildNodes();
                        for (int l = 0; l < hijos.getLength(); l++) {
                            switch (hijos.item(l).getNodeName()) {
                                case Constant.IDENTIFICADOR:
                                    mensaje.setIdentificador(hijos.item(l).getTextContent());
                                    break;
                                case Constant.MENSAJE:
                                    mensaje.setMensaje(hijos.item(l).getTextContent());
                                    break;
                                case Constant.TIPO:
                                    mensaje.setTipo(hijos.item(l).getTextContent());
                                    break;
                                case Constant.INFORMACION_ADICIONAL:
                                    mensaje.setInformacionAdicional(hijos.item(l).getTextContent());
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                    mensajes.getMensaje().add(mensaje);
                }
            }
        }
        return validarComprobanteResponse;
    }

    private static byte[] doc2bytes(Document document) {
        try {
            Source source = new DOMSource(document);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Result result = new StreamResult(out);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.transform(source, result);
            return out.toByteArray();
        } catch (TransformerException e) {
            log.log(Level.ERROR, "Error al convertir documento a arreglo de bytes.", e);
        }
        return null;
    }

    public static String appendCdata(String input) {
        CDATA cdata = DocumentHelper.createCDATA(input);
        return cdata.asXML();
    }

    private synchronized String getClaveAccesoLote(String tipoComprobante) {

        String getClaveAccesoLote = "{call SP_CLAVE_ACCESO_LOTE(?,?,?)}";

        Connection connection = null;
        CallableStatement callableStatement = null;
        try{
            connection = getConnection();
            callableStatement = connection.prepareCall(getClaveAccesoLote);
            callableStatement.setString(1, codEmpresa);
            callableStatement.setString(2, tipoComprobante);
            callableStatement.registerOutParameter(3, java.sql.Types.VARCHAR);

            callableStatement.executeUpdate();

            return callableStatement.getString(3);

        } catch (SQLException | NamingException e) {
            log.log(Level.ERROR, e);
            return null;
        } finally {
            closeConnections(connection,null, null, callableStatement, null);
        }
    }

    private void processResponse(Object item, ValidarComprobanteResponse validarComprobanteResponse,
                                 String claveAccesoLote) {
        String claveAcceso;
        StringBuffer secuencial;
        StringBuffer cabeceraSQL;
        String nombreTablaComprobante;
        String nombreSecuencial;
        if (((Map) item).get(Constant.COMPROBANTE) instanceof Factura) {
            Factura factura = (Factura) ((Map) item).get(Constant.COMPROBANTE);
            secuencial = new StringBuffer();
            secuencial.append(factura.getInfoTributaria().getEstab()).append(Constant.GUION)
                    .append(factura.getInfoTributaria().getPtoEmi()).append(Constant.GUION)
                    .append(factura.getInfoTributaria().getSecuencial());
            claveAcceso = factura.getInfoTributaria().getClaveAcceso();
            nombreTablaComprobante = Constant.FAC_FACTURA_C;
            nombreSecuencial = Constant.SECUENCIAL;
        } else if (((Map) item).get(Constant.COMPROBANTE) instanceof Liquidacion) {
            Liquidacion liquidacion = (Liquidacion) ((Map) item).get(Constant.COMPROBANTE);
            secuencial = new StringBuffer();
            secuencial.append(liquidacion.getInfoTributaria().getEstab()).append(Constant.GUION)
                    .append(liquidacion.getInfoTributaria().getPtoEmi()).append(Constant.GUION)
                    .append(liquidacion.getInfoTributaria().getSecuencial());
            claveAcceso = liquidacion.getInfoTributaria().getClaveAcceso();
            nombreTablaComprobante = Constant.CXP_DOC_PAGAR;
            nombreSecuencial = Constant.SECUENCIAL;
        } else if (((Map) item).get(Constant.COMPROBANTE) instanceof NotaCredito) {
            NotaCredito notaCredito = (NotaCredito) ((Map) item).get(Constant.COMPROBANTE);
            secuencial = new StringBuffer();
            secuencial.append(notaCredito.getInfoTributaria().getEstab()).append(Constant.GUION)
                    .append(notaCredito.getInfoTributaria().getPtoEmi()).append(Constant.GUION)
                    .append(notaCredito.getInfoTributaria().getSecuencial());
            claveAcceso = notaCredito.getInfoTributaria().getClaveAcceso();
            nombreTablaComprobante = Constant.FAC_DEVOLUCION_C;
            nombreSecuencial = Constant.NUM_SECUENCIAL;
        } else if (((Map) item).get(Constant.COMPROBANTE) instanceof NotaDebito) {
            NotaDebito notaDebito = (NotaDebito) ((Map) item).get(Constant.COMPROBANTE);
            secuencial = new StringBuffer();
            secuencial.append(notaDebito.getInfoTributaria().getEstab()).append(Constant.GUION)
                    .append(notaDebito.getInfoTributaria().getPtoEmi()).append(Constant.GUION)
                    .append(notaDebito.getInfoTributaria().getSecuencial());
            claveAcceso = notaDebito.getInfoTributaria().getClaveAcceso();
            nombreTablaComprobante = Constant.CXC_DOC_COBRAR;
            nombreSecuencial = Constant.NUM_SECUENCIAL;
        } else if (((Map) item).get(Constant.COMPROBANTE) instanceof GuiaRemision) {
            GuiaRemision guiaRemision = (GuiaRemision) ((Map) item).get(Constant.COMPROBANTE);
            secuencial = new StringBuffer();
            secuencial.append(guiaRemision.getInfoTributaria().getEstab()).append(Constant.GUION)
                    .append(guiaRemision.getInfoTributaria().getPtoEmi()).append(Constant.GUION)
                    .append(guiaRemision.getInfoTributaria().getSecuencial());
            claveAcceso = guiaRemision.getInfoTributaria().getClaveAcceso();
            nombreTablaComprobante = Constant.PED_DESPACHO_C;
            nombreSecuencial = Constant.NUM_SECUENCIAL;
        } else if (((Map) item).get(Constant.COMPROBANTE) instanceof ComprobanteRetencion) {
            ComprobanteRetencion comprobanteRetencion = (ComprobanteRetencion) ((Map) item).get(Constant.COMPROBANTE);
            secuencial = new StringBuffer();
            secuencial.append(comprobanteRetencion.getInfoTributaria().getEstab()).append(Constant.GUION)
                    .append(comprobanteRetencion.getInfoTributaria().getPtoEmi()).append(Constant.GUION)
                    .append(comprobanteRetencion.getInfoTributaria().getSecuencial());
            claveAcceso = comprobanteRetencion.getInfoTributaria().getClaveAcceso();
            nombreTablaComprobante = Constant.BAN_RETENCION_C;
            nombreSecuencial = Constant.NUM_SECUENCIAL;
        } else {
            throw new RuntimeException("El comprobante no es de alguna clase conocida.");
        }
        boolean existsError = false;
        for (Comprobante c : validarComprobanteResponse.getRespuestaRecepcionComprobante()
                .getComprobantes().getComprobante()) {
            if (claveAcceso.equals(c.getClaveAcceso())) {
                existsError = true;

                c.getMensajes().getMensaje().stream().map((mensaje) -> {
                    log.log(Level.INFO, "Identificador -> {}", mensaje.getIdentificador());
                    return mensaje;
                }).map((mensaje) -> {
                    log.log(Level.INFO, "Tipo -> {}", mensaje.getTipo());
                    return mensaje;
                }).map((mensaje) -> {
                    log.log(Level.INFO, "Mensaje -> {}", mensaje.getMensaje());
                    return mensaje;
                }).forEachOrdered((mensaje) -> {
                    log.log(Level.INFO, "InformacionAdicional -> {}", mensaje.getInformacionAdicional());
                });
                log.info("-------------------------------------------------------------");

                String estado = Constant.DEVUELTA;
                String claveAccesoRecibida = c.getClaveAcceso();
                String identificador = c.getMensajes().getMensaje().get(0).getIdentificador();
                String informacionAdicional = c.getMensajes().getMensaje().get(0).getInformacionAdicional();
                String mensaje = c.getMensajes().getMensaje().get(0).getMensaje();
                String tipo = c.getMensajes().getMensaje().get(0).getTipo();

                log.log(Level.INFO,"Secuencial: {}, Estado: {}, ClaveAcceso: {}, Identificador: {}, " +
                                "InformacionAdicional: {}, Mensaje: {}, Tipo: {}, Lote: {}",
                        secuencial, estado, claveAccesoRecibida, identificador, informacionAdicional, mensaje, tipo
                        , claveAccesoLote);

                StringBuffer motivo = new StringBuffer();
                motivo.append(", MOTIVO_SRI = '").append(identificador).append(":").append(tipo).append(":")
                        .append(mensaje).append("'");

                cabeceraSQL = new StringBuffer();
                cabeceraSQL.append("UPDATE ").append(nombreTablaComprobante).append(" SET ")
                        .append("ESTADO_SRI = ?, ").append("CLAVE_ACCESO_LOTE = ?").append(motivo)
                        .append(" WHERE ").append(nombreSecuencial).append(" = ?");

                if(log.isTraceEnabled()) {
                    log.trace("update {} -> {}", new Object[]{nombreTablaComprobante, cabeceraSQL});
                }

                Connection connection = null;
                PreparedStatement preparedStatement = null;
                try{
                    connection = getConnection();
                    preparedStatement = connection.prepareStatement(cabeceraSQL.toString());
                    preparedStatement.setString(1, estado);
                    preparedStatement.setString(2, claveAccesoLote);
                    preparedStatement.setString(3, secuencial.toString());
                    preparedStatement.executeUpdate();
                } catch (SQLException | NamingException e) {
                    log.log(Level.ERROR, e);
                } finally {
                    closeConnections(connection, preparedStatement, null, null, null);
                }
            }
        }
        if (existsError == false) {
            cabeceraSQL = new StringBuffer();
            cabeceraSQL.append("UPDATE ").append(nombreTablaComprobante).append(" SET ")
                    .append("ESTADO_SRI = 'RECIBIDA', ").append("CLAVE_ACCESO_LOTE = ? ").append("WHERE ")
                    .append(nombreSecuencial).append(" = ?");

            if(log.isTraceEnabled()){
                log.trace("update -> {}", cabeceraSQL);
            }

            executeSql(cabeceraSQL.toString(), claveAccesoLote , secuencial.toString());
        }
    }

    @Override
    public JAXBContext getContextInstance(Class objectClass) throws JAXBException {
        return SoapUtil.getContextInstance(objectClass);
    }
}