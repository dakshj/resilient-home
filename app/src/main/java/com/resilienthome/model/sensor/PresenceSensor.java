package com.resilienthome.model.sensor;

import com.resilienthome.enums.IoTType;
import com.resilienthome.enums.SensorType;

import java.io.Serializable;
import java.util.UUID;

public class PresenceSensor extends Sensor<Boolean> implements Serializable {

    public PresenceSensor(final UUID id, final IoTType ioTType, final SensorType sensorType) {
        super(id, ioTType, sensorType);
        setData(false);
    }

    /**
     * Returns whether the Presence Sensor is activated or not.
     *
     * @return {@code true} if the Presence Sensor is activate
     * (i.e., the Entrant is an authorized user);
     * {@code false} otherwise
     */
    @Override
    public Boolean getData() {
        return super.getData();
    }
}
