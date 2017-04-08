package com.resilienthome.model.config;

import com.resilienthome.model.Address;

import java.io.Serializable;

public class Config implements Serializable {

    private Address loadBalancerAddress;

    public Address getLoadBalancerAddress() {
        return loadBalancerAddress;
    }
}
