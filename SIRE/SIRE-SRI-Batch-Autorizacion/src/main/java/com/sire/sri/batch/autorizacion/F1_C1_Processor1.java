package com.sire.sri.batch.autorizacion;

import ec.gob.sri.ws.autorizacion.*;
import com.sire.soap.util.SoapUtil;
import com.sun.xml.messaging.saaj.soap.impl.ElementImpl;
import ec.gob.sri.comprobantes.modelo.LoteXml;
import java.io.StringReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.batch.api.chunk.ItemProcessor;
import javax.batch.runtime.BatchRuntime;
import javax.batch.runtime.context.JobContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import com.sire.logger.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@Named
public class F1_C1_Processor1 implements ItemProcessor {

    @Inject
    private JobContext jobCtx;
    private static final Logger log = LogManager.getLogger(F1_C1_Processor1.class);

    @Override
    public Object processItem(Object item) throws Exception {
        Properties runtimeParams = BatchRuntime.getJobOperator().getParameters(jobCtx.getExecutionId());
        String urlAutorizacion = runtimeParams.getProperty("urlAutorizacion");
        String claveAcceso = ((LoteXml) item).getClaveAcceso();

        Map mapCall = SoapUtil.call(
                createSOAPMessage(claveAcceso),
                new URL(urlAutorizacion),
                null);

        if(Objects.isNull(mapCall)) {
            return null;
        }

        SOAPMessage soapMessage = (SOAPMessage) mapCall.get("soapMessage");

        if(log.isTraceEnabled()) {
            log.trace("Soap Recepcion Response:");
            log.trace(SoapUtil.getStringFromSoapMessage(soapMessage));
        }

        RespuestaComprobante respuestaComprobante = toRespuestaComprobante(soapMessage);
        Map<String, Object> map = new HashMap();
        map.put("respuestaComprobante", respuestaComprobante);
        map.put("lote", item);
        return map;
    }

    private SOAPMessage createSOAPMessage(String claveAcceso) {
        try {
            MessageFactory factory = MessageFactory.newInstance();
            SOAPMessage soapMsg = factory.createMessage();
            SOAPPart part = soapMsg.getSOAPPart();

            SOAPEnvelope envelope = part.getEnvelope();
            envelope.addNamespaceDeclaration("ec", "http://ec.gob.sri.ws.autorizacion");
            SOAPBody body = envelope.getBody();
            MimeHeaders headers = soapMsg.getMimeHeaders();
            headers.addHeader("SOAPAction", "");
            SOAPBodyElement element = body.addBodyElement(
                    envelope.createName("ec:autorizacionComprobanteLote"));
            element.addChildElement("claveAccesoLote").addTextNode(claveAcceso);

            soapMsg.saveChanges();
            return soapMsg;
        } catch (SOAPException ex) {
            System.err.println(ex);
        }
        return null;
    }

    private RespuestaComprobante toRespuestaComprobante(SOAPMessage soapMessage) throws SOAPException, XPathExpressionException, ParseException, DatatypeConfigurationException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        Node claveAccesoLoteConsultadaNode = (Node) xpath.evaluate("//claveAccesoLoteConsultada", soapMessage.getSOAPBody(), XPathConstants.NODE);
        Node numeroComprobantesLoteNode = (Node) xpath.evaluate("//numeroComprobantesLote", soapMessage.getSOAPBody(), XPathConstants.NODE);

        RespuestaComprobante respuestaComprobante = new RespuestaComprobante();
        RespuestaComprobante.Autorizaciones autorizaciones = new RespuestaComprobante.Autorizaciones();
        NodeList autorizacionesNodeList = (NodeList) xpath.evaluate("//autorizaciones/autorizacion", soapMessage.getSOAPBody(), XPathConstants.NODESET);
        for (int i = 0; i < autorizacionesNodeList.getLength(); i++) {
            Autorizacion autorizacion = new Autorizacion();
            autorizaciones.getAutorizacion().add(autorizacion);
            Node autorizacionNode = (Node) autorizacionesNodeList.item(i);
            NodeList hijosAutorizacion = autorizacionNode.getChildNodes();

            for (int j = 0; j < hijosAutorizacion.getLength(); j++) {
                switch (hijosAutorizacion.item(j).getNodeName()) {
                    case "comprobante":
                        autorizacion.setComprobante(hijosAutorizacion.item(j).getTextContent());
                        break;
                    case "fechaAutorizacion":
                        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
                        Date date = format.parse(hijosAutorizacion.item(j).getTextContent());
                        GregorianCalendar cal = new GregorianCalendar();
                        cal.setTime(date);
                        XMLGregorianCalendar fechaAutorizacion = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
                        autorizacion.setFechaAutorizacion(fechaAutorizacion);
                        break;
                    case "estado":
                        autorizacion.setEstado(hijosAutorizacion.item(j).getTextContent());
                        break;
                    case "numeroAutorizacion":
                        autorizacion.setNumeroAutorizacion(hijosAutorizacion.item(j).getTextContent());
                        break;
                    case "ambiente":
                        autorizacion.setAmbiente(hijosAutorizacion.item(j).getTextContent());
                        break;
                    case "mensajes":
                        NodeList mensajesNodeList;
                        if(hijosAutorizacion.item(j) instanceof ElementImpl) {
                            ElementImpl elementImpl = (ElementImpl) hijosAutorizacion.item(j);
                            mensajesNodeList = elementImpl.getChildNodes();
                        }else {
                            mensajesNodeList = (NodeList) hijosAutorizacion.item(j);
                        }
                        Autorizacion.Mensajes mensajes = new Autorizacion.Mensajes();
                        autorizacion.setMensajes(mensajes);
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
                        break;
                    default:
                        break;
                }
            }
            if(autorizacion.getNumeroAutorizacion() == null && autorizacion.getComprobante() != null) {
                InputSource source = new InputSource(new StringReader(autorizacion.getComprobante()));
                autorizacion.setNumeroAutorizacion(xpath.evaluate("//claveAcceso", source));
            }
        }

        respuestaComprobante.setAutorizaciones(autorizaciones);
        if(claveAccesoLoteConsultadaNode != null){
            respuestaComprobante.setClaveAccesoConsultada(claveAccesoLoteConsultadaNode.getValue());
        }
        if(numeroComprobantesLoteNode != null){
            respuestaComprobante.setNumeroComprobantes(numeroComprobantesLoteNode.getValue());
        }
        return respuestaComprobante;
    }
}