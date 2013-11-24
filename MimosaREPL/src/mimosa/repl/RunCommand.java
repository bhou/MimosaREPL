package mimosa.repl;

import java.util.logging.Level;
import java.util.logging.Logger;


public class RunCommand extends LuaCommand implements ResultHandler{
  private final static Logger Log = Logger.getLogger(RunCommand.class.getName());

  public RunCommand(LuaService luaService) {
    super(luaService);
  }
  
  @Override
  public String execute() {
    String command = getCommand();
    if (command == null){
      return "Command could not be null!";
    }
    if (!command.startsWith("--run:")) {
      return "Error:'" + command + "' is Not a valide command";
    }
    
    String res = getLuaService().safeEvalLua(command, "tmp");
    Log.log(Level.INFO, "result: " + res);
    res = res.replace('\n', LuaService.REPLACE);
    return res;
  }

  @Override
  public void handle(String message) {
    Log.log(Level.INFO, "result: " + message);
  }
}
