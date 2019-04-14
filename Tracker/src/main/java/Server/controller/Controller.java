package Server.controller;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import Common.*;
import Server.model.*;
import Server.integration.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Implementations of the server's remote methods.
public class Controller extends UnicastRemoteObject implements BagStorage {

    private final BagsDAO bagsDB;
    private final ConnectionManager connectedClients = new ConnectionManager();
    private final String adminPassword = "a";
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    private final DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern("MMddHHmmss");
    private final String date = getDate();
    private final File log = new File("src\\main\\java\\Server\\log" + date + ".txt");

    public Controller(String datasource, String dbms) throws RemoteException, BDBException {

        super();
        System.out.println("Successfully called super");
        bagsDB = new BagsDAO(dbms, datasource);
        System.out.println("successfully created BagsDAO");
    }

    
    //Creates account in the database if username is not used yet and the correct admin password is given
    @Override
    public synchronized void createAccount(String holderName, String password, String passphrase) throws AccountException {
        String acctExistsMsg = "Account for: " + holderName + " already exists";
        String failureMsg = "Could not create account for: " + holderName;
        String noRightsMsg = "You need an administrative password to create an account";
        try {
            if (bagsDB.findAccountByName(holderName) != null) {
                writeToLog("Someone tried to create an accoount for '" + holderName + "' but it already exists.");
                failureMsg = failureMsg + "   " + acctExistsMsg;
                throw new AccountException(failureMsg);
            } else if (!passphrase.equals(adminPassword) || passphrase.equals("")) {
                writeToLog("Someone tried to create an accoount for '" + holderName + "' without the administrative password.");
                failureMsg = failureMsg + "   " + noRightsMsg;
                throw new AccountException(failureMsg);
            } else {
                writeToLog("The account for '" + holderName + "' has been created");
                bagsDB.createAccount(new Account(holderName, password));
            }
        } catch (Exception e) {
            throw new AccountException(failureMsg, e);
        }
    }

    //Logsin the user if the password is correct and the login is not used by another client
    @Override
    public synchronized String login(Client remoteNode, String holderName, String password) throws AccountException {
        String errMsg = "Could not search for account.";
        if (holderName == null) {
            return null;
        }

        try {
            String correctPassword = bagsDB.findAccountByName(holderName).getPassword();
            if (correctPassword.equals(password)) {
                if (connectedClients.isOnline(holderName)) {
                    writeToLog("Someone tried to login to '" + holderName + "' that is already online");
                    errMsg = "The user is already logged in";
                    throw new AccountException(errMsg);
                } else {
                    writeToLog(holderName + " has successfully logged in");
                    String participantId = connectedClients.createParticipant(remoteNode, holderName);
                    return participantId;
                }
            } else {
                writeToLog("Someone tried to login to '" + holderName + "' but username or password were wrong");
                errMsg = ("Incorrect username or password");
                throw new AccountException(errMsg);
            }
        } catch (Exception e) {
            throw new AccountException(errMsg, e);
        }

    }

    //Logs out a user
    @Override
    public synchronized void logout(String user) throws IOException {
        writeToLog(user + "has successfully logged out.");
        connectedClients.disconnect(user);

    }

    //Request to list all bags in the Storage Room
    @Override
    public synchronized List<? extends BagsDTO> listAll(String user) throws BDBException {
        try {
            writeToLog("Request to see all stored bags has been made by " + user);
            return bagsDB.findAllBags();
        } catch (Exception e) {
            throw new BDBException("Unable to list accounts.", e);
        }
    }
    
    //Lists all bags in the Storage room left by a particular guest.
    @Override
    public List<? extends BagsDTO> listAllName(String owner, String user) throws RemoteException, BDBException {
               try {
            writeToLog("Request to see all bags stored for: '"+ owner+ "' has been made by " + user);
            return bagsDB.findAllByOwner(owner);
        } catch (Exception e) {
            throw new BDBException("Unable to list accounts.", e);
        }
    }

    //Lists all bags in the Storage room stored by a particular user
    @Override
    public List<? extends BagsDTO> listAllStoredBy(String storedBy, String user) throws RemoteException, BDBException {
         try {
            writeToLog("Request to see all bags stored by: '"+ storedBy+ "' has been made by " + user);
            return bagsDB.findAllByOwner(storedBy);
        } catch (Exception e) {
            throw new BDBException("Unable to list accounts.", e);
        }
    }

    //Request to add bag to the Storage Room 
    @Override
    public synchronized void addBag(BagsDTO bags) throws RemoteException, RejectedException, BDBException {
        int id = bags.getId();
        String failureMsg = "Could not add the following bag: " + id;
        try {
            if (bagsDB.findBagsById(id) != null) {
                writeToLog(bags.getStoredBy() + " tried to store luggage with the existing number." + bags.getId());
                failureMsg = "Bag with the tag: '" + id + "' already exists";
                throw new BDBException(failureMsg);
            }
            writeToLog("Bag with the number " + id + " has been successfully stored by " + bags.getStoredBy() + ". For:" + bags.getOwnerName());
            bagsDB.storeBags(bags);
        } catch (Exception e) {
            throw new BDBException(failureMsg, e);
        }
    }

    //Request for information about a particular bag in the Storage Room
    @Override
    public synchronized BagsDTO getBag(int id, String user) throws BDBException {
        String errMsg = "Could not search for the bags.";
        if (id == 0) {
            return null;
        }
        try {
            BagsDTO bag = bagsDB.findBagsById(id);
            writeToLog("Bag with the tag number " + id + "has been successfully fetched by: '" + user + "'.");
            return bag;
        } catch (Exception e) {
            throw new BDBException(errMsg, e);
        }
    }

    //Request for information about a particular bag in the Storage Room without witing to log used to check the bag right after updating it 
    @Override
    public synchronized BagsDTO getBag(int id) throws BDBException {
        String errMsg = "Could not search for the bags.";
        if (id == 0) {
            return null;
        }
        try {
            BagsDTO bag = bagsDB.findBagsById(id);
            return bag;
        } catch (Exception e) {
            throw new BDBException(errMsg, e);
        }
    }

    //  Method to update info about the bag. Note that submitting a 0 as updated number of bags will not do anything
    @Override
    public synchronized Boolean change(int id, String user, int size, String room) throws RemoteException, RejectedException, BDBException {
        String failureMsg = "Could not change the following bag: " + id;
        Boolean changed = false;
        try {
            BagsDTO bagToUpdate = bagsDB.findBagsById(id);
            if (bagToUpdate != null) {
                if (size > 0) {
                    bagsDB.changeBagAmount(id, size);
                    writeToLog("Bag with the number " + id + " has been successfully updated by: '" + user + "'. New number of bags: " + size);
                    changed = true;
                }
                if (room != null) {
                    bagsDB.changeRoom(id, room);
                    writeToLog("Bag with the number " + id + " has been successfully updated by: '" + user + "' . New room number: " + room);
                    changed = true;
                }
            } else {
                writeToLog("Bag with the number " + id + " has failed to update by '" + user + "'. The bag is alrady fetched or was not added yet.");
                throw new BDBException("Bag has already been fetched or never added");
            }

        } catch (Exception e) {
            throw new BDBException(failureMsg, e);
        }
        return changed;
    }

      //  Method to update the amount of bags associated with a certain tag number. 
    @Override
    public synchronized Boolean changeNumber(int id, String user, int size) throws RemoteException, RejectedException, BDBException {
        String failureMsg = "Could not change the following bag: " + id;
        Boolean changed = false;
        try {
            BagsDTO bagToUpdate = bagsDB.findBagsById(id);
            if (bagToUpdate != null) {
                if (size > 0) {
                    bagsDB.changeBagAmount(id, size);
                    writeToLog("Bag with the number " + id + " has been successfully updated by: '" + user + "'. New number of bags: " + size);
                    changed = true;
                }
            } else {
                writeToLog("Bag with the number " + id + " has failed to update by '" + user + "'. The bag is alrady fetched or was not added yet.");
                throw new BDBException("Bag has already been fetched or never added");
            }

        } catch (Exception e) {
            throw new BDBException(failureMsg, e);
        }
        return changed;
    }

     //  Method to update the room number of bags associated with a certain tag number. 
    @Override
    public synchronized Boolean changeRoom(int id, String user, String room) throws RemoteException, RejectedException, BDBException {
        String failureMsg = "Could not change the following bag: " + id;
        Boolean changed = false;
        try {
            BagsDTO bagToUpdate = bagsDB.findBagsById(id);
            if (bagToUpdate != null) {
                if (room != null) {
                    bagsDB.changeRoom(id, room);
                    writeToLog("Bag with the number " + id + " has been successfully updated by: '" + user + "' . New room number: " + room);
                    changed = true;
                }
            } else {
                writeToLog("Bag with the number " + id + " has failed to update by '" + user + "'. The bag is alrady fetched or was not added yet.");
                throw new BDBException("Bag has already been fetched or never added");
            }

        } catch (Exception e) {
            throw new BDBException(failureMsg, e);
        }
        return changed;
    }

    //Method to remove the bag from the database
    @Override
    public synchronized Boolean delete(int id, String user) throws RemoteException, RejectedException, BDBException {
        Boolean deleted = false;
        String failureMsg = "The following tagNumber does not have any luggage associated with it: " + id;
        try {
            BagsDTO bagToFetch = bagsDB.findBagsById(id);
            bagsDB.removeBag(bagToFetch);
            writeToLog(" Bag with the number " + id + " has been removed by: " + user);
            deleted = true;
        } catch (Exception e) {
            throw new BDBException(failureMsg, e);
        }
        return deleted;
    }

    //Method to write to the log file
    private void writeToLog(String info) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(log, true));
        writer.append(getDate() + " " + info);
        writer.newLine();
        writer.close();

    }
    
//Retrieves current date and time for the log (includes seconds)
    private String getDate() {
        LocalDateTime now = LocalDateTime.now();
        String time = dtf2.format(now);
        return time;
    }


}
