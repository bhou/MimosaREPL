package mimosa.repl;


public abstract class Command {
  private LuaService luaService;
  private String command;

  public Command(LuaService luaService, String command) {
    this.luaService = luaService;
    this.command = command;
  }

  public String execute() {
    return null;
  }

  public String getCommand() {
    return command;
  }

  public LuaService getLuaService() {
    return this.luaService;
  }
}
