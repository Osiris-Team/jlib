# jlib
Frequently used code across all my Java projects inside one jar.
[Click here for maven/gradle/sbt/leinigen instructions](https://jitpack.io/#Osiris-Team/jlib/LATEST) (Java 8 or higher required).

### Logging
The `AL` class (short for AutoPlug-Logger) contains static methods for logging
with 4 levels: error, warn, info, and debug. All messages are pre-formatted
and support ANSI colors. 

It also allows registering listeners for specific or all messages and 
mirroring the console output to multiple files easily.

### Json
The `Json` class contains static methods for retrieving Json from an `URL`
and parsing it from/to a String. Under the hood it uses the `Gson` library (probably
the fastest Json library for Java).

The abstract `JsonFile` class is optimal when you need json configurations for example.
It makes serialisation easy and removes boilerplate, here is an example:
```java
class MyConfig extends JsonFile{
    public String appName = "My cool app";
    public String version = 1;
    
    public Person(){
        //load() // Do NOT call load inside the constructor
        
        // This creates the file if not existing and fills it
        // with the default values above.
        super(new File("my-config.json"));
        
        version = 2;
        save(); // To update "version" also in person.json (async).
        saveNow(); // Same as above, but blocks until finished.
    }
}
```

### Sorting
The `QuickSort` class implements the QuickSort algorithm and provides useful
static methods to sort **arrays** and **lists** of any type (including `JsonArrays`). Usage example:
```java
new QuickSort().sortJsonArray(arr, (thisEl, otherEl) -> {
    int thisId = thisEl.el.getAsJsonObject().get("id").getAsInt();
    int otherId = otherEl.el.getAsJsonObject().get("id").getAsInt();
    return Integer.compare(thisId, otherId);
});
```

### Update finder
The `Search` class contains methods for finding assets/updates on Maven and Github.

### TCP Client and Server
A blazing fast, async, TCP client and server using [Netty](),
with in/out in the style of DataInputStream 
and DataOutputStream.

This makes upgrading your current app that uses the default
Java Sockets API, a lot easier. Additional features:
- Graceful close, where both parties (host and remote)
get notified and transmission of data doesn't end abruptly.
- Send/Receive files or streams.

```java
        TCPServer server = new TCPServer();
        server.onClientConnected = c -> {
            //c.readers.addFirst(new LoggingHandler(LogLevel.INFO));
            // TODO server logic
        };
        server.open("localhost", 3555, false, true);

        TCPClient client = new TCPClient();
        client.open("localhost", 3555, false, true);
        //client.readers.addFirst(new LoggingHandler(LogLevel.INFO));
        // TODO client logic
```
Simple, local, client to server hello world, using `initLocalServerAndClient`
helper method:
```java
    @Test
    void clientToServer() throws Exception {
        initLocalServerAndClient((server, sclient) -> {
            sclient.in.readUTF().onSuccess(v -> {
                System.out.println("Received client to server msg: "+v);
                server.close_();
            });
        }, client -> {
            client.out.writeUTF("Hello world!");
            client.close_();
        });
    }
```
