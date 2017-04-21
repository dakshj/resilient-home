package com.resilienthome.server.ioT.db;

import com.resilienthome.exception.LogFileCreationFailedException;
import com.resilienthome.model.Log;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

class Logger {

    private static final String LOG_FILE_NAME = "Resilient Home Log";
    private static final String LOG_FOLDER_NAME = LOG_FILE_NAME + "s";

    private final Path logPath;
    private final File logFile;

    /**
     * Initializes (and creates the file on disk, if necessary) a {@link File} object pointing to
     * {@value LOG_FILE_NAME}.
     *
     * @param logFileUniqueIdentifier The unique identifier for creating this log file
     */
    Logger(final String logFileUniqueIdentifier) {
        logPath = Paths.get(System.getProperty("user.home"), LOG_FOLDER_NAME);

        logFile = new File(logPath.toFile(), LOG_FILE_NAME + " - "
                + logFileUniqueIdentifier + ".txt");

        initializeLogFile();
    }

    private void initializeLogPath() {
        if (!Files.exists(logPath)) {
            try {
                Files.createDirectories(logPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void initializeLogFile() {
        initializeLogPath();

        if (!logFile.exists()) {
            try {
                if (!logFile.createNewFile()) {
                    throw new LogFileCreationFailedException(logFile.getPath());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private File getLogFile() {
        initializeLogFile();
        return logFile;
    }

    /**
     * Logs the {@link Log} model as a single line to {@value LOG_FILE_NAME}.
     *
     * @param log The object to log
     */
    void log(final Log log) {
        appendLine(log.toString());
    }

    /**
     * Appends a single line to {@value LOG_FILE_NAME}.
     *
     * @param line The line to be appended to {@value LOG_FILE_NAME}
     */
    private void appendLine(final String line) {
        try {
            Files.write(getLogFile().toPath(), Collections.singletonList(line),
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println("Failed to write lines to " + LOG_FILE_NAME + "!");
            e.printStackTrace();
        }
    }

    /**
     * Reads the nth youngest log line from the log file and parses it into a {@link Log} object.
     *
     * @param n How young the element being fetched should be
     *          <p>
     *          E.g.: 1st youngest element --> Last element in the list
     * @return The nth youngest {@link Log}
     */
    Log getNthYoungestLog(final int n) {
        try {
            final List<String> list = Files.lines(getLogFile().toPath())
                    .filter(line -> line != null && !line.equals(""))
                    .collect(Collectors.toList());

            return Log.from(list.get(list.size() - n));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
