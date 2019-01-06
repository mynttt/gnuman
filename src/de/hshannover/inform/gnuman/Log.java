package de.hshannover.inform.gnuman;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A simple global logger that logs stuff. Because java.utils.logger is too much of a hassle to use for a small project on this scale.<p>
 *
 * This logger works with log levels, the minLogLevel decides what is to be logged.<br>
 * The hierarchy goes as this: <code>Debug > Info > Warning > Critical</code><p>
 *
 * If your level is set to DEBUG then you'll see DEBUG and all levels larger.<br>
 * If your level is set to INFO then you'll see INFO and all levels larger.<p>
 *
 * The lower your level is, the more information you're able to log.<br>
 * You'll need to instantiate the logger with the initiate() method first, then you can access it as a static class.<p>
 *
 * If the logger fails to set itself up you can retrieve an error boolean.<br>
 * You can suspend the logger via suspend(), it will free the resources, close the file and stop logging.<br>
 * The logger also ships with a global prefix, you can set that with setPrefix() and enablePrefix(). <p>
 *
 * If you log without a defined class, the logger will log with a placeholder instead. <br>
 * You can change that via setLocationDefault(), the default is set to "Unknown". <p>
 *
 * Feel free to customize the log format in the private log() method.
 *
 * @author Marc Herschel
 */

public class Log {
    private static final String DEFAULT_PREFIX = "DEF_PREFIX";
    private static final String DEFAULT_LOCATION = "Unknown";
    private static Log instance = null;
    private static boolean hasBeenBootstrapped = false;
    private FileOutputStream outputStream;
    private BufferedWriter writer;
    private LogLevel minLogLevel;
    private String timeFormat, file, prefix, location;
    private LogState state;
    private boolean prefixEnabled, timestampEnabled;

    /**
     * Levels from low (Log all) to high (Filter)
     */
    public enum LogLevel { DEBUG, INFO, WARNING, CRITICAL }

    /**
     * States that the logger can be in.
     */
    public enum LogState { UNDEFINED, READY, ERROR, SUSPENDED }

    private Log(String file, LogLevel minLogLevel, String timeFormat) {
        this.location = DEFAULT_LOCATION;
        this.state = LogState.UNDEFINED;
        this.timeFormat = timeFormat;
        this.minLogLevel = minLogLevel;
        this.file = file;
        setPrefix(DEFAULT_PREFIX);
        boot(false);
        hasBeenBootstrapped = true;
    }

    /**
     * Initiate the logger with all values needed.
     * @param file file to save the log to
     * @param minLogLevel The log level you want to log, the lower your level is, the more information you'll be able to log.
     * @param timeFormat A string containing a SimpleDateFormat valid format string.
     */
    public static void bootstrapLogger(String file, LogLevel minLogLevel, String timeFormat) {
        if(instance == null) { instance = new Log(file, minLogLevel, timeFormat); } else { System.out.println("Logger already bootstrapped!"); }
    }

    /**
     * Returns the instance, dies if not instantiated.
     * @return null if not instantiated, else logger.
     */
    public static Log logger() {
        if(instance == null) { System.out.println("Log: Invalid operation. Bootstrap the logger first!"); System.exit(-1); }
        return instance;
    }

    /**
     * @return true if bootstrapping has been successful at least one time.
     */
    public static boolean hasBeenBootstrapped() { return hasBeenBootstrapped; }

    /**
     * Suspend the logger from writing.
     */
    public void suspend() {
        if(state != LogState.READY) { return; }
        try {
            writer.close(); outputStream.close();
            state = LogState.SUSPENDED;
        } catch (IOException e) {
            System.out.println("Log: Failed to shutdown logger I/O.");
            state = LogState.ERROR;
        }
    }

    /**
     * Resumes logger after suspending it.
     */
    public void resume() { if(state == LogState.SUSPENDED) boot(true); }

    /**
     * Resets the logger if suspended.
     */
    public void reset() { if(state == LogState.SUSPENDED) boot(false); }

    /**
     * @return current state of logger.
     */
    public LogState state() { return state; }

    /**
     * Enable the prefix.
     */
    public void enablePrefix() { prefixEnabled = true; }

    /**
     * Disable the prefix.
     */
    public void disablePrefix() { prefixEnabled = false; }

    /**
     * Enable timestamp
     */
    public void enableTimestamp() { timestampEnabled = true; }

    /**
     * Disable timestamp
     */
    public void disableTimestamp() { timestampEnabled = false; }

    /**
     * Set a new prefix.
     * @param prefix String representing the prefix.
     */
    public void setPrefix(String prefix) {
        if (prefix != null && prefix.trim().length() > 0) { this.prefix = "["+prefix.trim()+"] "; }
    }

    /**
     * Set a new placeholder for default locations.
     * @param location String representing the location to log from (for example a class name, or filename).
     */
    public void setDefaultLocationPlaceholder(String location) {
        if(location != null && location.trim().length() > 0) { this.location = location.trim(); }
    }

    /**
     * @param data Data to log.
     */
    public static void info(String data) { info(instance.location, data); }

    /**
     * @param location Name of a location (for example a class name, or filename)
     * @param data Data to log.
     */
    public static void info(String location, String data) {
        if(instance == null) { return; }
        if(instance.logThis(LogLevel.INFO)) { instance.log(LogLevel.INFO, location + " :: " + data); }
    }

    /**
     * @param data Data to log.
     */
    public static void debug(String data) { debug(instance.location, data); }

    /**
     * @param location Name of a location (for example a class name, or filename)
     * @param data Data to log.
     */
    public static void debug(String location, String data) {
        if(instance == null) { return; }
        if(instance.logThis(LogLevel.DEBUG)) { instance.log(LogLevel.DEBUG, location + " :: " + data); }
    }

    /**
     * @param data Data to log.
     */
    public static void warning(String data) { warning(instance.location, data); }

    /**
     * @param location Name of a location (for example a class name, or filename)
     * @param data Data to log.
     */
    public static void warning(String location, String data) {
        if(instance == null) { return; }
        if(instance.logThis(LogLevel.WARNING)) { instance.log(LogLevel.WARNING, location + " :: " + data); }
    }

    /**
     * @param data Data to log.
     */
    public static void critical(String data) { critical(instance.location, data); }

    /**
     * @param location Name of a location (for example a class name, or filename)
     * @param data Data to log.
     */
    public static void critical(String location, String data) {
        if(instance == null) { return; }
        if(instance.logThis(LogLevel.CRITICAL)) { instance.log(LogLevel.CRITICAL, location + " :: " + data); }
    }

    /**
     * Decide if we log.
     * @param lvl to log
     * @return Logging allowed?
     */
    private boolean logThis(LogLevel lvl) { return state == LogState.READY && (lvl.ordinal() >= minLogLevel.ordinal()); }

    /**
     * Does the logging operation.
     * @param lvl Level to log.
     * @param rawdata String containing the data to log.
     */
    private void log(LogLevel lvl, String rawdata) {
        String formatted = String.format("%s[%-8s] [%s] :: %s%s",
                                prefixEnabled ? instance.prefix : "",
                                lvl.toString(),
                                timestampEnabled ? System.currentTimeMillis() : new SimpleDateFormat(timeFormat).format(new Date()),
                                rawdata,
                                System.lineSeparator()
                          );
        try {
            writer.write(formatted); writer.flush(); outputStream.flush();
        } catch (IOException e) {
            System.out.println("Log: Logging failed. \n\t-> " + formatted);
        }
    }

    /**
     * Start the underlying IO.
     * @param append if appending to file.
     */
    private void boot(boolean append) {
        if(state == LogState.READY) { return; }
        try {
            outputStream = new FileOutputStream(file, append);
            writer = new BufferedWriter(new OutputStreamWriter(outputStream));
            state = LogState.READY;
        } catch (FileNotFoundException e) {
            state = LogState.ERROR;
            System.out.println("Log: Failed to boot logger I/O.");
        }
    }
}
