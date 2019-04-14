package Common;

import Server.integration.BagsDAO;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Bags implements BagsDTO {

    private String name;
    private String room;
    private String storedBy;
    private int number;
    private int id;
    private transient BagsDAO bagServerDB;

    public Bags(int id, String name, String storedBy, int number, String room, BagsDAO bsd) {
        this.bagServerDB = bsd;
        this.name = name;
        this.number = number;
        this.room = room;
        this.storedBy = storedBy;
        this.id = id;

    }
    
    
   public Bags(int id, String name, String storedBy, int number, String room) {
        this.name = name;
        this.number = number;
        this.room = room;
        this.storedBy = storedBy;
        this.id = id;

    }

    public Bags(String name, String storedBy, int number, String room, BagsDAO bsd) {
        this.bagServerDB = bsd;
        this.name = name;
        this.number = number;
        this.room = room;
        this.storedBy = storedBy;
        id = createId();

    }
        public Bags(String name, String storedBy, int number, String room) {
        
        this.name = name;
        this.number = number;
        this.room = room;
        this.storedBy = storedBy;
        id = createId();

    }

    //1 bag by default
    public Bags(String name, String storedBy, String room, BagsDAO bsd) {
        this.bagServerDB = bsd;
        this.name = name;
        this.number = 1;
        this.room = room;
        id = createId();
    }

    //Not connected to the database
    public Bags(String name, int number, String room) {
        this.name = name;
        this.number = number;
        this.room = room;
        id = createId();
    }

    // if no room provided the guest is not checked in
    public Bags(String name, String storedBy, int number) {
        this.name = name;
        this.number = number;
        this.room = "Not checked in";
        this.storedBy = storedBy;
        id = createId();
    }

    public Bags(String name, String storedBy) {
        this.name = name;
        this.number = 1;
        this.room = "Not checked in";
        this.storedBy = storedBy;
        id = createId();
    }

    public Bags(String name, BagsDAO bagDB) {
        this.name = name;
        this.bagServerDB = bagDB;
    }


    public String toString() {
        StringBuilder stringRepresentation = new StringBuilder();
        stringRepresentation.append("Id number: ");
        stringRepresentation.append(id);
        stringRepresentation.append(", Owner name: '");
        stringRepresentation.append(name);
        stringRepresentation.append("', Number of bags: ");
        stringRepresentation.append(number);
        stringRepresentation.append(" Room: ");
        stringRepresentation.append(room);
        stringRepresentation.append(", Stored by: ");
        stringRepresentation.append(storedBy);
        return stringRepresentation.toString();
    }

    @Override
    public String getOwnerName() {
        return name;
    }

    @Override
    public String getRoom() {
        return room;
    }

    @Override
    public String getStoredBy() {
        return storedBy;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public int getId() {
        return id;
    }

    private int createId() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMddHHmmss");
        String time = dtf.format(now);
        int result = Integer.parseInt(time);
        return result;
    }
}
