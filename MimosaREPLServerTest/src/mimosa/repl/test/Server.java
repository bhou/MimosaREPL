package mimosa.repl.test;

import mimosa.repl.CommandFactory;
import mimosa.repl.LuaService;
import mimosa.repl.MimosaReplServer;

public class Server {

  public static void main(String[] args) {
    LuaService service = new LuaService();  // create a lua service
    CommandFactory.setLuaService(service);  // set the lua service to command factory
    Thread thread = new Thread(new MimosaReplServer()); // start mimosa repl server
    thread.start();
  }
}
