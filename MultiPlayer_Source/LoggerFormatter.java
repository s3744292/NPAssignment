import java.util.Date;
import java.util.logging.*;

/**
 * Format logger
 *
 * @author xiaoyu chen s3517183 from RMIT
 */
public class LoggerFormatter extends Formatter {
    @Override
    public String format(LogRecord logRecord) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[" + new Date() + "]" + " [" + logRecord.getLevel() + "] ");
        stringBuilder.append("| " + logRecord.getLoggerName() + " | "+ ": " + logRecord.getMessage() + "\n");
        return stringBuilder.toString();
    }//end of format
}// end of LoggerFormatter