/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.model.operations;

import fr.ans.psc.pscload.model.entities.RassEntity;
import fr.ans.psc.pscload.visitor.OperationType;
import fr.ans.psc.pscload.visitor.Visitable;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The Class OperationMap.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
@Getter
@Setter
public abstract class OperationMap<K, V> extends ConcurrentHashMap<String, RassEntity> implements Visitable {

	private OperationType operation;

	private ConcurrentMap<String, RassEntity> oldValues = new ConcurrentHashMap<>();

	
	/**
	 * Instantiates a new operation map.
	 */
	public OperationMap() {
		super();
	}

	/**
	 * Instantiates a new operation map.
	 *
	 * @param operation the operation
	 */
	public OperationMap(OperationType operation) {
		super();
		this.operation = operation;
	}

	/**
	 * Save old value.
	 *
	 * @param key the key
	 * @param value the value
	 */
	public void saveOldValue(String key, RassEntity value) {
		oldValues.put(key, value);
	}

	/**
	 * Gets the old value.
	 *
	 * @param key the key
	 * @return the old value
	 */
	public RassEntity getOldValue(String key) {
		return oldValues.get(key);
	}

}
