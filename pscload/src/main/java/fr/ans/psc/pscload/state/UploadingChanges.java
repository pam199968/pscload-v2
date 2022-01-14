/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.state;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import fr.ans.psc.pscload.model.entities.RassEntity;
import fr.ans.psc.pscload.model.operations.OperationMap;
import fr.ans.psc.pscload.state.exception.LoadProcessException;
import fr.ans.psc.pscload.visitor.MapsUploaderVisitorImpl;
import fr.ans.psc.pscload.visitor.MapsVisitor;
import lombok.extern.slf4j.Slf4j;

/**
 * The Class UploadingChanges.
 */
@Slf4j
public class UploadingChanges extends ProcessState {


    private String[] excludedProfessions;

    private String apiBaseUrl;

    /**
     * Instantiates a new Uploading Changes.
     *
     * @param excludedProfessions the excluded professions
     * @param apiBaseUrl          the api base url
     */
    public UploadingChanges(String[] excludedProfessions, String apiBaseUrl) {
        this.excludedProfessions = excludedProfessions;
        this.apiBaseUrl = apiBaseUrl;
    }

    /**
     * Instantiates a new uploading changes.
     */
    public UploadingChanges() {
		super();
	}

	@Override
    public void nextStep() throws LoadProcessException {
        log.info("UploadingChanges: nextStep()");
    	
		MapsVisitor visitor = new MapsUploaderVisitorImpl(excludedProfessions, apiBaseUrl);
		for (OperationMap<String, RassEntity> map : process.getMaps()) {
			map.accept(visitor);
		}
    }

    @Override
	public void write(Kryo kryo, Output output) {
    	kryo.writeClassAndObject(output, excludedProfessions);
    	output.writeString(apiBaseUrl);
    }

    @Override
	public void read(Kryo kryo, Input input) {
        excludedProfessions = (String[]) kryo.readClassAndObject(input);
        apiBaseUrl = input.readString();
    }

    @Override
    public boolean isAlreadyComputed() {
        return true;
    }

}
