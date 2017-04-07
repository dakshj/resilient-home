package com.resilienthome.model.config;

import com.resilienthome.enums.DeviceType;

import java.io.Serializable;

public class DeviceConfig extends ServerConfig implements Serializable {

    private DeviceType deviceType;

    public DeviceType getDeviceType() {
        return deviceType;
    }
}
