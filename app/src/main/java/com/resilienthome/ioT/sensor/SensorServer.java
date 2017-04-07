package com.resilienthome.ioT.sensor;

import com.resilienthome.ioT.IoTServer;
import com.resilienthome.model.Address;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public interface SensorServer extends IoTServer {

    String NAME = "Sensor Server";

    /**
     * Establishes a connection with a {@link SensorServer}.
     *
     * @param address The address of the {@link SensorServer}
     * @return An instance of the connected {@link SensorServer}, connected remotely via Java RMI
     * @throws RemoteException   Thrown when a Java RMI exception occurs
     * @throws NotBoundException Thrown when the remote binding does not exist in the {@link Registry}
     */
    static SensorServer connect(final Address address)
            throws RemoteException, NotBoundException {
        final Registry registry = LocateRegistry.getRegistry(address.getHost(), address.getPortNo());
        return (SensorServer) registry.lookup(NAME);
    }

    /**
     * Returns the current state of this sensor.
     *
     * @throws RemoteException Thrown when a Java RMI exception occurs
     */
    void queryState() throws RemoteException;

    /**
     * Triggers the Motion Sensor, and notifies the Gateway Server.
     * <p>
     * Additionally, if the Entrant is not authorized, raises the security system's alarm.
     *
     * @throws RemoteException Thrown when a Java RMI exception occurs
     */
    void triggerMotionSensor() throws RemoteException;

    /**
     * @param opened {@code true} if the door needs to be opened;
     *               {@code false} otherwise
     * @throws RemoteException Thrown when a Java RMI exception occurs
     */
    void openOrCloseDoor(final boolean opened) throws RemoteException;

    /**
     * Activates the Presence Sensor, which marks an Entrant as an authorized user.
     *
     * @param entrantAuthorized {@code true} if the Entrant is authorized,
     *                          and the Presence Sensor needs to be activated;
     *                          {@code false} otherwise
     * @throws RemoteException Thrown when a Java RMI exception occurs
     */
    void setPresenceServerActivated(final boolean entrantAuthorized) throws RemoteException;

    /**
     * Checks whether the Presence Sensor is activated.
     *
     * @return {@code true} if the Presence Sensor is activated;
     * {@code false} otherwise
     * @throws RemoteException Thrown when a Java RMI exception occurs
     */
    boolean isPresenceSensorActivated() throws RemoteException;
}
