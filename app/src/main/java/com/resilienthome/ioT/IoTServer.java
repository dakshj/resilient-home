package com.resilienthome.ioT;

import com.resilienthome.model.Address;
import com.resilienthome.model.IoT;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface IoTServer extends Remote {

    /**
     * Sets the {@link Map} of all IoTs which have been registered to the Gateway Server.
     *
     * @param registeredIoTs The Map to set within this server
     * @throws RemoteException Thrown when a Java RMI exception occurs
     */
    void setRegisteredIoTs(final Map<IoT, Address> registeredIoTs)
            throws RemoteException;
}
