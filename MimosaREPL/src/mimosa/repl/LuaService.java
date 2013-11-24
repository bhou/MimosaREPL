package mimosa.repl;
/**
 * 
 */


import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.keplerproject.luajava.JavaFunction;
import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaObject;
import org.keplerproject.luajava.LuaState;
import org.keplerproject.luajava.LuaStateFactory;

/**
 * This class is inspired by stevedonovan's AndroLua
 * 
 * @author BHOU
 * 
 */
public class LuaService {
  private final static Logger Log = Logger.getLogger(LuaService.class.getName());

  public final static int LISTEN_PORT = 3333;
  public final static char REPLACE = '\001';
  private static boolean printToString = true;
  private static PrintWriter printer = null;
  private static final StringBuilder output = new StringBuilder();

  private LuaState L = null;

  /**
   * constructor. create a new LuaService with a new luastate
   * 
   * @param startServer
   *          boolean, start a server or not
   */
  public LuaService() {
    L = newState();
  }

  /**
   * get active Luastate
   * 
   * @return the active Luastate
   */
  public LuaState getLuaState() {
    return L;
  }

  /**
   * launch a script by name
   * 
   * @param name
   *          the name of the script, no extension
   * @param nResult
   *          the number of returned value
   */
  public void launch(String name, int nResult) {
    if (findScript(L, name)) {
      L.call(0, nResult);
    } else {
      L.pop(1); // remove the error message from the stack
    }
  }

  /**
   * load a script string, this method will not run the script
   * 
   * @param buffer
   *          the script string
   * @param name
   *          the name of the script, just for debug information
   */
  public void load(String script, String name) {
    byte[] bytes = script.getBytes();
    L.LloadBuffer(bytes, name);
  }

  /**
   * require a lua module
   * 
   * @param mod
   *          module name
   * @return the luaObject otherwise return null
   */
  public LuaObject require(String mod) {
    L.getGlobal("require");
    L.pushString(mod);
    if (L.pcall(1, 1, 0) != 0) {
      Log.log(Level.SEVERE, "require " + L.toString(-1));
      return null;
    }
    return L.getLuaObject(-1);
  }

  /**
   * invoke a method in the module table
   * 
   * @param modTable
   *          the module table
   * @param name
   *          the name of the method
   * @param args
   *          the arguments
   * @return returned object
   */
  public Object invokeMethod(Object modTable, String name, Object... args) {
    if (modTable == null)
      return null;
    Object res = null;
    try {
      LuaObject f = ((LuaObject) modTable).getField(name);
      if (f.isNil())
        return null;
      res = f.call(args);
    } catch (Exception e) {
      Log.log(Level.SEVERE, "method " + name + ": " + e.getMessage());
    }
    return res;
  }

  /**
   * create a new Luastate and initiate the lua loaders, path & cpath. This function calls
   * openLibs() internally, do not call it again after this functions
   * 
   * @return instance of Luastate
   */
  private LuaState newState() {
    L = LuaStateFactory.newLuaState();
    L.openLibs();
    try {
      JavaFunction print = new JavaFunction(L) {
        @Override
        public int execute() throws LuaException {
          for (int i = 2; i <= L.getTop(); i++) {
            int type = L.type(i);
            String stype = L.typeName(type);
            String val = null;
            if (stype.equals("userdata")) {
              Object obj = L.toJavaObject(i);
              if (obj != null) // handle Java objects specially...
                val = obj.toString();
            }
            if (val == null) {
              L.getGlobal("tostring");
              L.pushValue(i);
              L.call(1, 1);
              val = L.toString(-1);
              L.pop(1);
            }
            output.append(val);
            output.append("\t");
          }
          output.append("\n");

          synchronized (L) {
            String out = output.toString();
            if (!printToString && printer != null) {
              printer.println(out + REPLACE);
              printer.flush();
              output.setLength(0);
            } 
          }
          return 0;
        }
      };

      JavaFunction scriptLoader = new JavaFunction(L) {
        @Override
        public int execute() throws LuaException {
          String name = L.toString(-1);
          name = name.replace('.', '/');
          findScript(L, name);
          return 1;
        }
      };

      print.register("print");

      /* comment below is the stack state, left -> right = stack bottom -> top */
      L.getGlobal("package"); // package
      L.getField(-1, "loaders"); // package loaders
      int nLoaders = L.objLen(-1); // package loaders

      L.pushJavaFunction(scriptLoader); // package loaders loader
      L.rawSetI(-2, nLoaders + 1); // package loaders
      L.pop(1); // package

      L.getField(-1, "path"); // package path
      String filesDir = "script";
      String customPath = filesDir + "/?.lua;" + filesDir + "/?/init.lua";
      L.pushString(";" + customPath); // package path custom
      L.concat(2); // package pathCustom
      L.setField(-2, "path"); // package

      L.getField(-1, "cpath"); // package cpath
      L.pushString(";lib/?.dll"); // package cpath customcpath
      L.concat(2); // package newcpath
      L.setField(-2, "cpath"); // package
      L.pop(1);
    } catch (Exception e) {
      Log.log(Level.SEVERE, "Lua: Cannot override print " + e.getMessage());
    }

    return L;
  }

  /**
   * find a script inside of the jar file. If the script is found, a loaded function is pushed onto
   * the stack, otherwise an error message is pushed onto the stack.
   * 
   * @param L
   *          the Lua state
   * @param name
   *          the name of the script without extension
   * @return true if found, false not found
   */
  private static boolean findScript(LuaState L, String name) {
    try {
      byte[] bytes;
      try {
        bytes = readScript("/" + name + ".lua");
      } catch (Exception e) {
        bytes = readScript("/" + name + "/init.lua");
      }

      L.LloadBuffer(bytes, name);
      return true;
    } catch (Exception e) {
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      e.printStackTrace(new PrintStream(os));
      L.pushString("Cannot load module " + name + ":\n" + os.toString());
      return false;
    }
  }

  /**
   * read script inside the jar
   * 
   * @param resource
   *          input script resource path
   * @return byte sequence
   * @throws Exception
   */
  private static byte[] readScript(String resource) throws Exception {
    InputStream is = LuaService.class.getResourceAsStream(resource);
    ByteArrayOutputStream output = new ByteArrayOutputStream(4096);
    byte[] buffer = new byte[4096];
    int n = 0;
    while (-1 != (n = is.read(buffer))) {
      output.write(buffer, 0, n);
    }
    return output.toByteArray();
  }

  String safeEvalLua(String src, String chunkName) {
    String res = null;
    try {
      res = evalLua(src, chunkName);
    } catch (LuaException e) {
      res = e.getMessage() + "\n";
    }
    return res;
  }

  public String evalLua(String src, String chunkName) throws LuaException {
    L.setTop(0);
    int ok = L.LloadBuffer(src.getBytes(), chunkName);
    if (ok == 0) {
      L.getGlobal("debug");
      L.getField(-1, "traceback");
      // stack is now -3 chunk -2 debug -1 traceback
      L.remove(-2);
      L.pushValue(-2);
      printToString = true;
      ok = L.pcall(0, 0, -2);
      printToString = false;
      if (ok == 0) {
        String res = output.toString();
        output.setLength(0);
        return res;
      }
    }
    throw new LuaException("Error code: " + ok + ": " + L.toString(-1));
  }
}
