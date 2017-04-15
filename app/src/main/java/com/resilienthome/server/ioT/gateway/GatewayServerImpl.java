package com.resilienthome.server.ioT.gateway;

import com.resilienthome.enums.DeviceType;
import com.resilienthome.enums.IoTType;
import com.resilienthome.enums.SensorType;
import com.resilienthome.model.Address;
import com.resilienthome.model.Device;
import com.resilienthome.model.IoT;
import com.resilienthome.model.Log;
import com.resilienthome.model.config.GatewayConfig;
import com.resilienthome.model.sensor.DoorSensor;
import com.resilienthome.model.sensor.MotionSensor;
import com.resilienthome.model.sensor.Sensor;
import com.resilienthome.model.sensor.TemperatureSensor;
import com.resilienthome.server.ioT.IoTServerImpl;
import com.resilienthome.server.ioT.db.DbServer;
import com.resilienthome.server.ioT.device.DeviceServer;
import com.resilienthome.server.ioT.sensor.SensorServer;
import com.resilienthome.server.loadbalancer.LoadBalancerServer;
import com.resilienthome.util.LimitedSizeArrayList;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GatewayServerImpl extends IoTServerImpl implements GatewayServer {

    private final Map<IoT, Address> registeredIoTs;

    private boolean alreadyRaisedAlarm;

    public GatewayServerImpl(final GatewayConfig gatewayConfig) throws RemoteException {
        super(gatewayConfig);
        registeredIoTs = new HashMap<>();
    }

    @Override
    public IoT createIoT() {
        return new IoT(UUID.randomUUID(), IoTType.GATEWAY);
    }

    @Override
    protected String getName() {
        return NAME;
    }

    private Map<IoT, Address> getRegisteredIoTs() {
        return registeredIoTs;
    }

    @Override
    public void register(final IoT ioT, final Address address) throws RemoteException {
        getRegisteredIoTs().put(ioT, address);
    }

    @Override
    public void deRegister(final IoT ioT) throws RemoteException {
        getRegisteredIoTs().remove(ioT);
    }

    @Override
    public void reportState(final IoT ioT) throws RemoteException {
        DbServer dbServer = null;

        try {
            dbServer = DbServer.connect(getGatewayConfig().getDbAddress());
        } catch (NotBoundException e) {
            e.printStackTrace();
        }

        assert dbServer != null;

        switch (ioT.getIoTType()) {
            case SENSOR:
                final Sensor sensor = ((Sensor) ioT);

                switch (sensor.getSensorType()) {
                    case TEMPERATURE:
                        final TemperatureSensor temperatureSensor = ((TemperatureSensor) sensor);
                        System.out.println("State of " + temperatureSensor + " : "
                                + temperatureSensor.getData() + "°F.");
                        dbServer.temperatureChanged(temperatureSensor);
                        break;

                    case MOTION:
                        final MotionSensor motionSensor = ((MotionSensor) sensor);
                        dbServer.motionDetected(motionSensor);

                    {
                        // TODO will not work unless all DB Servers have synchronized data
                        final LimitedSizeArrayList<Log> youngestLogsList =
                                dbServer.getYoungestLogsList();

                        if (youngestLogsList.getEldest().getIoTType() != null &&
                                youngestLogsList.getEldest().getIoTType() == IoTType.SENSOR &&
                                youngestLogsList.getEldest().getSensorType() != null &&
                                youngestLogsList.getEldest().getSensorType() == SensorType.DOOR) {
                            someoneEnteredHome(true);
                        }
                    }
                    break;

                    case DOOR:
                        final DoorSensor doorSensor = ((DoorSensor) sensor);
                        System.out.println("State of " + doorSensor + " : "
                                + (doorSensor.getData() ? "Open" : "Closed") + ".");
                        dbServer.doorToggled(doorSensor);

                    {
                        // TODO will not work unless all DB Servers have synchronized data
                        final LimitedSizeArrayList<Log> youngestLogsList =
                                dbServer.getYoungestLogsList();

                        if (youngestLogsList.getEldest().getIoTType() != null &&
                                youngestLogsList.getEldest().getIoTType() == IoTType.SENSOR &&
                                youngestLogsList.getEldest().getSensorType() != null &&
                                youngestLogsList.getEldest().getSensorType() == SensorType.MOTION) {
                            someoneEnteredHome(false);
                        }
                    }
                    break;
                }
                break;

            case DEVICE:
                final Device device = ((Device) ioT);
                System.out.println("State of " + device + " : "
                        + (device.getState() ? "On" : "Off") + ".");
                dbServer.deviceToggled(device);
                break;
        }
    }

    /**
     * Sets the state of the device.
     *
     * @param device The Device whose state needs to be changed
     * @param state  The new state of the device
     */
    private void setDeviceState(final Device device, final boolean state) {
        try {
            DeviceServer.connect(getRegisteredIoTs().get(device))
                    .setState(state);
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void raiseAlarm() throws RemoteException {
        if (alreadyRaisedAlarm) {
            return;
        }

        alreadyRaisedAlarm = true;

        System.out.println("An intruder has entered the Resilient Home!");
        switchOffAllOutlets();
        switchAllBulbs(true);
        System.out.println("Contacting 911 and the Home Owner!");
    }

    @Override
    public void entrantExecutionFinished() throws RemoteException {
        alreadyRaisedAlarm = false;
    }

    private void switchOffAllOutlets() {
        System.out.println("Switching off all outlets!");

        getRegisteredIoTs().keySet().stream()
                .filter(ioT -> ioT.getIoTType() == IoTType.DEVICE)
                .map(ioT -> ((Device) ioT))
                .filter(device -> device.getDeviceType() == DeviceType.OUTLET)
                .forEach(device -> new Thread(() -> setDeviceState(device, false)).start());
    }

    private void switchAllBulbs(final boolean status) {
        System.out.println("Switching " + (status ? "on" : "off") + " all bulbs!");

        getRegisteredIoTs().keySet().stream()
                .filter(ioT -> ioT.getIoTType() == IoTType.DEVICE)
                .map(ioT -> ((Device) ioT))
                .filter(device -> device.getDeviceType() == DeviceType.BULB)
                .forEach(device -> new Thread(() -> setDeviceState(device, status)).start());
    }

    private GatewayConfig getGatewayConfig() {
        return ((GatewayConfig) getServerConfig());
    }

    private void someoneEnteredHome(final boolean atHome) {
        try {
            if (!LoadBalancerServer.connect(getGatewayConfig().getLoadBalancerAddress())
                    .isRemotePresenceSensorActivated()) {
                try {
                    DbServer.connect(getGatewayConfig().getDbAddress()).intruderEntered();
                } catch (RemoteException | NotBoundException e) {
                    e.printStackTrace();
                }
                return;
            }
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }

        try {
            DbServer.connect(getGatewayConfig().getDbAddress()).userEntered(atHome);
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }

        System.out.println("User " + (atHome ? "entered" : "exited") + " the Resilient Home.");

        System.out.println("Switched to " + (atHome ? "HOME" : "AWAY") + " mode.");

        switchAllBulbs(atHome);

        if (!atHome) {
            switchOffAllOutlets();
            resetAllPresenceSensorsToInactive();
        }
    }

    private void resetAllPresenceSensorsToInactive() {
        getRegisteredIoTs().keySet().stream()
                .filter(ioT -> ioT.getIoTType() == IoTType.SENSOR)
                .map(ioT -> ((Sensor) ioT))
                .filter(sensor -> sensor.getSensorType() == SensorType.PRESENCE)
                .map(sensor -> getRegisteredIoTs().get(sensor))
                .forEach(address -> new Thread(() -> {
                    try {
                        SensorServer.connect(address).setPresenceServerActivated(false);
                    } catch (RemoteException | NotBoundException e) {
                        e.printStackTrace();
                    }
                }).start());
    }
}
