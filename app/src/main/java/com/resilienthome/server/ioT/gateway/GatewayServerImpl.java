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
import com.resilienthome.server.loadbalancer.LoadBalancerServer;
import com.resilienthome.util.LimitedSizeArrayList;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GatewayServerImpl extends IoTServerImpl implements GatewayServer {

    // TODO add souts for cache fetching

    private Map<IoT, Address> registeredIoTs;

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
        if (registeredIoTs == null) {
            registeredIoTs = new HashMap<>();
        }

        return registeredIoTs;
    }

    @Override
    public void register(final IoT ioT, final Address address) throws RemoteException {
        getRegisteredIoTs().put(ioT, address);
        System.out.println("Registered " + ioT + " " + ioT.getId() + ".");
    }

    @Override
    public void deRegister(final IoT ioT) throws RemoteException {
        getRegisteredIoTs().remove(ioT);
    }

    @Override
    public void reportState(final long time, final IoT ioT, final boolean reportFromSensorOrDevice)
            throws RemoteException {
        DbServer dbServer = null;

        try {
            dbServer = DbServer.connect(getGatewayConfig().getDbAddress());
        } catch (NotBoundException e) {
            e.printStackTrace();
        }

        assert dbServer != null;

        dbServer.synchronizeDatabases(reportFromSensorOrDevice);

        switch (ioT.getIoTType()) {
            case SENSOR:
                final Sensor sensor = ((Sensor) ioT);

                switch (sensor.getSensorType()) {
                    case TEMPERATURE:
                        final TemperatureSensor temperatureSensor = ((TemperatureSensor) sensor);
                        if (reportFromSensorOrDevice) {
                            System.out.println("State of " + temperatureSensor + " : "
                                    + temperatureSensor.getData() + "°F.");
                        }
                        dbServer.temperatureChanged(time, temperatureSensor);
                        break;

                    case MOTION:
                        final MotionSensor motionSensor = ((MotionSensor) sensor);
                        dbServer.motionDetected(time, motionSensor);

                    {
                        final LimitedSizeArrayList<Log> youngestLogsList =
                                dbServer.getYoungestLogsList();

                        if (youngestLogsList.getEldest().getIoTType() != null &&
                                youngestLogsList.getEldest().getIoTType() == IoTType.SENSOR &&
                                youngestLogsList.getEldest().getSensorType() != null &&
                                youngestLogsList.getEldest().getSensorType() == SensorType.DOOR) {
                            someoneEnteredHome(time, true);
                        }
                    }
                    break;

                    case DOOR:
                        final DoorSensor doorSensor = ((DoorSensor) sensor);
                        if (reportFromSensorOrDevice) {
                            System.out.println("State of " + doorSensor + " : "
                                    + (doorSensor.getData() ? "Open" : "Closed") + ".");
                        }
                        dbServer.doorToggled(time, doorSensor);

                    {
                        final LimitedSizeArrayList<Log> youngestLogsList =
                                dbServer.getYoungestLogsList();

                        if (youngestLogsList.getEldest().getIoTType() != null &&
                                youngestLogsList.getEldest().getIoTType() == IoTType.SENSOR &&
                                youngestLogsList.getEldest().getSensorType() != null &&
                                youngestLogsList.getEldest().getSensorType() == SensorType.MOTION) {
                            someoneEnteredHome(time, false);
                        }
                    }
                    break;
                }
                break;

            case DEVICE:
                final Device device = ((Device) ioT);
                if (reportFromSensorOrDevice) {
                    System.out.println("State of " + device + " : "
                            + (device.getState() ? "On" : "Off") + ".");
                }
                dbServer.deviceToggled(time, device);
                break;
        }

        if (reportFromSensorOrDevice) {
            try {
                LoadBalancerServer.connect(getGatewayConfig().getLoadBalancerAddress())
                        .broadcastStateToAllGateways(getIoT(), time, ioT);
            } catch (NotBoundException e) {
                e.printStackTrace();
            }
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
    public void entrantExecutionFinished(final long time) throws RemoteException {
        alreadyRaisedAlarm = false;

        try {
            if (!LoadBalancerServer.connect(getGatewayConfig().getLoadBalancerAddress())
                    .isRemotePresenceSensorActivated()) {
                DbServer.connect(getGatewayConfig().getDbAddress()).intruderTrapped(time);
            }
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
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

    private void someoneEnteredHome(final long time, final boolean atHome) {
        try {
            if (!LoadBalancerServer.connect(getGatewayConfig().getLoadBalancerAddress())
                    .isRemotePresenceSensorActivated()) {
                try {
                    DbServer.connect(getGatewayConfig().getDbAddress()).intruderEntered(time);
                } catch (RemoteException | NotBoundException e) {
                    e.printStackTrace();
                }

                raiseAlarm();

                return;
            }
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }

        try {
            DbServer.connect(getGatewayConfig().getDbAddress()).userEntered(time, atHome);
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }

        System.out.println("User " + (atHome ? "entered" : "exited") + " the Resilient Home.");

        System.out.println("Switched to " + (atHome ? "HOME" : "AWAY") + " mode.");

        switchAllBulbs(atHome);

        if (!atHome) {
            switchOffAllOutlets();
        }
    }
}
