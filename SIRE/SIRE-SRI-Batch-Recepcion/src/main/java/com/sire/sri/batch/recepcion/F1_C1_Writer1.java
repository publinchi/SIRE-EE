package com.sire.sri.batch.recepcion;

import com.sire.signature.GenericXMLSignature;
import com.sire.signature.XAdESBESSignature;
import com.sire.soap.util.SoapUtil;
import com.sire.sri.batch.commons.CommonsItemWriter;
import com.sun.xml.messaging.saaj.soap.impl.ElementImpl;
import ec.gob.sri.comprobantes.modelo.Lote;
import ec.gob.sri.comprobantes.modelo.factura.Factura;
import ec.gob.sri.comprobantes.modelo.guia.GuiaRemision;
import ec.gob.sri.comprobantes.modelo.notacredito.NotaCredito;
import ec.gob.sri.comprobantes.modelo.notadebito.NotaDebito;
import ec.gob.sri.comprobantes.modelo.rentencion.ComprobanteRetencion;

import java.io.*;

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
import java.util.Base64;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import recepcion.ws.sri.gob.ec.Mensaje;
import recepcion.ws.sri.gob.ec.RespuestaSolicitud;
import recepcion.ws.sri.gob.ec.ValidarComprobanteResponse;

@Named
public class F1_C1_Writer1 extends CommonsItemWriter {

    @Inject
    private JobContext jobCtx;
    private String pathSignature;
    private String passSignature;
    private String urlRecepcion;
    private String claveAccesoLote;
    private String codEmpresa;
    private Logger log = Logger.getLogger(F1_C1_Writer1.class.getName());

    @Override
    public void open(Serializable checkpoint) throws Exception {
        String home = System.getProperty("sire.home");
        if (home == null) {
            log.warning("SIRE HOME NOT FOUND.");
            return;
        }
        Properties runtimeParams = BatchRuntime.getJobOperator().getParameters(jobCtx.getExecutionId());
        codEmpresa = runtimeParams.getProperty("codEmpresa");
        pathSignature = home + File.separator + runtimeParams.getProperty("pathSignature");
        passSignature = runtimeParams.getProperty("passSignature");
        urlRecepcion = runtimeParams.getProperty("urlRecepcion");
    }

    @Override
    public void writeItems(List<Object> items) {
        try {
            Lote lote = new Lote();
            for (Object item : items) {
                if (claveAccesoLote == null) {
                    Logger.getLogger(F1_C1_Writer1.class.getName()).log(Level.INFO, "comprobante: " + ((Map) item).get("comprobante"));
                    if (((Map) item).get("comprobante") instanceof Factura) {
                        getClaveAccesoLote("01");
                        Logger.getLogger(F1_C1_Writer1.class.getName()).log(Level.INFO, "tipo: " + "01");
                    } else if (((Map) item).get("comprobante") instanceof NotaCredito) {
                        getClaveAccesoLote("04");
                        Logger.getLogger(F1_C1_Writer1.class.getName()).log(Level.INFO, "tipo: " + "04");
                    } else if (((Map) item).get("comprobante") instanceof NotaDebito) {
                        getClaveAccesoLote("05");
                        Logger.getLogger(F1_C1_Writer1.class.getName()).log(Level.INFO, "tipo: " + "05");
                    } else if (((Map) item).get("comprobante") instanceof GuiaRemision) {
                        getClaveAccesoLote("06");
                        Logger.getLogger(F1_C1_Writer1.class.getName()).log(Level.INFO, "tipo: " + "06");
                    } else if (((Map) item).get("comprobante") instanceof ComprobanteRetencion) {
                        getClaveAccesoLote("07");
                        Logger.getLogger(F1_C1_Writer1.class.getName()).log(Level.INFO, "tipo: " + "07");
                    }
                    lote.setClaveAcceso(claveAccesoLote);
                }
                GenericXMLSignature genericXMLSignature = XAdESBESSignature.firmar(xml2document(object2xml(((Map) item).get("comprobante"))), pathSignature, passSignature);
                String signedXml = genericXMLSignature.toSignedXml();
                lote.getComprobantes().getComprobante().add(appendCdata(signedXml));
            }

            Logger.getLogger(F1_C1_Writer1.class.getName()).log(Level.INFO, "# Items: " + items.size());
            Logger.getLogger(F1_C1_Writer1.class.getName()).log(Level.INFO, "Lote Clave Acceso: " + lote.getClaveAcceso());

            if (lote.getClaveAcceso() != null) {
                lote.setRuc(lote.getClaveAcceso().substring(10, 23));
                lote.setVersion("1.0.0");

                String loteXml = object2xmlUnicode(lote, null,null,null);

                log.log(Level.INFO, "loteXml: {0}", loteXml);

                Map mapCall = (Map) SoapUtil.call(
                        createSOAPMessage(new String(Base64.getEncoder().encode(doc2bytes(xml2document(loteXml))))),
                        new URL(urlRecepcion),
                        null,
                        null);
                SOAPMessage soapMessage = (SOAPMessage) mapCall.get("soapMessage");
                log.info("Soap Recepcion Response:");
                log.info(SoapUtil.getStringFromSoapMessage(soapMessage));
                ValidarComprobanteResponse validarComprobanteResponse = toValidarComprobanteResponse(soapMessage);

                String estadoLote = validarComprobanteResponse.getRespuestaRecepcionComprobante().getEstado();

                items.forEach((item) -> {
                    processResponse(item, validarComprobanteResponse);
                });
                String secuencial = claveAccesoLote.substring(30, 39);

                String fechaEstado = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime());

                String insertSQL = "INSERT INTO CEL_LOTE_AUTORIZADO "
                        + "VALUES ('" + codEmpresa + "',"+ secuencial + ",'01','" + claveAccesoLote + "','" + estadoLote + "','" + fechaEstado + "')";
                try (PreparedStatement preparedStatement = getConnection().prepareStatement(insertSQL)) {
                    preparedStatement.executeUpdate();
                    preparedStatement.close();
                }
            }
        } catch (SQLException | NamingException | SOAPException | XPathExpressionException | MalformedURLException | JAXBException ex) {
            Logger.getLogger(F1_C1_Writer1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String object2xml(Object item) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(item.getClass());
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        StringWriter sw = new StringWriter();
        jaxbMarshaller.marshal(item, sw);

        return sw.toString();
    }

    private Document xml2document(String xml) throws JAXBException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8.name()));
            return db.parse(stream);
        } catch (ParserConfigurationException | SAXException | IOException | IllegalArgumentException ex) {
            System.err.println("Error al parsear el documento");
            System.exit(-1);
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
            System.err.println(ex);
        }
        return null;
    }

    private ValidarComprobanteResponse toValidarComprobanteResponse(SOAPMessage soapMessage) throws SOAPException, XPathExpressionException {
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
            recepcion.ws.sri.gob.ec.Comprobante comprobante = new recepcion.ws.sri.gob.ec.Comprobante();
            comprobantes.getComprobante().add(comprobante);
            Node comprobanteNode = (Node) comprobantesNodeList.item(i);

            NodeList hijosComprobante = comprobanteNode.getChildNodes();

            for (int j = 0; j < hijosComprobante.getLength(); j++) {
                if (hijosComprobante.item(j).getNodeName().equals("claveAcceso")) {
                    comprobante.setClaveAcceso(hijosComprobante.item(j).getTextContent());
                } else if (hijosComprobante.item(j).getNodeName().equals("mensajes")) {
                    NodeList mensajesNodeList;
                    if(hijosComprobante.item(j) instanceof ElementImpl) {
                        ElementImpl elementImpl = (ElementImpl) hijosComprobante.item(j);
                        mensajesNodeList = elementImpl.getChildNodes();
                    }else {
                        mensajesNodeList = (NodeList) hijosComprobante.item(j);
                    }
                    recepcion.ws.sri.gob.ec.Comprobante.Mensajes mensajes = new recepcion.ws.sri.gob.ec.Comprobante.Mensajes();
                    comprobante.setMensajes(mensajes);
                    Mensaje mensaje = new Mensaje();
                    for (int k = 0; k < mensajesNodeList.getLength(); k++) {
                        Node mensajeNode = (Node) mensajesNodeList.item(k);
                        NodeList hijos = mensajeNode.getChildNodes();
                        for (int l = 0; l < hijos.getLength(); l++) {
                            switch (hijos.item(l).getNodeName()) {
                                case "identificador":
                                    mensaje.setIdentificador(hijos.item(l).getTextContent());
                                    break;
                                case "mensaje":
                                    mensaje.setMensaje(hijos.item(l).getTextContent());
                                    break;
                                case "tipo":
                                    mensaje.setTipo(hijos.item(l).getTextContent());
                                    break;
                                case "informacionAdicional":
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
            System.err.println("Error al convertir documento a arreglo de bytes.");
        }
        return null;
    }

    public static String appendCdata(String input) {
        CDATA cdata = DocumentHelper.createCDATA(input);
        return cdata.asXML();
    }

    private void getClaveAccesoLote(String tipoComprobante) throws SQLException, NamingException {
        Connection dbConnection = null;
        CallableStatement callableStatement = null;

        String getClaveAccesoLote = "{call SP_CLAVE_ACCESO_LOTE(?,?,?)}";

        try {
            dbConnection = getConnection();
            callableStatement = dbConnection.prepareCall(getClaveAccesoLote);

            callableStatement.setString(1, codEmpresa);
            callableStatement.setString(2, tipoComprobante);
            callableStatement.registerOutParameter(3, java.sql.Types.VARCHAR);

            callableStatement.executeUpdate();

            claveAccesoLote = callableStatement.getString(3);

        } catch (SQLException e) {
            log.info(e.getMessage());
        } finally {
            if (callableStatement != null) {
                callableStatement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }
        }

    }

    private void processResponse(Object item, ValidarComprobanteResponse validarComprobanteResponse) {
        String claveAcceso = null;
        String secuencial = null;
        String cabeceraSQL;
        String nombreTablaComprobante = null;
        String nombreSecuencial = null;
        if (((Map) item).get("comprobante") instanceof Factura) {
            Factura factura = (Factura) ((Map) item).get("comprobante");
            secuencial = factura.getInfoTributaria().getEstab() + "-" + factura.getInfoTributaria().getPtoEmi() + "-" + factura.getInfoTributaria().getSecuencial();
            claveAcceso = factura.getInfoTributaria().getClaveAcceso();
            nombreTablaComprobante = "FAC_FACTURA_C";
            nombreSecuencial = "SECUENCIAL";
        } else if (((Map) item).get("comprobante") instanceof NotaCredito) {
            NotaCredito notaCredito = (NotaCredito) ((Map) item).get("comprobante");
            secuencial = notaCredito.getInfoTributaria().getEstab() + "-" + notaCredito.getInfoTributaria().getPtoEmi() + "-" + notaCredito.getInfoTributaria().getSecuencial();
            claveAcceso = notaCredito.getInfoTributaria().getClaveAcceso();
            nombreTablaComprobante = "FAC_DEVOLUCION_C";
            nombreSecuencial = "NUM_SECUENCIAL";
        } else if (((Map) item).get("comprobante") instanceof NotaDebito) {
            NotaDebito notaDebito = (NotaDebito) ((Map) item).get("comprobante");
            secuencial = notaDebito.getInfoTributaria().getEstab() + "-" + notaDebito.getInfoTributaria().getPtoEmi() + "-" + notaDebito.getInfoTributaria().getSecuencial();
            claveAcceso = notaDebito.getInfoTributaria().getClaveAcceso();
            nombreTablaComprobante = "";
            nombreSecuencial = "";
        } else if (((Map) item).get("comprobante") instanceof GuiaRemision) {
            GuiaRemision guiaRemision = (GuiaRemision) ((Map) item).get("comprobante");
            secuencial = guiaRemision.getInfoTributaria().getEstab() + "-" + guiaRemision.getInfoTributaria().getPtoEmi() + "-" + guiaRemision.getInfoTributaria().getSecuencial();
            claveAcceso = guiaRemision.getInfoTributaria().getClaveAcceso();
            nombreTablaComprobante = "PED_DESPACHO_C";
            nombreSecuencial = "NUM_SECUENCIAL";
        } else if (((Map) item).get("comprobante") instanceof ComprobanteRetencion) {
            ComprobanteRetencion comprobanteRetencion = (ComprobanteRetencion) ((Map) item).get("comprobante");
            secuencial = comprobanteRetencion.getInfoTributaria().getEstab() + "-" + comprobanteRetencion.getInfoTributaria().getPtoEmi() + "-" + comprobanteRetencion.getInfoTributaria().getSecuencial();
            claveAcceso = comprobanteRetencion.getInfoTributaria().getClaveAcceso();
            nombreTablaComprobante = "BAN_RETENCION_C";
            nombreSecuencial = "NUM_SECUENCIAL";
        } else {
            throw new RuntimeException("El comprobante no es de alguna clase conocida.");
        }
        boolean existsError = false;
        for (recepcion.ws.sri.gob.ec.Comprobante c : validarComprobanteResponse.getRespuestaRecepcionComprobante().getComprobantes().getComprobante()) {
            if (claveAcceso.equals(c.getClaveAcceso())) {
                try {
                    existsError = true;

                    c.getMensajes().getMensaje().stream().map((mensaje) -> {
                        log.log(Level.INFO, "Identificador -> {0}", mensaje.getIdentificador());
                        return mensaje;
                    }).map((mensaje) -> {
                        log.log(Level.INFO, "Tipo -> {0}", mensaje.getTipo());
                        return mensaje;
                    }).map((mensaje) -> {
                        log.log(Level.INFO, "Mensaje -> {0}", mensaje.getMensaje());
                        return mensaje;
                    }).forEachOrdered((mensaje) -> {
                        log.log(Level.INFO, "InformacionAdicional -> {0}", mensaje.getInformacionAdicional());
                    });
                    log.info("-------------------------------------------------------------");

                    String estado = "DEVUELTA";
                    String claveAccesoRecibida = c.getClaveAcceso();
                    String identificador = c.getMensajes().getMensaje().get(0).getIdentificador();
                    String informacionAdicional = c.getMensajes().getMensaje().get(0).getInformacionAdicional();
                    String mensaje = c.getMensajes().getMensaje().get(0).getMensaje();
                    String tipo = c.getMensajes().getMensaje().get(0).getTipo();

                    System.out.print("Secuencial: " + secuencial
                            + ", Estado: " + estado
                            + ", ClaveAcceso: " + claveAccesoRecibida
                            + ", Identificador: " + identificador
                            + ", InformacionAdicional: " + informacionAdicional
                            + ", Mensaje: " + mensaje
                            + ", Tipo: " + tipo
                    );

                    String motivo = ", MOTIVO_SRI = '" + identificador + ":" + tipo + ":" + mensaje + "'";

                    cabeceraSQL = "UPDATE " + nombreTablaComprobante + " SET "
                            + "ESTADO_SRI = ?, "
                            + "CLAVE_ACCESO_LOTE = ?"
                            + motivo
                            + " WHERE " + nombreSecuencial + " = ?";
                    log.log(Level.INFO, "update {0} -> {1}", new Object[]{nombreTablaComprobante, cabeceraSQL});
                    try (PreparedStatement preparedStatement = getConnection().prepareStatement(cabeceraSQL)) {
                        preparedStatement.setString(1, estado);
                        preparedStatement.setString(2, claveAccesoLote);
                        preparedStatement.setString(3, secuencial);
                        preparedStatement.executeUpdate();
                        preparedStatement.close();
                    }
                } catch (SQLException | NamingException ex) {
                    Logger.getLogger(F1_C1_Writer1.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        if (existsError == false) {
            cabeceraSQL = "UPDATE " + nombreTablaComprobante + " SET "
                    + "ESTADO_SRI = 'RECIBIDA', "
                    + "CLAVE_ACCESO_LOTE = ? "
                    + "WHERE " + nombreSecuencial + " = ?";
            log.log(Level.INFO, "update -> {0}", cabeceraSQL);
            executeSql(cabeceraSQL, claveAccesoLote , secuencial);
        }
    }
}
