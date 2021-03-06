/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.model.operations;

import fr.ans.psc.pscload.model.entities.RassEntity;
import fr.ans.psc.pscload.visitor.MapsVisitor;
import fr.ans.psc.pscload.visitor.OperationType;

/**
 * The Class PsCreateMap.
 */
public class PsCreateMap extends OperationMap<String, RassEntity> {

	/**
	 * Instantiates a new ps create map.
	 */
	public PsCreateMap() {
		super();

	}

	/**
	 * Instantiates a new ps create map.
	 *
	 * @param operation the operation
	 */
	public PsCreateMap(OperationType operation) {
		super(operation);

	}

	@Override
	public void accept(MapsVisitor visitor) {
		visitor.visit(this);
		
	}

}
