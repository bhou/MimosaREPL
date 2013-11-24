package mimosa.repl.test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import mimosa.repl.MimosaReplClient;
import mimosa.repl.ResultHandler;

public class Client {
  private final static Logger Log = Logger.getLogger(Client.class.getName());
  public static void main(String[] args) {
    FileInputStream fis;
    try {

      fis = new FileInputStream(Client.class.getResource("/mimosa/repl/test/logger.properties")
          .getFile());
      LogManager.getLogManager().readConfiguration(fis);
    } catch (FileNotFoundException e1) {
      e1.printStackTrace();
    } catch (SecurityException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    MimosaReplClient client = new MimosaReplClient("localhost", 3333);
    client.addHandler("--run:", new ResultHandler() {      
      @Override
      public void handle(String message) {
        Log.log(Level.INFO, message);
      }
    });
    Thread thread = new Thread(client);
    thread.start();

    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    String line;
    try {
      while ((line = br.readLine()) != null) {
        client.send("--run:\n" + line);
      }
      br.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
