package nc.noumea.mairie.abs.service.rules.impl;

import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.domain.RefTypeGroupeAbsenceEnum;
import nc.noumea.mairie.abs.service.IAbsenceDataConsistencyRules;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class DataConsistencyRulesFactory {

	@Autowired
	@Qualifier("DefaultAbsenceDataConsistencyRulesImpl")
	private IAbsenceDataConsistencyRules defaultAbsenceDataConsistencyRulesImpl;

	@Autowired
	@Qualifier("AbsRecuperationDataConsistencyRulesImpl")
	private IAbsenceDataConsistencyRules absRecupDataConsistencyRules;

	@Autowired
	@Qualifier("AbsReposCompensateurDataConsistencyRulesImpl")
	private IAbsenceDataConsistencyRules absReposCompDataConsistencyRules;

	@Autowired
	@Qualifier("AbsAsaA48DataConsistencyRulesImpl")
	private IAbsenceDataConsistencyRules absAsaA48DataConsistencyRulesImpl;

	@Autowired
	@Qualifier("AbsAsaA52DataConsistencyRulesImpl")
	private IAbsenceDataConsistencyRules absAsaA52DataConsistencyRulesImpl;

	@Autowired
	@Qualifier("AbsAsaA53DataConsistencyRulesImpl")
	private IAbsenceDataConsistencyRules absAsaA53DataConsistencyRulesImpl;

	@Autowired
	@Qualifier("AbsAsaA54DataConsistencyRulesImpl")
	private IAbsenceDataConsistencyRules absAsaA54DataConsistencyRulesImpl;

	@Autowired
	@Qualifier("AbsAsaA55DataConsistencyRulesImpl")
	private IAbsenceDataConsistencyRules absAsaA55DataConsistencyRulesImpl;

	@Autowired
	@Qualifier("AbsAsaA49DataConsistencyRulesImpl")
	private IAbsenceDataConsistencyRules absAsaA49DataConsistencyRulesImpl;

	@Autowired
	@Qualifier("AbsAsaDataConsistencyRulesImpl")
	private IAbsenceDataConsistencyRules absAsaDataConsistencyRulesImpl;

	@Autowired
	@Qualifier("AbsCongesExcepDataConsistencyRulesImpl")
	private IAbsenceDataConsistencyRules absCongesExcepDataConsistencyRulesImpl;

	// Méthode permettant de récupérer les Factory
	public IAbsenceDataConsistencyRules getFactory(Integer groupe, Integer type) {

		switch (RefTypeGroupeAbsenceEnum.getRefTypeGroupeAbsenceEnum(groupe)) {
			case REPOS_COMP:
				return absReposCompDataConsistencyRules;
			case RECUP:
				return absRecupDataConsistencyRules;
			case ASA:
				return getFactoryAsa(type);
			case CONGES_EXCEP:
				return absCongesExcepDataConsistencyRulesImpl;
			default:
				break;
		}
		return defaultAbsenceDataConsistencyRulesImpl;
	}

	@SuppressWarnings("incomplete-switch")
	private IAbsenceDataConsistencyRules getFactoryAsa(Integer type) {

		switch (RefTypeAbsenceEnum.getRefTypeAbsenceEnum(type)) {

			case ASA_A48:
				return absAsaA48DataConsistencyRulesImpl;
			case ASA_A52:
				return absAsaA52DataConsistencyRulesImpl;
			case ASA_A53:
				return absAsaA53DataConsistencyRulesImpl;
			case ASA_A54:
				return absAsaA54DataConsistencyRulesImpl;
			case ASA_A55:
				return absAsaA55DataConsistencyRulesImpl;
			case ASA_A49:
				return absAsaA49DataConsistencyRulesImpl;
			case ASA_A50:
				return absAsaDataConsistencyRulesImpl;
		}
		return defaultAbsenceDataConsistencyRulesImpl;
	}
}
