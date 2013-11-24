package mimosa.repl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MimosaReplClient implements Runnable {
  private final static Logger Log = Logger.getLogger(MimosaReplClient.class.getName());

  private final static char REPLACE = '\001';

  private String host = null;
  private int port = 3333;

  private Socket server = null;

  private List<String> messages = null;

  private boolean stopped = false;

  private Map<String, List<ResultHandler>> handlers = null;

  public MimosaReplClient(String host, int port) {
    this.host = host;
    this.port = port;

    messages = Collections.synchronizedList(new ArrayList<String>());
    
    handlers = new HashMap<String, List<ResultHandler>>();
  }

  public Socket connect() {
    try {
      server = new Socket(host, port);
      Log.log(Level.INFO, "Connected to " + host + ":" + port);
    } catch (UnknownHostException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return server;
  }

  public void addHandler(String code, ResultHandler handler) {
    List<ResultHandler> handlerList = handlers.get(code);
    if (handlerList == null) {
      handlerList = new ArrayList<ResultHandler>();
      handlers.put(code, handlerList);
    }

    handlerList.add(handler);
  }

  public synchronized void send(String msg) {
    messages.add(msg);
  }

  public void run() {
    connect();

    if (server == null) {
      Log.log(Level.INFO, "Faied to connect to " + host + ":" + port);
      return;
    }

    try {
      BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));
      PrintWriter out = new PrintWriter(server.getOutputStream());

      while (!stopped) {
        while (messages.size() == 0) {
          try {
            Thread.sleep(100);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        
        String message = messages.get(0);

        String cmdCode = CommandFactory.getCommandCode(message);
        synchronized (out) {
          message = message.replace('\n', REPLACE);
          out.println(message);
          out.flush();
          // Log.log(Level.INFO, "send: " + message);
          messages.remove(0);
        }

        String line = null;
        if (!stopped && (line = in.readLine()) != null) {
          final String s = line.replace(REPLACE, '\n');
          List<ResultHandler> handlerList = handlers.get(cmdCode);
          if (handlerList != null) {
            for (ResultHandler handler : handlerList) {
              handler.handle(s);
            }
          }
        }
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}