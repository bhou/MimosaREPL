package mimosa.repl;

import java.util.logging.Level;
import java.util.logging.Logger;


public class RunCommand extends Command {
  private final static Logger Log = Logger.getLogger(RunCommand.class.getName());

  public RunCommand(LuaService L, String command) {
    super(L, command);
  }

  @Override
  public String execute() {
    String command = getCommand();
    if (!command.startsWith("--run:")) {
      return "";
    }
    String res = getLuaService().safeEvalLua(command, "tmp");
    Log.log(Level.INFO, "result: " + res);
    res = res.replace('\n', LuaService.REPLACE);
    return res;
  }
}
