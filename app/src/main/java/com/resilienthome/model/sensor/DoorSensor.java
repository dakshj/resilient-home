package com.resilienthome.model.sensor;

import com.resilienthome.enums.IoTType;
import com.resilienthome.enums.SensorType;

import java.io.Serializable;
import java.util.UUID;

public class DoorSensor extends Sensor<Boolean> implements Serializable {

    public DoorSensor(final UUID id, final IoTType ioTType, final SensorType sensorType) {
        super(id, ioTType, sensorType);
        setData(false);
    }

    /**
     * Returns whether the door is opened or closed.
     *
     * @return {@code true} if the door is opened;
     * {@code false} otherwise
     */
    @Override
    public Boolean getData() {
        return super.getData();
    }
}
