package Server.integration;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import Server.model.Account;
import Common.AccountDTO;
import Common.Bags;
import Common.BagsDTO;

// This data access object encapsulates all database calls in the BagServer application.
public class BagsDAO {

    private static final String TABLE_NAME = "ACCOUNTS";
    private static final String PASSWORD_COLUMN_NAME = "PASSWORD";
    private static final String HOLDER_COLUMN_NAME = "NAME";

    private static final String TABLE_NAME1 = "BAGS";
    private static final String ID_COLUMN_NAME = "ID";
    private static final String OWNER_COLUMN_NAME = "OWNER";
    private static final String STOREDBY_COLUMN_NAME = "STOREDBY";
    private static final String NUMBER_COLUMN_NAME = "AMOUNT";
    private static final String ROOM_COLUMN_NAME = "ROOM";

    private PreparedStatement createAccountStmt;
    private PreparedStatement findAccountStmt;

    private PreparedStatement addBagsStmt;
    private PreparedStatement findBagStmt;
    private PreparedStatement findBagStmtStoredBy;
    private PreparedStatement findOwnerBagStmt;
    private PreparedStatement findAllBagsStmt;
    private PreparedStatement changeAmountStatement;
    private PreparedStatement changeRoomStatement;
    private PreparedStatement removeBagsStmt;

//Constructs a new DAO object connected to the specified database.
    public BagsDAO(String dbms, String datasource) throws BDBException {
        try {
            Connection connection = createDatasource(dbms, datasource);
            System.out.println("Before prepare statements");
            prepareStatements(connection);
            System.out.println("After prepare statements");
        } catch (ClassNotFoundException | SQLException exception) {
            throw new BDBException("Could not connect to datasource.", exception);
        }
    }

    // Creates a new account.
    public void createAccount(AccountDTO account) throws BDBException, SQLException {
        String failureMsg = "Could not create the account: " + account;
        try {
            createAccountStmt.setString(1, account.getUserName());
            createAccountStmt.setString(2, account.getPassword());
            int rows = createAccountStmt.executeUpdate();
            if (rows != 1) {
                throw new BDBException(failureMsg);
            }
        } catch (SQLException sqle) {
            failureMsg = "Username already taken";
            throw new BDBException(failureMsg, sqle);
        }
    }

//Stores the provided bag
    public void storeBags(BagsDTO bags) throws BDBException, SQLException {
        String failureMsg = "Could not add the bags : " + bags;
        try {
            addBagsStmt.setInt(1, bags.getId());
            addBagsStmt.setString(2, bags.getOwnerName());
            addBagsStmt.setString(3, bags.getStoredBy());
            addBagsStmt.setInt(4, bags.getNumber());
            addBagsStmt.setString(5, bags.getRoom());
            int rows = addBagsStmt.executeUpdate();
            if (rows != 1) {

                throw new BDBException(failureMsg);
            }
        } catch (SQLException sqle) {
            failureMsg = "Bag ID already taken";
            throw new BDBException(failureMsg, sqle);
        }

    }

    //Searches for an account whose holder has the specified name..
    public Account findAccountByName(String holderName) throws BDBException {
        String failureMsg = "Could not search for specified account.";
        ResultSet result = null;
        try {
            findAccountStmt.setString(1, holderName);
            result = findAccountStmt.executeQuery();
            if (result.next()) {
                return new Account(holderName, result.getString(PASSWORD_COLUMN_NAME), this);
            }
        } catch (SQLException sqle) {
            throw new BDBException(failureMsg, sqle);
        } finally {
            try {
                result.close();
            } catch (Exception e) {
                throw new BDBException(failureMsg, e);
            }
        }
        return null;
    }

    // Searches for the bag by it's id tag   
    public Bags findBagsById(int id) throws BDBException {
        String failureMsg = "Could not search for the specified account.";
        ResultSet result = null;
        try {
            findBagStmt.setInt(1, id);
            result = findBagStmt.executeQuery();
            if (result.next()) {
                return new Bags(id, result.getString(OWNER_COLUMN_NAME), result.getString(STOREDBY_COLUMN_NAME), result.getInt(NUMBER_COLUMN_NAME), result.getString(ROOM_COLUMN_NAME), this);
            }
        } catch (SQLException sqle) {
            throw new BDBException(failureMsg, sqle);
        } finally {
            try {
                result.close();
            } catch (Exception e) {
                throw new BDBException(failureMsg, e);
            }
        }
        return null;
    }

    // Retrieves all stored bags
    public List<Bags> findAllBags() throws BDBException {
        String failureMsg = "Could not list the bags.";
        List<Bags> bags = new ArrayList<>();
        try (ResultSet result = findAllBagsStmt.executeQuery()) {
            while (result.next()) {
                bags.add(new Bags(result.getInt(ID_COLUMN_NAME), result.getString(OWNER_COLUMN_NAME), result.getString(STOREDBY_COLUMN_NAME), result.getInt(NUMBER_COLUMN_NAME), result.getString(ROOM_COLUMN_NAME)));
            }
        } catch (SQLException sqle) {
            throw new BDBException(failureMsg, sqle);
        }
        return bags;
    }

    // Retrieves all stored bags owned by a guest
    public List<Bags> findAllByOwner(String owner) throws BDBException, SQLException {
        String failureMsg = "The person has no bags stored.";
        List<Bags> bags = new ArrayList<>();
        findOwnerBagStmt.setString(1, owner);
        try (ResultSet result = findOwnerBagStmt.executeQuery()) {
            while (result.next()) {
                bags.add(new Bags(result.getInt(ID_COLUMN_NAME), result.getString(OWNER_COLUMN_NAME), result.getString(STOREDBY_COLUMN_NAME), result.getInt(NUMBER_COLUMN_NAME), result.getString(ROOM_COLUMN_NAME)));
            }
        } catch (SQLException sqle) {
            throw new BDBException(failureMsg, sqle);
        }
        return bags;
    }

    // Retrieves all bags stored by a particular user
    public List<Bags> findAllByStoredBy(String storedBy) throws BDBException, SQLException {
        String failureMsg = "The person has not stored any bags.";
        List<Bags> bags = new ArrayList<>();
        findBagStmtStoredBy.setString(1, storedBy);
        try (ResultSet result = findBagStmtStoredBy.executeQuery()) {
            while (result.next()) {
                bags.add(new Bags(result.getInt(ID_COLUMN_NAME), result.getString(OWNER_COLUMN_NAME), result.getString(STOREDBY_COLUMN_NAME), result.getInt(NUMBER_COLUMN_NAME), result.getString(ROOM_COLUMN_NAME)));
            }
        } catch (SQLException sqle) {
            throw new BDBException(failureMsg, sqle);
        }
        return bags;
    }

    //Changes the amount of bags associated with a tag number
    public void changeBagAmount(int id, int amount) throws BDBException {
        try {
            changeAmountStatement.setInt(1, amount);
            changeAmountStatement.setInt(2, id);
            changeAmountStatement.executeUpdate();

        } catch (SQLException sqle) {
            throw new BDBException("Could not update the bags data: " + id, sqle);
        }
    }

    //Changes the room number associated with a tag number
    public void changeRoom(int id, String room) throws BDBException {
        try {
            changeRoomStatement.setString(1, room);
            changeRoomStatement.setInt(2, id);
            changeRoomStatement.executeUpdate();

        } catch (SQLException sqle) {
            throw new BDBException("Could not update the bags data: " + id, sqle);
        }
    }

    //Deletes the bag by it's tag number
    public void removeBag(BagsDTO bagToFetch) throws BDBException {
        try {
            removeBagsStmt.setInt(1, bagToFetch.getId());
            removeBagsStmt.executeUpdate();
        } catch (SQLException sqle) {
            throw new BDBException("Could not remove the bag from the list. Already Fetched? " + bagToFetch, sqle);
        }
    }

    //Creates the data tables if they do not exist yet.
    private Connection createDatasource(String dbms, String datasource) throws
            ClassNotFoundException, SQLException, BDBException {
        Connection connection = connectToBagServerDB(dbms, datasource);
        if (!bserverTableExists(connection)) {
            System.out.println("Before statement 1");
            Statement statement = connection.createStatement();
            System.out.println("Before statement 1  execute update");
            statement.executeUpdate("CREATE TABLE " + TABLE_NAME
                    + " (" + HOLDER_COLUMN_NAME + " VARCHAR(32) PRIMARY KEY, "
                    + PASSWORD_COLUMN_NAME + " VARCHAR(32))");
        }
        if (!bagTableExists(connection)) {
            System.out.println("Before statement 2");
            Statement statement = connection.createStatement();
            System.out.println("Before statement 2  execute update");
            statement.executeUpdate("CREATE TABLE " + TABLE_NAME1
                    + " (" + ID_COLUMN_NAME + " INT PRIMARY KEY, " + OWNER_COLUMN_NAME + " VARCHAR(32), "
                    + STOREDBY_COLUMN_NAME + " VARCHAR(32), " + NUMBER_COLUMN_NAME + " INT, " + ROOM_COLUMN_NAME + " VARCHAR(32))");
            System.out.println("after statement 2  execute update");
        }
        System.out.println("After both");
        return connection;
    }

    //Checks whether the account database table exists
    private boolean bserverTableExists(Connection connection) throws SQLException {
        int tableNameColumn = 3;
        DatabaseMetaData dbm = connection.getMetaData();
        try (ResultSet rs = dbm.getTables(null, null, null, null)) {
            for (; rs.next();) {
                if (rs.getString(tableNameColumn).equals(TABLE_NAME)) {
                    return true;
                }
            }
            return false;
        }
    }

    //Checks whether the bag storage database table exists
    private boolean bagTableExists(Connection connection) throws SQLException {
        int tableNameColumn = 3;
        DatabaseMetaData dbm = connection.getMetaData();
        try (ResultSet rs = dbm.getTables(null, null, null, null)) {
            for (; rs.next();) {
                if (rs.getString(tableNameColumn).equals(TABLE_NAME1)) {
                    return true;
                }
            }
            return false;
        }
    }

    // Connection to the database server
    private Connection connectToBagServerDB(String dbms, String datasource)
            throws ClassNotFoundException, SQLException, BDBException {
        try {
            Class.forName("org.apache.derby.jdbc.ClientXADataSource");
            System.out.println("Successful try");
        } catch (ClassNotFoundException ex) {
            System.out.println("Failed to connect");
            ex.printStackTrace();
        }
        return DriverManager.getConnection("jdbc:derby://localhost:1527/Bservertrial1;create=true");
    }

    // Preparing SQL statements
    private void prepareStatements(Connection connection) throws SQLException {
        createAccountStmt = connection.prepareStatement("INSERT INTO "
                + TABLE_NAME + " VALUES (?, ?)");
        findAccountStmt = connection.prepareStatement("SELECT * from "
                + TABLE_NAME + " WHERE NAME = ?");
        findBagStmt = connection.prepareStatement("SELECT * from "
                + TABLE_NAME1 + " WHERE ID= ?");
        findBagStmtStoredBy = connection.prepareStatement("SELECT * from "
                + TABLE_NAME1 + " WHERE STOREDBY= ?");
        findOwnerBagStmt = connection.prepareStatement("SELECT * from "
                + TABLE_NAME1 + " WHERE OWNER= ?");
        findAllBagsStmt = connection.prepareStatement("SELECT * from "
                + TABLE_NAME1);

        addBagsStmt = connection.prepareStatement("INSERT INTO "
                + TABLE_NAME1 + " VALUES (?, ?, ?, ?, ?)");
        changeAmountStatement = connection.prepareStatement("UPDATE "
                + TABLE_NAME1
                + " SET amount = ? WHERE id= ? ");
        changeRoomStatement = connection.prepareStatement("UPDATE "
                + TABLE_NAME1
                + " SET room = ? WHERE id= ? ");

        removeBagsStmt = connection.prepareStatement("DELETE FROM "
                + TABLE_NAME1
                + " WHERE id = ?");
    }

}
