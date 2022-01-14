/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.state;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import fr.ans.psc.pscload.state.exception.ExtractException;
import fr.ans.psc.pscload.state.exception.LoadProcessException;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * The Class FileDownloaded.
 */
@Slf4j
public class ReadyToExtract extends ProcessState {

    public ReadyToExtract() {
        super();
    }

    @Override
    public void nextStep() throws LoadProcessException {
        try {
            process.setExtractedFilename(unzip(process.getDownloadedFilename()));
        } catch (IOException e) {
            log.error("Error during file extraction, e");
            throw new ExtractException(e);
        }

    }

    /**
     * Unzip.
     *
     * @param zipFilePath the zip file path
     * @return the absolute path of the new extract file
     * @throws IOException io exception
     * @throws ExtractException if
     */
    private String unzip(String zipFilePath) throws IOException, ExtractException {
        File zip = new File(zipFilePath);
        ZipFile zf = new ZipFile(zip);
        File destDir = zip.getParentFile();
        File[] existingFiles = zipsTextsNSers(destDir.listFiles()).get("txts").toArray(new File[0]);
        File newFile = null;
        byte[] buffer = new byte[1024];

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath))) {
            if (zf.size() == 1) {
                ZipEntry zipEntry = zis.getNextEntry();
                while (zipEntry != null) {
                    newFile = newFile(destDir, zipEntry);
                    // check only entries that are files
                    if (!zipEntry.isDirectory()) {
                        // check if newer than what exists, otherwise go to next entry
                        if (!isNew(newFile, existingFiles)) {
                            log.info("{} is not new, will not be extracted", newFile.getName());
                            // Exit here just for case of file exists from a previous process which abort during diff
                            // i.e. OOM, so we can continue
                            zis.closeEntry();
                            zf.close();
                            log.info("Deleting {}", zip.getName());
                            zip.delete();
                            return newFile.getPath();
                        }
                        // fix for Windows-created archives
                        File parent = newFile.getParentFile();
                        if (!parent.isDirectory() && !parent.mkdirs()) {
                            throw new IOException("Failed to create directory " + parent);
                        }
                        // write file content
                        log.info("unzipping into {}", newFile.getName());
                        FileOutputStream fos = new FileOutputStream(newFile);
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                        fos.close();
                        log.info("unzip complete!");
                    }
                    zipEntry = zis.getNextEntry();
                }
                zis.closeEntry();
                zf.close();
                log.info("Deleting {}", zip.getName());
                zip.delete();
                return newFile.getAbsolutePath();
            } else {
                zf.close();
                zip.delete();
                throw new ExtractException("Zip contains multiples files");
            }
        }

    }

    private File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    /**
     * Zips and texts map.
     *
     * @param listOfFiles the list of files
     * @return the map
     * @throws IOException io exception
     */
    private Map<String, List<File>> zipsTextsNSers(File[] listOfFiles) throws IOException {
        Map<String, List<File>> filesMap = new HashMap<>();
        filesMap.put("zips", new ArrayList<>());
        filesMap.put("txts", new ArrayList<>());
        filesMap.put("sers", new ArrayList<>());

        for (File file : listOfFiles != null ? listOfFiles : new File[0]) {
            String type = Files.probeContentType(file.toPath());
            if (file.getName().endsWith(".ser")) {
                filesMap.get("sers").add(file);
            } else if (type != null && type.contains("zip")) {
                filesMap.get("zips").add(file);
            } else if (type != null && type.contains("text")) {
                filesMap.get("txts").add(file);
            }
        }
        return filesMap;
    }

    private int compare(File f1, File f2) {
        try {
            return getDateFromFileName(f1).compareTo(getDateFromFileName(f2));
        } catch (ParseException e) {
            log.error("Error when date compare", e);
            ;
        }
        return 0;
    }

    private boolean isNew(File f1, File[] listF2) {
        if (listF2.length == 0) {
            return true;
        }
        for (File f2 : listF2) {
            if (compare(f1, f2) > 0) {
                return true;
            }
        }
        return false;
    }

    private Date getDateFromFileName(File file) throws ParseException {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMddhhmm");

        String regex = ".*(\\d{12}).*";
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(file.getName());
        if (m.find()) {
            return dateFormatter.parse(m.group(1));
        }
        return new Date(0);
    }

	@Override
	public void write(Kryo kryo, Output output) {
		
	}

	@Override
	public void read(Kryo kryo, Input input) {
		
	}

}
