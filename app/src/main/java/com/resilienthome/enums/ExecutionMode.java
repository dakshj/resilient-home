package com.resilienthome.enums;

public enum ExecutionMode {

    /**
     * Indicates that a Gateway Server needs to be started.
     */
    GATEWAY,

    /**
     * Indicates that a Database Server needs to be started.
     */
    DB,

    /**
     * Indicates that a Sensor Server needs to be started.
     */
    SENSOR,

    /**
     * Indicates that a Device Server needs to be started.
     */
    DEVICE,

    /**
     * Indicates that a User Server needs to be started.
     */
    ENTRANT;

    public static ExecutionMode from(final int mode) {
        switch (mode) {
            case 0:
                return GATEWAY;

            case 1:
                return DB;

            case 2:
                return SENSOR;

            case 3:
                return DEVICE;

            case 4:
                return ENTRANT;
        }

        return null;
    }
}
