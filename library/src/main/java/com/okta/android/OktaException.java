package com.okta.android;

public class OktaException extends Exception {

    public OktaException() {
        super();
    }

    public OktaException(String message) {
        super(message);
    }

    public OktaException(String message, Throwable ex) {
        super(message, ex);
    }
}
