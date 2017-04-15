package com.resilienthome.server.ioT.db;

import com.resilienthome.enums.IoTType;
import com.resilienthome.enums.LogType;
import com.resilienthome.model.Device;
import com.resilienthome.model.IoT;
import com.resilienthome.model.Log;
import com.resilienthome.model.config.ServerConfig;
import com.resilienthome.model.sensor.DoorSensor;
import com.resilienthome.model.sensor.MotionSensor;
import com.resilienthome.model.sensor.TemperatureSensor;
import com.resilienthome.server.ioT.IoTServerImpl;
import com.resilienthome.util.LimitedSizeArrayList;

import java.rmi.RemoteException;
import java.util.UUID;

public class DbServerImpl extends IoTServerImpl implements DbServer {

    private static final int YOUNGEST_LOGS_LIST_SIZE = 2;

    private final Logger logger;
    private final LimitedSizeArrayList<Log> youngestLogsList;

    public DbServerImpl(final ServerConfig serverConfig) throws RemoteException {
        super(serverConfig);
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
    public void temperatureChanged(final long time, final TemperatureSensor temperatureSensor)
            throws RemoteException {
        final Log log = new Log(time, LogType.RAW, temperatureSensor.getId(),
                temperatureSensor.getIoTType(), temperatureSensor.getSensorType(), null,
                "Temperature changed to " + temperatureSensor.getData() + ".");

        getYoungestLogsList().add(log);

        getLogger().log(log);
    }

    @Override
    public void motionDetected(final long time, final MotionSensor motionSensor)
            throws RemoteException {
        final Log log = new Log(time, LogType.RAW, motionSensor.getId(),
                motionSensor.getIoTType(), motionSensor.getSensorType(), null,
                "Motion detected.");

        getYoungestLogsList().add(log);

        getLogger().log(log);
    }

    @Override
    public void doorToggled(final long time, final DoorSensor doorSensor) throws RemoteException {
        final Log log = new Log(time, LogType.RAW, doorSensor.getId(),
                doorSensor.getIoTType(), doorSensor.getSensorType(), null,
                "Door " + (doorSensor.getData() ? "opened" : "closed") + ".");

        getYoungestLogsList().add(log);

        getLogger().log(log);
    }

    @Override
    public void deviceToggled(final long time, final Device device) throws RemoteException {
        final Log log = new Log(time, LogType.RAW, device.getId(),
                device.getIoTType(), null, device.getDeviceType(),
                device.getDeviceType() + " switched "
                        + (device.getState() ? "on" : "off") + ".");

        getYoungestLogsList().add(log);

        getLogger().log(log);
    }

    @Override
    public void intruderEntered(final long time)
            throws RemoteException {
        getLogger().log(new Log(time, LogType.INFERRED,
                null, null, null, null, "Intruder entered the Resilient Home."));
    }

    @Override
    public void userEntered(final long time, final boolean atHome) throws RemoteException {
        getLogger().log(new Log(time, LogType.INFERRED, null, null, null, null,
                "User " + (atHome ? "entered" : "exited") + " the Resilient Home."));
    }

    @Override
    public LimitedSizeArrayList<Log> getYoungestLogsList() throws RemoteException {
        return youngestLogsList;
    }

    private Logger getLogger() {
        return logger;
    }
}
