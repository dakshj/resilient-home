package com.resilienthome.model.config;

import com.resilienthome.enums.SensorType;

import java.io.Serializable;

public class SensorConfig extends ServerConfig implements Serializable {

    private SensorType sensorType;

    public SensorType getSensorType() {
        return sensorType;
    }
}
