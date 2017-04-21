package com.resilienthome.entrant;

import com.resilienthome.enums.IoTType;
import com.resilienthome.enums.SensorType;
import com.resilienthome.model.Address;
import com.resilienthome.model.Device;
import com.resilienthome.model.IoT;
import com.resilienthome.model.config.EntrantConfig;
import com.resilienthome.model.sensor.Sensor;
import com.resilienthome.server.ioT.device.DeviceServer;
import com.resilienthome.server.ioT.sensor.SensorServer;
import com.resilienthome.server.loadbalancer.LoadBalancerServer;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class Entrant {

    /**
     * The minimum time (in milliseconds) gap for randomly performing the next action.
     */
    private static final long TIME_DELAY_MIN = 500;

    /**
     * The maximum time (in milliseconds) gap for randomly performing the next action.
     */
    private static final long TIME_DELAY_MAX = 1500;

    private final EntrantConfig entrantConfig;

    private Map<IoT, Address> registeredIoTs;

    public Entrant(final EntrantConfig entrantConfig) throws RemoteException {
        this.entrantConfig = entrantConfig;

        try {
            System.out.println("Fetching the Map of all Registered IoTs from Load-Balancer...");

            setRegisteredIoTs(LoadBalancerServer.connect(entrantConfig.getLoadBalancerAddress())
                    .fetchRegisteredIoTs());

            System.out.println("Successfully fetched.");
        } catch (NotBoundException e) {
            e.printStackTrace();
        }

        setPresenceSensorActivationStatus();

        performActions();
    }

    /**
     * Internal method which activates the Presence Sensor
     * if the current Entrant is an authorized user.
     */
    private void setPresenceSensorActivationStatus() {
        getRegisteredIoTs().keySet().stream()
                .filter(ioT -> ioT.getIoTType() == IoTType.SENSOR)
                .map(ioT -> ((Sensor) ioT))
                .filter(sensor -> sensor.getSensorType() == SensorType.PRESENCE)
                .map(sensor -> getRegisteredIoTs().get(sensor))
                .forEach(address -> new Thread(() -> {
                    try {
                        SensorServer.connect(address)
                                .setPresenceServerActivated(getEntrantConfig().isAuthorized());
                    } catch (RemoteException | NotBoundException e) {
                        e.printStackTrace();
                    }
                }).start());
    }

    private void performActions() {
        System.out.println("Opening the door.");
        setDoorSensors(true);

        addRandomDelay();

        System.out.println("Closing the door.");
        setDoorSensors(false);

        addRandomDelay();

        System.out.println("Moving around the Resilient Home.");
        triggerMotionSensors();

        System.out.println("Randomly switching devices on and off, and randomly moving around," +
                " and randomly checking the temperature.");
        randomlyToggleDevicesAndTriggerMotionSensorsAndQueryTemperatureSensor();

        System.out.println("Moving towards the door to leave the Resilient Home.");
        triggerMotionSensors();

        addRandomDelay();

        if (getEntrantConfig().isAuthorized()) {
            System.out.println("Opening the door.");
            setDoorSensors(true);

            addRandomDelay();

            System.out.println("Closing the door.");
            setDoorSensors(false);
        } else {
            System.out.println("Cannot use the door because the Entrant is an Intruder!");
        }

        try {
            LoadBalancerServer.connect(getEntrantConfig().getLoadBalancerAddress())
                    .entrantExecutionFinished();
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param opened {@code true} of the door(s) need to be opened;
     *               {@code false} otherwise
     */
    private void setDoorSensors(final boolean opened) {
        getRegisteredIoTs().keySet().stream()
                .filter(ioT -> ioT.getIoTType() == IoTType.SENSOR)
                .map(ioT -> ((Sensor) ioT))
                .filter(sensor -> sensor.getSensorType() == SensorType.DOOR)
                .map(doorSensor -> getRegisteredIoTs().get(doorSensor))
                .forEach(address -> new Thread(() -> {
                    try {
                        SensorServer.connect(address).openOrCloseDoor(opened);
                    } catch (RemoteException | NotBoundException e) {
                        e.printStackTrace();
                    }
                }).start());
    }

    private void triggerMotionSensors() {
        getRegisteredIoTs().keySet().stream()
                .filter(ioT -> ioT.getIoTType() == IoTType.SENSOR)
                .map(ioT -> ((Sensor) ioT))
                .filter(sensor -> sensor.getSensorType() == SensorType.MOTION)
                .map(motionSensor -> getRegisteredIoTs().get(motionSensor))
                .forEach(address -> new Thread(() -> {
                    try {
                        SensorServer.connect(address).triggerMotionSensor();
                    } catch (RemoteException | NotBoundException e) {
                        e.printStackTrace();
                    }
                }).start());
    }

    private void queryTemperatureSensors() {
        getRegisteredIoTs().keySet().stream()
                .filter(ioT -> ioT.getIoTType() == IoTType.SENSOR)
                .map(ioT -> ((Sensor) ioT))
                .filter(sensor -> sensor.getSensorType() == SensorType.TEMPERATURE)
                .map(temperatureSensor -> getRegisteredIoTs().get(temperatureSensor))
                .forEach(address -> new Thread(() -> {
                    try {
                        SensorServer.connect(address).queryState();
                    } catch (RemoteException | NotBoundException e) {
                        e.printStackTrace();
                    }
                }).start());
    }

    /**
     * Randomly selects a few devices and toggles their status.
     * <p>
     * Additionally, randomly triggers motion sensors with a 50% probability.
     * <p>
     * Additionally, randomly queries temperature sensors with a 50% probability.
     * <p>
     * Finally, randomly recursively calls self with a 80% probability.
     */
    private void randomlyToggleDevicesAndTriggerMotionSensorsAndQueryTemperatureSensor() {
        getRegisteredIoTs().keySet().stream()
                .filter(ioT -> ioT.getIoTType() == IoTType.DEVICE)

                // Adds randomness to the device toggling action
                .filter(ioT -> ThreadLocalRandom.current().nextBoolean())

                .map(ioT -> ((Device) ioT))
                .forEach(device -> {
                    addRandomDelay();

                    if (getEntrantConfig().isAuthorized()) {
                        System.out.println("Toggling the " + device + ".");

                        final Address address = getRegisteredIoTs().get(device);

                        try {
                            DeviceServer.connect(address).toggleState();
                        } catch (RemoteException | NotBoundException e) {
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println("Cannot toggle the " + device + " because" +
                                " the Entrant is an Intruder!");
                    }
                });

        addRandomDelay();

        if (ThreadLocalRandom.current().nextBoolean()) {
            System.out.println("Moving around the Resilient Home.");
            triggerMotionSensors();
        }

        if (ThreadLocalRandom.current().nextBoolean()) {
            System.out.println("Checking the Temperature.");
            queryTemperatureSensors();
        }

        addRandomDelay();

        // Randomly recursively calls self with a 80% probability
        if (ThreadLocalRandom.current().nextInt(1, 101) <= 80) {
            randomlyToggleDevicesAndTriggerMotionSensorsAndQueryTemperatureSensor();
        }
    }

    private void addRandomDelay() {
        final long randomDelay = ThreadLocalRandom.current().nextLong(
                TIME_DELAY_MIN,
                TIME_DELAY_MAX
        );

        try {
            Thread.sleep(randomDelay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private EntrantConfig getEntrantConfig() {
        return entrantConfig;
    }

    private Map<IoT, Address> getRegisteredIoTs() {
        return registeredIoTs;
    }

    private void setRegisteredIoTs(final Map<IoT, Address> registeredIoTs) {
        this.registeredIoTs = registeredIoTs;
    }
}
