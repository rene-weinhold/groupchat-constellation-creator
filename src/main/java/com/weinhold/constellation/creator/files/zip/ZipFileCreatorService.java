package com.weinhold.constellation.creator.files.zip;

import static java.lang.String.format;
import static java.nio.file.Files.readAllBytes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.stereotype.Service;

import com.weinhold.constellation.creator.files.FileCreator;
import com.weinhold.constellation.creator.files.icalendar.ICSFileCreatorService;
import com.weinhold.constellation.creator.files.model.FileCreationInput;
import com.weinhold.constellation.creator.files.workbook.WorkbookFileCreatorService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class ZipFileCreatorService implements FileCreator {

    private final WorkbookFileCreatorService workbookFileCreatorService;
    private final ICSFileCreatorService ICSFileCreatorService;

    /**
     * Creates a ZIP file containing the workbook and ICS files based on the provided input.
     *
     * @param input the input containing constellation ID, entries, dates, people, and year
     * @return the created ZIP file
     */
    @Override
    public File createFile(FileCreationInput input) {
        var workbookFile = workbookFileCreatorService.createFile(input);
        var icsFile = ICSFileCreatorService.createFile(input);

        var zipFileName = format("Constellations-%s.zip", input.getConstellationId());
        var zipFile = new File(zipFileName);
        try (var zipOut = new ZipOutputStream(new FileOutputStream(zipFile))) {
            var workbookEntry = new ZipEntry(workbookFile.getName());
            zipOut.putNextEntry(workbookEntry);
            zipOut.write(readAllBytes(workbookFile.toPath()));
            zipOut.closeEntry();

            var icsEntry = new ZipEntry(icsFile.getName());
            zipOut.putNextEntry(icsEntry);
            zipOut.write(readAllBytes(icsFile.toPath()));
            zipOut.closeEntry();
        } catch (IOException e) {
            log.error("Error creating zip file for constellationId {}: {}", input.getConstellationId(), e.getMessage(), e);
        } finally {
            // Clean up the temporary workbook file
            if (workbookFile != null && workbookFile.exists()) {
                if (!workbookFile.delete()) {
                    log.warn("Could not delete temporary workbook file: {}", workbookFile.getAbsolutePath());
                }
            }
        }
        return zipFile;
    }
}
