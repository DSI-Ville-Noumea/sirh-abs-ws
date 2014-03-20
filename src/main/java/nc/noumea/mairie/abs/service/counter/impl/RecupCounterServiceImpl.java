package nc.noumea.mairie.abs.service.counter.impl;

import java.util.Date;

import nc.noumea.mairie.abs.domain.AgentRecupCount;
import nc.noumea.mairie.abs.domain.AgentWeekRecup;
import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.dto.CompteurDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;

import org.springframework.stereotype.Service;

@Service("RecupCounterServiceImpl")
public class RecupCounterServiceImpl extends AbstractCounterService {

	@Override
	public ReturnMessageDto majManuelleCompteurToAgent(Integer idAgent, CompteurDto compteurDto) {

		logger.info("Trying to update Recuperation manually counters for Agent {} ...", compteurDto.getIdAgent());

		ReturnMessageDto result = new ReturnMessageDto();

		result = super.majManuelleCompteurToAgent(idAgent, compteurDto);
		if (!result.getErrors().isEmpty())
			return result;

		result = majManuelleCompteurRecupToAgent(idAgent, compteurDto, result, RefTypeAbsenceEnum.RECUP.getValue());

		return result;
	}

	/**
	 * appeler depuis Kiosque ou SIRH l historique ABS_AGENT_WEEK_ALIM_MANUELLE
	 * mise a jour
	 */
	private ReturnMessageDto majManuelleCompteurRecupToAgent(Integer idAgent, CompteurDto compteurDto,
			ReturnMessageDto result, Integer idRefTypeAbsence) {

		logger.info("Trying to update manually recuperation counters for Agent {} ...", compteurDto.getIdAgent());

		int minutes = helperService.calculMinutesAlimManuelleCompteur(compteurDto);

		try {
			return majManuelleCompteurToAgent(AgentRecupCount.class, idAgent, compteurDto, minutes, idRefTypeAbsence,
					result);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException("An error occured while trying to update recuperation counters :", e);
		}
	}

	/**
	 * appeler par PTG exclusivement l historique utilise a pour seul but de
	 * rectifier le compteur en cas de modification par l agent dans ses
	 * pointages
	 */
	@Override
	public int addToAgentForPTG(Integer idAgent, Date dateMonday, Integer minutes) {

		logger.info("Trying to update recuperation counters for Agent [{}] and date [{}] with {} minutes...", idAgent,
				dateMonday, minutes);

		try {
			return addMinutesToAgent(AgentRecupCount.class, AgentWeekRecup.class, idAgent, dateMonday, minutes);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException("An error occured while trying to update recuperation counters :", e);
		}
	}

	/**
	 * appeler depuis ABSENCE l historique ABS_AGENT_WEEK_... n est pas utilise
	 */
	@Override
	public ReturnMessageDto majCompteurToAgent(ReturnMessageDto srm, Demande demande, Integer minutes) {

		logger.info("Trying to update recuperation counters for Agent [{}] with {} minutes...", demande.getIdAgent(),
				minutes);

		try {
			return majCompteurToAgent(AgentRecupCount.class, demande.getIdAgent(), minutes, srm);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException("An error occured while trying to update recuperation counters :", e);
		}
	}

}
