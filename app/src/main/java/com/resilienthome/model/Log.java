package com.resilienthome.model;

import com.resilienthome.enums.DeviceType;
import com.resilienthome.enums.IoTType;
import com.resilienthome.enums.LogType;
import com.resilienthome.enums.SensorType;

import java.io.Serializable;
import java.util.UUID;

public class Log implements Serializable {

    private final long time;
    private final LogType logType;
    private final UUID id;
    private final IoTType ioTType;
    private final SensorType sensorType;
    private final DeviceType deviceType;
    private final String message;

    public Log(final long time, final LogType logType, final UUID id, final IoTType ioTType,
            final SensorType sensorType, final DeviceType deviceType, final String message) {
        this.time = time;
        this.logType = logType;
        this.id = id;
        this.ioTType = ioTType;
        this.sensorType = sensorType;
        this.deviceType = deviceType;
        this.message = message;
    }

    public long getTime() {
        return time;
    }

    public LogType getLogType() {
        return logType;
    }

    public UUID getId() {
        return id;
    }

    public IoTType getIoTType() {
        return ioTType;
    }

    public SensorType getSensorType() {
        return sensorType;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public String getMessage() {
        return message;
    }
}
