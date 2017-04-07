package com.resilienthome.ioT.db;

import com.resilienthome.enums.IoTType;
import com.resilienthome.enums.LogType;
import com.resilienthome.ioT.IoTServerImpl;
import com.resilienthome.model.Device;
import com.resilienthome.model.IoT;
import com.resilienthome.model.Log;
import com.resilienthome.model.config.ServerConfig;
import com.resilienthome.model.sensor.DoorSensor;
import com.resilienthome.model.sensor.MotionSensor;
import com.resilienthome.model.sensor.TemperatureSensor;
import com.resilienthome.util.LimitedSizeArrayList;

import java.rmi.RemoteException;
import java.util.UUID;

public class DbServerImpl extends IoTServerImpl implements DbServer {

    private static final int YOUNGEST_LOGS_LIST_SIZE = 2;

    private final Logger logger;
    private final LimitedSizeArrayList<Log> youngestLogsList;

    public DbServerImpl(final ServerConfig serverConfig) throws RemoteException {
        super(serverConfig, true);
        logger = new Logger();
        youngestLogsList = new LimitedSizeArrayList<>(YOUNGEST_LOGS_LIST_SIZE);
    }

    @Override
    public IoT createIoT() {
        return new IoT(UUID.randomUUID(), IoTType.DB);
    }

    @Override
    protected String getName() {
        return NAME;
    }

    @Override
    public void temperatureChanged(final TemperatureSensor temperatureSensor) throws RemoteException {
        final Log log = new Log(System.currentTimeMillis(), LogType.RAW, temperatureSensor.getId(),
                temperatureSensor.getIoTType(), temperatureSensor.getSensorType(), null,
                "Temperature changed to " + temperatureSensor.getData() + ".");

        getYoungestLogsList().add(log);

        getLogger().log(log);
    }

    @Override
    public void motionDetected(final MotionSensor motionSensor) throws RemoteException {
        final Log log = new Log(System.currentTimeMillis(), LogType.RAW, motionSensor.getId(),
                motionSensor.getIoTType(), motionSensor.getSensorType(), null,
                "Motion detected.");

        getYoungestLogsList().add(log);

        getLogger().log(log);
    }

    @Override
    public void doorToggled(final DoorSensor doorSensor) throws RemoteException {
        final Log log = new Log(System.currentTimeMillis(), LogType.RAW, doorSensor.getId(),
                doorSensor.getIoTType(), doorSensor.getSensorType(), null,
                "Door " + (doorSensor.getData() ? "opened" : "closed") + ".");

        getYoungestLogsList().add(log);

        getLogger().log(log);
    }

    @Override
    public void deviceToggled(final Device device) throws RemoteException {
        final Log log = new Log(System.currentTimeMillis(), LogType.RAW, device.getId(),
                device.getIoTType(), null, device.getDeviceType(),
                device.getDeviceType() + " switched "
                        + (device.getState() ? "on" : "off") + ".");

        getYoungestLogsList().add(log);

        getLogger().log(log);
    }

    @Override
    public LimitedSizeArrayList<Log> getYoungestLogsList() throws RemoteException {
        return youngestLogsList;
    }

    @Override
    public void intruderEntered()
            throws RemoteException {
        getLogger().log(new Log(System.currentTimeMillis(), LogType.INFERRED,
                null, null, null, null, "Intruder entered the Resilient Home."));
    }

    @Override
    public void userEntered(final boolean atHome) throws RemoteException {
        getLogger().log(new Log(System.currentTimeMillis(), LogType.INFERRED, null, null, null, null,
                "User " + (atHome ? "entered" : "exited") + " the Resilient Home."));
    }

    private Logger getLogger() {
        return logger;
    }
}
