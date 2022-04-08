package com.sire.exception;

import javax.xml.soap.SOAPException;

public class SireRuntimeException extends RuntimeException {
    public SireRuntimeException(String message, SOAPException e) {
        super(message, e);
    }
}