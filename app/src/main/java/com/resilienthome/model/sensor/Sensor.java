package com.resilienthome.model.sensor;

import com.resilienthome.enums.IoTType;
import com.resilienthome.enums.SensorType;
import com.resilienthome.model.IoT;

import java.io.Serializable;
import java.util.UUID;

public class Sensor<T> extends IoT implements Serializable {

    private final SensorType sensorType;

    private T data;

    public Sensor(final UUID id, final IoTType ioTType, final SensorType sensorType) {
        super(id, ioTType);
        this.sensorType = sensorType;
    }

    public SensorType getSensorType() {
        return sensorType;
    }

    public T getData() {
        return data;
    }

    public void setData(final T data) {
        this.data = data;
    }
}
