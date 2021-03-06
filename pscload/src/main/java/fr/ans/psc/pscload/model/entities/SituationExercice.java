/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.model.entities;

import fr.ans.psc.model.WorkSituation;
import lombok.EqualsAndHashCode;

/**
 * Can equal.
 *
 * @param other the other
 * @return true, if successful
 */

/**
 * Can equal.
 *
 * @param other the other
 * @return true, if successful
 */
@EqualsAndHashCode(callSuper = true)
public class SituationExercice extends WorkSituation {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2664005257511101075L;

	/**
	 * Instantiates a new situation exercice.
	 */
	
	public SituationExercice() {
		super();
	}

	/**
	 * Instantiates a new situation exercice.
	 *
	 * @param items the items
	 */
	public SituationExercice(String[] items) {
		super();
		setModeCode(items[RassItems.SITUATION_MODE_CODE.column]);
		setActivitySectorCode(items[RassItems.ACTIVITY_SECTOR_CODE.column]);
		setPharmacistTableSectionCode(items[RassItems.PHARMACIST_TABLE_SECTION_CODE.column]);
		setRoleCode(items[RassItems.SITUATION_ROLE_CODE.column]);
		addStructuresItem(new RefStructure(items[28])); // structureTechnicalId
	}
}
