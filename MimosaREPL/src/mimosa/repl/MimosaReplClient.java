package mimosa.repl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MimosaReplClient implements Runnable {
  private final static Logger Log = Logger.getLogger(MimosaReplClient.class.getName());

  private final static char REPLACE = '\001';

  private String host = null;
  private int port = 3333;

  private Socket server = null;

  private String message = null;

  private boolean stopped = false;

  public MimosaReplClient(String host, int port) {
    this.host = host;
    this.port = port;
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

  public synchronized void send(String msg) {
    message = msg;
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
        while (message == null) {
          try {
            Thread.sleep(100);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }

        synchronized (out) {
          message = message.replace('\n', REPLACE);
          out.println(message);
          out.flush();
          Log.log(Level.INFO, "send message: " + message);
          message = null;
        }

        String line = null;
        if (!stopped && (line = in.readLine()) != null) {
          final String s = line.replace(REPLACE, '\n');
          Log.log(Level.INFO, "return value: " + s);
        }
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}