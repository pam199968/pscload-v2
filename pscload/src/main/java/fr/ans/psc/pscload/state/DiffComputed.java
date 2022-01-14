/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.state;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import fr.ans.psc.pscload.metrics.CustomMetrics;
import fr.ans.psc.pscload.model.entities.RassEntity;
import fr.ans.psc.pscload.model.operations.OperationMap;
import fr.ans.psc.pscload.visitor.MapsMetricsSetterVisitorImpl;
import fr.ans.psc.pscload.visitor.MapsVisitor;
import lombok.extern.slf4j.Slf4j;

/**
 * The Class DiffComputed.
 */
@Slf4j
public class DiffComputed extends ProcessState {

	private CustomMetrics customMetrics;

	/**
	 * Instantiates a new diff computed.
	 *
	 * @param customMetrics the custom metrics
	 */
	public DiffComputed(CustomMetrics customMetrics) {
		super();
		this.customMetrics = customMetrics;
	}

	public DiffComputed() {
		super();
	}

	@Override
	public void nextStep() {
		log.info("DiffComputed: nextStep()");

		MapsVisitor visitor = new MapsMetricsSetterVisitorImpl(customMetrics);
		for (OperationMap<String, RassEntity> map : process.getMaps()) {
			map.accept(visitor);
		}
	}

	@Override
	public boolean isAlreadyComputed() {
		return true;
	}


	@Override
	public void write(Kryo kryo, Output output) {
	}

	@Override
	public void read(Kryo kryo, Input input) {
	}

	public CustomMetrics getCustomMetrics() {
		return customMetrics;
	}
}
