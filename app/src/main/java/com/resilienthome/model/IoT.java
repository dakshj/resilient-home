package com.resilienthome.model;

import com.resilienthome.enums.IoTType;
import com.resilienthome.model.sensor.Sensor;

import java.io.Serializable;
import java.util.UUID;

public class IoT implements Serializable {

    private final UUID id;
    private final IoTType ioTType;

    public IoT(final UUID id, final IoTType ioTType) {
        this.id = id;
        this.ioTType = ioTType;
    }

    public UUID getId() {
        return id;
    }

    public IoTType getIoTType() {
        return ioTType;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final IoT ioT = (IoT) o;

        return id != null ? id.equals(ioT.id) : ioT.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        switch (getIoTType()) {
            case GATEWAY:
            case DB:
                return getIoTType().name();

            case DEVICE:
                return ((Device) this).getDeviceType().name();

            case SENSOR:
                return ((Sensor) this).getSensorType().name();
        }

        return null;
    }
}
