package com.resilienthome.server.ioT.db;

import com.resilienthome.model.Address;
import com.resilienthome.model.Device;
import com.resilienthome.model.Log;
import com.resilienthome.model.sensor.DoorSensor;
import com.resilienthome.model.sensor.MotionSensor;
import com.resilienthome.model.sensor.TemperatureSensor;
import com.resilienthome.server.ioT.IoTServer;
import com.resilienthome.util.LimitedSizeArrayList;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public interface DbServer extends IoTServer {

    String NAME = "DB Server";

    /**
     * Establishes a connection with a {@link DbServer}.
     *
     * @param address The address of the {@link DbServer}
     * @return An instance of the connected {@link DbServer}, connected remotely via Java RMI
     * @throws RemoteException   Thrown when a Java RMI exception occurs
     * @throws NotBoundException Thrown when the remote binding does not exist in the {@link Registry}
     */
    static DbServer connect(final Address address)
            throws RemoteException, NotBoundException {
        final Registry registry = LocateRegistry.getRegistry(address.getHost(), address.getPortNo());
        return (DbServer) registry.lookup(NAME);
    }

    /**
     * Logs a temperature change, as provided by the temperature sensor.
     *
     * @param time              The time at which the temperature changed
     * @param temperatureSensor The temperature sensor which reported the temperature change
     * @throws RemoteException Thrown when a Java RMI exception occurs
     */
    void temperatureChanged(final long time, final TemperatureSensor temperatureSensor)
            throws RemoteException;

    /**
     * Logs a detected motion, as provided by the motion sensor.
     *
     * @param time         The time at which the motion was detected
     * @param motionSensor The motion sensor which detected any motion
     * @throws RemoteException Thrown when a Java RMI exception occurs
     */
    void motionDetected(final long time, final MotionSensor motionSensor) throws RemoteException;

    /**
     * Logs the opened state of a door, as provided by the door sensor.
     *
     * @param time       The time at which the door's state changed
     * @param doorSensor The door sensor which reported a door that was opened or closed
     * @throws RemoteException Thrown when a Java RMI exception occurs
     */
    void doorToggled(final long time, final DoorSensor doorSensor) throws RemoteException;

    /**
     * Logs the current state of the device.
     *
     * @param time   The time at which the device's state changed
     * @param device The device whose state was toggled
     * @throws RemoteException Thrown when a Java RMI exception occurs
     */
    void deviceToggled(final long time, final Device device) throws RemoteException;

    /**
     * Logs the inferred log of when an Intruder entered the Resilient Home.
     *
     * @param time The time at which the intruder entered
     * @throws RemoteException Thrown when a Java RMI exception occurs
     */
    void intruderEntered(final long time) throws RemoteException;

    /**
     * Logs the inferred log of when a User entered the Resilient Home.
     *
     * @param time   The time at which the user entered
     * @param atHome {@code true} if the User entered the Resilient Home;
     *               {@code false} otherwise
     * @throws RemoteException Thrown when a Java RMI exception occurs
     */
    void userEntered(final long time, final boolean atHome) throws RemoteException;

    /**
     * Returns a limited-size list of the latest inserted {@link Log}s.
     *
     * @return A limited-size list of the latest inserted {@link Log}s
     * @throws RemoteException Thrown when a Java RMI exception occurs
     */
    LimitedSizeArrayList<Log> getYoungestLogsList() throws RemoteException;
}
