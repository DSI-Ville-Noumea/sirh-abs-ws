package nc.noumea.mairie.abs.service.counter.impl;

import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.service.ICounterService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class CounterServiceFactory {

	@Autowired
	@Qualifier("DefaultCounterServiceImpl")
	private ICounterService defaultCounterServiceImpl;
	
	@Autowired
	@Qualifier("RecupCounterServiceImpl")
	private ICounterService recupCounterServiceImpl;

	@Autowired
	@Qualifier("ReposCompCounterServiceImpl")
	private ICounterService reposCompCounterServiceImpl;
	
	//Méthode permettant de récupérer les Factory
	public ICounterService getFactory(int type){
		
		switch (RefTypeAbsenceEnum.getRefTypeAbsenceEnum(type)) {
			case CONGE_ANNUEL:
				// TODO
				break;
			case REPOS_COMP:
				return reposCompCounterServiceImpl;
			case RECUP:
				return recupCounterServiceImpl;
			case ASA_A48:
				// TODO
				break;
			case AUTRES:
				// TODO
				break;
			case MALADIES:
				// TODO
				break;
		}
		return defaultCounterServiceImpl;
	}
}
