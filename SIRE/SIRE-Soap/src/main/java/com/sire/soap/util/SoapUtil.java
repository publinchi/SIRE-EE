/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sire.soap.util;

import com.sire.exception.SireRuntimeException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.bind.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.soap.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

/**
 *
 * @author pestupinan
 */
public class SoapUtil {

    private SoapUtil() {}

    private static final Map<Class<?>, JAXBContext> contextStore = new ConcurrentHashMap<>();
    private static final Logger LOGGER = Logger.getLogger(SoapUtil.class.getName());

    /**
     * Invoca servicio soap, envía un mensaje tipo SOAPMessage y recibe un mapa de objetos como respuesta
     * @param soapMsg Mensaje Soap
     * @param url URL servicio externo
     * @param aClass Tipo de clase para objeto de respuesta
     * @return Mapa con objetos de respuesta, ejemplo claves: soapMessage, object
     */
    public static Map<Object, Object> call(SOAPMessage soapMsg, URL url, Class<?> aClass)
            throws SOAPException, TransformerException {
        SOAPConnection soapConnection = null;
        String cookie;
        try {
            // Create SOAP Connection
            SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
            soapConnection = soapConnectionFactory.createConnection();

            // Send SOAP Message to SOAP Server
            SOAPMessage soapMessage;
            try {
                soapMessage = soapConnection.call(soapMsg, url);
            } catch(Exception ex) {
                LOGGER.log(Level.SEVERE, null, ex);
                return new HashMap<>();
            }

            MimeHeaders session = soapMessage.getMimeHeaders();
            String[] cookies = session.getHeader("Set-Cookie");

            Map<Object, Object> map = new HashMap<>();

            if (cookies != null && cookies.length == 1) {
                cookie = cookies[0];
                map.put("cookie", cookie);
            }

            map.put("soapMessage", clone(soapMessage));
            if (aClass != null) {
                Object object = SoapUtil.getObjectFromSoapMessage(soapMessage, aClass);
                if (Objects.isNull(object))
                    object = SoapUtil.getObjectFromNode(soapMessage.getSOAPBody().getFirstChild(), aClass);
                if (object != null)
                    map.put("object", object);
            }

            return map;
        } finally {
            if (soapConnection != null) {
                try {
                    soapConnection.close();
                } catch (SOAPException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    protected static Object getObjectFromSoapMessage(SOAPMessage soapResponse, Class<?> aClass) {
        try {
            JAXBContext jaxbContext = getContextInstance(aClass);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            Document document = extractContentAsDocument(soapResponse);

            JAXBElement<?> root = unmarshaller.unmarshal(document, aClass);

            Object object = root.getValue();

            if (object != null) {
                return object;
            }

            if (soapResponse.getSOAPBody().hasFault()) {
                object = soapResponse.getSOAPBody().getFault();
            } else {
                object = unmarshaller.unmarshal(document);
            }
            return object;
        } catch (SOAPException | JAXBException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public static JAXBContext getContextInstance(Class<?> objectClass) throws JAXBException {
        JAXBContext context = contextStore.get(objectClass);
        if (context==null){
            try {
                context = JAXBContext.newInstance(objectClass);
                contextStore.put(objectClass, context);
            } catch (JAXBException e) {
                throw new JAXBException(e);
            }
        }
        return context;
    }

    public static String getStringFromSoapMessage(SOAPMessage soapMessage) {
        try {
            Source sourceContent = soapMessage.getSOAPPart().getContent();
            StreamResult sr = new StreamResult();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            sr.setOutputStream(out);

            Transformer transformer = createTransformer();

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.setOutputProperty("standalone", "no");
            transformer.transform(sourceContent, sr);

            return out.toString();
        } catch (TransformerException | SOAPException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public static String getStringFromObject(Object object) {
        try {
            StringWriter stringWriter = new StringWriter();
            JAXBContext jaxbContext = getContextInstance(object.getClass());
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            // format the XML output
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

            XmlType xmlType = object.getClass().getDeclaredAnnotation(XmlType.class);

            XmlRootElement xmlRootElement = object.getClass().getDeclaredAnnotation(XmlRootElement.class);

            QName qName = new QName(object.getClass().getPackage().getName(),
                    (Objects.nonNull(xmlRootElement) && !Objects.equals(xmlRootElement.name(), "##default"))
                            ? xmlRootElement.name() : xmlType.name());

            JAXBElement<?> root = new JAXBElement<>(qName, (Class<? super Object>) object.getClass(), object);

            jaxbMarshaller.marshal(root, stringWriter);

            return stringWriter.toString();
        } catch (JAXBException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return null;
        }
    }

    public static Object getObjectFromString(String xml, Class<?> aClass) {
        try {
            JAXBContext jaxbContext = getContextInstance(aClass);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            //Do unmarshall operation
            Source xmlSource = new SAXSource(createSAXParserFactory().newSAXParser().getXMLReader()
                    , new InputSource(new StringReader(xml)));

            JAXBElement<?> root = jaxbUnmarshaller.unmarshal(xmlSource, aClass);
            return root.getValue();

        } catch (JAXBException | ParserConfigurationException | SAXException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return null;
        }
    }

    public static SOAPMessage getSoapMessageFromString(String xml) {
        try {
            MessageFactory factory = MessageFactory.newInstance();
            return factory.createMessage(new MimeHeaders(),
                    new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        } catch (IOException | SOAPException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public static String object2xml(Object item) throws JAXBException {
        JAXBContext jaxbContext = getContextInstance(item.getClass());
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        StringWriter sw = new StringWriter();
        jaxbMarshaller.marshal(item, sw);

        return sw.toString();
    }

    /**
     * Convierte el mensaje xml (tipo InputStream) a Objeto de negocio
     * @param inputStream InputStream
     * @param aClass Class
     * @return Object
     */
    public static Object getObjectFromInputStream(InputStream inputStream, Class<?> aClass) {
        try {
            JAXBContext jaxbContext = getContextInstance(aClass);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            //Do unmarshall operation
            Source xmlSource = new SAXSource(createSAXParserFactory().newSAXParser().getXMLReader()
                    , new InputSource(inputStream));

            JAXBElement<?> root = jaxbUnmarshaller.unmarshal(xmlSource, aClass);
            return root.getValue();
        } catch (JAXBException | ParserConfigurationException | SAXException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Convierte el contenido del SOAPMessage (tipo Node) a Objeto de negocio
     * @param node Node
     * @param aClass Class
     * @return Object
     */
    public static Object getObjectFromNode(Node node, Class<?> aClass) {
        try {
            JAXBContext jaxbContext = getContextInstance(aClass);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            JAXBElement<?> root = jaxbUnmarshaller.unmarshal(cloneNode(node), aClass);

            return root.getValue();
        } catch (JAXBException | IOException | ParserConfigurationException | TransformerException | SAXException e) {
            LOGGER.log(Level.SEVERE, null, e);
            return null;
        }
    }

    /**
     * Convierte el contenido xml de un elemento y lo transforma a objeto de negocio
     * @param soapMessage SOAPMessage
     * @param elementName String
     * @param aClass Class
     * @return Object
     */
    public static Object getObjectFromElement(SOAPMessage soapMessage, String elementName, Class<?> aClass) {
        try {
            if (soapMessage.getSOAPBody().hasFault()){
                LOGGER.log(Level.INFO, soapMessage.getSOAPBody().getFault()
                        .getFaultString());
                return soapMessage.getSOAPBody().getFault();
            } else {
                String xml = soapMessage.getSOAPBody().extractContentAsDocument().getElementsByTagName(elementName)
                        .item(0).getTextContent();
                return SoapUtil.getObjectFromString(xml, aClass);
            }
        } catch (SOAPException e) {
            LOGGER.log(Level.SEVERE, null, e);
            return null;
        }
    }

    @SuppressWarnings("unused")
    public static List<Object> getDataSetFromSoapMessage(SOAPMessage soapResponse, Class<?> aClass) {
        try {
            XmlType annotation = aClass.getDeclaredAnnotation(XmlType.class);
            String type = annotation.name();
            if (Objects.isNull(type) || Objects.equals("", type))
                type = aClass.getSimpleName();

            Document document = extractContentAsDocument(soapResponse);
            org.w3c.dom.Element root = document.getDocumentElement();
            NodeList dataSets = root.getElementsByTagName(type);

            JAXBContext jaxbContext = getContextInstance(aClass);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            List<Object> list = new ArrayList<>();

            for (int i = 0; i < dataSets.getLength() ; i++) {
                Node node = dataSets.item(i);

                JAXBElement<?> r = jaxbUnmarshaller.unmarshal(cloneNode(node), aClass);

                list.add(r.getValue());
            }
            return list;
        } catch (SOAPException | JAXBException | ParserConfigurationException | TransformerException | SAXException
                | IOException e) {
            LOGGER.log(Level.SEVERE, null, e);
            return new ArrayList<>();
        }
    }

    private static SOAPMessage clone(SOAPMessage message) throws TransformerException, SOAPException {
        return toSOAPMessage(toDocument(message));
    }

    private static SOAPMessage toSOAPMessage(Document doc) {
        try {
            DOMSource domSource = new DOMSource(doc);
            MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.DEFAULT_SOAP_PROTOCOL);
            SOAPMessage retorno = messageFactory.createMessage();
            retorno.getSOAPPart().setContent(domSource);
            return retorno;
        } catch (SOAPException e) {
            throw new SireRuntimeException(e.getMessage(), e);
        }
    }

    private static Document toDocument(SOAPMessage soapMSG) throws TransformerException, SOAPException {
        Source source = soapMSG.getSOAPPart().getContent();

        DOMResult retorno = new DOMResult();
        createTransformer().transform(source, retorno);
        return (Document) retorno.getNode();
    }

    private static Document extractContentAsDocument(SOAPMessage soapResponse) throws SOAPException {

        Iterator<?> iterator = soapResponse.getSOAPBody().getChildElements();
        javax.xml.soap.Node firstBodyElement = null;

        while (iterator.hasNext() && !(firstBodyElement instanceof SOAPElement))
            firstBodyElement = (javax.xml.soap.Node) iterator.next();

        boolean exactlyOneChildElement = true;
        if (firstBodyElement == null)
            exactlyOneChildElement = false;
        else {
            for (Node node = firstBodyElement.getNextSibling(); node != null; node = node.getNextSibling()) {
                if (node instanceof org.w3c.dom.Element) {
                    exactlyOneChildElement = false;
                    break;
                }
            }
        }

        if (!exactlyOneChildElement) {
            LOGGER.log(Level.SEVERE,"SAAJ0250.impl.body.should.have.exactly.one.child");
            throw new SOAPException("Cannot extract Document from body");
        }

        Document document;
        try {
            DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.newDocument();

            org.w3c.dom.Element rootElement = (org.w3c.dom.Element) document.importNode(firstBodyElement,true);

            document.appendChild(rootElement);

        } catch(Exception e) {
            LOGGER.log(Level.SEVERE,"SAAJ0251.impl.cannot.extract.document.from.body");
            throw new SOAPException("Unable to extract Document from body", e);
        }

        firstBodyElement.detachNode();

        return document;
    }

    private static Node cloneNode(Node node) throws TransformerException, ParserConfigurationException, IOException
            , SAXException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Source xmlSource = new DOMSource(node);
        Result outputTarget = new StreamResult(outputStream);

        createTransformer().transform(xmlSource, outputTarget);

        DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        factory.setNamespaceAware(true);

        InputStream is = new ByteArrayInputStream(outputStream.toByteArray());

        return factory.newDocumentBuilder().parse(is);
    }

    private static Transformer createTransformer() throws TransformerConfigurationException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

        return transformerFactory.newTransformer();
    }

    private static SAXParserFactory createSAXParserFactory() throws SAXNotSupportedException, SAXNotRecognizedException
            , ParserConfigurationException {
        //Disable XXE
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        saxParserFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        saxParserFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        saxParserFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",false);
        return saxParserFactory;
    }
}