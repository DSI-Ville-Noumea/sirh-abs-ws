package nc.noumea.mairie.abs.service.impl;

import java.util.Date;

import nc.noumea.mairie.abs.domain.AgentRecupCount;
import nc.noumea.mairie.abs.domain.AgentWeekRecup;
import nc.noumea.mairie.abs.dto.SoldeDto;
import nc.noumea.mairie.abs.repository.IRecuperationRepository;
import nc.noumea.mairie.abs.repository.ISirhRepository;
import nc.noumea.mairie.abs.service.AgentNotFoundException;
import nc.noumea.mairie.abs.service.IRecuperationService;
import nc.noumea.mairie.abs.service.NotAMondayException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RecuperationService implements IRecuperationService {

	private Logger logger = LoggerFactory.getLogger(RecuperationService.class);

	@Autowired
	private IRecuperationRepository recuperationRepository;

	@Autowired
	private ISirhRepository sirhRepository;

	@Autowired
	private HelperService helperService;

	@Override
	public int addRecuperationToAgent(Integer idAgent, Date dateMonday, Integer minutes) {

		if (sirhRepository.getAgent(idAgent) == null) {
			logger.error("There is no Agent [{}]. Impossible to update its Recuperation counters.", idAgent);
			throw new AgentNotFoundException();
		}

		if (!helperService.isDateAMonday(dateMonday)) {
			logger.error("Given monday date [{}] is not a Monday. Impossible to update recuperation counters.",
					dateMonday);
			throw new NotAMondayException();
		}

		AgentWeekRecup awr = recuperationRepository.getWeekRecupForAgentAndDate(idAgent, dateMonday);

		if (awr == null) {
			awr = new AgentWeekRecup();
			awr.setIdAgent(idAgent);
			awr.setDateMonday(dateMonday);
		}

		int minutesBeforeUpdate = awr.getMinutesRecup();
		awr.setMinutesRecup(minutes);
		awr.setLastModification(helperService.getCurrentDate());

		AgentRecupCount arc = recuperationRepository.getAgentRecupCount(idAgent);

		if (arc == null) {
			arc = new AgentRecupCount();
			arc.setIdAgent(idAgent);
		}

		arc.setTotalMinutes(arc.getTotalMinutes() + minutes - minutesBeforeUpdate);
		arc.setLastModification(helperService.getCurrentDate());

		if (awr.getIdAgentWeekRecup() == null)
			recuperationRepository.persistEntity(awr);

		if (arc.getIdAgentRecupCount() == null)
			recuperationRepository.persistEntity(arc);

		return arc.getTotalMinutes();
	}

	@Override
	public SoldeDto getAgentSoldeRecuperation(Integer idAgent) {
		AgentRecupCount soldeRecup = recuperationRepository.getAgentRecupCount(idAgent);
		SoldeDto dto = new SoldeDto();
		dto.setSolde((double) (soldeRecup == null ? 0 : soldeRecup.getTotalMinutes()));
		return dto;
	}

}
