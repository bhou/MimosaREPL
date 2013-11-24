package mimosa.repl;


public class EchoCommand extends Command {

  public EchoCommand(LuaService luaService, String command) {
    super(luaService, command);
  }

  @Override
  public String execute() {
    return "echo: " + getCommand();
  }
}
