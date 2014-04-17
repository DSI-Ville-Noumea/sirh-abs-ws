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
	
	//Méthode permettant de récupérer les Factory
	public IAbsenceDataConsistencyRules getFactory(int type){
		
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
