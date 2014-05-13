package nc.noumea.mairie.abs.service.rules.impl;

import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
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

	// Méthode permettant de récupérer les Factory
	public IAbsenceDataConsistencyRules getFactory(int type) {

		switch (RefTypeAbsenceEnum.getRefTypeAbsenceEnum(type)) {
			case CONGE_ANNUEL:
				// TODO
				break;
			case REPOS_COMP:
				return absReposCompDataConsistencyRules;
			case RECUP:
				return absRecupDataConsistencyRules;
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
			case AUTRES:
				// TODO
				break;
			case MALADIES:
				// TODO
				break;
		}
		return defaultAbsenceDataConsistencyRulesImpl;
	}
}
