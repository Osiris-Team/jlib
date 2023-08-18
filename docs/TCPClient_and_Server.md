# TCP Client and Server

### Protocol
The problem with regular Netty data handlers/readers:
- Sent data is received without order.
- Fix: writeID and readID, which gets incremented for each write/read operation.
The writeID gets prepended to the send data.
- The caveats with this approach:
  - All writes/reads must be known by both server and client before
  creating the connection, thus those writes/reads must be initiated on a single thread, to ensure correct order.
  - New writes/reads that are added later are not possible.
  - Additional 32 bits sent for the writeID integer (the effect of this
  can be minimized, by making one single write with more data, instead of many
  writes with little data).
  - It's not possible to switch between protocols.
    - Fix: logic to handle multiple protocols concurrently? major performance overhead and complexity increase... 

The protocol class combines the input and output into a single class.

Each client-server connection has a single communication protocol created by you.

This approach allows a low-code, simple and easy to maintain solution,
for normally complex, async communication.



In the following I will show you some basic examples.
Let's start simple by just sending "Hello!" from the client.
```java
// Client
new Protocol()
        .writeUTF("Hello!");

// Server
Future<String> helloWorld = new Future<>();        
new Protocol()
        .readUTF(helloWorld);
```
If the server should respond we can do the following:
```java
// Server
Future<String> helloWorld = new Future<>();        
new Protocol()
        .readUTF(helloWorld)
        .await()
        .writeUTF("Hello! Nice to see you!");
```

By using `await()` we ensure previous reads are completed and their data
received.

Keep in mind that `await()` does **NOT** actually block code execution.
Instead, you can think of it as a marker.

If you want to execute code at any point in the protocol you can use `async()`
like so:

```java
// Server
Future<String> helloWorld = new Future<>();        
new Protocol()
        .readUTF(helloWorld)
        .await()
        .async(() -> {
            System.out.println(helloWorld.get());
        })
        .writeUTF("Hello! Nice to see you!");
```

You can loop the protocol how many times you want:
```java
client.open(host, port, ssl,
        () -> {return new Protocol();},
        10, // Loop the protocol 10 times, set to -1 for infinite loop
        1) // Interval between each loop in seconds 
```

While on the topic of loops, sometimes we need to use loops
inside the actual protocol.
```java
// Client
String[] arr = {"H", "i"};
new Protocol()
        .loopStart(arr.length)
        .writeUTF(i -> {
            return arr[i];
        })
        .loopEnd();

// Server
Future<String> char_ = new Future<>();
new Protocol()
        .loopStart()
        .readUTF(char_ -> {
            
        })
        .loopEnd();
```