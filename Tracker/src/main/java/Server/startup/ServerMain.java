package Server.startup;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import Common.BagStorage;
import Server.controller.Controller;
import Server.integration.BDBException;

//Starts the Bag Server
public class ServerMain {

    private static final String USAGE = "java bserverjdbc.Server [bserver name in rmi registry] "
            + "[bagserver database name] [dbms: derby or mysql]";
    private String serverName = BagStorage.BAGSERVER_NAME_IN_REGISTRY;
    private String datasource = "Bservertrial1";
    private String dbms = "derby";

    public static void main(String[] args) {

        try {
            ServerMain server = new ServerMain();
            server.parseCommandLineArgs(args);
            server.startRMIServant();
            System.out.println("Bag Storage Service  started.");
        } catch (RemoteException | MalformedURLException | BDBException e) {
            System.out.println("Failed to start Bag Storage Service.");
        }
    }

    private void startRMIServant() throws RemoteException, MalformedURLException, BDBException {
        try {
            LocateRegistry.getRegistry().list();
        } catch (RemoteException noRegistryRunning) {
            LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
        }
        Controller contr = new Controller(datasource, dbms);
        Naming.rebind(serverName, contr);
    }

    private void parseCommandLineArgs(String[] args) {
        if (args.length > 3 || (args.length > 0 && args[0].equalsIgnoreCase("-h"))) {
            System.out.println(USAGE);
            System.exit(1);
        }

        if (args.length > 0) {
            serverName = args[0];
        }

        if (args.length > 1) {
            datasource = args[1];
        }

        if (args.length > 2) {
            dbms = args[2];
        }
    }
}
