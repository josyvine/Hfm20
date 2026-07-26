package com.vineyard.hfm.app;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Deep Diagnostic Logging System for HFM.
 * Writes detailed diagnostic metrics to the public folder:
 * /storage/emulated/0/hfm log report/hfm_diagnostic_log.txt
 */
public class AppLogger {

    private static final String TAG = "HFM_AppLogger";
    private static final String LOG_FOLDER_NAME = "hfm log report";
    private static final String LOG_FILE_NAME = "hfm_diagnostic_log.txt";

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);

    /**
     * Retrieves or creates the public log directory.
     * Path: /storage/emulated/0/hfm log report/
     */
    public static File getPublicLogFolder() {
        File publicDir = Environment.getExternalStorageDirectory();
        File logDir = new File(publicDir, LOG_FOLDER_NAME);
        if (!logDir.exists()) {
            boolean created = logDir.mkdirs();
            Log.d(TAG, "Created public log folder: " + logDir.getAbsolutePath() + " -> " + created);
        }
        return logDir;
    }

    /**
     * Retrieves the log file reference inside the public folder.
     */
    public static File getLogFile() {
        return new File(getPublicLogFolder(), LOG_FILE_NAME);
    }

    /**
     * Writes a line to the log file in the public folder.
     */
    public static synchronized void log(String tag, String message) {
        String timestamp = DATE_FORMAT.format(new Date());
        String formattedLine = String.format(Locale.US, "[%s] [%s] %s\n", timestamp, tag, message);

        // Print to logcat
        Log.d(tag, message);

        BufferedWriter writer = null;
        try {
            File logFile = getLogFile();
            writer = new BufferedWriter(new FileWriter(logFile, true));
            writer.write(formattedLine);
            writer.flush();
        } catch (IOException e) {
            Log.e(TAG, "Failed to write diagnostic log line to public folder", e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ignored) {}
            }
        }
    }

    /**
     * Logs operation performance metrics including exact execution duration in milliseconds.
     */
    public static void logMetric(String tag, String operation, long durationMs, String details) {
        String msg = String.format(Locale.US, "METRIC | Op: %s | Duration: %d ms | Details: %s", operation, durationMs, details);
        log(tag, msg);
    }

    /**
     * Logs detailed errors along with complete Java stack traces.
     */
    public static synchronized void logError(String tag, String message, Throwable throwable) {
        StringBuilder errorMsg = new StringBuilder();
        errorMsg.append("ERROR | ").append(message);

        if (throwable != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            errorMsg.append("\nStackTrace:\n").append(sw.toString());
        }

        log(tag, errorMsg.toString());
    }

    /**
     * Clears previous log entries in the public log file.
     */
    public static synchronized void clearLog() {
        File logFile = getLogFile();
        if (logFile.exists()) {
            boolean deleted = logFile.delete();
            Log.d(TAG, "Public log file cleared: " + deleted);
        }
    }
}