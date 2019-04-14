package Client.view;

public enum Command {
    /**
     * Creates a new account with username and password provided. A special
     * administrative password needed to register.
     */
    REGISTER,
    /**
     * Logs the user in if password and name are correct.
     */
    LOGIN,
    /**
     * Stores the luggage left by the guests in the server's database
     */
    STORE,
    /**
     * Checks whether the guest has anything stored
     */
    CHECK,
    /**
     * Displays all the bags that are left for safekeeping
     */
    LISTALL,
    /**
     * Displays all the bags that are left for safekeeping by ownername
     */
    LISTOWNER,
    /**
     * Displays all the bags that are left for safekeeping by a user
     */
    LISTSTOREDBY,
    /**
     * Updates the number of bags or the room or both of the guest
     */
    CHANGE,
    /**
     * Updates the number of bags or the room or both of the guest
     */
    CHANGEN,
    /**
     * Updates the number of bags or the room or both of the guest
     */
    CHANGER,
    /**
     * Removes the bags of the guests from safekeeping database
     */
    FETCH,
    /**
     * Lists all commands.
     */
    HELP,
    /**
     * logout to e.g. change the user basically ghost mode
     */
    LOGOUT,
    /**
     * Leave the chat application.
     */
    QUIT,
    /**
     * None of the valid commands above was specified.
     */
    ILLEGAL_COMMAND
}
