package mimosa.repl.test;

import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LoggerFormat extends Formatter {

  private static final MessageFormat messageFormat = new MessageFormat(
      "[{1}|{2,date,h:mm:ss}]: {3}\n");

  public LoggerFormat() {
    super();
  }

  @Override
  public String format(LogRecord record) {
    Object[] arguments = new Object[6];
    // arguments[0] = record.getLoggerName();
    arguments[1] = record.getLevel();
    arguments[2] = new Date(record.getMillis());
    arguments[3] = record.getMessage();
    return messageFormat.format(arguments);
  }

}
