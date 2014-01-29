package nc.noumea.mairie.abs.service.impl;

import nc.noumea.mairie.abs.domain.AgentRecupCount;
import nc.noumea.mairie.abs.domain.AgentReposCompCount;
import nc.noumea.mairie.abs.dto.SoldeDto;
import nc.noumea.mairie.abs.repository.ICounterRepository;
import nc.noumea.mairie.abs.repository.ISirhRepository;
import nc.noumea.mairie.abs.service.ISoldeService;
import nc.noumea.mairie.domain.SpSold;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SoldeService implements ISoldeService {

	private Logger logger = LoggerFactory.getLogger(SoldeService.class);

	@Autowired
	private ISirhRepository sirhRepository;

	@Autowired
	private ICounterRepository counterRepository;

	@Override
	public SoldeDto getAgentSolde(Integer idAgent) {
		// on traite les congés
		SpSold soldeConge = sirhRepository.getSpsold(idAgent);

		// on traite les recup
		AgentRecupCount soldeRecup = counterRepository.getAgentCounter(AgentRecupCount.class, idAgent);

		// on traite les respo comp
		AgentReposCompCount soldeReposComp = counterRepository.getAgentCounter(AgentReposCompCount.class, idAgent);

		// on alimente le DTO
		SoldeDto dto = new SoldeDto();
		dto.setSoldeCongeAnnee(soldeConge == null ? 0 : soldeConge.getSoldeAnneeEnCours());
		dto.setSoldeCongeAnneePrec(soldeConge == null ? 0 : soldeConge.getSoldeAnneePrec());
		dto.setSoldeRecup((double) (soldeRecup == null ? 0 : soldeRecup.getTotalMinutes()));
		dto.setSoldeReposCompAnnee((double) (soldeReposComp == null ? 0 : soldeReposComp.getTotalMinutes()));
		dto.setSoldeReposCompAnneePrec((double) (soldeReposComp == null ? 0 : soldeReposComp.getTotalMinutesAnneeN1()));
		return dto;
	}
}
