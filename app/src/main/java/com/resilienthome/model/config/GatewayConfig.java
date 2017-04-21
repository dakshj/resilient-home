package com.resilienthome.model.config;

import com.resilienthome.model.Address;

import java.io.Serializable;

public class GatewayConfig extends ServerConfig implements Serializable {

    private Address dbAddress;

    private boolean cachingEnabled;

    private int cacheSize;

    public Address getDbAddress() {
        return dbAddress;
    }

    public boolean isCachingEnabled() {
        return cachingEnabled;
    }

    public int getCacheSize() {
        return cacheSize;
    }
}
