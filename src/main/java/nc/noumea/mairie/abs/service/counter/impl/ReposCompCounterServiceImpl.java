package nc.noumea.mairie.abs.service.counter.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentHistoAlimManuelle;
import nc.noumea.mairie.abs.domain.AgentReposCompCount;
import nc.noumea.mairie.abs.domain.AgentWeekReposComp;
import nc.noumea.mairie.abs.domain.BaseAgentWeekHisto;
import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeReposComp;
import nc.noumea.mairie.abs.domain.MotifCompteur;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.dto.CompteurDto;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.service.AgentNotFoundException;
import nc.noumea.mairie.abs.service.NotAMondayException;

import org.springframework.stereotype.Service;

@Service("ReposCompCounterServiceImpl")
public class ReposCompCounterServiceImpl extends AbstractCounterService {

	/**
	 * appeler par PTG exclusivement l historique utilise a pour seul but de
	 * rectifier le compteur en cas de modification par l agent dans ses
	 * pointages
	 */
	@Override
	public int addToAgentForPTG(Integer idAgent, Date dateMonday, Integer minutes) {

		logger.info("Trying to update repos compensateurs counters for Agent [{}] and date [{}] with {} minutes...",
				idAgent, dateMonday, minutes);

		try {
			return addMinutesToAgent(AgentReposCompCount.class, AgentWeekReposComp.class, idAgent, dateMonday, minutes);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException("An error occured while trying to update repos compensateur counters :", e);
		}
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

		AgentReposCompCount arc = (AgentReposCompCount) counterRepository.getAgentCounter(T1, idAgent);

		if (arc == null) {
			arc = new AgentReposCompCount();
			arc.setIdAgent(idAgent);
		}

		int totalMinutesAnneeN1 = 0;
		int totalMinutes = 0;
		// Met on a jour le compteur de l annee precedente ou l annee en cours
		int precedentYear = GregorianCalendar.getInstance().get(Calendar.YEAR) - 1;
		GregorianCalendar calStr1 = new GregorianCalendar();
		calStr1.setTime(dateMonday);
		int yearDateMonday = calStr1.get(Calendar.YEAR);
		// annee precedente
		if (precedentYear >= yearDateMonday) {
			// si compteur annee N-1 negatif, on met a jour les compteurs N-1 et
			// N
			if (arc.getTotalMinutesAnneeN1() + minutes - minutesBeforeUpdate < 0) {
				totalMinutesAnneeN1 = 0;
				totalMinutes = arc.getTotalMinutes() + (arc.getTotalMinutesAnneeN1() + minutes - minutesBeforeUpdate);
			} else {
				totalMinutesAnneeN1 = arc.getTotalMinutesAnneeN1() + minutes - minutesBeforeUpdate;
				totalMinutes = arc.getTotalMinutes();
			}
		}
		// annee en cours
		if (precedentYear < yearDateMonday) {
			totalMinutesAnneeN1 = arc.getTotalMinutesAnneeN1();
			totalMinutes = arc.getTotalMinutes() + minutes - minutesBeforeUpdate;
		}

		arc.setTotalMinutesAnneeN1(totalMinutesAnneeN1);
		arc.setTotalMinutes(totalMinutes);
		arc.setLastModification(helperService.getCurrentDate());

		counterRepository.persistEntity(awr);
		counterRepository.persistEntity(arc);

		return arc.getTotalMinutes();
	}

	public ReturnMessageDto resetCompteurRCAnneePrecedente(Integer idAgentReposCompCount) {

		logger.info("reset CompteurRCAnneePrecedente for idAgentReposCompCount {} ...", idAgentReposCompCount);

		ReturnMessageDto srm = new ReturnMessageDto();

		AgentReposCompCount arc = counterRepository.getAgentReposCompCountByIdCounter(idAgentReposCompCount);

		if (arc == null) {
			logger.warn(COMPTEUR_INEXISTANT);
			srm.getErrors().add(String.format(COMPTEUR_INEXISTANT));
			return srm;
		}

		// selon la SFD, compteur annee en cours = solde annee en cours + modulo
		// 4 en heures de l annee precedente

		AgentHistoAlimManuelle histo = new AgentHistoAlimManuelle();
		histo.setIdAgent(arc.getIdAgent());
		histo.setIdAgentConcerne(arc.getIdAgent());
		histo.setDateModification(helperService.getCurrentDate());
		histo.setMotifCompteur(null);
		histo.setMotifTechnique(RESET_COMPTEUR_ANNEE_PRECEDENTE);
		String textLog = "Retrait de " + (0 - arc.getTotalMinutesAnneeN1()) + " minutes sur l'année précédente.";
		histo.setText(textLog);

		RefTypeAbsence rta = new RefTypeAbsence();
		rta.setIdRefTypeAbsence(RefTypeAbsenceEnum.REPOS_COMP.getValue());
		histo.setType(rta);

		arc.setTotalMinutesAnneeN1(0);

		counterRepository.persistEntity(arc);
		counterRepository.persistEntity(histo);

		return srm;
	}

	public ReturnMessageDto resetCompteurRCAnneenCours(Integer idAgentReposCompCount) {

		logger.info("reset CompteurRCAnneePrecedente for idAgentReposCompCount {} ...", idAgentReposCompCount);

		ReturnMessageDto srm = new ReturnMessageDto();

		AgentReposCompCount arc = counterRepository.getAgentReposCompCountByIdCounter(idAgentReposCompCount);

		if (arc == null) {
			logger.warn(COMPTEUR_INEXISTANT);
			srm.getErrors().add(String.format(COMPTEUR_INEXISTANT));
			return srm;
		}

		AgentHistoAlimManuelle histo = new AgentHistoAlimManuelle();
		histo.setIdAgent(arc.getIdAgent());
		histo.setIdAgentConcerne(arc.getIdAgent());
		histo.setDateModification(helperService.getCurrentDate());
		histo.setMotifCompteur(null);
		histo.setMotifTechnique(RESET_COMPTEUR_ANNEE_EN_COURS);
		String textLog = "Retrait de " + (0 - arc.getTotalMinutes()) + " minutes sur l'année.";

		histo.setText(textLog);

		RefTypeAbsence rta = new RefTypeAbsence();
		rta.setIdRefTypeAbsence(RefTypeAbsenceEnum.REPOS_COMP.getValue());
		histo.setType(rta);

		arc.setTotalMinutesAnneeN1(arc.getTotalMinutesAnneeN1() + arc.getTotalMinutes());
		arc.setTotalMinutes(0);

		counterRepository.persistEntity(arc);
		counterRepository.persistEntity(histo);

		return srm;
	}

	public List<Integer> getListAgentReposCompCountForResetAnneePrcd() {
		return counterRepository.getListAgentReposCompCountForResetAnneePrcd();
	}

	public List<Integer> getListAgentReposCompCountForResetAnneeEnCours() {
		return counterRepository.getListAgentReposCompCountForResetAnneeEnCours();
	}

	/**
	 * appeler depuis Kiosque ou SIRH l historique ABS_AGENT_WEEK_ALIM_MANUELLE
	 * mise a jour
	 */
	@Override
	public ReturnMessageDto majManuelleCompteurToAgent(Integer idAgent, CompteurDto compteurDto) {

		logger.info("Trying to update Repos Compensateur manually counters for Agent {} ...", compteurDto.getIdAgent());

		ReturnMessageDto result = new ReturnMessageDto();

		result = super.majManuelleCompteurToAgent(idAgent, compteurDto);
		if (!result.getErrors().isEmpty())
			return result;

		result = majManuelleCompteurToAgent(idAgent, compteurDto, result, RefTypeAbsenceEnum.REPOS_COMP.getValue());

		return result;
	}

	private ReturnMessageDto majManuelleCompteurToAgent(Integer idAgent, CompteurDto compteurDto, ReturnMessageDto srm,
			Integer idRefTypeAbsence) {

		logger.info("Trying to update manually Repos Comp. counters for Agent {} ...", compteurDto.getIdAgent());

		Integer minutes = null;
		Integer minutesAnneeN1 = null;

		if (compteurDto.isAnneePrecedente()) {
			Double dMinutesAnneeN1 = helperService.calculMinutesAlimManuelleCompteur(compteurDto);
			minutesAnneeN1 = null != dMinutesAnneeN1 ? dMinutesAnneeN1.intValue() : 0;
		} else {
			Double dMinutes = helperService.calculMinutesAlimManuelleCompteur(compteurDto);
			minutes = null != dMinutes ? dMinutes.intValue() : 0;
		}

		try {
			return majManuelleCompteurToAgent(idAgent, compteurDto, minutes, minutesAnneeN1, idRefTypeAbsence, srm);
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
	private ReturnMessageDto majManuelleCompteurToAgent(Integer idAgentOperateur, CompteurDto compteurDto,
			Integer minutes, Integer minutesAnneeN1, Integer idRefTypeAbsence, ReturnMessageDto srm)
			throws InstantiationException, IllegalAccessException {

		if (sirhRepository.getAgent(compteurDto.getIdAgent()) == null) {
			logger.error("There is no Agent [{}]. Impossible to update its counters.", compteurDto.getIdAgent());
			throw new AgentNotFoundException();
		}

		logger.info("updating counters for Agent [{}] with {} minutes...", compteurDto.getIdAgent(), minutes);

		AgentReposCompCount arc = (AgentReposCompCount) counterRepository.getAgentCounter(AgentReposCompCount.class,
				compteurDto.getIdAgent());

		if (arc == null) {
			arc = new AgentReposCompCount();
			arc.setIdAgent(compteurDto.getIdAgent());
		}

		// on verifie que le solde est positif
		controlCompteurPositif(minutes, arc.getTotalMinutes(), srm);
		controlCompteurPositif(minutesAnneeN1, arc.getTotalMinutesAnneeN1(), srm);
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
			if (compteurDto.isAnneePrecedente()) {
				textLog = "Ajout de " + minutesAnneeN1 + " minutes sur le compteur de l'année précédente.";
			} else {
				textLog = "Ajout de " + minutes + " minutes sur le compteur de l'année.";
			}
		}
		if (null != compteurDto.getDureeARetrancher()) {
			if (compteurDto.isAnneePrecedente()) {
				textLog = "Retrait de " + minutesAnneeN1 + " minutes sur le compteur de l'année précédente.";
			} else {
				textLog = "Retrait de " + minutes + " minutes sur le compteur de l'année.";
			}
		}
		histo.setText(textLog);

		RefTypeAbsence rta = new RefTypeAbsence();
		rta.setIdRefTypeAbsence(idRefTypeAbsence);
		histo.setType(rta);

		if (null != minutes) {
			arc.setTotalMinutes(arc.getTotalMinutes() + minutes);
		}
		if (null != minutesAnneeN1) {
			arc.setTotalMinutesAnneeN1(arc.getTotalMinutesAnneeN1() + minutesAnneeN1);
		}
		arc.setLastModification(helperService.getCurrentDate());

		counterRepository.persistEntity(arc);
		counterRepository.persistEntity(histo);

		return srm;
	}

	@Override
	public ReturnMessageDto majCompteurToAgent(ReturnMessageDto srm, Demande demande, Integer minutes) {

		logger.info("Trying to update recuperation counters for Agent [{}] with {} minutes...", demande.getIdAgent(),
				minutes);

		try {
			return majCompteurToAgent((DemandeReposComp) demande, minutes, srm);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException("An error occured while trying to update recuperation counters :", e);
		}
	}

	@Override
	public int calculMinutesCompteur(DemandeEtatChangeDto demandeEtatChangeDto, Demande demande) {
		int minutes = 0;
		// si on approuve, le compteur decremente
		if (demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.APPROUVEE.getCodeEtat())) {
			minutes = 0 - ((DemandeReposComp) demande).getDuree();
		}
		// si on passe de Approuve a Refuse, le compteur incremente
		if ((demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.REFUSEE.getCodeEtat()) || demandeEtatChangeDto
				.getIdRefEtat().equals(RefEtatEnum.ANNULEE.getCodeEtat()))
				&& demande.getLatestEtatDemande().getEtat().equals(RefEtatEnum.APPROUVEE)) {
			minutes = ((DemandeReposComp) demande).getDuree() + ((DemandeReposComp) demande).getDureeAnneeN1();
		}

		return minutes;
	}

	/**
	 * Mets à jour le compteur de minutes désiré (en fonction des types passés
	 * en paramètre) sans mettre a jour l historique
	 * 
	 * Dans le cas des ReposComp, il faut gérer l'année N-1 et N dans le debit
	 * et le credit
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
	protected <T1, T2> ReturnMessageDto majCompteurToAgent(DemandeReposComp demande, Integer minutes,
			ReturnMessageDto srm) throws InstantiationException, IllegalAccessException {

		if (sirhRepository.getAgent(demande.getIdAgent()) == null) {
			logger.error("There is no Agent [{}]. Impossible to update its counters.", demande.getIdAgent());
			throw new AgentNotFoundException();
		}

		logger.info("updating counters for Agent [{}] with {} minutes...", demande.getIdAgent(), minutes);

		AgentReposCompCount arc = (AgentReposCompCount) counterRepository.getAgentCounter(AgentReposCompCount.class,
				demande.getIdAgent());

		if (arc == null) {
			arc = new AgentReposCompCount();
			arc.setIdAgent(demande.getIdAgent());
			arc.setTotalMinutes(0);
			arc.setTotalMinutesAnneeN1(0);
		}

		// on verifie que le solde est positif seulement si on debite le
		// compteur
		if (0 > minutes && 0 > arc.getTotalMinutes() + arc.getTotalMinutesAnneeN1() + minutes) {
			logger.warn(SOLDE_COMPTEUR_NEGATIF);
			srm.getErrors().add(String.format(SOLDE_COMPTEUR_NEGATIF));
			return srm;
		}

		Integer minutesAnneeN1 = 0;
		Integer minutesAnneeEnCours = 0;
		// dans le cas ou on débite
		if (0 > minutes) {
			if (0 > arc.getTotalMinutesAnneeN1() + minutes) {
				minutesAnneeN1 = 0 - arc.getTotalMinutesAnneeN1();
				minutesAnneeEnCours = arc.getTotalMinutesAnneeN1() + minutes;
				demande.setDuree(0 - minutesAnneeEnCours);
				demande.setDureeAnneeN1(arc.getTotalMinutesAnneeN1());
			} else {
				minutesAnneeN1 = minutes;
				demande.setDuree(0);
				demande.setDureeAnneeN1(0 - minutesAnneeN1);
			}
		}
		// dans le cas ou on recredite
		if (0 < minutes) {
			minutesAnneeEnCours = null != demande.getDuree() ? demande.getDuree() : 0;
			minutesAnneeN1 = null != demande.getDureeAnneeN1() ? demande.getDureeAnneeN1() : 0;
			demande.setDuree(minutesAnneeN1 + minutesAnneeEnCours);
			demande.setDureeAnneeN1(0);
		}

		arc.setTotalMinutes(arc.getTotalMinutes() + minutesAnneeEnCours);
		arc.setTotalMinutesAnneeN1(arc.getTotalMinutesAnneeN1() + minutesAnneeN1);
		arc.setLastModification(helperService.getCurrentDate());

		counterRepository.persistEntity(arc);
		counterRepository.persistEntity(demande);

		return srm;
	}
}
