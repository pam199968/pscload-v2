/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.controller;

import fr.ans.psc.pscload.model.MapsHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

/**
 * The Class MaintenanceController.
 */
@Slf4j
@RestController
public class MaintenanceController {

    @Value("${files.directory}")
    private String filesDirectory;

    /**
     * Generate ser file.
     *
     * @param restoreFile the restore file
     * @return the response entity
     */
    @PostMapping(value = "/maintenance/regen-ser-file")
    public ResponseEntity<Void> generateSerFile(@RequestParam MultipartFile restoreFile) {

        try {
            InputStream initialStream = restoreFile.getInputStream();
            byte[] buffer = new byte[initialStream.available()];
            initialStream.read(buffer);

            File origin = File.createTempFile(filesDirectory + File.separator + "restore_extract_RASS", "tmp");
            try (OutputStream out = new FileOutputStream(origin)) {
                out.write(buffer);
            }

            MapsHandler mapsToSerialize = new MapsHandler();
            mapsToSerialize.loadMapsFromFile(origin);
            mapsToSerialize.serializeMaps(filesDirectory + File.separator + "maps.ser");

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IOException e) {
            log.error("Error while generating serialized file");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
