# LuggageTracker
The luggage tracker program 

   The program is a Client-Server application written in Java. The connection between Clients and the Server is done by using remote method invocation (rmi). The program is designed to keep track of luggage left for safekeeping at a hotel (can also be used at airports, railway stations and so on). 

Server:
   The server is connected to a database (derby) with two tables- one to keep track of users and one to keep track of the left luggage. If the database is not initialised, the server will create it on the first run. 
   The server supports multiple clients being connected at the same time and serves all client requests. The server keeps track of all connected clients with a help of ConnectionManager class.
   The main functionality of the server is client handling, and processing database requests. The server also keeps track of all the requests in a log file stored in the Server folder (automatically created on the first request once the server is started). All entries in the log file are time-stamped. If the server was to be restarted it will create another log file. Log filename is based on the date and time it has been created.

Client:
	The client side provides user interface (currently non-grafical). The client can use the following commands (most of which are requests to the server). 

Help- lists all the available commands in the program and the way they are supposed to be executed. Commands are not case sensitive.
Register- type register, and submit your desired username, password and current adminpassword. If the username is taken you will have to choose a new one.  Username and password are case sensitive. To execute: 'register user password adminpassword'

Login- type login and submit your username and password sperated by a space to login onto the server. To execute 'login username password'

Store- stores the bags of the guest and its parameters(number of bags and the room number). If the guest does not have a room leave it blank.  To execute: 'store Bill Green 1 111' Or 'store Bill Green 1' or 'store Bill Green' (By default 1 bag and not checked in)

Check- checks whether the guest has luggage left in the luggage room. To execute 'check TagNumber'. 

Change- changes bags data both number of bags and the room. Type new amount and room number.  To execute: 'change tagNumber 100 525'

ChangeN- changes only the number of bags stored. Type new amount.  To execute: 'changen tagNumber 3'

ChangeR- changes only the room number. Type new room, or leave blank for 'Not checked in'.  To execute: 'changer tagNumber 111'

Listall- lists all the bags that are present in the storage room. To execute: 'listall'

Listowner- lists all the bags that are present in the storage room. To execute: 'listowner Bill Green'

Liststoredby- lists all the bags that are present in the storage room. To execute: 'liststoredby Charlie

Fetch- deletes a bag with the provided tagNumber. To execute: 'fetch tagNumber'. 

Logout- Logs you out of the system but does not close the program, enabling you to login on a different account.  

Quit- Closes the Program and logs you off.

In order to register one has to type in an administrative password in addition to the desired username and password. Current admin password is ‘a’.  No multilogin or multiple users with the same username are allowed. As mentioned previously, all the requests are processed by the server, so in order to avoid ddos attacks from unidentified users, the client checks whether the user is logged in before submiting requests to the server. 

Potential improvements:
   The next step to improve the server, can be adding support for multiple storage rooms (e.g. create a new table in the database, for each Luggage room. If this was the case now, all the luggage will be stored in the same database, which will result in many more entries in the storage database, and potentially increase the search times and more importantly users from one place will be able to remove bags stored in the other place, so currently it will purely operate on trust.

   For the client side, implementing a GUI would be useful.
