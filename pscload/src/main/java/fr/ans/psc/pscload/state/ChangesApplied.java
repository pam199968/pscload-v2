/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.state;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import fr.ans.psc.pscload.metrics.CustomMetrics;
import fr.ans.psc.pscload.model.EmailTemplate;
import fr.ans.psc.pscload.model.MapsHandler;
import fr.ans.psc.pscload.state.exception.ExtractTriggeringException;
import fr.ans.psc.pscload.state.exception.SerFileGenerationException;
import fr.ans.psc.pscload.visitor.MapsCleanerVisitorImpl;
import fr.ans.psc.pscload.visitor.MapsVisitor;
import lombok.extern.slf4j.Slf4j;

/**
 * The Class ChangesApplied.
 */
@Slf4j
public class ChangesApplied extends ProcessState {

    private CustomMetrics customMetrics;
    private String extractBaseUrl;

    private final String FAILURE_REPORT_FILENAME = "PSCLOAD_changements_en_échec.";

    /**
     * Instantiates a new changes applied.
     *
     * @param customMetrics the custom metrics
     * @param extractBaseUrl the extract base url
     */
    public ChangesApplied(CustomMetrics customMetrics, String extractBaseUrl) {
        super();
        this.customMetrics = customMetrics;
        this.extractBaseUrl = extractBaseUrl;
    }

    @Override
    public boolean isAlreadyComputed() {
        return true;
    }

    @Override
    public void nextStep() {
    	//First step
    	processRemainingPS();
    	// after this memory is cleared
        callPscExtract();
    }

	private void callPscExtract() throws ExtractTriggeringException{
		RestTemplate restTemplate = new RestTemplate();
		try {
			restTemplate.execute(extractBaseUrl + "/generate-extract", HttpMethod.POST, null, null);
		} catch (RestClientException e) {
			log.info("error when trying to generate extract, return message : {}", e.getLocalizedMessage());
	        throw new ExtractTriggeringException(e);
		}
	}

	private void processRemainingPS() throws SerFileGenerationException {
		MapsHandler newMaps = new MapsHandler();
        String lockedFilePath = process.getTmpMapsPath();
        String serFileName = new File(lockedFilePath).getParent() + File.separator + "maps.ser";
        File lockedSerFile = new File(lockedFilePath);
        File serFile = new File(serFileName);

        try {
        	newMaps.deserializeMaps(lockedFilePath);
        } catch (IOException | ClassNotFoundException e) {
            String msgLogged = e.getClass().equals(IOException.class) ? "Error during deserialization" : "Serialized file not found";
            log.error(msgLogged, e.getLocalizedMessage());
            throw new SerFileGenerationException(e);
        }

        try {
            if (process.isRemainingPsOrStructuresInMaps()) {
                StringBuilder message = new StringBuilder();
                List<String> dataLines = new ArrayList<>();
                
                MapsVisitor cleaner = new MapsCleanerVisitorImpl(newMaps, dataLines);
                // Clean all maps and collect reports infos
                process.getMaps().stream().forEach(map -> {
                	message.append(String.format("{} en échec : {}", map.getOperation().toString(), map.size()));
                	map.accept(cleaner);
                });

                message.append("Si certaines modifications n'ont pas été appliquées, ")
                        .append("vérifiez la plateforme et tentez de relancer le process à partir du endpoint" +
                                " \"resume\"");

                DateFormat df = new SimpleDateFormat("yyyMMddhhmm");
                String now = df.format(new Date());
                File csvOutputFile = new File(serFile.getParent(), FAILURE_REPORT_FILENAME + now + ".csv");
                try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
                    pw.println("Entité/opération;identifiant;Http status");
                    dataLines.stream().forEach(pw::println);
                }
                customMetrics.setStageMetric(70, EmailTemplate.UPLOAD_INCOMPLETE, message.toString(), csvOutputFile);
                csvOutputFile.delete();
            } else {
                customMetrics.setStageMetric(70, EmailTemplate.PROCESS_FINISHED,
                        "Le process PSCLOAD s'est terminé, le fichier " + process.getExtractedFilename() +
                                " a été correctement traité.", null);
            }
            serFile.delete();
            newMaps.serializeMaps(serFileName);
            lockedSerFile.delete();


        } catch (IOException e) {
            log.error("Error during serialization");
            throw new SerFileGenerationException("Error during serialization");
        }
	}


    @Override
	public void write(Kryo kryo, Output output) {
		output.writeString(extractBaseUrl);
	}

	@Override
	public void read(Kryo kryo, Input input) {
        extractBaseUrl = input.readString();
    }
}
