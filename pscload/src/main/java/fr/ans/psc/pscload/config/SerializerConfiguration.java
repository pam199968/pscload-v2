/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.config;

import com.esotericsoftware.kryo.Kryo;
import fr.ans.psc.pscload.component.ProcessRegistry;
import fr.ans.psc.pscload.model.LoadProcess;
import fr.ans.psc.pscload.model.entities.*;
import fr.ans.psc.pscload.model.operations.*;
import fr.ans.psc.pscload.state.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Class SerializerConfiguration.
 */
@Configuration
public class SerializerConfiguration {

	@Bean
	public Kryo getKryo() {
		Kryo kryo = new Kryo();
		kryo.register(HashMap.class, 9);
		kryo.register(ArrayList.class, 10);
		kryo.register(Professionnel.class, 11);
		kryo.register(ExerciceProfessionnel.class, 12);
		kryo.register(SavoirFaire.class, 13);
		kryo.register(SituationExercice.class, 14);
		kryo.register(RefStructure.class, 15);
		kryo.register(Structure.class, 16);
		kryo.register(ProcessRegistry.class, 17);
		kryo.register(LoadProcess.class, 18);
		kryo.register(ProcessState.class, 19);
		kryo.register(Submitted.class, 20);
		kryo.register(DiffComputed.class, 21);
		kryo.register(ReadyToComputeDiff.class, 22);
		kryo.register(ReadyToExtract.class, 23);
		kryo.register(UploadingChanges.class, 24);
		kryo.register(ChangesApplied.class, 25);
		kryo.register(String[].class, 27);
		kryo.register(ConcurrentHashMap.class, 28);
		kryo.register(UploadInterrupted.class, 29);
		kryo.register(SerializationInterrupted.class, 30);
		kryo.register(OperationMap.class, 31);
		kryo.register(PsCreateMap.class, 32);
		kryo.register(PsUpdateMap.class, 33);
		kryo.register(PsDeleteMap.class, 34);
		kryo.register(StructureCreateMap.class, 35);
		kryo.register(StructureUpdateMap.class, 36);
		return kryo;
	}
}
