/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.component;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import fr.ans.psc.pscload.model.LoadProcess;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * The Class ProcessRegistry.
 */
@Component
@Slf4j
public class ProcessRegistry implements  KryoSerializable  {
	

    @Value("${files.directory}")
    private String filesDirectory;
	
	private Map<String, LoadProcess> registry = new HashMap<String, LoadProcess>();
	
	private int id;
	
	/**
	 * Instantiates a new process registry.
	 */
	public ProcessRegistry() {
	}

	/**
	 * Instantiates a new process registry.
	 *
	 * @param filesDirectory the files directory
	 */
	public ProcessRegistry(String filesDirectory) {
		this.filesDirectory = filesDirectory;
	}

	/**
	 * Next id.
	 *
	 * @return the int
	 */
	public int nextId() {
		return ++id;
	}
	
	/**
	 * Current id.
	 *
	 * @return the int
	 */
	public int currentId() {
		return id;
	}
	
	/**
	 * Register.
	 *
	 * @param id the id
	 * @param process the process
	 * @throws DuplicateKeyException the duplicate key exception
	 */
	public void register(String id, LoadProcess process) throws DuplicateKeyException {
		if(registry.get(id) == null) {
			registry.put(id, process);
			process.setId(id);
		}else {
			throw new DuplicateKeyException(String.format("Process : %s is already registered" , id));
		}
	}
	
	/**
	 * Unregister.
	 *
	 * @param id the id
	 */
	public void unregister(String id) {
		registry.remove(id);
	}
	

	public LoadProcess getCurrentProcess() {
		List<String> sortedKeys=new ArrayList<>(registry.keySet());
		Collections.sort(sortedKeys);
		if (registry.size() > 0) {
		return registry.get(sortedKeys.get(sortedKeys.size()-1));
		}else {
			return null;
		}
			
	}
	
	/**
	 * Gets the process by id.
	 *
	 * @param id the id
	 * @return the process by id
	 */
	public LoadProcess getProcessById(String id) {
		return registry.get(id);		
	}
	
	/**
	 * List.
	 *
	 * @return the list of processes.
	 */
	public List<LoadProcess> list(){
		return List.copyOf(registry.values());
	}
	
	/**
	 * Clear.
	 */
	public void clear() {
		registry.clear();
	}
	
	public boolean isEmpty() {
		return registry.isEmpty();
	}
	@Override
	public void write(Kryo kryo, Output output) {
		output.writeInt(id);
		kryo.writeClassAndObject(output, registry);		
	}

	@SuppressWarnings("unchecked")
	@Override
	public void read(Kryo kryo, Input input) {
		id = input.readInt();
		registry = (Map<String,LoadProcess>)  kryo.readClassAndObject(input);
	}
	
}
