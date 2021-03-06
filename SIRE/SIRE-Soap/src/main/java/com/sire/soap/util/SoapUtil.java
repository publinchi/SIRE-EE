/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sire.soap.util;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import javax.xml.bind.*;
import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 *
 * @author pestupinan
 */
public class SoapUtil {

    static {
        disableSslVerification();
    }

    private static final Map<Class, JAXBContext> contextStore = new ConcurrentHashMap();

    public static Map<Object, Object> call(SOAPMessage soapMsg, URL url,
                                           Class aClass) throws SOAPException, TransformerException {
        return call(soapMsg,url,null, aClass);
    }

    /**
     *
     * @param soapMsg
     * @param url
     * @param returnObjectName
     * @param aClass
     * @return
     */
    public static Map<Object, Object> call(SOAPMessage soapMsg, URL url, String returnObjectName,
                                           Class aClass) throws SOAPException, TransformerException {
        SOAPConnection soapConnection = null;
        String cookie;
        try {
            // Create SOAP Connection
            SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
            soapConnection = soapConnectionFactory.createConnection();

//            printHeaders(soapMsg);
            // Send SOAP Message to SOAP Server
            SOAPMessage soapMessage;
            try {
                soapMessage = soapConnection.call(soapMsg, url);
            } catch(Exception ex) {
                Logger.getLogger(SoapUtil.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }

            MimeHeaders session = soapMessage.getMimeHeaders();
            String[] cookies = session.getHeader("Set-Cookie");

//            printHeaders(soapMessage);
            Map<Object, Object> map = new HashMap();

            if (cookies != null && cookies.length == 1) {
                cookie = cookies[0];
                map.put("cookie", cookie);
            }

//            Logger.getLogger(SoapUtil.class.getName()).log(Level.INFO,
//                    "Response SOAP Message cookie: {0}", cookie);
//
            map.put("soapMessage", clone(soapMessage));
            if (aClass != null) {
                Object object = SoapUtil.getObjectFromSoapMessage(soapMessage, aClass);
                if(object != null)
                    map.put("object", object);
            }

            return map;
        } finally {
            if (soapConnection != null) {
                try {
                    soapConnection.close();
                } catch (SOAPException ex) {
                    Logger.getLogger(SoapUtil.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    protected static Object getObjectFromSoapMessage(SOAPMessage soapResponse, Class aClass) {
        Object object = null;
        try {
            JAXBContext jaxbContext = getContextInstance(aClass);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            Document document = soapResponse.getSOAPBody().extractContentAsDocument();

            JAXBElement root = unmarshaller.unmarshal(document, aClass);

            object = root.getValue();

            if(object != null) {
                return object;
            }

            if(soapResponse.getSOAPBody().hasFault()){
                Logger.getLogger(SoapUtil.class.getName()).log(Level.INFO, soapResponse.getSOAPBody().getFault()
                        .getFaultString());
                object = unmarshaller.unmarshal(soapResponse.getSOAPBody().getFault());
            } else {
                object = unmarshaller.unmarshal(document);
            }
        } catch (SOAPException ex) {
            Logger.getLogger(SoapUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JAXBException ex) {
            Logger.getLogger(SoapUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return object;
    }

    public static JAXBContext getContextInstance(Class objectClass) {
        JAXBContext context = contextStore.get(objectClass);
        if (context==null){
            try {
                context = JAXBContext.newInstance(objectClass);
            } catch (JAXBException e) {
                Logger.getLogger(SoapUtil.class.getName()).log(Level.SEVERE, null, e);
            }
            contextStore.put(objectClass, context);
        }
        return context;
    }

    public static String getStringFromSoapMessage(SOAPMessage soapMessage) {
        try {
            Source sourceContent = soapMessage.getSOAPPart().getContent();
            StreamResult sr = new StreamResult();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            sr.setOutputStream(out);

            Transformer trans = TransformerFactory.newInstance().newTransformer();
            trans.setOutputProperty(OutputKeys.INDENT, "yes");
            trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            trans.transform(sourceContent, sr);

            //Logger.getLogger(SoapUtil.class.getName()).log(Level.INFO, "Transform: {0}", out);

            return out.toString();
        } catch (TransformerException ex) {
            Logger.getLogger(SoapUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SOAPException ex) {
            Logger.getLogger(SoapUtil.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public static String getStringFromObject(Object object) {
        String result = null;
        try {
            StringWriter stringWriter = new StringWriter();
            JAXBContext jaxbContext = getContextInstance(object.getClass());
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            // format the XML output
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
                    true);

            Annotation annotation = object.getClass().getDeclaredAnnotation(javax.xml.bind.annotation.XmlType.class);

            QName qName = new QName(object.getClass().getPackage().getName(),
                    ((javax.xml.bind.annotation.XmlType)annotation).name());
            JAXBElement root = new JAXBElement(qName, object.getClass(), object);

            jaxbMarshaller.marshal(root, stringWriter);

            result = stringWriter.toString();
            //Logger.getLogger(SoapUtil.class.getName()).log(Level.INFO, result);
        } catch (JAXBException e) {
            Logger.getLogger(SoapUtil.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        }
        return result;
    }

    public static Object getObjectFromString(String xml, Class aClass) {
        Object object = null;
        try {
            JAXBContext jaxbContext = getContextInstance(aClass);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            JAXBElement root = jaxbUnmarshaller.unmarshal(new StreamSource(new StringReader(xml)), aClass);
            object = root.getValue();

            //Logger.getLogger(SoapUtil.class.getName()).log(Level.INFO, object.toString());
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return object;
    }

    public static SOAPMessage getSoapMessageFromString(String xml) {
        MessageFactory factory;
        SOAPMessage message = null;
        try {
            factory = MessageFactory.newInstance();
            message = factory.createMessage(new MimeHeaders(), new ByteArrayInputStream(xml.getBytes(Charset
                    .forName("UTF-8"))));
        } catch (IOException ex) {
            Logger.getLogger(SoapUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SOAPException ex) {
            Logger.getLogger(SoapUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return message;
    }

    public static String object2xml(Object item) throws JAXBException {
        JAXBContext jaxbContext = getContextInstance(item.getClass());
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        StringWriter sw = new StringWriter();
        jaxbMarshaller.marshal(item, sw);

        return sw.toString();
    }

    public static Object getObjectFromInputStream(InputStream inputStream, Class aClass) {
        Object object = null;
        try {
            JAXBContext jaxbContext = getContextInstance(aClass);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            JAXBElement root = jaxbUnmarshaller.unmarshal(new StreamSource(inputStream), aClass);
            object = root.getValue();

            //Logger.getLogger(SoapUtil.class.getName()).log(Level.INFO, object.toString());
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return object;
    }

    public static Object getObjectFromNode(Node node, Class aClass) {
        Object object = null;
        try {
            JAXBContext jaxbContext = getContextInstance(aClass);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            JAXBElement root = jaxbUnmarshaller.unmarshal(node, aClass);
            object = root.getValue();
        } catch (JAXBException e) {
            Logger.getLogger(SoapUtil.class.getName()).log(Level.SEVERE, null, e);
        }
        return object;
    }

    public static Object getObjectFromElement(SOAPMessage soapMessage, String elementName, Class aClass) {
        Object object = null;
        try {
            if(soapMessage.getSOAPBody().hasFault()){
                Logger.getLogger(SoapUtil.class.getName()).log(Level.INFO, soapMessage.getSOAPBody().getFault()
                        .getFaultString());
                object = soapMessage.getSOAPBody().getFault();
            } else {
                String xml = soapMessage.getSOAPBody().extractContentAsDocument().getElementsByTagName(elementName)
                        .item(0).getTextContent();
                object = SoapUtil.getObjectFromString(xml, aClass);
            }

        } catch (SOAPException e) {
            Logger.getLogger(SoapUtil.class.getName()).log(Level.SEVERE, null, e);
        }
        return object;
    }

    public static List getDataSetFromSoapMessage(SOAPMessage soapResponse, Class aClass) {
        return getDataSetFromSoapMessage(soapResponse, aClass, false);
    }

    public static List getDataSetFromSoapMessage(SOAPMessage soapResponse, Class aClass, boolean reflection) {
        try {
            Annotation annotation = aClass.getDeclaredAnnotation(javax.xml.bind.annotation.XmlType.class);
            String type = ((javax.xml.bind.annotation.XmlType)annotation).name();
            if(Objects.isNull(type) || Objects.equals("", type))
                type = aClass.getSimpleName();

            Document document = soapResponse.getSOAPBody().extractContentAsDocument();
            org.w3c.dom.Element root = document.getDocumentElement();
            NodeList dataSets = root.getElementsByTagName(type);

            JAXBContext jaxbContext = getContextInstance(aClass);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            List list = new ArrayList<>();

            for (int i = 0; i < dataSets.getLength() ; i++) {
                org.w3c.dom.Node node = dataSets.item(i);

                Object object;

                if(reflection){
                    NodeList children = node.getChildNodes();
                    object = aClass.newInstance();

                    for (int j = 0; j < children.getLength(); j++) {
                        org.w3c.dom.Node childNode = children.item(j);
                        String nodeName = childNode.getNodeName();
                        Field field = aClass.getDeclaredField(nodeName.substring(0, 1).toLowerCase()
                                + nodeName.substring(1));
                        field.setAccessible(true);
                        field.set(object, childNode.getTextContent());
                        field.setAccessible(false);
                    }
                } else {
                    JAXBElement r = jaxbUnmarshaller.unmarshal(node, aClass);
                    object = r.getValue();
                }

                list.add(object);
            }
            return list;
        } catch (SOAPException | JAXBException | IllegalAccessException | InstantiationException | NoSuchFieldException
                ex) {
            Logger.getLogger(SoapUtil.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private static void disableSslVerification() {
        try {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        @Override
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            // TODO Cambiar a expresion lambda
            // HostnameVerifier allHostsValid = (String hostname, SSLSession session) -> true;
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                @Override public boolean verify(String s, SSLSession session) {
                    return true;
                }
            };

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(SoapUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (KeyManagementException ex) {
            Logger.getLogger(SoapUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void printHeaders(SOAPMessage soapMessage) {
        MimeHeaders mimeHeaders = soapMessage.getMimeHeaders();
        Iterator<MimeHeader> it = mimeHeaders.getAllHeaders();
        while (it.hasNext()) {
            MimeHeader mime = it.next();
            Logger.getLogger(SoapUtil.class.getName()).log(Level.INFO,
                    "Name: {0} Value: {1}", new Object[]{mime.getName(), mime.getValue()});
        }
    }

    private static SOAPMessage clone(SOAPMessage message) throws TransformerException, SOAPException {
        return toSOAPMessage(toDocument(message));
    }

    private static SOAPMessage toSOAPMessage(Document doc) {
        return toSOAPMessage(doc, SOAPConstants.DEFAULT_SOAP_PROTOCOL);
    }

    private static SOAPMessage toSOAPMessage(Document doc, String protocol) {
        DOMSource domSource;
        SOAPMessage retorno;
        MessageFactory messageFactory;
        try {
            domSource = new DOMSource(doc);
            messageFactory = MessageFactory.newInstance(protocol);
            retorno = messageFactory.createMessage();
            retorno.getSOAPPart().setContent(domSource);
            return retorno;
        } catch (SOAPException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static Document toDocument(SOAPMessage soapMSG) throws TransformerException, SOAPException {
        Source source = soapMSG.getSOAPPart().getContent();
        TransformerFactory factoryTransform = TransformerFactory.newInstance();
        Transformer transform = factoryTransform.newTransformer();
        DOMResult retorno = new DOMResult();
        transform.transform(source, retorno);
        return (Document) retorno.getNode();
    }
}