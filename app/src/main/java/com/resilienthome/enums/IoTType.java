package com.resilienthome.enums;

public enum IoTType {

    /**
     * Indicates that the IoT type is a Sensor.
     */
    SENSOR,

    /**
     * Indicates that the IoT type is a Device.
     */
    DEVICE,

    /**
     * Indicates that the IoT type is a Gateway.
     */
    GATEWAY,

    /**
     * Indicates that the IoT type is a Database.
     */
    DB;

    public static IoTType from(final int type) {
        switch (type) {
            case 0:
                return SENSOR;

            case 1:
                return DEVICE;

            case 2:
                return GATEWAY;

            case 3:
                return DB;
        }

        return null;
    }
}
