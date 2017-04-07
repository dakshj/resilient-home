package com.resilienthome.ioT.device;

import com.resilienthome.enums.IoTType;
import com.resilienthome.ioT.IoTServerImpl;
import com.resilienthome.ioT.gateway.GatewayServer;
import com.resilienthome.model.Device;
import com.resilienthome.model.IoT;
import com.resilienthome.model.config.DeviceConfig;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.UUID;

public class DeviceServerImpl extends IoTServerImpl implements DeviceServer {

    public DeviceServerImpl(final DeviceConfig deviceConfig) throws RemoteException {
        super(deviceConfig, true);
    }

    @Override
    public IoT createIoT() {
        return new Device(UUID.randomUUID(), IoTType.DEVICE, getDeviceConfig().getDeviceType());
    }

    private DeviceConfig getDeviceConfig() {
        return ((DeviceConfig) getServerConfig());
    }

    private Device getDevice() {
        return ((Device) getIoT());
    }

    @Override
    protected String getName() {
        return NAME;
    }

    @Override
    public void queryState() throws RemoteException {
        try {
            GatewayServer.connect(getDeviceConfig().getGatewayAddress())
                    .reportState(getDevice());
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setState(final boolean state) throws RemoteException {
        if (getDevice().getState() == state) {
            return;
        }

        getDevice().setState(state);
        System.out.println(getDevice() + " switched "
                + (getDevice().getState() ? "on" : "off") + ".");

        queryState();
    }

    @Override
    public void toggleState() throws RemoteException {
        getDevice().setState(!getDevice().getState());
        queryState();
    }
}
