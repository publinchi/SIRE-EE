/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sire.signature;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Enumeration;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import xades4j.algorithms.EnvelopedSignatureTransform;
import xades4j.production.DataObjectReference;
import xades4j.production.SignedDataObjects;
import xades4j.production.XadesBesSigningProfile;
import xades4j.production.XadesSigner;
import xades4j.properties.DataObjectDesc;
import xades4j.properties.DataObjectFormatProperty;
import xades4j.providers.KeyingDataProvider;
import xades4j.providers.impl.DirectKeyingDataProvider;
import xades4j.providers.impl.FileSystemKeyStoreKeyingDataProvider;

/**
 *
 * @author pestupinan
 */
public abstract class GenericXMLSignature {

    private String pathSignature;
    private String passSignature;
    private Document docSigned;

    public String getPathSignature() {
        return this.pathSignature;
    }

    public void setPathSignature(String pathSignature) {
        this.pathSignature = pathSignature;
    }

    public String getPassSignature() {
        return this.passSignature;
    }

    public void setPassSignature(String passSignature) {
        this.passSignature = passSignature;
    }

    protected GenericXMLSignature execute() {
        KeyStore keyStore = getKeyStore();
        if (keyStore == null) {
            System.err.println("No se pudo obtener almacen de firma.");
            return null;
        }
        String alias = getAlias(keyStore);

        X509Certificate certificate;
        try {
            certificate = (X509Certificate) keyStore.getCertificate(alias);
            if (certificate == null) {
                System.err.println("No existe ningún certificado para firmar.");
                return null;
            } else {
                return sign(keyStore, alias, certificate);
            }
        } catch (KeyStoreException e) {
            System.err.println("Error al obtener certificado con alias " + alias);
            return null;
        }
    }

    protected abstract Document createDataToSign();

    protected abstract String getSignatureFileName();

    protected abstract String getPathOut();

    protected Document getDocument(String resource) {
        File file = new File(resource);
        return getDoc(file);
    }

    protected Document getDocument(File file) {
        return getDoc(file);
    }

    private Document getDoc(File file) {
        Document doc = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(file);
        } catch (ParserConfigurationException | SAXException | IOException | IllegalArgumentException ex) {
            System.err.println("Error al parsear el documento");
            System.exit(-1);
        }
        return doc;
    }

    private KeyStore getKeyStore() {
        KeyStore ks = null;
        try {
            ks = KeyStore.getInstance("PKCS12");
            ks.load(new FileInputStream(this.pathSignature),
                    this.passSignature.toCharArray());
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            System.err.println("Error al obtener keystore desde la ruta " + this.pathSignature);
        }

        return ks;
    }

    private static String getAlias(KeyStore keyStore) {
        String alias = null;
        try {
            Enumeration<String> nombres = keyStore.aliases();
            while (nombres.hasMoreElements()) {
                String tmpAlias = (String) nombres.nextElement();
                if (keyStore.isKeyEntry(tmpAlias)) {
                    alias = tmpAlias;
                }
            }
        } catch (KeyStoreException e) {
            System.err.println("Error al obtener alias.");
        }
        return alias;
    }

    public static void saveDocumenteDisk(Document document, String pathXml) {
        try {
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File(pathXml));

            TransformerFactory transformerFactory
                    = TransformerFactory.newInstance();

            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(source, result);
        } catch (TransformerException e) {
            System.err.println("Error al guardar documento en disco.");
        }
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

    private static String doc2String(Document document) {
        try {
            Source source = new DOMSource(document);
            StringWriter stringWriter = new StringWriter();
            Result result = new StreamResult(stringWriter);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.transform(source, result);
            return stringWriter.getBuffer().toString();
        } catch (TransformerException e) {
            System.err.println("Error al convertir documento a astring.");
        }
        return null;
    }

    private GenericXMLSignature sign(KeyStore keyStore, String alias, X509Certificate certificate) {
        PrivateKey privateKey = null;
        KeyStore tmpKs = keyStore;
        try {
            privateKey = (PrivateKey) tmpKs.getKey(alias,
                    this.passSignature.toCharArray());
        } catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e) {
            System.err.println("No existe clave privada para firmar.");
            System.err.println(e);
        }

        Document dataToSign = createDataToSign();

        try {
            KeyingDataProvider kp = new DirectKeyingDataProvider(certificate, privateKey);

            DataObjectDesc obj = new DataObjectReference("")
                    .withTransform(new EnvelopedSignatureTransform())
                    .withDataObjectFormat(new DataObjectFormatProperty("text/xml"));
            XadesSigner signer = new XadesBesSigningProfile(kp).newSigner();
            signer.sign(new SignedDataObjects(obj), dataToSign);
            docSigned = dataToSign;
        } catch (Exception ex) {
            System.err.println("Error realizando la firma: " + ex);
            return null;
        }

        if (getPathOut() != null && getSignatureFileName() != null) {
            String filePath = getPathOut() + File.separatorChar
                    + getSignatureFileName();
            System.out.println("Firma salvada en: " + filePath);
            saveDocumenteDisk(docSigned, filePath);
        }

        return this;
    }

    public String toBase64() {
        byte[] result = doc2bytes(docSigned);
        String base64 = new String(Base64.getEncoder().encode(result));
//        System.out.println("base64 ->" + base64);
        return base64;
    }

    public String toSignedXml() {
        String signedXml = doc2String(docSigned);
//        System.out.println("signedXml -> " + signedXml);
        return signedXml;
    }
}
