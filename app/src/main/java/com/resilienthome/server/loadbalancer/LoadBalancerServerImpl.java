package com.resilienthome.server.loadbalancer;

import com.resilienthome.enums.IoTType;
import com.resilienthome.enums.SensorType;
import com.resilienthome.exception.NoRegisteredGatewayException;
import com.resilienthome.model.Address;
import com.resilienthome.model.GatewayAssignment;
import com.resilienthome.model.IoT;
import com.resilienthome.model.config.ServerConfig;
import com.resilienthome.model.sensor.Sensor;
import com.resilienthome.server.ServerImpl;
import com.resilienthome.server.ioT.device.DeviceServer;
import com.resilienthome.server.ioT.gateway.GatewayServer;
import com.resilienthome.server.ioT.sensor.SensorServer;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class LoadBalancerServerImpl extends ServerImpl implements LoadBalancerServer {

    private static final long GATEWAY_PING_DELAY = 3000;

    private final Map<IoT, Address> registeredGateways;
    private final Map<IoT, Address> registeredIoTs;
    private final List<GatewayAssignment> gatewayAssignments;

    public LoadBalancerServerImpl(final ServerConfig serverConfig) throws RemoteException {
        super(serverConfig);
        registeredGateways = new HashMap<>();
        registeredIoTs = new HashMap<>();
        gatewayAssignments = new ArrayList<>();
    }

    @Override
    protected String getName() {
        return NAME;
    }

    @Override
    public Address register(final IoT ioT, final Address address) {
        switch (ioT.getIoTType()) {
            case GATEWAY:
                getRegisteredGateways().put(ioT, address);
                System.out.println("Gateway " + ioT.getId() + " registered.");

                final GatewayAssignment gatewayAssignment = new GatewayAssignment(ioT);
                if (!getGatewayAssignments().contains(gatewayAssignment)) {
                    getGatewayAssignments().add(gatewayAssignment);

                    System.out.println("Starting periodic pinging to Gateway " + ioT.getId() + "...");
                    startPeriodicGatewayPinging(ioT);
                }
                return null;

            case SENSOR:
            case DEVICE:
                if (!getRegisteredIoTs().containsKey(ioT)) {
                    getRegisteredIoTs().put(ioT, address);
                    System.out.println(ioT + " " + ioT.getId() + " registered.");
                    return assignIoTToLeastLoadedGateway(ioT, address);
                }
        }

        return null;
    }

    /**
     * Periodically pings a Gateway every {@value #GATEWAY_PING_DELAY} ms
     * to check if it is alive or not.
     *
     * @param gateway The Gateway to ping
     */
    private void startPeriodicGatewayPinging(final IoT gateway) {
        System.out.println("Pinging Gateway " + gateway.getId() + "...");

        boolean success = false;

        try {
            GatewayServer.connect(getRegisteredGateways().get(gateway)).ping();
            success = true;
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }

        if (success) {
            System.out.println("Ping successful.");

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    startPeriodicGatewayPinging(gateway);
                }
            }, GATEWAY_PING_DELAY);
        } else {
            gatewayDown(gateway);
        }
    }

    private Address assignIoTToLeastLoadedGateway(final IoT ioT, final Address address) {
        if (getRegisteredGateways().isEmpty()) {
            throw new NoRegisteredGatewayException();
        }

        System.out.println("Assigning " + ioT + " " + ioT.getId()
                + " to the least loaded Gateway...");

        // Remove previous Gateway assignment, in case an IoT calls register more than once
        getGatewayAssignments().stream()
                .filter(gatewayAssignment -> gatewayAssignment.containsIoT(ioT))
                .forEach(gatewayAssignment -> {
                    gatewayAssignment.removeIoT(ioT);
                    try {
                        GatewayServer.connect(getRegisteredGateways()
                                .get(gatewayAssignment.getGateway()))
                                .deRegister(ioT);
                    } catch (RemoteException | NotBoundException e) {
                        e.printStackTrace();
                    }
                });

        // Sort the List -> Assign IoT to least loaded Gateway -> Sort the List
        Collections.sort(getGatewayAssignments());
        final GatewayAssignment assignment = getGatewayAssignments().get(0);

        System.out.println("Assigned to Gateway " + assignment.getGateway().getId());
        assignment.addIoT(ioT);
        Collections.sort(getGatewayAssignments());

        System.out.println("Registering " + ioT + " to Gateway "
                + assignment.getGateway().getId() + "...");
        try {
            GatewayServer.connect(getRegisteredGateways().get(assignment.getGateway()))
                    .register(ioT, address);
            System.out.println("Successfully registered.");
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
            System.out.println("Failed to register!");
        }

        return getRegisteredGateways().get(assignment.getGateway());
    }

    /**
     * Removes the Gateway and its IoT assignments, and reassigns them when a Gateway goes down.
     *
     * @param gateway The Gateway that has gone down
     */
    private void gatewayDown(final IoT gateway) {
        System.out.println("Gateway " + gateway.getId() + "is down!");

        getRegisteredGateways().remove(gateway);
        final GatewayAssignment gatewayAssignment = getGatewayAssignments().get(
                getGatewayAssignments().indexOf(new GatewayAssignment(gateway))
        );
        getGatewayAssignments().remove(gatewayAssignment);

        System.out.println("Reassigning IoTs to new Gateways...");
        gatewayAssignment.getIoTs().forEach(ioT -> {
            final Address gatewayAddress =
                    assignIoTToLeastLoadedGateway(ioT, getRegisteredIoTs().get(ioT));

            // Send the updated Gateway Address to the IoT
            switch (ioT.getIoTType()) {
                case SENSOR:
                    try {
                        SensorServer.connect(getRegisteredIoTs().get(ioT))
                                .setGatewayAddress(gatewayAddress);
                    } catch (RemoteException | NotBoundException e) {
                        e.printStackTrace();
                    }
                    break;

                case DEVICE:
                    try {
                        DeviceServer.connect(getRegisteredIoTs().get(ioT))
                                .setGatewayAddress(gatewayAddress);
                    } catch (RemoteException | NotBoundException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        });
    }

    private Map<IoT, Address> getRegisteredGateways() {
        return registeredGateways;
    }

    private Map<IoT, Address> getRegisteredIoTs() {
        return registeredIoTs;
    }

    private List<GatewayAssignment> getGatewayAssignments() {
        return gatewayAssignments;
    }

    @Override
    public Map<IoT, Address> fetchRegisteredIoTs() throws RemoteException {
        return getRegisteredIoTs();
    }

    /**
     * Calls {@link GatewayServer#entrantExecutionFinished(long)} for all registered Gateways.
     *
     * @throws RemoteException Thrown when a Java RMI exception occurs
     */
    @Override
    public void entrantExecutionFinished() throws RemoteException {
        final long time = System.currentTimeMillis();

        getRegisteredGateways().values()
                .forEach(address -> {
                    try {
                        GatewayServer.connect(address).entrantExecutionFinished(time);
                    } catch (RemoteException | NotBoundException e) {
                        e.printStackTrace();
                    }
                });

        resetAllPresenceSensorsToInactive();
    }

    @Override
    public boolean isRemotePresenceSensorActivated() throws RemoteException {
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

    private void resetAllPresenceSensorsToInactive() {
        getRegisteredIoTs().keySet().stream()
                .filter(ioT -> ioT.getIoTType() == IoTType.SENSOR)
                .map(ioT -> ((Sensor) ioT))
                .filter(sensor -> sensor.getSensorType() == SensorType.PRESENCE)
                .map(sensor -> getRegisteredIoTs().get(sensor))
                .forEach(address -> new Thread(() -> {
                    try {
                        SensorServer.connect(address).setPresenceServerActivated(false);
                    } catch (RemoteException | NotBoundException e) {
                        e.printStackTrace();
                    }
                }).start());
    }

    @Override
    public void broadcastStateToAllGateways(final IoT senderGateway, final long time,
            final IoT reportingIoT) throws RemoteException {
        getRegisteredGateways().keySet().stream()
                .filter(gateway -> !gateway.equals(senderGateway))
                .forEach(gateway -> {
                    final Address address = getRegisteredGateways().get(gateway);

                    try {
                        GatewayServer.connect(address)
                                .reportState(time, reportingIoT, false);
                    } catch (RemoteException | NotBoundException e) {
                        e.printStackTrace();
                    }
                });
    }
}
