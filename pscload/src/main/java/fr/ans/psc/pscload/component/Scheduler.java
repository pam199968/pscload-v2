/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.component;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import fr.ans.psc.pscload.metrics.CustomMetrics;
import fr.ans.psc.pscload.service.LoadProcess;
import fr.ans.psc.pscload.state.DiffComputed;
import fr.ans.psc.pscload.state.FileDownloaded;
import fr.ans.psc.pscload.state.FileExtracted;
import fr.ans.psc.pscload.state.Idle;
import fr.ans.psc.pscload.state.ProcessState;
import fr.ans.psc.pscload.state.exception.LoadProcessException;
import lombok.extern.slf4j.Slf4j;

/**
 * The Class Scheduler.
 */
@Slf4j
@Component
public class Scheduler {

	@Autowired
	private ProcessRegistry processRegistry;

	@Autowired
	private CustomMetrics customMetrics;

	@Value("${enable.scheduler:true}")
	private boolean enabled;

	@Value("${extract.download.url}")
	private String extractDownloadUrl;

	@Value("${cert.path}")
	private String certfile;

	@Value("${key.path}")
	private String keyfile;

	@Value("${ca.path}")
	private String cafile;

	@Value("${keystore.password:mysecret}")
	private String kspwd;

	@Value("${files.directory}")
	private String filesDirectory;

	@Value("${use.x509.auth:true}")
	private boolean useX509Auth;

	/**
	 * Run.
	 *
	 * @throws GeneralSecurityException the general security exception
	 * @throws IOException              Signals that an I/O exception has occurred.
	 * @throws DuplicateKeyException    the duplicate key exception
	 */
	@Scheduled(fixedDelayString = "${schedule.rate.ms}")
	public void run() throws GeneralSecurityException, IOException, DuplicateKeyException {
		if (enabled) {
			if (processRegistry.isEmpty()) {
				String id = Integer.toString(processRegistry.nextId());
				ProcessState idle;
				if (useX509Auth) {
					idle = new Idle(keyfile, certfile, cafile, kspwd, extractDownloadUrl, filesDirectory);
				} else {
					idle = new Idle(extractDownloadUrl, filesDirectory);
				}
				LoadProcess process = new LoadProcess(idle);
				processRegistry.register(id, process);
				try {
					// Step 1 : Download
					process.runtask();
					process.setState(new FileDownloaded());
					customMetrics.setStageMetric(10);
					// Step 2 : Extract
					process.runtask();
					process.setState(new FileExtracted());
					customMetrics.setStageMetric(20);
					// Step 4 : Load maps and compute diff
					process.runtask();
					process.setState(new DiffComputed(customMetrics));
					customMetrics.setStageMetric(30);
					// Step 3 : publish metrics
					process.runtask();
					// End of scheduled steps
				} catch (LoadProcessException e) {
					log.error("Error when loading RASS data", e);
					processRegistry.unregister(id);
				}
			} else {
				log.warn("A process is already running !");
			}
		}
	}

}
