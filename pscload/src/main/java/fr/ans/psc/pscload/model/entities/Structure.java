/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.model.entities;

import lombok.EqualsAndHashCode;

/**
 * Can equal.
 *
 * @param other the other
 * @return true, if successful
 */
@EqualsAndHashCode(callSuper = true)
public class Structure extends fr.ans.psc.model.Structure implements RassEntity {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4792642166799662339L;
	
	/**
	 * returnStatus after failure in change request
	 */
	private int returnStatus = 100;

	
	/**
	 * Instantiates a new structure.
	 */
	public Structure() {
		super();
	}

	/**
	 * Instantiates a new structure.
	 *
	 * @param items the items
	 */
	public Structure(String[] items) {
		super();
		setSiteSIRET(items[RassItems.SITE_SIRET.column]);
		setSiteSIREN(items[RassItems.SITE_SIREN.column]);
		setSiteFINESS(items[RassItems.SITE_FINESS.column]);
		setLegalEstablishmentFINESS(items[RassItems.LEGAL_ESTABLISHMENT_FINESS.column]);
		setStructureTechnicalId(items[RassItems.STRUCTURE_TECHNICAL_ID.column]);
		setLegalCommercialName(items[RassItems.LEGAL_COMMERCIAL_NAME.column]);
		setPublicCommercialName(items[RassItems.PUBLIC_COMMERCIAL_NAME.column]);
		setRecipientAdditionalInfo(items[RassItems.RECIPIENT_ADDITIONAL_INFO.column]);
		setGeoLocationAdditionalInfo(items[RassItems.GEO_LOCATION_ADDITIONAL_INFO.column]);
		setStreetNumber(items[RassItems.STREET_NUMBER.column]);
		setStreetNumberRepetitionIndex(items[RassItems.STREET_NUMBER_REPETITION_INDEX.column]);
		setStreetCategoryCode(items[RassItems.STREET_CATEGORY_CODE.column]);
		setStreetLabel(items[RassItems.STREET_LABEL.column]);
		setDistributionMention(items[RassItems.DISTRIBUTION_MENTION.column]);
		setCedexOffice(items[RassItems.CEDEX_OFFICE.column]);
		setPostalCode(items[RassItems.POSTAL_CODE.column]);
		setCommuneCode(items[RassItems.COMMUNE_CODE.column]);
		setCountryCode(items[RassItems.COUNTRY_CODE.column]);
		setPhone(items[RassItems.STRUCTURE_PHONE.column]);
		setPhone2(items[RassItems.STRUCTURE_PHONE_2.column]);
		setFax(items[RassItems.STRUCTURE_FAX.column]);
		setEmail(items[RassItems.STRUCTURE_EMAIL.column]);
		setDepartmentCode(items[RassItems.DEPARTMENT_CODE.column]);
		setOldStructureId(items[RassItems.OLD_STRUCTURE_ID.column]);
		setRegistrationAuthority(items[RassItems.REGISTRATION_AUTHORITY.column]);
	}


	@Override
	public int getReturnStatus() {
		return returnStatus;
	}

	@Override
	public void setReturnStatus(int returnStatus) {
		this.returnStatus = returnStatus;
	}

	@Override
	public String getInternalId() {
		return getStructureTechnicalId();
	}

	@Override
	public String getIdType() {
		return "ALL";
	}

}
