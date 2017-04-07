package com.resilienthome.ioT;

import com.resilienthome.enums.IoTType;
import com.resilienthome.enums.SensorType;
import com.resilienthome.ioT.db.DbServer;
import com.resilienthome.ioT.device.DeviceServer;
import com.resilienthome.ioT.gateway.GatewayServer;
import com.resilienthome.ioT.sensor.SensorServer;
import com.resilienthome.model.Address;
import com.resilienthome.model.IoT;
import com.resilienthome.model.config.ServerConfig;
import com.resilienthome.model.sensor.Sensor;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public abstract class IoTServerImpl implements IoTServer {

    private final ServerConfig serverConfig;
    private final IoT ioT;

    private Map<IoT, Address> registeredIoTs;

    /**
     * Creates an instance of an IoT using a provided config.
     * <p>
     * Additionally, registers itself remotely to the Gateway, if itself is not a Gateway.
     *
     * @param serverConfig              The config used to initialize its IoT server
     * @param registerRemotelyToGateway If {@code true} then register remotely to Gateway;
     *                                  else register locally to its contained
     *                                  {@link #registeredIoTs}.
     * @throws RemoteException Thrown when a Java RMI exception occurs
     */
    protected IoTServerImpl(final ServerConfig serverConfig, final boolean registerRemotelyToGateway)
            throws RemoteException {
        this.serverConfig = serverConfig;
        ioT = createIoT();
        registeredIoTs = new HashMap<>();

        startServer(serverConfig.getAddress().getPortNo());

        if (registerRemotelyToGateway) {
            System.out.println("Registering to Gateway...");
            try {
                GatewayServer.connect(serverConfig.getGatewayAddress())
                        .register(ioT, serverConfig.getAddress());
            } catch (RemoteException | NotBoundException e) {
                e.printStackTrace();
            }
            System.out.println("Successfully registered.");
        } else {
            getRegisteredIoTs().put(getIoT(), getServerConfig().getAddress());
        }
    }

    public abstract IoT createIoT();

    /**
     * Starts the DB Server on the provided port number.
     *
     * @param portNo The port number to start the DB Server on
     * @throws RemoteException Thrown when a Java RMI exception occurs
     */
    private void startServer(final int portNo) throws RemoteException {
        UnicastRemoteObject.exportObject(this, portNo);
        final Registry registry = LocateRegistry.createRegistry(portNo);
        registry.rebind(getName(), this);

        System.out.println("Server started.");
    }

    protected abstract String getName();

    protected ServerConfig getServerConfig() {
        return serverConfig;
    }

    protected IoT getIoT() {
        return ioT;
    }

    protected Map<IoT, Address> getRegisteredIoTs() {
        return registeredIoTs;
    }

    @Override
    public void setRegisteredIoTs(final Map<IoT, Address> registeredIoTs) throws RemoteException {
        System.out.println("Received Map of Registered IoTs from Gateway.");

        this.registeredIoTs = registeredIoTs;
    }

    /**
     * Checks whether a Presence Sensor located on another server is activated or not.
     *
     * @return {@code true} if the remote Presence Sensor is activated;
     * {@code false} otherwise
     */
    protected boolean isRemotePresenceSensorActivated() {
        final boolean[] authorizedUser = new boolean[1];

        getRegisteredIoTs().keySet().stream()
                .filter(ioT -> ioT.getIoTType() == IoTType.SENSOR)
                .map(ioT -> ((Sensor) ioT))
                .filter(sensor -> sensor.getSensorType() == SensorType.PRESENCE)
                .map(sensor -> getRegisteredIoTs().get(sensor))
                .forEach((Address address) -> {
                    try {
                        authorizedUser[0] = SensorServer.connect(address)
                                .isPresenceSensorActivated();
                    } catch (RemoteException | NotBoundException e) {
                        e.printStackTrace();
                    }
                });

        return authorizedUser[0];
    }

    protected void raiseRemoteAlarm() {
        System.out.println("Raising Alarm!");
        System.out.println("Informing Gateway...");

        try {
            GatewayServer.connect(getServerConfig().getGatewayAddress()).raiseAlarm();
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Broadcasts the {@link Map} of all registered IoTs to each IoT.
     */
    private void broadcastRegisteredIoTs() {
        // Send to DB
        getRegisteredIoTs().keySet().stream()
                .filter(ioT1 -> ioT1.getIoTType() == IoTType.DB)
                .map(ioT1 -> getRegisteredIoTs().get(ioT1))
                .forEach(address -> new Thread(() -> {
                    try {
                        DbServer.connect(address).setRegisteredIoTs(getRegisteredIoTs()
                        );
                    } catch (RemoteException | NotBoundException e) {
                        e.printStackTrace();
                    }
                }).start());

        // Send to all Sensors
        getRegisteredIoTs().keySet().stream()
                .filter(ioT1 -> ioT1.getIoTType() == IoTType.SENSOR)
                .map(ioT1 -> getRegisteredIoTs().get(ioT1))
                .forEach(address -> new Thread(() -> {
                    try {
                        SensorServer.connect(address).setRegisteredIoTs(getRegisteredIoTs()
                        );
                    } catch (RemoteException | NotBoundException e) {
                        e.printStackTrace();
                    }
                }).start());

        // Send to all Devices
        getRegisteredIoTs().keySet().stream()
                .filter(ioT1 -> ioT1.getIoTType() == IoTType.DEVICE)
                .map(ioT1 -> getRegisteredIoTs().get(ioT1))
                .forEach(address -> new Thread(() -> {
                    try {
                        DeviceServer.connect(address).setRegisteredIoTs(getRegisteredIoTs()
                        );
                    } catch (RemoteException | NotBoundException e) {
                        e.printStackTrace();
                    }
                }).start());
    }
}
