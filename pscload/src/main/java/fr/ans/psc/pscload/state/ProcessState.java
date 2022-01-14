/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.state;

import com.esotericsoftware.kryo.KryoSerializable;
import fr.ans.psc.pscload.model.LoadProcess;
import fr.ans.psc.pscload.state.exception.LoadProcessException;

/**
 * The Class ProcessState.
 */
public abstract class ProcessState implements KryoSerializable {
	
	/** The process. */
	protected LoadProcess process;

	public boolean isAlreadyComputed() {
		return false;
	}

	/**
	 * Instantiates a new process state.
	 */
	// Constructor for deserializing process.
	public ProcessState() {}

	/**
	 * Run task.
	 *
	 * @throws LoadProcessException the load process exception
	 */
	abstract public void nextStep() throws LoadProcessException;
	
	public LoadProcess getProcess() {
		return process;
	}

	public void setProcess(LoadProcess process) {
		this.process = process;
	}

}
