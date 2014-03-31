package nc.noumea.mairie.abs.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentAsaA48Count;
import nc.noumea.mairie.abs.domain.AgentHistoAlimManuelle;
import nc.noumea.mairie.abs.domain.AgentRecupCount;
import nc.noumea.mairie.abs.domain.AgentReposCompCount;
import nc.noumea.mairie.abs.dto.HistoriqueSoldeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.dto.SoldeDto;
import nc.noumea.mairie.abs.repository.ICounterRepository;
import nc.noumea.mairie.abs.repository.ISirhRepository;
import nc.noumea.mairie.abs.service.ISoldeService;
import nc.noumea.mairie.abs.service.rules.impl.AbsReposCompensateurDataConsistencyRulesImpl;
import nc.noumea.mairie.domain.SpSold;

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
	public SoldeDto getAgentSolde(Integer idAgent, Date dateDebut, Date dateFin) {

		logger.info("Read getAgentSolde for Agent {}, and dateDeb {}, and dateFin {} ...", idAgent, dateDebut, dateFin);
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

		// on traite les ASA A48 pour l'année en parametre
		AgentAsaA48Count soldeAsaA48 = counterRepository.getAgentCounterByDate(AgentAsaA48Count.class, idAgent,
				dateDebut, dateFin);
		dto.setAfficheSoldeAsaA48(soldeAsaA48 == null ? false : true);
		dto.setSoldeAsaA48(soldeAsaA48 == null ? 0 : soldeAsaA48.getTotalJours());

		return dto;
	}

	@Override
	public List<HistoriqueSoldeDto> getHistoriqueSoldeAgentByTypeAbsence(Integer idAgent, Integer codeRefTypeAbsence) {
		List<HistoriqueSoldeDto> result = new ArrayList<HistoriqueSoldeDto>();
		List<AgentHistoAlimManuelle> list = counterRepository.getListHistoByRefTypeAbsenceAndAgent(idAgent,
				codeRefTypeAbsence);
		for (AgentHistoAlimManuelle aha : list) {
			HistoriqueSoldeDto dto = new HistoriqueSoldeDto(aha);
			result.add(dto);
		}
		return result;
	}
}
