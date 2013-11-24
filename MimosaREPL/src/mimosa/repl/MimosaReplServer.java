package mimosa.repl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MimosaReplServer implements Runnable {
  private final static Logger Log = Logger.getLogger(MimosaReplServer.class.getName());

  private ServerSocket server;
  private Socket client;
  private boolean stopped;

  public MimosaReplServer() {
    stopped = false;
  }

  @Override
  public void run() {
    try {
      server = new ServerSocket(LuaService.LISTEN_PORT);
      Log.log(Level.INFO, "Server started on port " + LuaService.LISTEN_PORT);
    } catch (IOException e) {
      e.printStackTrace();
    }

    while (!stopped) {
      try {
        Log.log(Level.INFO, "listening on port " + LuaService.LISTEN_PORT);
        client = server.accept();

        Log.log(Level.INFO, "client accepted");

        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        PrintWriter out = new PrintWriter(client.getOutputStream());

        String line = null;
        Log.log(Level.INFO, "ready to read data");
        while (!stopped && (line = in.readLine()) != null) {
          final String s = line.replace(LuaService.REPLACE, '\n');
          System.out.println(s);
          Command cmd = CommandFactory.getCommand(s);

          String ret = null;
          if (cmd != null) {
            ret = cmd.execute();
          } else {
            ret = "Unknow command\n";
          }

          ret = ret.replace('\n', LuaService.REPLACE);
          out.println(ret);
          out.flush();
        }
        client.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

  }
}
