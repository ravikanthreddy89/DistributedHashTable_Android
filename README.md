A simple key value storage distributed system implemented for android devices. Have used Chord protocol.

Design : The design of the app follows the semantics presented in the paper:
(http://www.cse.buffalo.edu/~stevko/courses/cse486/spring12/lectures/dht.pptx).
The avd0 or the avd 5554 is considered as bootstrap node. Every node which joins the chord
network should send its “Join request” to this node. All communications between the nodes
happen with the help of messages sent through the TCP sockets. The concept of using messages
is borrowed from the “Simpella” Project specifications which we have implemented in Modern
Networking Concepts, fall’12.
For the purpose of the project I have used 6 types of messages.
1. Request message: Upon startup new node sends this message to bootstrap node. The
bootstrap node based on the hash value of incoming avd takes necessary action.
Message Format : - <“request”: “portno”>
2. Response message: When the request message is received by appropriate node, it
generates two messages, response and update. Response message, containing successor
and predecessor port numbers, is sent to new node. The new node upon receipt of
response message updates successor and predecessor ports.
Message Format :- <”response”:”predecessor_portno”:”successor_portno”>
3. Update message: The very node which replies with response message to new node sends
another message to its successor informing that it is no more its predecessor.
Message Format :- <”update”: “predecessor_portno”>
4. Query message: This message is used to query certain key in the chord ring.
Message Format:- <”query”:”portno_”:”query_string”>
5. QueryHit message: Used by nodes to reply if there is a hit in its local database to the
incoming query.
Message Format :- <”queryhit”:”query_result”>
6. Insert message (to insert a new key-value pair into the chord ring)
Message Format: - <”insert”: “portno”:”key”:”value”>
All the messages are java Strings with various fields separated by “:”.The first field in the
message is used to identify the type of message. The other fields contain other information such
as predecessor port number, successor port no, query string, queryhit result, etc.
Query Message: This message is used to serve two purposes, for querying single key_value pair
and to implement GDUMP. It differentiate normal query from GDUMP query the query string
field is filled with four hashes.
Format of query message for GDUMP : <”query”:”portno_” :”####”>
Upon receiving a query message with query field containing fours hashes (“####”), query
handler method (method which handles query messages) attaches all the local key-value pairs to
the query message and passes the message to successor. When this message reaches the avd
which queried it(eventually the message will reach the avd which initiated the GDUMP), does
the same(attaches all local key-value pairs) and builds a cursor to return.
The below figure depicts the joining mechanism of 5558 into the chord ring. (NOTE: The avd
numbers are assumed to be node ids for this example, however in the implementation hash of
avd numbers is used for node placement in the chord ring).
Step 1: 5558 sends request message to 5554.
Step 2 : 5554 checks the incoming node id and forwards it to its successor as its value is greater
than itself and successor id.(Other special conditions are also handled.)
Step 3 : 5556 identifies the position of 5558 as between itself and its successor. Creates response
message containing predecessor and successor port numbers and forwards it to 5558.
Simultaneously creates update message containing new predecessor port number and forwards it
to 5560 informing about its new predecessor.

List of java files in the project are :

SimpleDhtMainActivity
SimpleDhtProvider
OnTestClickListener
LdumpListener
GdumpListener
Functions
Message


The main activity is left unchanged except the calls to LdumpListener and
GdumpListener are added.
The LdumpListener is click functionality for LDump button. When clicked a query
message with querystring= “ldump” is created and forwarded to content provider, which
returns a cursor containing the local key-value pairs.
The GdumpListener is click functionality for GDump button. When clicked a query
message with query string =”####” is created and forwarded to content provider. The
content provider forwards this message to its successor. All the nodes in the ring upon
receiving this kind of message attach their local key-value pairs to the message and
forward it to their successor. Eventually when this message reaches the original avd
where GDump was clicked the key-values pairs are stripped and added to matrix cursor
along with local key-value pairs. This matrix cursor is returned.
Functions: this class contains various functions/methods which are invoked whenever a
message is received at an avd. The received message is passed to The methods are :
o Request_handler
o Response_handler
o Update_handler
o Query_handler
o Queryhit_handler
o Insert_handler
Message: this class contains various methods which are used to create different types of
messages. Methods used are :
o Request_create()
o Response_create()
o Update_create(int portno)
o Query_create(String querystring)
o Queryhit_create(String querystring
o Insert_create(String key, String value)
Testing Results : When tested using three avds the average time to pass the TEST case is around
11 seconds.

