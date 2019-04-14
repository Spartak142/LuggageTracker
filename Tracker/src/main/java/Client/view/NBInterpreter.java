package Client.view;

import java.util.List;
import java.util.Scanner;
import Common.BagStorage;
import Common.AccountDTO;
import Common.Client;
import Common.Bags;
import Common.BagsDTO;
import Server.integration.BDBException;
import Server.model.AccountException;
import Server.model.RejectedException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class NBInterpreter implements Runnable {

    private static final String PROMPT = "Type your command: ";
    private final Scanner console = new Scanner(System.in);
    private final StdOut outMgr = new StdOut();
    private BagStorage bserver;
    private boolean rCmds = false;
    private String activeUser = null;
    private String[] commandList = new String[15];
    /*Keeps track if someone is logged in. The main reason why this check is done on the client side
    for most of the commands is to improve server performance, since it will not have to even deal with the requests
    from non-logged in users. */
    private boolean lIn = false;
    private String nBID;
    private final Client myRemoteObj;

    public NBInterpreter() throws RemoteException {
        myRemoteObj = new ClientOutput();
    }

    // Starts the interpreter
    public void start(BagStorage bserver) {
        this.bserver = bserver;
        if (rCmds) {
            return;
        }
        rCmds = true;
        new Thread(this).start();
    }

    /**
     * Interprets and performs user commands, by making appropriate calls to the
     * server. Some of the used methods are defined below
     */
    @Override
    public void run() {
        AccountDTO acct = null;
        while (rCmds) {
            try {
                CmdLine cmdLine = new CmdLine(readNextLine());
                switch (cmdLine.getCmd()) {
                    case HELP:
                        defineCommandUsage();
                        break;
                    case QUIT:
                        bserver.logout(activeUser);
                        activeUser = null;
                        rCmds = false;
                        break;
                    case REGISTER:
                        bserver.createAccount(cmdLine.getParameter(0), cmdLine.getParameter(1), cmdLine.getParameter(2));
                        break;
                    case LOGIN:
                        login(cmdLine.getParameter(0), cmdLine.getParameter(1), acct);
                        break;
                    case LISTALL:
                        listAll();
                        break;
                    case LISTOWNER:
                        listOwner(cmdLine.getParameter(0), cmdLine.getParameter(1));
                        break;
                    case LISTSTOREDBY:
                        listStoredBy(cmdLine.getParameter(0));
                        break;
                    case STORE:
                        store(cmdLine.getUserInput());
                        break;
                    case CHECK:
                        check(cmdLine.getParameter(0));
                        break;
                    case CHANGE:
                        change(cmdLine.getParameter(0), cmdLine.getParameter(1), cmdLine.getParameter(2));
                        break;
                    case CHANGER:
                        updateRoom(cmdLine.getUserInput());
                        break;
                    case CHANGEN:
                        updateNumber(cmdLine.getParameter(0), cmdLine.getParameter(1));
                        break;
                    case FETCH:
                        fetch(cmdLine.getParameter(0));
                        break;
                    case LOGOUT:
                        bserver.logout(activeUser);
                        activeUser = null;
                        lIn = false;
                        break;
                    default:
                        outMgr.println("illegal command");
                }
            } catch (Exception e) {
                outMgr.println("Operation failed");
                outMgr.println(e.getMessage());
            }
        }
    }

    private String readNextLine() {
        outMgr.print(PROMPT);
        return console.nextLine();
    }

    //Login request
    private void login(String username, String password, AccountDTO acct) throws RemoteException, AccountException {
        if (lIn) {
            outMgr.println("Already logged in as " + activeUser);
            return;
        }
        nBID = bserver.login(myRemoteObj, username, password);
        if (nBID != null) {
            activeUser = username;
            lIn = true;
        }
    }
    
    // Request to list all stored bags
    private void listAll() throws RemoteException, BDBException {
        if (lIn) {
            List<? extends BagsDTO> bags = bserver.listAll(activeUser);
            for (BagsDTO bag : bags) {
                outMgr.println(bag.getId() + ": " + bag.getOwnerName() + ": " + bag.getNumber() + ": " + bag.getRoom() + ": Stored by: " + bag.getStoredBy());
            }
        } else {
            outMgr.println("You have to be logged in to view stored bags");
        }
    }
    
    //Request to list all bags stored under certain ownername
    private void listOwner(String name, String surname) throws RemoteException, BDBException {
        if (lIn) {
            String owner = name + " " + surname;
            List<? extends BagsDTO> bags = bserver.listAllName(owner, activeUser);
            for (BagsDTO bag : bags) {
                outMgr.println(bag.getId() + ": " + bag.getOwnerName() + ": " + bag.getNumber() + ": " + bag.getRoom() + ": Stored by: " + bag.getStoredBy());
            }
        } else {
            outMgr.println("You have to be logged in to view stored bags");
        }
    }

     //Request to list all bags stored by a certain user
    private void listStoredBy(String storedBy) throws RemoteException, BDBException {
        if (lIn) {
            List<? extends BagsDTO> bags = bserver.listAllStoredBy(storedBy, activeUser);
            for (BagsDTO bag : bags) {
                outMgr.println(bag.getId() + ": " + bag.getOwnerName() + ": " + bag.getNumber() + ": " + bag.getRoom() + ": Stored by: " + bag.getStoredBy());
            }
        } else {
            outMgr.println("You have to be logged in to view stored bags");
        }
    }

    //Request to store a bag
    private void store(String input) throws RemoteException, BDBException, RejectedException {
        if (lIn) {
            String name, room;
            int amount;
            String[] params = input.split(" ");
            switch (params.length) {
                case 5:
                    name = params[1] + " " + params[2];
                    amount = Integer.parseInt(params[3]);
                    room = params[4];
                    bserver.addBag(new Bags(name, activeUser, amount, room));
                    break;
                case 4:
                    name = params[1] + " " + params[2];
                    amount = Integer.parseInt(params[3]);
                    ;
                    room = "Not checked in";
                    bserver.addBag(new Bags(name, activeUser, amount, room));
                    break;
                case 3:
                    name = params[1] + " " + params[2];
                    amount = 1;
                    room = "Not checked in";
                    bserver.addBag(new Bags(name, activeUser, amount, room));
                    break;
                default:
                    outMgr.println("Please follow the required format, for list of commands please type 'Help'");
                    break;
            }

        } else {
            outMgr.println("You have to be logged in to update the storage.");
        }
    }

    //request to check contents of a bag with a certain tag number.
    private void check(String tagN) throws RemoteException, RejectedException, BDBException {
        if (!lIn) {
            outMgr.println("You have to be logged in to update the storage");
            return;
        }
        int tag = Integer.parseInt(tagN);
        BagsDTO storedBag = bserver.getBag(tag, activeUser);
        if (storedBag != null) {
            outMgr.println("The guest has the following bags stored '" + storedBag.getNumber() + "' The guest stayed at : " + storedBag.getRoom());
        }
    }

    //Request to update amount and the room numbers
    private void change(String tagN, String size, String room) throws RemoteException, BDBException, RejectedException {
        if (!lIn) {
            outMgr.println("You have to be logged in to change bags");
            return;
        }
        int tag = Integer.parseInt(tagN);
        Boolean success = bserver.change(tag, activeUser, Integer.parseInt(size), room);
        if (success) {
            BagsDTO changed = bserver.getBag(tag);
            outMgr.println("The bag has been successfully changed. Current state of the required bag: " + changed.toString());
        }
    }

    //Request to update the room number
    private void updateRoom(String input) throws RemoteException, BDBException, RejectedException {
        if (!lIn) {
            outMgr.println("You have to be logged in to change bags");
            return;
        }
        Boolean success;
        int tag;
        String room;
        String[] params = input.split(" ");
        switch (params.length) {
            case 3:
                tag = Integer.parseInt(params[1]);
                room = params[2];
                success = bserver.changeRoom(tag, activeUser, room);
                break;
            case 2:
                tag = Integer.parseInt(params[1]);
                success = bserver.changeRoom(tag, activeUser, "Not checked in");
                break;
            default:
                outMgr.println("Please follow the required format, for list of commands please type 'Help'");
                return;
        }
        if (success) {
            BagsDTO changed = bserver.getBag(tag);
            outMgr.println("The bag has been successfully changed. Current state of the required bag: " + changed.toString());
        }
    }

    // Request to update the number of bags
    private void updateNumber(String tagN, String amount) throws RemoteException, BDBException, RejectedException {
        if (!lIn) {
            outMgr.println("You have to be logged in to change bags");
            return;
        }
        int tag = Integer.parseInt(tagN);
        Boolean success = bserver.changeNumber(tag, activeUser, Integer.parseInt(amount));
        if (success) {
            BagsDTO changed = bserver.getBag(tag);
            outMgr.println("The bag has been successfully changed. Current state of the required bag: " + changed.toString());
        }
    }

    //request to fetch the bag (removes from the storage)
    private void fetch(String tagN) throws RemoteException, RejectedException, BDBException {
        if (!lIn) {
            outMgr.println("You have to be logged in to delete bags");
            return;
        }
        int tag = Integer.parseInt(tagN);
        Boolean success = bserver.delete(tag, activeUser);
        if (success) {
            outMgr.println("The bag '" + tagN + "' has been successfully deleted.");
        }
    }

    //Method to display commands and their functionality to the user
    private void defineCommandUsage() {
        commandList[0] = new String(" Help- lists all the available commands in the program and the way they are supposed to be executed. Commands are not case sensitive.");
        commandList[1] = new String(" Register- type register, and submit your desired username, password and current adminpassword. If the username is taken you will have to choose a new one." + "\n" + "     Username and password are case sensitive. To execute: 'register user password adminpassword'");
        commandList[2] = new String(" Login- type login and submit your username and password sperated by a space to login onto the server. To execute 'login username password'");
        commandList[3] = new String(" Store- stores the bags of the guest and its parameters(number of bags and the room number). If the guest does not have a room leave it blank." + "\n" + "      To execute: 'store Bill Green 1 111' Or 'store Bill Green 1' or 'store Bill Green' (By default 1 bag and not checked in)");
        commandList[4] = new String(" Check- checks whether the guest has luggage left in the luggage room. To execute 'check TagNumber' ");
        commandList[5] = new String(" Change- changes bags data both number of bags and the room. Type new amount and room number.  " + "\n" + "      To execute: 'change tagNumber 100 525'");
        commandList[6] = new String(" ChangeN- changes only the number of bags stored. Type new amount.  To execute: 'changen tagNumber 3'");
        commandList[7] = new String(" ChangeR- changes only the room number. Type new room, or leave blank for 'Not checked in'.  To execute: 'changer tagNumber 111'");
        commandList[8] = new String(" Listall- lists all the bags that are present in the storage room. To execute: 'listall'");
        commandList[9] = new String(" Listowner- lists all the bags that are present in the storage room. To execute: 'listowner Bill Green'");
        commandList[10] = new String(" Liststoredby- lists all the bags that are present in the storage room. To execute: 'liststoredby Charlie");
        commandList[11] = new String(" Fetch- deletes a bag with the provided tagNumber. To execute: 'fetch tagNumber'. ");
        commandList[12] = new String(" Logout- Logs you out of the system but does not close the program, enabling you to login on a different account.  ");
        commandList[13] = new String(" Quit- Closes the Program and logs you off." + "\n");
        for (int i = 0; i < 10; i++) {
            outMgr.println(commandList[i]);
        }
    }

    private class ClientOutput extends UnicastRemoteObject implements Client {

        public ClientOutput() throws RemoteException {
        }

        @Override
        public void recieveNotification(String notification) {
            outMgr.println((String) notification);
        }
    }

}
