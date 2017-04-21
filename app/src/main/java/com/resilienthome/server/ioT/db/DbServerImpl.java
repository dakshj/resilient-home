package com.resilienthome.server.ioT.db;

import com.resilienthome.enums.IoTType;
import com.resilienthome.enums.LogType;
import com.resilienthome.model.Device;
import com.resilienthome.model.IoT;
import com.resilienthome.model.Log;
import com.resilienthome.model.config.DbConfig;
import com.resilienthome.model.sensor.DoorSensor;
import com.resilienthome.model.sensor.MotionSensor;
import com.resilienthome.model.sensor.TemperatureSensor;
import com.resilienthome.server.ioT.IoTServerImpl;

import java.rmi.RemoteException;
import java.util.UUID;

public class DbServerImpl extends IoTServerImpl implements DbServer {

    private final Logger logger;

    public DbServerImpl(final DbConfig dbConfig) throws RemoteException {
        super(dbConfig);
        logger = new Logger(dbConfig.getLogFileUniqueIdentifier());
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
    public Log temperatureChanged(final long time, final TemperatureSensor temperatureSensor)
            throws RemoteException {
        final Log log = new Log(time, LogType.RAW, temperatureSensor.getId(),
                temperatureSensor.getIoTType(), temperatureSensor.getSensorType(), null,
                "The temperature is " + temperatureSensor.getData() + "°F.");

        getLogger().log(log);

        return log;
    }

    @Override
    public Log motionDetected(final long time, final MotionSensor motionSensor)
            throws RemoteException {
        final Log log = new Log(time, LogType.RAW, motionSensor.getId(),
                motionSensor.getIoTType(), motionSensor.getSensorType(), null,
                "Motion detected.");

        getLogger().log(log);

        return log;
    }

    @Override
    public Log doorToggled(final long time, final DoorSensor doorSensor) throws RemoteException {
        final Log log = new Log(time, LogType.RAW, doorSensor.getId(),
                doorSensor.getIoTType(), doorSensor.getSensorType(), null,
                "Door " + (doorSensor.getData() ? "opened" : "closed") + ".");

        getLogger().log(log);

        return log;
    }

    @Override
    public Log deviceToggled(final long time, final Device device) throws RemoteException {
        final Log log = new Log(time, LogType.RAW, device.getId(),
                device.getIoTType(), null, device.getDeviceType(),
                device.getDeviceType() + " switched "
                        + (device.getState() ? "on" : "off") + ".");

        getLogger().log(log);

        return log;
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
    public void intruderTrapped(final long time) throws RemoteException {
        getLogger().log(new Log(time, LogType.INFERRED, null, null, null, null,
                "Intruder was trapped in the Resilient Home."));
    }

    @Override
    public void synchronizeDatabases(final boolean originator) throws RemoteException {
        if (originator) {
            System.out.println("Syncing with other DBs.");
        } else {
            System.out.println("Synced with other DBs.");
        }
    }

    @Override
    public Log getNthYoungestLog(final int n) throws RemoteException {
        return getLogger().getNthYoungestLog(n);
    }

    private Logger getLogger() {
        return logger;
    }
}
