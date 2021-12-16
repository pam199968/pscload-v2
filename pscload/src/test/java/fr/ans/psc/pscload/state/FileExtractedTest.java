/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.state;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import fr.ans.psc.pscload.service.LoadProcess;
import fr.ans.psc.pscload.state.exception.LoadProcessException;
import lombok.extern.slf4j.Slf4j;

/**
 * The Class FileExtractedTest.
 */
@Slf4j
class FileExtractedTest {

	/**
	 * Initial diff task test.
	 *
	 * @throws Exception the exception
	 */
	@Test
	@DisplayName("Initial diff with no old ser file and 5 ps")
	void initialDiffTaskTest() throws Exception {
		String rootpath = Thread.currentThread().getContextClassLoader().getResource(".").getPath();
		File mapser = new File(rootpath + File.separator + "maps.ser");
		if (mapser.exists()) {
			mapser.delete();
		}
		LoadProcess p = new LoadProcess(new FileExtracted());
		p.setExtractedFilename(Thread.currentThread().getContextClassLoader()
				.getResource("Extraction_ProSanteConnect_Personne_activite_202112120512.txt").getPath());
		p.runtask();
		assertEquals(5, p.getPsToCreate().size());
		assertEquals(0, p.getPsToDelete().size());
	}

	/**
	 * Diff task test.
	 *
	 * @throws Exception the exception
	 */
	@Test
	@DisplayName(" diff with 1 supp, 2 modifs and 1 add")
	void diffTaskTest() throws Exception {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		String rootpath = cl.getResource(".").getPath();
		File mapser = new File(rootpath + File.separator + "maps.ser");
		if (mapser.exists()) {
			mapser.delete();
		}
		LoadProcess p = new LoadProcess(new FileExtracted());
		p.setExtractedFilename(cl.getResource("Extraction_ProSanteConnect_Personne_activite_202112120512.txt").getPath());
		p.runtask();
		LoadProcess p2 = new LoadProcess(new FileExtracted());
		p2.setExtractedFilename(cl.getResource("Extraction_ProSanteConnect_Personne_activite_202112120515.txt").getPath());
		p2.runtask();
		assertEquals(1,p2.getPsToDelete().size());
		assertEquals(1,p2.getPsToCreate().size());
		assertEquals(2, p2.getPsToUpdate().size());
	}

	@Test
	@DisplayName("initial diff from large file (100000 lines)")
	public void diffFromLargeFile() throws Exception {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		String rootpath = cl.getResource(".").getPath();
		File mapser = new File(rootpath + File.separator + "maps.ser");
		if (mapser.exists()) {
			mapser.delete();
		}
		LoadProcess p = new LoadProcess(new FileExtracted());
		p.setExtractedFilename(cl.getResource("Extraction_ProSanteConnect_Personne_activite_202112140852.txt").getPath());
		p.runtask();
		assertEquals(p.getPsToCreate().size(), 99171);
		assertEquals(p.getStructureToCreate().size(), 37534);
	}
}
