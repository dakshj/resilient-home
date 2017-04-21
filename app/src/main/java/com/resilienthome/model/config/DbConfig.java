package com.resilienthome.model.config;

import java.io.Serializable;

public class DbConfig extends ServerConfig implements Serializable {

    private String logFileUniqueIdentifier;

    public String getLogFileUniqueIdentifier() {
        return logFileUniqueIdentifier;
    }
}
