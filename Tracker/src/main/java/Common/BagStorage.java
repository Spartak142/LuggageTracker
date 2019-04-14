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

    //Login method. Logs in accounts and associates the remote node to the username if name and password are correct, otherwise, asks for a new attempt
    public String login(Client remoteNode, String name, String password) throws RemoteException, AccountException;

    // Adds the bags information including tag number, ownername, person to store the bag, the number of bags and the room number.
    public void addBag(BagsDTO bag) throws RemoteException, RejectedException, BDBException;

    // Lists all bags in the storage
    public List<? extends BagsDTO> listAll(String user) throws RemoteException, BDBException;
    
    // Lists all bags in the storage left by a certain person
    public List<? extends BagsDTO> listAllName(String owner, String user) throws RemoteException, BDBException;
   
    // Lists all bags in the storage taht were taken care of by a user
    public List<? extends BagsDTO> listAllStoredBy(String storedBy, String user) throws RemoteException, BDBException;

    //Updates the data of a given bag both room number and number of bags.
    public Boolean change(int id, String user, int amount, String room) throws RemoteException, RejectedException, BDBException;

    //Updates only room number on a given storage entry
    public Boolean changeRoom(int id, String user, String room) throws RemoteException, RejectedException, BDBException;

    //Updates only number of bags stored in a particular entry
    public Boolean changeNumber(int id, String user, int amount) throws RemoteException, RejectedException, BDBException;

    // Gets bag's data by it's tag number. Adds request to the log file
    public BagsDTO getBag(int id, String user) throws RemoteException, BDBException;

   // Gets bag's data by it's tag number. Does not add the request to the log file (used only to display updated bags after changes are done)
    public BagsDTO getBag(int id) throws RemoteException, BDBException;
  
    // Removes bag from storage
    public Boolean delete(int id, String user) throws RemoteException, RejectedException, BDBException;

    //Logs out the user and removes the connection from the ConnectionManager list.
    public void logout(String user) throws IOException;
}
