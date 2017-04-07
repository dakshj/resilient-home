package com.resilienthome.model.config;

import com.resilienthome.model.Address;

import java.io.Serializable;

public class ServerConfig extends Config implements Serializable {

    private Address address;

    public Address getAddress() {
        return address;
    }
}
