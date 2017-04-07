package com.resilienthome.ioT.gateway;

import com.resilienthome.ioT.IoTServer;
import com.resilienthome.model.Address;
import com.resilienthome.model.IoT;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;

public interface GatewayServer extends IoTServer {

    String NAME = "Gateway Server";

    /**
     * Establishes a connection with a {@link GatewayServer}.
     *
     * @param address The address of the {@link GatewayServer}
     * @return An instance of the connected {@link GatewayServer}, connected remotely via Java RMI
     * @throws RemoteException   Thrown when a Java RMI exception occurs
     * @throws NotBoundException Thrown when the remote binding does not exist in the {@link Registry}
     */
    static GatewayServer connect(final Address address)
            throws RemoteException, NotBoundException {
        final Registry registry = LocateRegistry.getRegistry(address.getHost(), address.getPortNo());
        return (GatewayServer) registry.lookup(NAME);
    }

    /**
     * Registers a sensor with the gateway.
     * <p>
     * Stores the UUID and Address of the sensor in a {@link java.util.Map}.
     *
     * @param ioT     The IoT which needs to be registered
     * @param address The address of the IoT Server
     * @throws RemoteException Thrown when a Java RMI Exception occurs
     */
    void register(final IoT ioT, final Address address) throws RemoteException;

    /**
     * Reports the current state of the sensor.
     *
     * @param ioT The IoT model object, containing the current state of the IoT
     * @throws RemoteException Thrown when a Java RMI Exception occurs
     */
    void reportState(final IoT ioT) throws RemoteException;

    /**
     * Returns the {@link Map} of registered IoTs to the calling IoT server.
     *
     * @return The {@link Map} of registered IoTs
     * @throws RemoteException Thrown when a Java RMI exception occurs
     */
    Map<IoT, Address> fetchRegisteredIoTs() throws RemoteException;

    /**
     * Raises an alarm to alert that an intruder has entered the house.
     *
     * @throws RemoteException Thrown when a Java RMI Exception occurs
     */
    void raiseAlarm() throws RemoteException;

    /**
     * Notifies that the Entrant has finished its execution.
     *
     * @throws RemoteException Thrown when a Java RMI exception occurs
     */
    void entrantExecutionFinished() throws RemoteException;
}
