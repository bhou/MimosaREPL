MimosaREPL
==========

MimosaREPL is a java implemented lua REPL lib

### How to use it

####Server side
`````java
LuaService service = new LuaService();  // create a lua service
CommandFactory.setLuaService(service);  // set the lua service to command factory
Thread thread = new Thread(new MimosaReplServer()); // start mimosa repl server
thread.start();
`````

####Client side
`````java
MimosaReplClient client = new MimosaReplClient("localhost", 3333);
client.addHandler("--run:", new ResultHandler() {      
  @Override
  public void handle(String message) {
    System.out.println(message);
  }
});
Thread thread = new Thread(client);
thread.start();

String line = "print('hello')";
client.send("--run:\n" + line);

`````

The basic command is RunCommand: it sends any lua code to server, run it, and send the result back. You should create 
your RunCmdHandler to hadnle the result. (show it on the ui, post-processing, etc)
