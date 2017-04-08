package com.resilienthome.exception;

public class NoRegisteredGatewayException extends RuntimeException {

    public NoRegisteredGatewayException() {
        super("There is no registered Gateway in this Resilient Home!");
    }
}
