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

	@Autowired
	@Qualifier("AsaA48CounterServiceImpl")
	private ICounterService asaA48CounterServiceImpl;

	@Autowired
	@Qualifier("AsaA54CounterServiceImpl")
	private ICounterService asaA54CounterServiceImpl;

	@Autowired
	@Qualifier("AsaA55CounterServiceImpl")
	private ICounterService asaA55CounterServiceImpl;

	// Méthode permettant de récupérer les Factory
	public ICounterService getFactory(int type) {

		switch (RefTypeAbsenceEnum.getRefTypeAbsenceEnum(type)) {
			case CONGE_ANNUEL:
				// TODO
				break;
			case REPOS_COMP:
				return reposCompCounterServiceImpl;
			case RECUP:
				return recupCounterServiceImpl;
			case ASA_A48:
				return asaA48CounterServiceImpl;
			case ASA_A54:
				return asaA54CounterServiceImpl;
			case ASA_A55:
				return asaA55CounterServiceImpl;
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
