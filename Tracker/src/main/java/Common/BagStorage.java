package Common;

import Server.model.AccountException;
import Server.integration.BDBException;
import Server.model.RejectedException;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface BagStorage extends Remote {

    public static final String BAGSERVER_NAME_IN_REGISTRY = "bserver";

    // Creates an account with the specified name and password. In the account table
    public void createAccount(String name, String password, String passphrase) throws RemoteException, AccountException;

    //Login Method Returns account if name and password are correct, otherwise, asks for a new attempt
    public String login(Client remoteNode, String name, String password) throws RemoteException, AccountException;

    // Uploads the bags metadata including owner, size, name, write and read permissions
    public void addBag(BagsDTO bag) throws RemoteException, RejectedException, BDBException;

    // Lists all bags in the catalog
    public List<? extends BagsDTO> listAll(String user) throws RemoteException, BDBException;
     // Lists all bags in the catalog
    public List<? extends BagsDTO> listAllName(String owner, String user) throws RemoteException, BDBException;
     // Lists all bags in the catalog
    public List<? extends BagsDTO> listAllStoredBy(String storedBy, String user) throws RemoteException, BDBException;

    //Changes the metadata of a given bag. Doing so notifies the bag owner with a name of the changer and changes done.
    public Boolean change(int id, String user, int amount, String room) throws RemoteException, RejectedException, BDBException;
    //Changes the metadata of a given bag. Doing so notifies the bag owner with a name of the changer and changes done.
    public Boolean changeRoom(int id, String user, String room) throws RemoteException, RejectedException, BDBException;
    //Changes the metadata of a given bag. Doing so notifies the bag owner with a name of the changer and changes done.
    public Boolean changeNumber(int id, String user, int amount) throws RemoteException, RejectedException, BDBException;

    // Gets bag's metadate by it's name. (basically download without user check) Used only by the program.
    public BagsDTO getBag(int id, String user) throws RemoteException, BDBException;

    // Gets bag's metadate by it's name. (basically download without user check) Used only by the program.
    public BagsDTO getBag(int id) throws RemoteException, BDBException;
    // Deletes the bag if allowed

    public Boolean delete(int id, String user) throws RemoteException, RejectedException, BDBException;

    public void logout(String user) throws IOException;
}
