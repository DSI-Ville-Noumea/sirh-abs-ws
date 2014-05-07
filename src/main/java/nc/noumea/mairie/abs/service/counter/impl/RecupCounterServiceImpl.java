package nc.noumea.mairie.abs.service.counter.impl;

import java.util.Date;

import nc.noumea.mairie.abs.domain.AgentRecupCount;
import nc.noumea.mairie.abs.domain.AgentWeekRecup;
import nc.noumea.mairie.abs.domain.BaseAgentWeekHisto;
import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeRecup;
import nc.noumea.mairie.abs.domain.MotifCompteur;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.dto.CompteurDto;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.service.AgentNotFoundException;
import nc.noumea.mairie.abs.service.NotAMondayException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("RecupCounterServiceImpl")
public class RecupCounterServiceImpl extends AbstractCounterService {

	/**
	 * appeler depuis Kiosque ou SIRH l historique ABS_AGENT_WEEK_ALIM_MANUELLE
	 * mise a jour
	 */
	@Override
	protected ReturnMessageDto majManuelleCompteurToAgent(Integer idAgent, CompteurDto compteurDto,
			ReturnMessageDto result, MotifCompteur motifCompteur) {

		logger.info("Trying to update manually recuperation counters for Agent {} ...", compteurDto.getIdAgent());

		Double dMinutes = helperService.calculMinutesAlimManuelleCompteur(compteurDto);
		Integer minutes = null != dMinutes ? dMinutes.intValue() : 0;
		try {
			return majManuelleCompteurToAgent(idAgent, compteurDto, minutes, RefTypeAbsenceEnum.RECUP.getValue(), result, motifCompteur);
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
	@Transactional(value = "absTransactionManager")
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
	public ReturnMessageDto majCompteurToAgent(ReturnMessageDto srm, Demande demande, DemandeEtatChangeDto demandeEtatChangeDto) {

		logger.info("Trying to update recuperation counters for Agent [{}] ...", demande.getIdAgent());
		
		Integer minutes = calculMinutesCompteur(demandeEtatChangeDto, demande);

		if(0 != minutes) {
			try {
				return majCompteurToAgent(demande.getIdAgent(), minutes, srm);
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException("An error occured while trying to update recuperation counters :", e);
			}
		}
		return srm;
	}
	
	protected int calculMinutesCompteur(DemandeEtatChangeDto demandeEtatChangeDto, Demande demande) {
		int duree = 0;
		// si on approuve, le compteur decremente
		if (demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.APPROUVEE.getCodeEtat())) {
			duree = 0 - ((DemandeRecup) demande).getDuree();
		}
		// si on passe de Approuve a Refuse, le compteur incremente
		if ((demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.REFUSEE.getCodeEtat()) || demandeEtatChangeDto
				.getIdRefEtat().equals(RefEtatEnum.ANNULEE.getCodeEtat()))
				&& demande.getLatestEtatDemande().getEtat().equals(RefEtatEnum.APPROUVEE)) {
			duree = ((DemandeRecup) demande).getDuree();
		}

		return duree;
	}

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

		AgentRecupCount arc = (AgentRecupCount) counterRepository.getAgentCounter(T1, idAgent);

		if (arc == null) {
			arc = new AgentRecupCount();
			arc.setIdAgent(idAgent);
		}

		arc.setTotalMinutes(arc.getTotalMinutes() + minutes - minutesBeforeUpdate);
		arc.setLastModification(helperService.getCurrentDate());

		counterRepository.persistEntity(awr);
		counterRepository.persistEntity(arc);

		return arc.getTotalMinutes();
	}

	/**
	 * Mets à jour le compteur de minutes désiré (en fonction des types passés
	 * en paramètre) sans mettre a jour l historique
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
	protected <T1, T2> ReturnMessageDto majCompteurToAgent(Integer idAgent, Integer minutes, ReturnMessageDto srm)
			throws InstantiationException, IllegalAccessException {

		if (sirhRepository.getAgent(idAgent) == null) {
			logger.error("There is no Agent [{}]. Impossible to update its counters.", idAgent);
			throw new AgentNotFoundException();
		}

		logger.info("updating counters for Agent [{}] with {} minutes...", idAgent, minutes);

		AgentRecupCount arc = (AgentRecupCount) counterRepository.getAgentCounter(AgentRecupCount.class, idAgent);

		if (arc == null) {
			arc = new AgentRecupCount();
			arc.setIdAgent(idAgent);
		}

		// on verifie que le solde est positif seulement si on debite le
		// compteur
		if (0 > minutes && 0 > arc.getTotalMinutes() + minutes) {
			logger.warn(SOLDE_COMPTEUR_NEGATIF);
			srm.getErrors().add(String.format(SOLDE_COMPTEUR_NEGATIF));
			return srm;
		}

		arc.setTotalMinutes(arc.getTotalMinutes() + minutes);
		arc.setLastModification(helperService.getCurrentDate());

		counterRepository.persistEntity(arc);

		return srm;
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
			Integer minutes, Integer idRefTypeAbsence, ReturnMessageDto srm, MotifCompteur motifCompteur) throws InstantiationException,
			IllegalAccessException {

		if (sirhRepository.getAgent(compteurDto.getIdAgent()) == null) {
			logger.error("There is no Agent [{}]. Impossible to update its counters.", compteurDto.getIdAgent());
			throw new AgentNotFoundException();
		}

		logger.info("updating counters for Agent [{}] with {} minutes...", compteurDto.getIdAgent(), minutes);

		AgentRecupCount arc = (AgentRecupCount) counterRepository.getAgentCounter(AgentRecupCount.class,
				compteurDto.getIdAgent());

		if (arc == null) {
			arc = new AgentRecupCount();
			arc.setIdAgent(compteurDto.getIdAgent());
		}

		// on verifie que le solde est positif seulement si on debite le
		// compteur
		controlCompteurPositif(minutes, arc.getTotalMinutes(), srm);
		if (!srm.getErrors().isEmpty()) {
			return srm;
		}

		String textLog = "";
		if (null != compteurDto.getDureeAAjouter()) {
			textLog = "Ajout de " + minutes + " minutes.";
		}
		if (null != compteurDto.getDureeARetrancher()) {
			textLog = "Retrait de " + minutes + " minutes.";
		}

		arc.setTotalMinutes(arc.getTotalMinutes() + minutes);
		arc.setLastModification(helperService.getCurrentDate());

		counterRepository.persistEntity(arc);
		majAgentHistoAlimManuelle(idAgentOperateur, compteurDto.getIdAgent(), motifCompteur, textLog, arc, idRefTypeAbsence);

		return srm;
	}
}
