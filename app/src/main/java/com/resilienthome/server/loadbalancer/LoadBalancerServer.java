package com.resilienthome.server.loadbalancer;

import com.resilienthome.enums.IoTType;
import com.resilienthome.model.Address;
import com.resilienthome.model.IoT;
import com.resilienthome.server.ioT.gateway.GatewayServer;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;

public interface LoadBalancerServer extends Remote {

    String NAME = "Load Balancer Server";

    /**
     * Establishes a connection with a {@link LoadBalancerServer}.
     *
     * @param address The address of the {@link LoadBalancerServer}
     * @return An instance of the connected {@link LoadBalancerServer}, connected remotely via Java RMI
     * @throws RemoteException   Thrown when a Java RMI exception occurs
     * @throws NotBoundException Thrown when the remote binding does not exist in the {@link Registry}
     */
    static LoadBalancerServer connect(final Address address)
            throws RemoteException, NotBoundException {
        final Registry registry = LocateRegistry.getRegistry(address.getHost(), address.getPortNo());
        return (LoadBalancerServer) registry.lookup(NAME);
    }

    /**
     * Registers an IoT with the load balancer.
     *
     * @param ioT     The IoT which needs to be registered
     * @param address The address of the IoT Server
     * @return The address of the {@link GatewayServer} assigned to this IoT, if this IoT is not
     * of the {@link IoTType#GATEWAY} type; else {@code null}
     * @throws RemoteException Thrown when a Java RMI Exception occurs
     */
    Address register(final IoT ioT, final Address address) throws RemoteException;

    /**
     * Returns the {@link Map} of registered IoTs to the calling IoT server.
     *
     * @return The {@link Map} of registered IoTs
     * @throws RemoteException Thrown when a Java RMI exception occurs
     */
    Map<IoT, Address> fetchRegisteredIoTs() throws RemoteException;

    /**
     * Notifies that the Entrant has finished its execution.
     *
     * @throws RemoteException Thrown when a Java RMI exception occurs
     */
    void entrantExecutionFinished() throws RemoteException;

    /**
     * Checks whether a Presence Sensor located on another server is activated or not.
     *
     * @return {@code true} if the remote Presence Sensor is activated;
     * {@code false} otherwise
     */
    boolean isRemotePresenceSensorActivated();

    /**
     * Broadcasts the reported state of one Gateway to all other Gateways
     *
     * @param senderGateway The Gateway where the state reporting originated
     * @param time          The time at which the state was originally reported
     */
    void broadcastStateToAllGateways(IoT senderGateway, final long time);
}
