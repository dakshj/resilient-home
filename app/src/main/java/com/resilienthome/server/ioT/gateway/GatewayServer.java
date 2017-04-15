package com.resilienthome.server.ioT.gateway;

import com.resilienthome.model.Address;
import com.resilienthome.model.IoT;
import com.resilienthome.server.ioT.IoTServer;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

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
     * Registers an IoT with the gateway.
     * <p>
     * Stores the UUID and Address of the IoT in a {@link java.util.Map}.
     *
     * @param ioT     The IoT which needs to be registered
     * @param address The address of the IoT Server
     * @throws RemoteException Thrown when a Java RMI Exception occurs
     */
    void register(final IoT ioT, final Address address) throws RemoteException;

    /**
     * De-registers an IoT from the gateway.
     *
     * @param ioT The IoT which needs to be de-registered
     * @throws RemoteException Thrown when a Java RMI Exception occurs
     */
    void deRegister(final IoT ioT) throws RemoteException;

    /**
     * Reports the current state of the sensor.
     *
     * @param time                     The time at which the current state of the sensor was reported
     * @param ioT                      The IoT model object, containing the current state of the IoT
     * @param reportFromSensorOrDevice {@code true} if called from a Sensor or a Device;
     *                                 {@code false} otherwise
     * @throws RemoteException Thrown when a Java RMI Exception occurs
     */
    void reportState(final long time, final IoT ioT, final boolean reportFromSensorOrDevice)
            throws RemoteException;

    /**
     * Raises an alarm to alert that an intruder has entered the house.
     *
     * @throws RemoteException Thrown when a Java RMI Exception occurs
     */
    void raiseAlarm() throws RemoteException;

    /**
     * Notifies that the Entrant has finished its execution.
     *
     * @param time The time at which the entrant execution finished
     * @throws RemoteException Thrown when a Java RMI exception occurs
     */
    void entrantExecutionFinished(final long time) throws RemoteException;

    /**
     * Pings this {@link GatewayServer} to check if it is alive or not.
     *
     * @throws RemoteException Thrown when a Java RMI exception occurs
     */
    default void ping() throws RemoteException {}
}
