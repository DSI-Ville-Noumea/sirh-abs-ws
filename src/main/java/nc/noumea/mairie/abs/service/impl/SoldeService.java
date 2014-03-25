package nc.noumea.mairie.abs.service.impl;

import java.util.Calendar;
import java.util.Date;

import nc.noumea.mairie.abs.domain.AgentAsaA48Count;
import nc.noumea.mairie.abs.domain.AgentRecupCount;
import nc.noumea.mairie.abs.domain.AgentReposCompCount;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.dto.SoldeDto;
import nc.noumea.mairie.abs.repository.ICounterRepository;
import nc.noumea.mairie.abs.repository.ISirhRepository;
import nc.noumea.mairie.abs.service.ISoldeService;
import nc.noumea.mairie.abs.service.rules.impl.AbsReposCompensateurDataConsistencyRulesImpl;
import nc.noumea.mairie.domain.SpSold;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class SoldeService implements ISoldeService {

	private Logger logger = LoggerFactory.getLogger(SoldeService.class);

	@Autowired
	private ISirhRepository sirhRepository;

	@Autowired
	private ICounterRepository counterRepository;

	@Autowired
	@Qualifier("AbsReposCompensateurDataConsistencyRulesImpl")
	private AbsReposCompensateurDataConsistencyRulesImpl absReposCompDataConsistencyRules;

	@Override
	public SoldeDto getAgentSolde(Integer idAgent) {

		logger.info("Read getAgentSolde for Agent {} ...", idAgent);
		ReturnMessageDto msg = new ReturnMessageDto();
		SoldeDto dto = new SoldeDto();

		// on traite les congés
		SpSold soldeConge = sirhRepository.getSpsold(idAgent);
		dto.setAfficheSoldeConge(true);
		dto.setSoldeCongeAnnee(soldeConge == null ? 0 : soldeConge.getSoldeAnneeEnCours());
		dto.setSoldeCongeAnneePrec(soldeConge == null ? 0 : soldeConge.getSoldeAnneePrec());

		// on traite les recup
		AgentRecupCount soldeRecup = counterRepository.getAgentCounter(AgentRecupCount.class, idAgent);
		dto.setAfficheSoldeRecup(true);
		dto.setSoldeRecup((double) (soldeRecup == null ? 0 : soldeRecup.getTotalMinutes()));

		// on traite les respo comp
		AgentReposCompCount soldeReposComp = counterRepository.getAgentCounter(AgentReposCompCount.class, idAgent);
		msg = absReposCompDataConsistencyRules.checkStatutAgent(msg, idAgent);
		dto.setAfficheSoldeReposComp(msg.getErrors().isEmpty() ? true : false);
		dto.setSoldeReposCompAnnee((double) (soldeReposComp == null ? 0 : soldeReposComp.getTotalMinutes()));
		dto.setSoldeReposCompAnneePrec((double) (soldeReposComp == null ? 0 : soldeReposComp.getTotalMinutesAnneeN1()));

		// on traite les ASA A48 pour l'année en cours
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		Integer annee = cal.get(Calendar.YEAR);
		AgentAsaA48Count soldeAsaA48 = counterRepository.getAgentCounterByDate(AgentAsaA48Count.class, idAgent,
				new DateTime(annee, 1, 1, 0, 0, 0).toDate(), new DateTime(annee, 12, 31, 23, 59, 0).toDate());
		dto.setAfficheSoldeAsaA48(soldeAsaA48 == null ? false : true);
		dto.setSoldeAsaA48(soldeAsaA48 == null ? 0 : soldeAsaA48.getTotalJours());

		return dto;
	}
}
