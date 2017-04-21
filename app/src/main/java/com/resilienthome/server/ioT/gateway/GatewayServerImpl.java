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

    private final LimitedSizeArrayList<Log> youngestLogsList;

    private Map<IoT, Address> registeredIoTs;
    private boolean alreadyRaisedAlarm;

    public GatewayServerImpl(final GatewayConfig gatewayConfig) throws RemoteException {
        super(gatewayConfig);

        if (gatewayConfig.isCachingEnabled()) {
            youngestLogsList = new LimitedSizeArrayList<>(
                    gatewayConfig.getCacheSize() >= 2 ? gatewayConfig.getCacheSize() : 2
            );
        } else {
            youngestLogsList = null;
        }

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

    /**
     * Returns a limited-size list of the latest inserted {@link Log}s.
     *
     * @return A limited-size list of the latest inserted {@link Log}s
     */
    private LimitedSizeArrayList<Log> getYoungestLogsList() {
        return youngestLogsList;
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
        System.out.println("De-registered " + ioT + " " + ioT.getId() + ".");
    }

    @Override
    public void reportState(final long time, final IoT ioT, final boolean reportFromSensorOrDevice)
            throws RemoteException {
        if (reportFromSensorOrDevice) {
            try {
                LoadBalancerServer.connect(getGatewayConfig().getLoadBalancerAddress())
                        .broadcastStateToAllGateways(getIoT(), time, ioT);
            } catch (NotBoundException e) {
                e.printStackTrace();
            }
        }

        DbServer dbServer = null;

        try {
            dbServer = DbServer.connect(getGatewayConfig().getDbAddress());
        } catch (NotBoundException e) {
            e.printStackTrace();
        }

        assert dbServer != null;

        dbServer.synchronizeDatabases(reportFromSensorOrDevice);

        Log createdLog = null;

        switch (ioT.getIoTType()) {
            case SENSOR:
                final Sensor sensor = ((Sensor) ioT);

                switch (sensor.getSensorType()) {
                    case TEMPERATURE: {
                        final TemperatureSensor temperatureSensor = ((TemperatureSensor) sensor);
                        if (reportFromSensorOrDevice) {
                            System.out.println("State of " + temperatureSensor + " : "
                                    + temperatureSensor.getData() + "°F.");
                        }

                        createdLog = dbServer.temperatureChanged(time, temperatureSensor);
                    }
                    break;

                    case MOTION: {
                        final MotionSensor motionSensor = ((MotionSensor) sensor);

                        createdLog = dbServer.motionDetected(time, motionSensor);
                    }
                    break;

                    case DOOR: {
                        final DoorSensor doorSensor = ((DoorSensor) sensor);
                        if (reportFromSensorOrDevice) {
                            System.out.println("State of " + doorSensor + " : "
                                    + (doorSensor.getData() ? "Open" : "Closed") + ".");
                        }

                        createdLog = dbServer.doorToggled(time, doorSensor);
                    }
                    break;
                }
                break;

            case DEVICE: {
                final Device device = ((Device) ioT);
                if (reportFromSensorOrDevice) {
                    System.out.println("State of " + device + " : "
                            + (device.getState() ? "On" : "Off") + ".");
                }

                createdLog = dbServer.deviceToggled(time, device);
            }
            break;
        }

        if (getGatewayConfig().isCachingEnabled()) {
            getYoungestLogsList().add(createdLog);
        }

        final Log secondYoungestLog;
        final long millisToFetchData = System.currentTimeMillis();
        if (getGatewayConfig().isCachingEnabled() && getYoungestLogsList().size() >= 2) {
            System.out.println("Fetching previous Log entry from the Cache.");
            secondYoungestLog = getYoungestLogsList().getNthYoungest(2);
        } else {
            System.out.println("Fetching previous Log entry from the DB.");
            secondYoungestLog = dbServer.getNthYoungestLog(2);
        }

        System.out.println("Time taken to fetch data = " +
                (System.currentTimeMillis() - millisToFetchData) + " ms.");

        checkEntrantEntryOrExit(ioT, secondYoungestLog, time);
    }

    private void checkEntrantEntryOrExit(final IoT ioT, final Log secondYoungestLog,
            final long time) {
        if (ioT.getIoTType() == IoTType.SENSOR &&
                ((Sensor) ioT).getSensorType() == SensorType.MOTION) {
            if (secondYoungestLog.getIoTType() != null &&
                    secondYoungestLog.getIoTType() == IoTType.SENSOR &&
                    secondYoungestLog.getSensorType() != null &&
                    secondYoungestLog.getSensorType() == SensorType.DOOR) {
                someoneEnteredHome(time, true);
            }
        }

        if (ioT.getIoTType() == IoTType.SENSOR &&
                ((Sensor) ioT).getSensorType() == SensorType.DOOR) {
            if (secondYoungestLog != null &&
                    secondYoungestLog.getIoTType() != null &&
                    secondYoungestLog.getIoTType() == IoTType.SENSOR &&
                    secondYoungestLog.getSensorType() != null &&
                    secondYoungestLog.getSensorType() == SensorType.MOTION) {
                someoneEnteredHome(time, false);
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
