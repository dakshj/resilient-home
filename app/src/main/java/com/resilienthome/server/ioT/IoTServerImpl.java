package com.resilienthome.server.ioT;

import com.resilienthome.enums.IoTType;
import com.resilienthome.model.Address;
import com.resilienthome.model.IoT;
import com.resilienthome.model.config.ServerConfig;
import com.resilienthome.server.ServerImpl;
import com.resilienthome.server.ioT.gateway.GatewayServer;
import com.resilienthome.server.loadbalancer.LoadBalancerServer;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public abstract class IoTServerImpl extends ServerImpl implements IoTServer {

    private final IoT ioT;

    private Address gatewayAddress;

    /**
     * Creates an instance of an IoT using a provided config.
     * <p>
     * Additionally, registers itself remotely to the Gateway, if itself is not a Gateway.
     *
     * @param serverConfig The config used to initialize its IoT server
     * @throws RemoteException Thrown when a Java RMI exception occurs
     */
    protected IoTServerImpl(final ServerConfig serverConfig) throws RemoteException {
        super(serverConfig);
        ioT = createIoT();

        // Register to Load Balancer
        if (getIoT().getIoTType() == IoTType.GATEWAY ||
                getIoT().getIoTType() == IoTType.SENSOR ||
                getIoT().getIoTType() == IoTType.DEVICE) {
            try {
                System.out.println("Registering to Load Balancer...");
                setGatewayAddress(LoadBalancerServer.connect(getServerConfig()
                        .getLoadBalancerAddress())
                        .register(getIoT(), getServerConfig().getAddress()));
                System.out.println("Successfully registered.");
            } catch (RemoteException | NotBoundException e) {
                e.printStackTrace();
                System.out.println("Failed to register!");
            }
        }

        // Register to Assigned Gateway
        if (getIoT().getIoTType() == IoTType.SENSOR ||
                getIoT().getIoTType() == IoTType.DEVICE) {
            try {
                System.out.println("Registering to Gateway...");
                GatewayServer.connect(getGatewayAddress())
                        .register(ioT, getServerConfig().getAddress());
                System.out.println("Successfully registered.");
            } catch (RemoteException | NotBoundException e) {
                e.printStackTrace();
                System.out.println("Failed to register!");
            }
        }
    }

    public abstract IoT createIoT();

    protected IoT getIoT() {
        return ioT;
    }

    protected Address getGatewayAddress() {
        return gatewayAddress;
    }

    private void setGatewayAddress(final Address gatewayAddress) {
        this.gatewayAddress = gatewayAddress;
    }

    protected void raiseRemoteAlarm() {
        System.out.println("Raising Alarm!");
        System.out.println("Informing Gateway...");

        try {
            GatewayServer.connect(getGatewayAddress()).raiseAlarm();
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }
}
