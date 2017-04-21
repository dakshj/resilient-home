package com.resilienthome.model.config;

import com.resilienthome.model.Address;

import java.io.Serializable;

public class GatewayConfig extends ServerConfig implements Serializable {

    private Address dbAddress;

    private int cacheSize;

    public Address getDbAddress() {
        return dbAddress;
    }

    public int getCacheSize() {
        return cacheSize;
    }
}
