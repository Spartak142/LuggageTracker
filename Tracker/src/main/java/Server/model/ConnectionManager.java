package Server.model;

import Common.Client;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ConnectionManager {

    private final Map<String, LoggedInUser> participants = Collections.synchronizedMap(new HashMap<>());

    public String createParticipant(Client remoteNode, String credentials) {
        String participantId = credentials;
        LoggedInUser newParticipant = new LoggedInUser(participantId, credentials, remoteNode, this);
        participants.put(participantId, newParticipant);
        return participantId;
    }

    //Checks whether a particular client is online
    public Boolean isOnline(String id) {
        return participants.containsKey(id);
    }

    //Removes a particular client from the connected list.
    public void disconnect(String id) {
        participants.remove(id);
    }
}
