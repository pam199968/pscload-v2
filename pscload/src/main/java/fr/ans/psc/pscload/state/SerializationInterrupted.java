/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.state;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import fr.ans.psc.pscload.state.exception.LoadProcessException;

/**
 * The Class SerializationInterrupted.
 */
public class SerializationInterrupted extends ProcessState {

	/**
	 * Instantiates a new serialization interrupted.
	 */
	public SerializationInterrupted() {
		super();
	}

    @Override
    public void nextStep() throws LoadProcessException {
    }

	@Override
	public void write(Kryo kryo, Output output) {
	}

	@Override
	public void read(Kryo kryo, Input input) {
	}

	@Override
	public boolean isAlreadyComputed() {
		return true;
	}
}
