package com.sire.soap.util;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class Test {
    public static void main(String args[]) throws MalformedURLException, SOAPException, TransformerException {
        String url = args[0];
        System.out.println("URL: " + url);
        String xml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"http://service.demo.admin.ecobis.cobiscorp\" xmlns:dto=\"http://dto.demo.admin.ecobis.cobiscorp\">\n" +
                "<soapenv:Header/>\n" +
                "<soapenv:Body>\n" +
                "<ser:GetDemo>\n" +
                "<!--Optional:-->\n" +
                "<ser:inDemoDto>\n" +
                "<!--Optional:-->\n" +
                "<dto:trn>?</dto:trn>\n" +
                "<!--Optional:-->\n" +
                "<dto:operation>?</dto:operation>\n" +
                "<!--Optional:-->\n" +
                "<dto:name>?</dto:name>\n" +
                "<!--Optional:-->\n" +
                "<dto:returnVarchar>?</dto:returnVarchar>\n" +
                "<!--Optional:-->\n" +
                "<dto:returnInt>?</dto:returnInt>\n" +
                "</ser:inDemoDto>\n" +
                "</ser:GetDemo>\n" +
                "</soapenv:Body>\n" +
                "</soapenv:Envelope>";
        SOAPMessage soapMessage = SoapUtil.getSoapMessageFromString(xml);
        System.out.println("SOAPMessage Request:");
        System.out.println(SoapUtil.getStringFromSoapMessage(soapMessage));
        Map map = SoapUtil.call(soapMessage, new URL(url),
                null);
        System.out.println("SOAPMessage Response:");
        System.out.println(SoapUtil.getStringFromSoapMessage((SOAPMessage) map.get("soapMessage")));
    }
}