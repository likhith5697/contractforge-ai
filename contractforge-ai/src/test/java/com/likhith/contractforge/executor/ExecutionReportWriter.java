package com.likhith.contractforge.executor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.likhith.contractforge.executor.model.TestRunReport;

/**
 * Writes the run report to {@code <reportDirectory>/<artifactId>-<timestamp>.json},
 * overwriting the same file as the report grows across scenarios. The
 * filename is fixed the first time this instance writes, based on the
 * report's {@code startedAt}, so repeated calls for the same run always land
 * in the same file.
 */
public class ExecutionReportWriter {

    private static final Logger log = LoggerFactory.getLogger(ExecutionReportWriter.class);
    private static final String DEFAULT_REPORT_DIRECTORY = "target/contractforge/reports";
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").withZone(ZoneOffset.UTC);

    private final Path reportDirectory;
    // Plain Jackson defaults to epoch-timestamp serialization for Instant, unlike Spring
    // Boot's auto-configured ObjectMapper used elsewhere in this project - disabled here so
    // the report reads the same human-friendly ISO-8601 way everywhere else does.
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private Path reportFile;

    public ExecutionReportWriter() {
        this.reportDirectory = Path.of(
                System.getProperty("contractforge.artifacts.report-directory", DEFAULT_REPORT_DIRECTORY));
    }

    /** Never throws - a report-writing hiccup must never mask a scenario's real pass/fail outcome. */
    public void writeSafely(TestRunReport report) {
        try {
            write(report);
        } catch (IOException ex) {
            log.warn("Failed to write execution report (test results are unaffected): {}", ex.getMessage());
        }
    }

    public Path write(TestRunReport report) throws IOException {
        Files.createDirectories(reportDirectory);
        if (reportFile == null) {
            reportFile = reportDirectory.resolve(report.artifactId() + "-" + TIMESTAMP_FORMAT.format(report.startedAt()) + ".json");
        }
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(reportFile.toFile(), report);
        return reportFile;
    }
}
