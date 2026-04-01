package com.project.edusync.superadmin.service.impl;

import com.project.edusync.common.exception.EdusyncException;
import com.project.edusync.superadmin.model.dto.LogEntryDto;
import com.project.edusync.superadmin.model.dto.LogTailResponseDto;
import com.project.edusync.superadmin.service.ApplicationLogService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ApplicationLogServiceImpl implements ApplicationLogService {

    private static final int DEFAULT_LINES = 200;
    private static final int MAX_LINES = 2000;
    private static final Set<String> ALLOWED_LEVELS = Set.of("ERROR", "WARN", "INFO", "DEBUG");

    private static final Pattern LOG_PATTERN = Pattern.compile(
            "^(\\S+)\\s+(TRACE|DEBUG|INFO|WARN|ERROR)\\s+.*?\\[([^\\]]+)]\\s+([^:]+)\\s*:\\s*(.*)$"
    );

    @Value("${logging.file.name:}")
    private String configuredLogFile;

    @Override
    public LogTailResponseDto tailLogs(Integer lines, String level) {
        int requestedLines = normalizeLines(lines);
        String normalizedLevel = normalizeLevel(level);

        Path logPath = resolveConfiguredLogPath();
        List<LogEntryDto> entries = tailEntries(logPath.toFile(), requestedLines, normalizedLevel);

        return new LogTailResponseDto(logPath.toString(), entries.size(), entries);
    }

    private Path resolveConfiguredLogPath() {
        if (configuredLogFile == null || configuredLogFile.isBlank()) {
            throw new EdusyncException("No log file configured. Set logging.file.name to enable this endpoint.", HttpStatus.NOT_FOUND);
        }

        Path path = Paths.get(configuredLogFile);
        if (!path.isAbsolute()) {
            path = path.toAbsolutePath().normalize();
        }

        if (!path.toFile().exists() || !path.toFile().isFile()) {
            throw new EdusyncException("Configured log file not found: " + path, HttpStatus.NOT_FOUND);
        }

        return path;
    }

    private List<LogEntryDto> tailEntries(File file, int maxResults, String levelFilter) {
        List<LogEntryDto> reversedEntries = new ArrayList<>();

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            long pointer = raf.length() - 1;
            StringBuilder lineBuilder = new StringBuilder();

            while (pointer >= 0 && reversedEntries.size() < maxResults) {
                raf.seek(pointer);
                int read = raf.read();

                if (read == '\n') {
                    appendIfMatches(reversedEntries, lineBuilder, levelFilter);
                } else if (read != '\r') {
                    lineBuilder.append((char) read);
                }
                pointer--;
            }

            if (!lineBuilder.isEmpty() && reversedEntries.size() < maxResults) {
                appendIfMatches(reversedEntries, lineBuilder, levelFilter);
            }
        } catch (IOException ex) {
            throw new EdusyncException("Unable to read application log file.", HttpStatus.INTERNAL_SERVER_ERROR, ex);
        }

        Collections.reverse(reversedEntries);
        return reversedEntries;
    }

    private void appendIfMatches(List<LogEntryDto> collector, StringBuilder reversedLine, String levelFilter) {
        String line = reversedLine.reverse().toString();
        reversedLine.setLength(0);

        if (line.isBlank()) {
            return;
        }

        LogEntryDto entry = parseEntry(line);
        if (levelFilter == null || levelFilter.equals(entry.level())) {
            collector.add(entry);
        }
    }

    private LogEntryDto parseEntry(String line) {
        Matcher matcher = LOG_PATTERN.matcher(line);
        if (!matcher.matches()) {
            return new LogEntryDto(null, "INFO", "unknown", "unknown", line);
        }

        return new LogEntryDto(
                matcher.group(1),
                matcher.group(2),
                matcher.group(4).trim(),
                matcher.group(3).trim(),
                matcher.group(5)
        );
    }

    private int normalizeLines(Integer lines) {
        if (lines == null || lines <= 0) {
            return DEFAULT_LINES;
        }
        return Math.min(lines, MAX_LINES);
    }

    private String normalizeLevel(String level) {
        if (level == null || level.isBlank()) {
            return null;
        }
        String normalized = level.trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_LEVELS.contains(normalized)) {
            throw new EdusyncException("Invalid log level. Allowed values: ERROR, WARN, INFO, DEBUG.", HttpStatus.BAD_REQUEST);
        }
        return normalized;
    }
}

