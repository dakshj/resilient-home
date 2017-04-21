package com.resilienthome;

import com.resilienthome.entrant.Entrant;
import com.resilienthome.enums.ExecutionMode;
import com.resilienthome.model.config.DbConfig;
import com.resilienthome.model.config.DeviceConfig;
import com.resilienthome.model.config.EntrantConfig;
import com.resilienthome.model.config.GatewayConfig;
import com.resilienthome.model.config.SensorConfig;
import com.resilienthome.model.config.ServerConfig;
import com.resilienthome.server.ioT.db.DbServerImpl;
import com.resilienthome.server.ioT.device.DeviceServerImpl;
import com.resilienthome.server.ioT.gateway.GatewayServerImpl;
import com.resilienthome.server.ioT.sensor.SensorServerImpl;
import com.resilienthome.server.loadbalancer.LoadBalancerServerImpl;
import com.resilienthome.util.ConfigReader;

import java.net.UnknownHostException;
import java.rmi.RemoteException;

public class Main {
    /**
     * @param args <p>
     *             args[0]:
     *             <p>
     *             Use "0" to start a Load Balancer Server
     *             <p>
     *             Use "1" to start a Gateway Server
     *             <p>
     *             Use "2" to start a DB Server
     *             <p>
     *             Use "3" to start a Sensor Server
     *             <p>
     *             Use "4" to start a Device Server
     *             <p>
     *             Use "5" to start an Entrant
     *             <p>
     *             <p>
     *             args[1]:
     *             <p>
     *             Configuration JSON file path
     * @throws RemoteException Thrown when a Java RMI Exception occurs
     */
    public static void main(String[] args) throws RemoteException, UnknownHostException {
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("No command-line arguments provided." +
                    " Please refer the JavaDoc to know more on these arguments.");
        }

        ExecutionMode executionMode;
        try {
            executionMode = ExecutionMode.from(Integer.parseInt(args[0]));
        } catch (NumberFormatException ignored) {
            throw new IllegalArgumentException("Execution Mode is invalid.");
        }

        assert executionMode != null;

        String configFilePath = args[1];

        switch (executionMode) {
            case LOAD_BALANCER: {
                ConfigReader<ServerConfig> reader = new ConfigReader<>(ServerConfig.class);
                final ServerConfig serverConfig = reader.read(configFilePath);
                new LoadBalancerServerImpl(serverConfig);
            }
            break;

            case GATEWAY: {
                ConfigReader<GatewayConfig> reader = new ConfigReader<>(GatewayConfig.class);
                final GatewayConfig gatewayConfig = reader.read(configFilePath);
                new GatewayServerImpl(gatewayConfig);
            }
            break;

            case DB: {
                ConfigReader<DbConfig> reader = new ConfigReader<>(DbConfig.class);
                final DbConfig dbConfig = reader.read(configFilePath);
                new DbServerImpl(dbConfig);
            }
            break;

            case SENSOR: {
                ConfigReader<SensorConfig> reader = new ConfigReader<>(SensorConfig.class);
                final SensorConfig sensorConfig = reader.read(configFilePath);
                new SensorServerImpl(sensorConfig);
            }
            break;

            case DEVICE: {
                ConfigReader<DeviceConfig> reader = new ConfigReader<>(DeviceConfig.class);
                final DeviceConfig deviceConfig = reader.read(configFilePath);
                new DeviceServerImpl(deviceConfig);
            }
            break;

            case ENTRANT: {
                ConfigReader<EntrantConfig> reader = new ConfigReader<>(EntrantConfig.class);
                final EntrantConfig entrantConfig = reader.read(configFilePath);
                new Entrant(entrantConfig);
            }
            break;
        }
    }
}
