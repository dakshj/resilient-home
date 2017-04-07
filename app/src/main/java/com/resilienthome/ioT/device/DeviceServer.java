package com.resilienthome.ioT.device;

import com.resilienthome.ioT.IoTServer;
import com.resilienthome.model.Address;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public interface DeviceServer extends IoTServer {

    String NAME = "Device Server";

    /**
     * Establishes a connection with a {@link DeviceServer}.
     *
     * @param address The address of the {@link DeviceServer}
     * @return An instance of the connected {@link DeviceServer}, connected remotely via Java RMI
     * @throws RemoteException   Thrown when a Java RMI exception occurs
     * @throws NotBoundException Thrown when the remote binding does not exist in the {@link Registry}
     */
    static DeviceServer connect(final Address address)
            throws RemoteException, NotBoundException {
        final Registry registry = LocateRegistry.getRegistry(address.getHost(), address.getPortNo());
        return (DeviceServer) registry.lookup(NAME);
    }

    /**
     * Returns the current state of this device.
     *
     * @throws RemoteException Thrown when a Java RMI exception occurs
     */
    void queryState() throws RemoteException;

    /**
     * Sets the state of this device.
     *
     * @param state The state this device needs to be set to
     * @throws RemoteException Thrown when a Java RMI exception occurs
     */
    void setState(final boolean state) throws RemoteException;

    /**
     * Toggles the state of the IoT Device
     *
     * @throws RemoteException Thrown when a Java RMI exception occurs
     */
    void toggleState() throws RemoteException;
}
