package com.resilienthome.model;

import com.resilienthome.enums.DeviceType;
import com.resilienthome.enums.IoTType;

import java.io.Serializable;
import java.util.UUID;

public class Device extends IoT implements Serializable {

    private final DeviceType deviceType;

    private boolean state;

    public Device(final UUID id, final IoTType ioTType, final DeviceType deviceType) {
        super(id, ioTType);
        this.deviceType = deviceType;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public boolean getState() {
        return state;
    }

    public void setState(final boolean state) {
        this.state = state;
    }
}
