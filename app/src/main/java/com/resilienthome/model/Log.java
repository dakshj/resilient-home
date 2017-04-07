package com.resilienthome.model;

import com.resilienthome.enums.DeviceType;
import com.resilienthome.enums.IoTType;
import com.resilienthome.enums.LogType;
import com.resilienthome.enums.SensorType;

import java.io.Serializable;
import java.util.UUID;

public class Log implements Serializable {

    private static final char DELIMITER = '`';

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

    @Override
    public String toString() {
        return "" +
                time + DELIMITER +
                logType + DELIMITER +
                id + DELIMITER +
                ioTType + DELIMITER +
                sensorType + DELIMITER +
                deviceType + DELIMITER +
                message;
    }

    /**
     * Reads a line of log text and converts it into a Log object.
     *
     * @param line The log line that needs to be converted into a Log object
     * @return The Log object created by reading the line of log text
     */
    public static Log from(final String line) {
        final String[] tokens = line.split(String.valueOf(DELIMITER));
        return new Log(
                Long.parseLong(tokens[0]),
                LogType.from(Integer.parseInt(tokens[1])),
                tokens[2].equals("null") ? null : UUID.fromString(tokens[2]),
                tokens[3].equals("null") ? null : IoTType.from(Integer.parseInt(tokens[3])),
                tokens[4].equals("null") ? null : SensorType.from(Integer.parseInt(tokens[4])),
                tokens[5].equals("null") ? null : DeviceType.from(Integer.parseInt(tokens[5])),
                tokens[6]
        );
    }
}
