package com.resilienthome.server;

import com.resilienthome.model.config.ServerConfig;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public abstract class ServerImpl implements Server {

    private final ServerConfig serverConfig;

    protected ServerImpl(final ServerConfig serverConfig) throws RemoteException {
        this.serverConfig = serverConfig;

        startServer(getServerConfig().getAddress().getPortNo());
    }

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

    protected ServerConfig getServerConfig() {
        return serverConfig;
    }

    protected abstract String getName();
}
