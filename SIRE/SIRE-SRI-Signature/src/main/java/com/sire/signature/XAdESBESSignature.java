/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sire.signature;

import java.io.File;
import org.w3c.dom.Document;

/**
 *
 * @author pestupinan
 */
public class XAdESBESSignature extends GenericXMLSignature {

    private static String nameFile;
    private static String pathFile;
    private String xmlPath;
    private Document document;
    private File fileToSign;

    public XAdESBESSignature(File fileToSign) {
        this.fileToSign = fileToSign;
    }

    public XAdESBESSignature(String stringToSign) {
        this.xmlPath = stringToSign;
    }

    public XAdESBESSignature(Document document) {
        this.document = document;
    }

    public static void firmar(String xmlPath, String pathSignature, String passSignature, String pathOut, String nameFileOut) {
        XAdESBESSignature signature = new XAdESBESSignature(xmlPath);
        signature.setPassSignature(passSignature);
        signature.setPathSignature(pathSignature);
        pathFile = pathOut;
        nameFile = nameFileOut;

        signature.execute();
    }

    public static GenericXMLSignature firmar(Document document, String pathSignature, String passSignature) {
        XAdESBESSignature signature = new XAdESBESSignature(document);
        signature.setPassSignature(passSignature);
        signature.setPathSignature(pathSignature);

        return signature.execute();
    }

    protected Document createDataToSign() {
        Document docToSign = null;
        if (this.document != null) {
            docToSign = this.document;
        } else if (this.xmlPath != null) {
            docToSign = getDocument(this.xmlPath);
        } else if (this.fileToSign != null) {
            docToSign = getDocument(this.fileToSign);
        }
        return docToSign;
    }

    protected String getSignatureFileName() {
        return nameFile;
    }

    protected String getPathOut() {
        return pathFile;
    }
}
