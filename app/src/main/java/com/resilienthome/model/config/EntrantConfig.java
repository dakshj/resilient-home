package com.resilienthome.model.config;

import java.io.Serializable;

public class EntrantConfig extends Config implements Serializable {

    private boolean authorized;

    public boolean isAuthorized() {
        return authorized;
    }
}
