package nc.noumea.mairie.abs.service.impl;

import java.util.Date;

import nc.noumea.mairie.abs.domain.AgentRecupCount;
import nc.noumea.mairie.abs.domain.AgentReposCompCount;
import nc.noumea.mairie.abs.domain.AgentWeekRecup;
import nc.noumea.mairie.abs.domain.AgentWeekReposComp;
import nc.noumea.mairie.abs.domain.BaseAgentCount;
import nc.noumea.mairie.abs.domain.BaseAgentWeekHisto;
import nc.noumea.mairie.abs.repository.ICounterRepository;
import nc.noumea.mairie.abs.repository.ISirhRepository;
import nc.noumea.mairie.abs.service.AgentNotFoundException;
import nc.noumea.mairie.abs.service.ICounterService;
import nc.noumea.mairie.abs.service.NotAMondayException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CounterService implements ICounterService {

	private Logger logger = LoggerFactory.getLogger(CounterService.class);

	@Autowired
	private ICounterRepository counterRepository;

	@Autowired
	private ISirhRepository sirhRepository;

	@Autowired
	private HelperService helperService;

	/**
	 * Mets à jour le compteur de minutes désiré (en fonction des types passés
	 * en paramètre)
	 * 
	 * @param T1
	 *            inherits BaseAgentCount
	 * @param T2
	 *            inherits BaseAgentWeekHisto
	 * @param idAgent
	 * @param minutes
	 * @param dateMonday
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	protected <T1, T2> int addMinutesToAgent(Class<T1> T1, Class<T2> T2, Integer idAgent, Date dateMonday,
			Integer minutes) throws InstantiationException, IllegalAccessException {

		if (sirhRepository.getAgent(idAgent) == null) {
			logger.error("There is no Agent [{}]. Impossible to update its counters.", idAgent);
			throw new AgentNotFoundException();
		}

		if (!helperService.isDateAMonday(dateMonday)) {
			logger.error("Given monday date [{}] is not a Monday. Impossible to update counters.", dateMonday);
			throw new NotAMondayException();
		}

		logger.info("updating counters for Agent [{}] and date [{}] with {} minutes...", idAgent, dateMonday, minutes);

		BaseAgentWeekHisto awr = (BaseAgentWeekHisto) counterRepository.getWeekHistoForAgentAndDate(T2, idAgent,
				dateMonday);

		if (awr == null) {
			awr = (BaseAgentWeekHisto) T2.newInstance();
			awr.setIdAgent(idAgent);
			awr.setDateMonday(dateMonday);
		}

		int minutesBeforeUpdate = awr.getMinutes();
		awr.setMinutes(minutes);
		awr.setLastModification(helperService.getCurrentDate());

		BaseAgentCount arc = (BaseAgentCount) counterRepository.getAgentCounter(T1, idAgent);

		if (arc == null) {
			arc = (BaseAgentCount) T1.newInstance();
			arc.setIdAgent(idAgent);
		}

		arc.setTotalMinutes(arc.getTotalMinutes() + minutes - minutesBeforeUpdate);
		arc.setLastModification(helperService.getCurrentDate());

		counterRepository.persistEntity(awr);
		counterRepository.persistEntity(arc);

		return arc.getTotalMinutes();
	}

	@Override
	public int addRecuperationToAgent(Integer idAgent, Date dateMonday, Integer minutes) {

		logger.info("Trying to update recuperation counters for Agent [{}] and date [{}] with {} minutes...", idAgent,
				dateMonday, minutes);

		try {
			return addMinutesToAgent(AgentRecupCount.class, AgentWeekRecup.class, idAgent, dateMonday, minutes);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException("An error occured while trying to update recuperation counters :", e);
		}
	}

	@Override
	public int addReposCompensateurToAgent(Integer idAgent, Date dateMonday, Integer minutes) {

		logger.info("Trying to update repos compensateurs counters for Agent [{}] and date [{}] with {} minutes...",
				idAgent, dateMonday, minutes);

		try {
			return addMinutesToAgent(AgentReposCompCount.class, AgentWeekReposComp.class, idAgent, dateMonday, minutes);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException("An error occured while trying to update repos compensateur counters :", e);
		}

	}

}
