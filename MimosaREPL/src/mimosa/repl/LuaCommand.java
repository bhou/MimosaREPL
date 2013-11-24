package mimosa.repl;

public class LuaCommand implements Command{
  private LuaService luaService;
  private String command;
  
  public LuaCommand(LuaService luaService) {
    this.luaService = luaService;
  }
  
  /**
   * construct command from parameters
   * @param params
   */
  public void init(Object[] params){
    command = null;
  }
  
  /**
   * set command directly
   * @param command
   */
  public void setCommand(String command){
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
