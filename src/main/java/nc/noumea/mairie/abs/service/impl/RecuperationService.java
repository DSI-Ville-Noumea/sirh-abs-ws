package nc.noumea.mairie.abs.service.impl;

import java.util.Date;

import nc.noumea.mairie.abs.domain.AgentRecupCount;
import nc.noumea.mairie.abs.domain.AgentWeekRecup;
import nc.noumea.mairie.abs.repository.IRecuperationRepository;
import nc.noumea.mairie.abs.repository.ISirhRepository;
import nc.noumea.mairie.abs.service.AgentNotFoundException;
import nc.noumea.mairie.abs.service.HelperService;
import nc.noumea.mairie.abs.service.IRecuperationService;

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
		
		AgentWeekRecup awr = recuperationRepository.getWeekRecupForAgentAndDate(idAgent, dateMonday);
		
		if (awr == null) {
			awr = new AgentWeekRecup();
			awr.setIdAgent(idAgent);
			awr.setDateMonday(dateMonday);
			awr.setLastModification(helperService.getCurrentDate());
			recuperationRepository.persistEntity(awr);
		}
		
		int minutesBeforeUpdate = awr.getMinutesRecup();
		awr.setMinutesRecup(minutes);
		
		AgentRecupCount arc = recuperationRepository.getAgentRecupCount(idAgent);
		
		if (arc == null) {
			arc = new AgentRecupCount();
			arc.setIdAgent(idAgent);
			arc.setLastModification(helperService.getCurrentDate());
			recuperationRepository.persistEntity(arc);
		}
		
		arc.setTotalMinutes(arc.getTotalMinutes() + minutes - minutesBeforeUpdate);
		
		return arc.getTotalMinutes();
	}	
	
}
