package com.resilienthome.exception;

public class LogFileCreationFailedException extends RuntimeException {

    public LogFileCreationFailedException(final String logFilePath) {
        super("Failed to create the log file at " + logFilePath + "!");
    }
}
