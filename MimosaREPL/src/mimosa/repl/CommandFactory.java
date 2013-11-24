package mimosa.repl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.keplerproject.luajava.LuaState;

public class CommandFactory {
  private static Map<String, Class<? extends Command>> commandMap = new HashMap<String, Class<? extends Command>>();
  private static LuaService luaService = null;
  
  static {
    register("--run:", RunCommand.class);
  }
  
  public static void setLuaService(LuaService luaService){
    CommandFactory.luaService = luaService;
  }
  
  public static void register(String code, Class<? extends Command> cmd) {
    commandMap.put(code, cmd);
  }

  public static Command getCommand(String command) {
    String code = getCommandCode(command);

    if (code == null) {
      return null;
    }

    Class<? extends Command> cmdClass = commandMap.get(code);

    try {
      Constructor<? extends Command> constructor = cmdClass.getConstructor(LuaService.class, String.class);
      Command cmd = constructor.newInstance(CommandFactory.luaService, command);
      return cmd;
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (SecurityException e) {
      e.printStackTrace();
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }

    return null;
  }

  /**
   * get the command code from the command line the command code starts with -- and end with \n
   * 
   * @param command
   * @return
   */
  public static String getCommandCode(String command) {
    int index = command.indexOf('\n');

    String code = command.substring(0, index);
    return code;
  }
}
