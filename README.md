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
MimosaReplClient client = new MimosaReplClient("localhost", 3333);  // connect to server
// handle return value 
client.setHandler(RunCommand.class, new RunCmdHandler())
client.setHandler(ModuleCommand.class, new ModuleCmdHandler())

Thread thread = new Thread(client); // start a thread to handle network stuff
thread.start();

// command type is defined as first line comment
client.send("--run:\n" + line); // directly send code as string
client.send("--module:\n" + chunkStr);  // send a module file

client.send(RunCommand.create(line)); 
client.send(ModuleCommand.create(moduleName));
`````

The basic command is RunCommand: it sends any lua code to server, run it, and send the result back. You should create 
your RunCmdHandler to hadnle the result. (show it on the ui, post-processing, etc)
