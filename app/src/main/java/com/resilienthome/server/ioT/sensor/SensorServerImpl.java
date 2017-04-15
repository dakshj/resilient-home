package com.resilienthome.server.ioT.sensor;

import com.resilienthome.enums.IoTType;
import com.resilienthome.enums.SensorType;
import com.resilienthome.model.IoT;
import com.resilienthome.model.config.SensorConfig;
import com.resilienthome.model.sensor.DoorSensor;
import com.resilienthome.model.sensor.MotionSensor;
import com.resilienthome.model.sensor.PresenceSensor;
import com.resilienthome.model.sensor.Sensor;
import com.resilienthome.model.sensor.TemperatureSensor;
import com.resilienthome.server.ioT.IoTServerImpl;
import com.resilienthome.server.ioT.gateway.GatewayServer;

import java.math.BigDecimal;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class SensorServerImpl extends IoTServerImpl implements SensorServer {

    public SensorServerImpl(final SensorConfig sensorConfig) throws RemoteException {
        super(sensorConfig);

        if (getSensor().getSensorType() == SensorType.TEMPERATURE) {
            periodicallyGenerateTemperatureValues();
        }
    }

    @Override
    public IoT createIoT() {
        switch (getSensorConfig().getSensorType()) {
            case TEMPERATURE:
                return new TemperatureSensor(UUID.randomUUID(), IoTType.SENSOR,
                        getSensorConfig().getSensorType());

            case MOTION:
                return new MotionSensor(UUID.randomUUID(), IoTType.SENSOR,
                        getSensorConfig().getSensorType());

            case DOOR:
                return new DoorSensor(UUID.randomUUID(), IoTType.SENSOR,
                        getSensorConfig().getSensorType());

            case PRESENCE:
                return new PresenceSensor(UUID.randomUUID(), IoTType.SENSOR,
                        getSensorConfig().getSensorType());
        }

        return null;
    }

    @Override
    protected String getName() {
        return NAME;
    }

    @Override
    public void queryState() throws RemoteException {
        try {
            GatewayServer.connect(getGatewayAddress())
                    .reportState(System.currentTimeMillis(), getSensor(), true);
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void triggerMotionSensor() throws RemoteException {
        if (getSensor().getSensorType() != SensorType.MOTION) {
            return;
        }

        System.out.println("Detected motion.");

        queryState();
    }

    @Override
    public void openOrCloseDoor(final boolean opened) throws RemoteException {
        if (getSensor().getSensorType() != SensorType.DOOR) {
            return;
        }

        System.out.println("Door " + (opened ? "opened" : "closed") + ".");

        final DoorSensor doorSensor = ((DoorSensor) getSensor());
        doorSensor.setData(opened);

        queryState();
    }

    @Override
    public void setPresenceServerActivated(final boolean entrantAuthorized) throws RemoteException {
        if (getSensor().getSensorType() != SensorType.PRESENCE) {
            return;
        }

        PresenceSensor presenceSensor = ((PresenceSensor) getSensor());
        presenceSensor.setData(entrantAuthorized);
    }

    @Override
    public boolean isPresenceSensorActivated() throws RemoteException {
        if (getSensor().getSensorType() != SensorType.PRESENCE) {
            return false;
        }

        PresenceSensor presenceSensor = ((PresenceSensor) getSensor());
        return presenceSensor.getData();
    }

    /**
     * Generates a random Temperature value (in °F).
     * <p>
     * Next, waits for a random duration.
     * <p>
     * Finally, repeats the above.
     */
    private void periodicallyGenerateTemperatureValues() {
        final long delayForNextValueGeneration = ThreadLocalRandom.current().nextLong(
                TemperatureSensor.VALUE_GENERATION_GAP_MIN,
                TemperatureSensor.VALUE_GENERATION_GAP_MAX
        );

        double nextTemp = ThreadLocalRandom.current().nextDouble(
                TemperatureSensor.VALUE_MIN,
                TemperatureSensor.VALUE_MAX
        );

        nextTemp = new BigDecimal(nextTemp)
                .setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();

        System.out.println("New Temperature: " + nextTemp + "°F.");

        //noinspection RedundantCast
        ((TemperatureSensor) getSensor()).setData(nextTemp);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                periodicallyGenerateTemperatureValues();
            }
        }, delayForNextValueGeneration);
    }

    private SensorConfig getSensorConfig() {
        return ((SensorConfig) getServerConfig());
    }

    private Sensor getSensor() {
        return ((Sensor) getIoT());
    }
}
