package com.resilienthome.model;

import java.util.HashSet;
import java.util.Set;

public class GatewayAssignment implements Comparable<GatewayAssignment> {

    private final IoT gateway;
    private final Set<IoT> ioTs;

    public GatewayAssignment(final IoT gateway) {
        this.gateway = gateway;
        ioTs = new HashSet<>();
    }

    public IoT getGateway() {
        return gateway;
    }

    public Set<IoT> getIoTs() {
        return ioTs;
    }

    public boolean containsIoT(final IoT ioT) {
        return getIoTs().contains(ioT);
    }

    public void addIoT(final IoT ioT) {
        getIoTs().add(ioT);
    }

    public void removeIoT(final IoT ioT) {
        getIoTs().remove(ioT);
    }

    public int size() {
        return ioTs.size();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final GatewayAssignment that = (GatewayAssignment) o;

        return gateway.equals(that.gateway);
    }

    @Override
    public int hashCode() {
        return gateway.hashCode();
    }

    @Override
    public int compareTo(final GatewayAssignment gatewayAssignment) {
        return Integer.compare(size(), gatewayAssignment.size());
    }
}
