/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.model;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import fr.ans.psc.pscload.model.entities.RassEntity;
import fr.ans.psc.pscload.model.operations.OperationMap;
import fr.ans.psc.pscload.model.operations.OperationMapFactory;
import fr.ans.psc.pscload.state.ProcessState;
import fr.ans.psc.pscload.state.exception.LoadProcessException;
import fr.ans.psc.pscload.visitor.OperationType;
import lombok.Getter;
import lombok.Setter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * The Class LoadProcess.
 */
@Getter
@Setter
public class LoadProcess implements KryoSerializable {

	private String downloadedFilename;

	private String extractedFilename;

	private String tmpMapsPath;

	private List<OperationMap<String, RassEntity>> maps = new ArrayList<>();

	private long timestamp;

	private ProcessState state;

	private String id;
	
	/**
	 * Instantiates a new load process.
	 */
	public LoadProcess() {
		super();
		init();
	}

	/**
	 * Instantiates a new load process.
	 *
	 * @param state the state
	 */
	public LoadProcess(ProcessState state) {
		super();
		this.state = state;
		this.state.setProcess(this);
		init();
	}

	private void init() {
		timestamp = Calendar.getInstance().getTimeInMillis();
		Arrays.asList(OperationType.values()).forEach(operation -> {
			maps.add(OperationMapFactory.getOperationMap(operation));
		});
	}

	/**
	 * Instantiates a new load process.
	 *
	 * @param state the state
	 * @param id    the id
	 */
	public LoadProcess(ProcessState state, String id) {
		this(state);
		this.id = id;
	}

	/**
	 * Runtask.
	 *
	 * @throws LoadProcessException the load process exception
	 */
	public void nextStep() throws LoadProcessException {
		state.nextStep();
	}

	public void setState(ProcessState state) {
		this.state = state;
		state.setProcess(this);
	}

	public boolean isRemainingPsOrStructuresInMaps() {
		int count = 0;
		for (@SuppressWarnings("rawtypes") OperationMap map : maps) {
			count += map.size();
		}
		return count > 0;

	}

	public ProcessInfo getProcessInfos() {
		ProcessInfo processInfo = new ProcessInfo();
		processInfo.setProcessId(id);
		DateFormat df = new SimpleDateFormat();
		processInfo.setCreatedOn(df.format(new Date(timestamp)));
		processInfo.setState(state.getClass().getSimpleName());
		processInfo.setDownloadedFileName(downloadedFilename);
		processInfo.setExtractFileName(extractedFilename);
		processInfo.setLockedSerializedFileName(tmpMapsPath);
		if (state.isAlreadyComputed()) {
			processInfo.setPsToCreate(maps.stream().filter(map -> map.getOperation().equals(OperationType.PS_CREATE))
					.findFirst().get().size());
			processInfo.setPsToUpdate(maps.stream().filter(map -> map.getOperation().equals(OperationType.PS_UPDATE))
					.findFirst().get().size());
			processInfo.setPsToDelete(maps.stream().filter(map -> map.getOperation().equals(OperationType.PS_DELETE))
					.findFirst().get().size());
			processInfo.setStructureToCreate(maps.stream()
					.filter(map -> map.getOperation().equals(OperationType.STRUCTURE_CREATE)).findFirst().get().size());
			processInfo.setStructureToUpdate(maps.stream()
					.filter(map -> map.getOperation().equals(OperationType.STRUCTURE_UPDATE)).findFirst().get().size());
		}

		return processInfo;
	}

	@Override
	public void write(Kryo kryo, Output output) {
		output.writeString(id);
		output.writeLong(timestamp);
		output.writeString(downloadedFilename);
		output.writeString(extractedFilename);
		// We need to write the class also because state is an abstract class (hope
		// never null)
		kryo.writeClassAndObject(output, state);
		kryo.writeObjectOrNull(output, maps, ArrayList.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void read(Kryo kryo, Input input) {
		id = input.readString();
		timestamp = input.readLong();
		downloadedFilename = input.readString();
		extractedFilename = input.readString();
		state = (ProcessState) kryo.readClassAndObject(input);
		maps = (List<OperationMap<String, RassEntity>>) kryo.readObjectOrNull(input, ArrayList.class);
	}

}
