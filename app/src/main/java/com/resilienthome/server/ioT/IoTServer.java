package com.resilienthome.server.ioT;

import com.resilienthome.model.Address;
import com.resilienthome.server.Server;
import com.resilienthome.server.ioT.gateway.GatewayServer;

import java.rmi.RemoteException;

public interface IoTServer extends Server {

    /**
     * Sets the {@link Address} of the {@link GatewayServer} to which this {@link IoTServer}
     * has been assigned.
     *
     * @param gatewayAddress The address of the {@link GatewayServer}
     * @throws RemoteException Thrown when a Java RMI exception occurs
     */
    void setGatewayAddress(final Address gatewayAddress) throws RemoteException;
}
