/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.model.entities;

import fr.ans.psc.model.Profession;
import fr.ans.psc.model.Ps;
import lombok.EqualsAndHashCode;

import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
public class Professionnel extends Ps implements RassEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2859304844064466893L;
	
	/**
	 * returnStatus after failure in change request
	 */
	private int returnStatus = 100;

	/**
	 * Instantiates a new professionnel.
	 */
	public Professionnel() {
		super();
	}

	/**
	 * Instantiates a new professionnel.
	 *
	 * @param items the items
	 * @param deep the deep
	 */
	public Professionnel(String[] items, boolean deep) {
		super();
		setIdType(items[RassItems.ID_TYPE.column]);
		setId(items[RassItems.ID.column]);
		setNationalId(items[RassItems.NATIONAL_ID.column]);
		setLastName(items[RassItems.LAST_NAME.column]);
		setFirstName(items[RassItems.FIRST_NAME.column]);
		setDateOfBirth(items[RassItems.DOB.column]);
		setBirthAddressCode(items[RassItems.BIRTH_ADDRESS_CODE.column]);
		setBirthCountryCode(items[RassItems.BIRTH_COUNTRY_CODE.column]);
		setBirthAddress(items[RassItems.BIRTH_ADDRESS.column]);
		setGenderCode(items[RassItems.GENDER_CODE.column]);
		setPhone(items[RassItems.PHONE.column]);
		setEmail(items[RassItems.EMAIL.column]);
		setSalutationCode(items[RassItems.SALUTATION_CODE.column]);
		if (deep) {
			addProfessionsItem(new ExerciceProfessionnel(items));
		}
	}

	/**
	 * Gets the profession by code and category.
	 *
	 * @param code the code
	 * @param category the category
	 * @return the profession by code and category
	 */
	public Optional<Profession> getProfessionByCodeAndCategory(String code, String category) {
		return getProfessions().stream()
				.filter(exo -> exo.getCode().concat(exo.getCategoryCode()).equals(code.concat(category))).findAny();

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
		return getNationalId();
	}
	
	@Override
	public String getIdType() {
		return super.getIdType();
	}

}
