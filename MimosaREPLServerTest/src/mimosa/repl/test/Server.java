package mimosa.repl.test;

import mimosa.repl.CommandFactory;
import mimosa.repl.LuaService;
import mimosa.repl.MimosaReplServer;

public class Server {

  public static void main(String[] args) {
    LuaService service = new LuaService();
    CommandFactory.setLuaService(service);
    Thread thread = new Thread(new MimosaReplServer());
    thread.start();
  }
}
