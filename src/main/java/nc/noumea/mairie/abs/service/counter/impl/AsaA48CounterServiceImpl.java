package nc.noumea.mairie.abs.service.counter.impl;

import java.util.Date;

import nc.noumea.mairie.abs.domain.AgentAsaA48Count;
import nc.noumea.mairie.abs.domain.AgentHistoAlimManuelle;
import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.MotifCompteur;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.dto.CompteurDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.service.AgentNotFoundException;

import org.springframework.stereotype.Service;

@Service("AsaA48CounterServiceImpl")
public class AsaA48CounterServiceImpl extends AbstractCounterService {

	@Override
	public ReturnMessageDto majManuelleCompteurToAgent(Integer idAgent, CompteurDto compteurDto) {

		logger.info("Trying to update ASA A48 manually counters for Agent {} ...", compteurDto.getIdAgent());

		ReturnMessageDto result = new ReturnMessageDto();

		result = super.majManuelleCompteurToAgent(idAgent, compteurDto);
		if (!result.getErrors().isEmpty())
			return result;

		result = majManuelleCompteurAsaA48ToAgent(idAgent, compteurDto, result, RefTypeAbsenceEnum.ASA_A48.getValue());

		return result;
	}

	/**
	 * appeler depuis Kiosque ou SIRH l historique ABS_AGENT_WEEK_ALIM_MANUELLE
	 * mise a jour
	 */
	private ReturnMessageDto majManuelleCompteurAsaA48ToAgent(Integer idAgent, CompteurDto compteurDto,
			ReturnMessageDto result, Integer idRefTypeAbsence) {

		logger.info("Trying to update manually ASA A48 counters for Agent {} ...", compteurDto.getIdAgent());

		int nbJours = helperService.calculJoursAlimManuelleCompteur(compteurDto);

		try {
			return majManuelleCompteurToAgent(idAgent, compteurDto, nbJours, idRefTypeAbsence, result);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException("An error occured while trying to update recuperation counters :", e);
		}
	}

	/**
	 * Mise à jour manuelle du compteur de récup
	 * 
	 * @param T1
	 *            inherits BaseAgentCount
	 * @param idAgent
	 * @param minutes
	 *            : negatif pour debiter, positif pour crediter
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	protected <T1, T2> ReturnMessageDto majManuelleCompteurToAgent(Integer idAgentOperateur, CompteurDto compteurDto,
			Integer nbJours, Integer idRefTypeAbsence, ReturnMessageDto srm) throws InstantiationException,
			IllegalAccessException {

		if (sirhRepository.getAgent(compteurDto.getIdAgent()) == null) {
			logger.error("There is no Agent [{}]. Impossible to update its counters.", compteurDto.getIdAgent());
			throw new AgentNotFoundException();
		}

		logger.info("updating counters for Agent [{}] with {} nbJours for Year {}...", compteurDto.getIdAgent(),
				nbJours, compteurDto.getDateDebut().getYear());

		AgentAsaA48Count arc = (AgentAsaA48Count) counterRepository.getAgentCounter(AgentAsaA48Count.class,
				compteurDto.getIdAgent());

		if (arc == null) {
			arc = new AgentAsaA48Count();
			arc.setIdAgent(compteurDto.getIdAgent());
		}

		if (!srm.getErrors().isEmpty()) {
			return srm;
		}

		MotifCompteur motifCompteur = counterRepository
				.getEntity(MotifCompteur.class, compteurDto.getIdMotifCompteur());
		if (null == motifCompteur) {
			logger.warn(MOTIF_COMPTEUR_INEXISTANT);
			srm.getErrors().add(String.format(MOTIF_COMPTEUR_INEXISTANT));
			return srm;
		}

		AgentHistoAlimManuelle histo = new AgentHistoAlimManuelle();
		histo.setIdAgent(idAgentOperateur);
		histo.setIdAgentConcerne(compteurDto.getIdAgent());
		histo.setDateModification(helperService.getCurrentDate());
		histo.setMotifCompteur(motifCompteur);
		String textLog = "";
		if (null != compteurDto.getDureeAAjouter()) {
			textLog = "Mise en place de " + nbJours + " jours pour l'année " + compteurDto.getDateDebut().getYear();
		}
		histo.setText(textLog);

		RefTypeAbsence rta = new RefTypeAbsence();
		rta.setIdRefTypeAbsence(idRefTypeAbsence);
		histo.setType(rta);

		arc.setTotalJours(nbJours);
		arc.setDateDebut(compteurDto.getDateDebut());
		arc.setDateFin(compteurDto.getDateFin());
		arc.setLastModification(helperService.getCurrentDate());

		counterRepository.persistEntity(arc);
		counterRepository.persistEntity(histo);

		return srm;
	}

	@Override
	public int addToAgentForPTG(Integer idAgent, Date dateMonday, Integer minutes) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ReturnMessageDto majCompteurToAgent(ReturnMessageDto srm, Demande demande, Integer minutes) {
		// TODO Auto-generated method stub
		return null;
	}

}
