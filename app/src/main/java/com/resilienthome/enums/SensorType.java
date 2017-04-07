package com.resilienthome.enums;

public enum SensorType {

    /**
     * Indicates that the Sensor type is a Temperature Sensor.
     */
    TEMPERATURE,

    /**
     * Indicates that the Sensor type is a Motion Sensor.
     */
    MOTION,

    /**
     * Indicates that the Sensor type is a Door Sensor.
     */
    DOOR,

    /**
     * Indicates that the Sensor type is a Presence Sensor.
     */
    PRESENCE;

    public static SensorType from(final int mode) {
        switch (mode) {
            case 0:
                return TEMPERATURE;

            case 1:
                return MOTION;

            case 2:
                return DOOR;

            case 3:
                return PRESENCE;
        }

        return null;
    }
}
