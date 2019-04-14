package Client.startup;

import Client.view.NBInterpreter;
import Common.BagStorage;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Main {
//starts client and invokes the Interpreter which handles the user commands

    public static void main(String[] args) {
        try {
            BagStorage bserver = (BagStorage) Naming.lookup(BagStorage.BAGSERVER_NAME_IN_REGISTRY);
            System.out.println("Welcome to the bag storage service.");
            System.out.println("In order to be able to do anything you should register and login.");
            System.out.println("In order to register you need to have a special admin password currently 'a'.");
            System.out.println("Type 'Help' to see all possible commands");
            new NBInterpreter().start(bserver);
        } catch (NotBoundException | MalformedURLException | RemoteException ex) {
            System.out.println("Could not start bag client.");
        }
    }
}
