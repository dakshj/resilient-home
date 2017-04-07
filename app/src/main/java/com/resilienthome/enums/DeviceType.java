package com.resilienthome.enums;

public enum DeviceType {

    /**
     * Indicates that the Device type is a Smart Bulb.
     */
    BULB,

    /**
     * Indicates that the Device type is a Smart Outlet.
     */
    OUTLET;

    public static DeviceType from(final int mode) {
        switch (mode) {
            case 0:
                return BULB;

            case 1:
                return OUTLET;
        }

        return null;
    }
}
