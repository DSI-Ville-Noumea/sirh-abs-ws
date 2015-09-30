package nc.noumea.mairie.abs.service.counter.impl;

import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.domain.RefTypeGroupeAbsenceEnum;
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
	@Qualifier("AsaCounterServiceImpl")
	private ICounterService asaCounterServiceImpl;

	@Autowired
	@Qualifier("AsaA48CounterServiceImpl")
	private ICounterService asaA48CounterServiceImpl;

	@Autowired
	@Qualifier("AsaA52CounterServiceImpl")
	private ICounterService asaA52CounterServiceImpl;

	@Autowired
	@Qualifier("AsaA53CounterServiceImpl")
	private ICounterService asaA53CounterServiceImpl;

	@Autowired
	@Qualifier("AsaA54CounterServiceImpl")
	private ICounterService asaA54CounterServiceImpl;

	@Autowired
	@Qualifier("AsaA55CounterServiceImpl")
	private ICounterService asaA55CounterServiceImpl;

	@Autowired
	@Qualifier("AsaAmicaleCounterServiceImpl")
	private ICounterService asaAmicaleCounterServiceImpl;

	@Autowired
	@Qualifier("CongesExcepCounterServiceImpl")
	private ICounterService congesExcepCounterServiceImpl;

	@Autowired
	@Qualifier("CongeAnnuelCounterServiceImpl")
	private ICounterService congesAnnuelsCounterServiceImpl;

	// Méthode permettant de récupérer les Factory
	public ICounterService getFactory(int groupe, int type) {

		switch (RefTypeGroupeAbsenceEnum.getRefTypeGroupeAbsenceEnum(groupe)) {
			case REPOS_COMP:
				return reposCompCounterServiceImpl;
			case RECUP:
				return recupCounterServiceImpl;
			case AS:
				return getFactoryAsa(type);
			case CONGES_EXCEP:
				return congesExcepCounterServiceImpl;
			case CONGES_ANNUELS:
				return congesAnnuelsCounterServiceImpl;
			default:
				break;
		}
		return defaultCounterServiceImpl;
	}

	@SuppressWarnings("incomplete-switch")
	private ICounterService getFactoryAsa(int type) {

		switch (RefTypeAbsenceEnum.getRefTypeAbsenceEnum(type)) {

			case ASA_A48:
				return asaA48CounterServiceImpl;
			case ASA_A49:
				return asaCounterServiceImpl;
			case ASA_A50:
				return asaCounterServiceImpl;
			case ASA_A52:
				return asaA52CounterServiceImpl;
			case ASA_A53:
				return asaA53CounterServiceImpl;
			case ASA_A54:
				return asaA54CounterServiceImpl;
			case ASA_A55:
				return asaA55CounterServiceImpl;
			case ASA_AMICALE:
				return asaAmicaleCounterServiceImpl;
		}
		return defaultCounterServiceImpl;
	}
}
