package com.resilienthome.model.sensor;

import com.resilienthome.enums.IoTType;
import com.resilienthome.enums.SensorType;

import java.io.Serializable;
import java.util.UUID;

public class TemperatureSensor extends Sensor<Double> implements Serializable {

    /**
     * The minimum time (in milliseconds) gap for generating a new Temperature value.
     */
    public static final long VALUE_GENERATION_GAP_MIN = 1000;

    /**
     * The maximum time (in milliseconds) gap for generating a new Temperature value.
     */
    public static final long VALUE_GENERATION_GAP_MAX = 5000;

    /**
     * The minimum Temperature value that can be generated.
     */
    public static final long VALUE_MIN = 65;

    /**
     * The maximum Temperature value that can be generated.
     */
    public static final long VALUE_MAX = 85;

    public TemperatureSensor(final UUID id, final IoTType ioTType, final SensorType sensorType) {
        super(id, ioTType, sensorType);
    }
}
