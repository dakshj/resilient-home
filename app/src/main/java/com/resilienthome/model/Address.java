package com.resilienthome.model;

import java.io.Serializable;

public class Address implements Serializable {

    private final String host;
    private final int portNo;

    public Address(final String host, final int portNo) {
        this.host = host;
        this.portNo = portNo;
    }

    public String getHost() {
        return host;
    }

    public int getPortNo() {
        return portNo;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Address address = (Address) o;

        return portNo == address.portNo && host.equals(address.host);
    }

    @Override
    public int hashCode() {
        int result = host.hashCode();
        result = 31 * result + portNo;
        return result;
    }

    @Override
    public String toString() {
        return getHost() + ":" + getPortNo();
    }
}
