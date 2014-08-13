package nc.noumea.mairie.abs.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentAsaA48Count;
import nc.noumea.mairie.abs.domain.AgentAsaA54Count;
import nc.noumea.mairie.abs.domain.AgentAsaA55Count;
import nc.noumea.mairie.abs.domain.AgentCount;
import nc.noumea.mairie.abs.domain.AgentHistoAlimManuelle;
import nc.noumea.mairie.abs.domain.AgentRecupCount;
import nc.noumea.mairie.abs.domain.AgentReposCompCount;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.dto.HistoriqueSoldeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.dto.SoldeDto;
import nc.noumea.mairie.abs.dto.SoldeMonthDto;
import nc.noumea.mairie.abs.dto.SoldeSpecifiqueDto;
import nc.noumea.mairie.abs.repository.ICounterRepository;
import nc.noumea.mairie.abs.repository.ISirhRepository;
import nc.noumea.mairie.abs.service.ICounterService;
import nc.noumea.mairie.abs.service.ISoldeService;
import nc.noumea.mairie.abs.service.rules.impl.AbsReposCompensateurDataConsistencyRulesImpl;
import nc.noumea.mairie.domain.SpSold;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
	
	@Autowired
	@Qualifier("CongesExcepCounterServiceImpl")
	private ICounterService congesExcepCounterServiceImpl;
	
	@Override
	@Transactional(readOnly = true)
	public SoldeDto getAgentSolde(Integer idAgent, Date dateDeb, Date dateFin, Integer typeDemande) {

		logger.info("Read getAgentSolde for Agent {}, and date {} ...", idAgent, dateDeb);
		ReturnMessageDto msg = new ReturnMessageDto();
		SoldeDto dto = new SoldeDto();

		// on traite les congés
		if(null == typeDemande || 0 == typeDemande) {
			getSoldeConges(idAgent, dto);
			getSoldeReposComp(idAgent, dto, msg);
			getSoldeRecup(idAgent, dto);
			getSoldeAsaA48(idAgent, dto, dateDeb);
			getSoldeAsaA54(idAgent, dto, dateDeb);
			getSoldeAsaA55(idAgent, dto, dateDeb, dateFin);
			getSoldeCongesExcep(idAgent, dto, dateDeb, dateFin);
		}else{
			switch (RefTypeAbsenceEnum.getRefTypeAbsenceEnum(typeDemande)) {
				case CONGE_ANNUEL:
					getSoldeConges(idAgent, dto);
					break;
				case REPOS_COMP:
					getSoldeReposComp(idAgent, dto, msg);
					break;
				case RECUP:
					getSoldeRecup(idAgent, dto);
					break;
				case ASA_A48:
					getSoldeAsaA48(idAgent, dto, dateDeb);
					break;
				case ASA_A54:
					getSoldeAsaA54(idAgent, dto, dateDeb);
					break;
				case ASA_A55:
					getSoldeAsaA55(idAgent, dto, dateDeb, dateFin);
					break;
				case MALADIES:
					// TODO
					break;
				default:
					break;
			}
		}
		
		return dto;
	}
	
	private void getSoldeConges(Integer idAgent, SoldeDto dto) {
		SpSold soldeConge = sirhRepository.getSpsold(idAgent);
		dto.setAfficheSoldeConge(true);
		dto.setSoldeCongeAnnee(soldeConge == null ? 0 : soldeConge.getSoldeAnneeEnCours());
		dto.setSoldeCongeAnneePrec(soldeConge == null ? 0 : soldeConge.getSoldeAnneePrec());
	}
	
	private void getSoldeRecup(Integer idAgent, SoldeDto dto) {
		// on traite les recup
		AgentRecupCount soldeRecup = counterRepository.getAgentCounter(AgentRecupCount.class, idAgent);
		dto.setAfficheSoldeRecup(true);
		dto.setSoldeRecup((double) (soldeRecup == null ? 0 : soldeRecup.getTotalMinutes()));
	}
	
	private void getSoldeReposComp(Integer idAgent, SoldeDto dto, ReturnMessageDto msg) {
		// on traite les respo comp
		AgentReposCompCount soldeReposComp = counterRepository.getAgentCounter(AgentReposCompCount.class, idAgent);
		msg = absReposCompDataConsistencyRules.checkStatutAgent(msg, idAgent);
		dto.setAfficheSoldeReposComp(msg.getErrors().isEmpty() ? true : false);
		dto.setSoldeReposCompAnnee((double) (soldeReposComp == null ? 0 : soldeReposComp.getTotalMinutes()));
		dto.setSoldeReposCompAnneePrec((double) (soldeReposComp == null ? 0 : soldeReposComp.getTotalMinutesAnneeN1()));
	}
	
	private void getSoldeAsaA48(Integer idAgent, SoldeDto dto, Date dateDeb) {
		// on traite les ASA A48 pour la date en parametre
		AgentAsaA48Count soldeAsaA48 = counterRepository
				.getAgentCounterByDate(AgentAsaA48Count.class, idAgent, dateDeb);
		dto.setAfficheSoldeAsaA48(soldeAsaA48 == null ? false : true);
		dto.setSoldeAsaA48(soldeAsaA48 == null ? 0 : soldeAsaA48.getTotalJours());
	}
	
	private void getSoldeAsaA54(Integer idAgent, SoldeDto dto, Date dateDeb) {
		// on traite les ASA A54 pour la date en parametre
		AgentAsaA54Count soldeAsaA54 = counterRepository
				.getAgentCounterByDate(AgentAsaA54Count.class, idAgent, dateDeb);
		dto.setAfficheSoldeAsaA54(soldeAsaA54 == null ? false : true);
		dto.setSoldeAsaA54(soldeAsaA54 == null ? 0 : soldeAsaA54.getTotalJours());
	}
	
	private void getSoldeAsaA55(Integer idAgent, SoldeDto dto, Date dateDeb, Date dateFin) {
		// on traite les ASA A55 pour la date en parametre
		// on affiche le solde courant
		AgentAsaA55Count soldeAsaA55 = counterRepository
				.getAgentCounterByDate(AgentAsaA55Count.class, idAgent, dateDeb);
		dto.setAfficheSoldeAsaA55(soldeAsaA55 == null ? false : true);
		dto.setSoldeAsaA55((double) (soldeAsaA55 == null ? 0 : soldeAsaA55.getTotalMinutes()));
		// on affiche tous les soldes de l'année
		List<AgentAsaA55Count> listeSoldeAsaA55 = counterRepository
				.getListAgentCounterByDate(idAgent, dateDeb, dateFin);
		List<SoldeMonthDto> listDto = new ArrayList<SoldeMonthDto>();
		for (AgentAsaA55Count arc : listeSoldeAsaA55) {
			SoldeMonthDto dtoMonth = new SoldeMonthDto();
			dtoMonth.setSoldeAsaA55(arc.getTotalMinutes());
			dtoMonth.setDateDebut(arc.getDateDebut());
			dtoMonth.setDateFin(arc.getDateFin());
			listDto.add(dtoMonth);
		}
		dto.setListeSoldeAsaA55(listDto);
	}
	
	private void getSoldeCongesExcep(Integer idAgent, SoldeDto dto, Date dateDeb, Date dateFin) {
		// Conges Exceptionnels
		List<SoldeSpecifiqueDto> listeSoldeCongesExcep = congesExcepCounterServiceImpl.getListAgentCounterByDate(idAgent, dateDeb, dateFin);
		if(null != listeSoldeCongesExcep
				&& !listeSoldeCongesExcep.isEmpty()) {
			dto.setAfficheSoldeCongesExcep(true);
			dto.setListeSoldeCongesExcep(listeSoldeCongesExcep);
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<HistoriqueSoldeDto> getHistoriqueSoldeAgent(Integer idAgent, Integer codeRefTypeAbsence, Date dateDeb,
			Date dateFin) {
		logger.info(
				"Read getHistoriqueSoldeAgent for Agent {}, and dateDeb {}, and dateFin {}, and typeAbsence {} ...",
				idAgent, dateDeb, dateFin, RefTypeAbsenceEnum.getRefTypeAbsenceEnum(codeRefTypeAbsence));

		List<AgentCount> listAgentCount = new ArrayList<AgentCount>();
		// on recupere le compteur correspondant
		switch (RefTypeAbsenceEnum.getRefTypeAbsenceEnum(codeRefTypeAbsence)) {
			case CONGE_ANNUEL:
				// TODO
				break;
			case REPOS_COMP:
				AgentReposCompCount countReposComp = counterRepository.getAgentCounter(AgentReposCompCount.class,
						idAgent);
				if (countReposComp != null)
					listAgentCount.add(countReposComp);
				break;
			case RECUP:
				AgentRecupCount countRecup = counterRepository.getAgentCounter(AgentRecupCount.class, idAgent);
				if (countRecup != null)
					listAgentCount.add(countRecup);
				break;
			case ASA_A48:
				AgentAsaA48Count countA48 = counterRepository.getAgentCounterByDate(AgentAsaA48Count.class, idAgent,
						dateDeb);
				if (countA48 != null)
					listAgentCount.add(countA48);
				break;
			case ASA_A54:
				AgentAsaA54Count countA54 = counterRepository.getAgentCounterByDate(AgentAsaA54Count.class, idAgent,
						dateDeb);
				if (countA54 != null)
					listAgentCount.add(countA54);
				break;
			case ASA_A55:
				List<AgentAsaA55Count> countA55 = counterRepository
						.getListAgentCounterByDate(idAgent, dateDeb, dateFin);
				if (countA55 != null)
					listAgentCount.addAll(countA55);
				break;
			case MALADIES:
				// TODO
				break;
			default:
				break;
		}
		List<HistoriqueSoldeDto> result = new ArrayList<HistoriqueSoldeDto>();
		for (AgentCount agentCount : listAgentCount) {
			List<AgentHistoAlimManuelle> list = counterRepository.getListHisto(idAgent, agentCount);
			for (AgentHistoAlimManuelle aha : list) {
				HistoriqueSoldeDto dto = new HistoriqueSoldeDto(aha);
				result.add(dto);
			}
		}
		return result;
	}
}
