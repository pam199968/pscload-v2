/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.visitor;

import fr.ans.psc.pscload.model.MapsHandler;
import fr.ans.psc.pscload.model.entities.Professionnel;
import fr.ans.psc.pscload.model.entities.RassEntity;
import fr.ans.psc.pscload.model.entities.Structure;
import fr.ans.psc.pscload.model.operations.*;
import org.springframework.http.HttpStatus;

import java.util.Collection;
import java.util.List;

/**
 * The Class MapsCleanerVisitorImpl.
 */
public class MapsCleanerVisitorImpl implements MapsVisitor {

	private MapsHandler maps;
	
	private List<String> report;

	/**
	 * Instantiates a new maps cleaner visitor impl.
	 *
	 * @param maps the maps
	 */
	public MapsCleanerVisitorImpl(MapsHandler maps, List<String> report) {
		super();
		this.maps = maps;
		this.report = report;
	}

	@Override
	public void visit(PsCreateMap map) {
		Collection<RassEntity> items = map.values();
		items.forEach(item -> {
			generateReportLine(map, report, item);
			if (isInconsistentWithDatabase(item.getReturnStatus())) {
				maps.getPsMap().remove(item.getInternalId());
			}
		});
	}

	@Override
	public void visit(PsUpdateMap map) {
		Collection<RassEntity> items = map.values();
		items.forEach(item -> {
			generateReportLine(map, report, item);
			if (isInconsistentWithDatabase(item.getReturnStatus())) {
				maps.getPsMap().replace(item.getInternalId(), (Professionnel) map.getOldValue(item.getInternalId()));
			}
		});
	}

	@Override
	public void visit(PsDeleteMap map) {
		Collection<RassEntity> items = map.values();
		items.forEach(item -> {
			generateReportLine(map, report, item);
			if (isInconsistentWithDatabase(item.getReturnStatus())) {
				maps.getPsMap().put(item.getInternalId(), (Professionnel) item);
			}
		});
	}

	@Override
	public void visit(StructureCreateMap map) {
		Collection<RassEntity> items = map.values();
		items.forEach(item -> {
			generateReportLine(map, report, item);
			if (isInconsistentWithDatabase(item.getReturnStatus())) {
				maps.getStructureMap().remove(item.getInternalId());
			}
		});

	}

	@Override
	public void visit(StructureUpdateMap map) {
		Collection<RassEntity> items = map.values();
		items.forEach(item -> {
			generateReportLine(map, report, item);
			if (isInconsistentWithDatabase(item.getReturnStatus())) {
				maps.getStructureMap().replace(item.getInternalId(), (Structure) map.getOldValue(item.getInternalId()));
			}
		});
	}

	private boolean isInconsistentWithDatabase(int rawReturnStatus) {
		HttpStatus status = HttpStatus.valueOf(rawReturnStatus);

		// CONFLICT and GONE mean that the db is already in the state we want
		return !status.equals(HttpStatus.CONFLICT) && !status.equals(HttpStatus.GONE);
	}

	private void generateReportLine(OperationMap<String, RassEntity> map, List<String> report, RassEntity item) {
		String[] dataItems = new String[] { map.getOperation().toString(), item.getInternalId(),
				String.valueOf(item.getReturnStatus()), };
		report.add(String.join(";", dataItems));
	}

}
