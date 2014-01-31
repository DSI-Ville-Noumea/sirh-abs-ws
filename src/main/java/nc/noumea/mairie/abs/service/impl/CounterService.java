package nc.noumea.mairie.abs.service.impl;

import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentHistoAlimManuelle;
import nc.noumea.mairie.abs.domain.AgentRecupCount;
import nc.noumea.mairie.abs.domain.AgentReposCompCount;
import nc.noumea.mairie.abs.domain.AgentWeekRecup;
import nc.noumea.mairie.abs.domain.AgentWeekReposComp;
import nc.noumea.mairie.abs.domain.BaseAgentCount;
import nc.noumea.mairie.abs.domain.BaseAgentWeekHisto;
import nc.noumea.mairie.abs.domain.MotifCompteur;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.dto.CompteurDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.IAccessRightsRepository;
import nc.noumea.mairie.abs.repository.ICounterRepository;
import nc.noumea.mairie.abs.repository.ISirhRepository;
import nc.noumea.mairie.abs.service.AgentNotFoundException;
import nc.noumea.mairie.abs.service.ICounterService;
import nc.noumea.mairie.abs.service.NotAMondayException;
import nc.noumea.mairie.ws.ISirhWSConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CounterService implements ICounterService {

	private Logger logger = LoggerFactory.getLogger(CounterService.class);

	@Autowired
	private ISirhWSConsumer sirhWSConsumer;
	
	@Autowired
	private ICounterRepository counterRepository;

	@Autowired
	private ISirhRepository sirhRepository;

	@Autowired
	private HelperService helperService;

	@Autowired
	private IAccessRightsRepository accessRightsRepository;

	public static final String MOTIF_COMPTEUR_INEXISTANT = "Le motif n'existe pas.";
	public static final String SOLDE_COMPTEUR_NEGATIF = "Le solde du compteur de l'agent ne peut pas être négatif.";
	public static final String OPERATEUR_INEXISTANT = "Vous n'êtes pas habilité à mettre à jour le compteur de cet agent.";
	public static final String DUREE_A_SAISIR = "La durée à ajouter ou retrancher n'est pas saisie.";
	public static final String ERREUR_DUREE_SAISIE = "Un seul des champs Durée à ajouter ou Durée à retrancher doit être saisi.";
	public static final String COMPTEUR_INEXISTANT = "Le compteur n'existe pas.";
	
	public static final String RESET_COMPTEUR_ANNEE_PRECEDENTE = "Remise à 0 du compteur Année précédente";
	public static final String RESET_COMPTEUR_ANNEE_EN_COURS = "Remise à 0 du compteur Année en cours";
	
	
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

	/**
	 * appeler par PTG exclusivement
	 * l historique utilise a pour seul but de rectifier le compteur en cas de modification par l agent dans ses pointages
	 */
	@Override
	public int addRecuperationToAgentForPTG(Integer idAgent, Date dateMonday, Integer minutes) {

		logger.info("Trying to update recuperation counters for Agent [{}] and date [{}] with {} minutes...", idAgent,
				dateMonday, minutes);

		try {
			return addMinutesToAgent(AgentRecupCount.class, AgentWeekRecup.class, idAgent, dateMonday, minutes);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException("An error occured while trying to update recuperation counters :", e);
		}
	}


	/**
	 * appeler par PTG exclusivement
	 * l historique utilise a pour seul but de rectifier le compteur en cas de modification par l agent dans ses pointages
	 */
	@Override
	public int addReposCompensateurToAgentForPTG(Integer idAgent, Date dateMonday, Integer minutes) {

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
	 * en paramètre) sans mettre a jour l historique
	 * 
	 * @param T1
	 *            inherits BaseAgentCount
	 * @param idAgent
	 * @param minutes : negatif pour debiter, positif pour crediter
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	protected <T1, T2> ReturnMessageDto majCompteurToAgent(Class<T1> T1, Integer idAgent, Integer minutes, ReturnMessageDto srm) 
			throws InstantiationException, IllegalAccessException {

		if (sirhRepository.getAgent(idAgent) == null) {
			logger.error("There is no Agent [{}]. Impossible to update its counters.", idAgent);
			throw new AgentNotFoundException();
		}

		logger.info("updating counters for Agent [{}] with {} minutes...", idAgent, minutes);
		
		BaseAgentCount arc = (BaseAgentCount) counterRepository.getAgentCounter(T1, idAgent);
		
		if (arc == null) {
			arc = (BaseAgentCount) T1.newInstance();
			arc.setIdAgent(idAgent);
		}
		
		// on verifie que le solde est positif seulement si on debite le compteur
		if(0 > minutes
				&& 0 > arc.getTotalMinutes() + minutes) {
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
	 * appeler depuis ABSENCE
	 * l historique ABS_AGENT_WEEK_... n est pas utilise
	 */
	@Override
	public ReturnMessageDto majCompteurRecupToAgent(ReturnMessageDto srm, Integer idAgent, Integer minutes) {

		logger.info("Trying to update recuperation counters for Agent [{}] with {} minutes...", idAgent, minutes);

		try {
			return majCompteurToAgent(AgentRecupCount.class, idAgent, minutes, srm);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException("An error occured while trying to update recuperation counters :", e);
		}
	}
	
	/**
	 * appeler depuis Kiosque ou SIRH
	 * l historique ABS_AGENT_WEEK_ALIM_MANUELLE mise a jour
	 */
	@Override
	public ReturnMessageDto majManuelleCompteurToAgent(Integer idAgent, CompteurDto compteurDto, RefTypeAbsenceEnum refTypeAbsence) {
		
		logger.info("Trying to update manually counters for Agent {} ...", compteurDto.getIdAgent());
		
		ReturnMessageDto result = new ReturnMessageDto();
		
		// seul l operateur peut mettre a jour les compteurs de ses agents
		if(!accessRightsRepository.isOperateurOfAgent(idAgent, compteurDto.getIdAgent())){
			// tester si agent est un utilisateur SIRH
			ReturnMessageDto isUtilisateurSIRH = sirhWSConsumer.isUtilisateurSIRH(idAgent);
			if(!isUtilisateurSIRH.getErrors().isEmpty()) {
				logger.warn(OPERATEUR_INEXISTANT);
				result.getErrors().add(String.format(OPERATEUR_INEXISTANT));
				return result;
			}
		}
		
		controlSaisieAlimManuelleCompteur(compteurDto, result);
		if(!result.getErrors().isEmpty())
			return result;
		
		switch (refTypeAbsence) {
			case CONGE_ANNUEL:
				// TODO
				break;
			case REPOS_COMP:
				result = majManuelleCompteurRCToAgent(idAgent, compteurDto, result, RefTypeAbsenceEnum.REPOS_COMP.getValue());
				break;
			case RECUP:
				result = majManuelleCompteurRecupToAgent(idAgent, compteurDto, result, RefTypeAbsenceEnum.RECUP.getValue());
				break;
			case ASA:
				// TODO
				break;
			case AUTRES:
				// TODO
				break;
			case MALADIES:
				// TODO
				break;
		}
		
		return result;
	}
	
	/**
	 * appeler depuis Kiosque ou SIRH
	 * l historique ABS_AGENT_WEEK_ALIM_MANUELLE mise a jour
	 */
	protected ReturnMessageDto majManuelleCompteurRecupToAgent(Integer idAgent, CompteurDto compteurDto, ReturnMessageDto result, Integer idRefTypeAbsence) {

		logger.info("Trying to update manually recuperation counters for Agent {} ...", compteurDto.getIdAgent());
		
		int minutes = helperService.calculMinutesAlimManuelleCompteur(compteurDto);
		
		try {
			return majManuelleCompteurToAgent(AgentRecupCount.class, idAgent, compteurDto.getIdAgent(), minutes, null, compteurDto.getIdMotifCompteur(), idRefTypeAbsence, result);
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
	 * @param minutes : negatif pour debiter, positif pour crediter
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	protected <T1, T2> ReturnMessageDto majManuelleCompteurToAgent(
			Class<T1> T1, Integer idAgentOperateur, Integer idAgent, Integer minutes, Integer minutesAnneeN1, Integer idMotifCompteur, Integer idRefTypeAbsence, ReturnMessageDto srm) 
			throws InstantiationException, IllegalAccessException {

		if (sirhRepository.getAgent(idAgent) == null) {
			logger.error("There is no Agent [{}]. Impossible to update its counters.", idAgent);
			throw new AgentNotFoundException();
		}

		logger.info("updating counters for Agent [{}] with {} minutes...", idAgent, minutes);
		
		BaseAgentCount arc = (BaseAgentCount) counterRepository.getAgentCounter(T1, idAgent);
		
		if (arc == null) {
			arc = (BaseAgentCount) T1.newInstance();
			arc.setIdAgent(idAgent);
		}
		
		// on verifie que le solde est positif seulement si on debite le compteur
		controlCompteurPositif(minutes, arc.getTotalMinutes(), srm);
		if(!srm.getErrors().isEmpty()) {
			return srm;
		}
		
		MotifCompteur motifCompteur = counterRepository.getEntity(MotifCompteur.class, idMotifCompteur);
		if(null == motifCompteur) {
			logger.warn(MOTIF_COMPTEUR_INEXISTANT);
			srm.getErrors().add(String.format(MOTIF_COMPTEUR_INEXISTANT));
			return srm;
		}
		
		AgentHistoAlimManuelle histo = new AgentHistoAlimManuelle();
			histo.setIdAgent(idAgentOperateur);
			histo.setMinutes(minutes);
			histo.setMinutesAnneeN1(minutesAnneeN1);
			histo.setDateModification(helperService.getCurrentDate());
			histo.setMotifCompteur(motifCompteur);
		
		RefTypeAbsence rta = new RefTypeAbsence();
			rta.setIdRefTypeAbsence(idRefTypeAbsence);
			histo.setType(rta);
				
		arc.setTotalMinutes(arc.getTotalMinutes() + minutes);
		arc.setLastModification(helperService.getCurrentDate());

		counterRepository.persistEntity(arc);
		counterRepository.persistEntity(histo);

		return srm;
	}
	
	protected void controlSaisieAlimManuelleCompteur(CompteurDto compteurDto, ReturnMessageDto result) {
		
		if(null == compteurDto.getDureeAAjouter() && null == compteurDto.getDureeARetrancher()) {
			logger.debug(DUREE_A_SAISIR);
			result.getErrors().add(String.format(DUREE_A_SAISIR));
		}
		
		if(null != compteurDto.getDureeAAjouter() && null != compteurDto.getDureeARetrancher()) {
			logger.debug(ERREUR_DUREE_SAISIE);
			result.getErrors().add(String.format(ERREUR_DUREE_SAISIE));
		}
	}
	
	protected ReturnMessageDto majManuelleCompteurRCToAgent(Integer idAgent, CompteurDto compteurDto, ReturnMessageDto srm, Integer idRefTypeAbsence) {
		
		logger.info("Trying to update manually Repos Comp. counters for Agent {} ...", compteurDto.getIdAgent());
		
		Integer minutes = null;
		Integer minutesAnneeN1 = null;
		
		if(compteurDto.isAnneePrécedente()) {
			minutesAnneeN1 = helperService.calculMinutesAlimManuelleCompteur(compteurDto);
		}else{
			minutes = helperService.calculMinutesAlimManuelleCompteur(compteurDto);
		}
		
		try {
			return majManuelleCompteurRCToAgent(idAgent, compteurDto.getIdAgent(), minutes, minutesAnneeN1, compteurDto.getIdMotifCompteur(), idRefTypeAbsence, srm);
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
	 * @param minutes : negatif pour debiter, positif pour crediter
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	protected ReturnMessageDto majManuelleCompteurRCToAgent(
			Integer idAgentOperateur, Integer idAgent, Integer minutes, Integer minutesAnneeN1, Integer idMotifCompteur, Integer idRefTypeAbsence, ReturnMessageDto srm) 
			throws InstantiationException, IllegalAccessException {

		if (sirhRepository.getAgent(idAgent) == null) {
			logger.error("There is no Agent [{}]. Impossible to update its counters.", idAgent);
			throw new AgentNotFoundException();
		}

		logger.info("updating counters for Agent [{}] with {} minutes...", idAgent, minutes);
		
		AgentReposCompCount arc = (AgentReposCompCount) counterRepository.getAgentCounter(AgentReposCompCount.class, idAgent);
		
		if (arc == null) {
			arc = new AgentReposCompCount();
			arc.setIdAgent(idAgent);
		}
		
		// on verifie que le solde est positif
		controlCompteurPositif(minutes, arc.getTotalMinutes(), srm);
		controlCompteurPositif(minutesAnneeN1, arc.getTotalMinutesAnneeN1(), srm);
		if(!srm.getErrors().isEmpty()) {
			return srm;
		}
		
		MotifCompteur motifCompteur = counterRepository.getEntity(MotifCompteur.class, idMotifCompteur);
		if(null == motifCompteur) {
			logger.warn(MOTIF_COMPTEUR_INEXISTANT);
			srm.getErrors().add(String.format(MOTIF_COMPTEUR_INEXISTANT));
			return srm;
		}
		
		AgentHistoAlimManuelle histo = new AgentHistoAlimManuelle();
			histo.setIdAgent(idAgentOperateur);
			histo.setMinutes(minutes);
			histo.setMinutesAnneeN1(minutesAnneeN1);
			histo.setDateModification(helperService.getCurrentDate());
			histo.setMotifCompteur(motifCompteur);
			
		RefTypeAbsence rta = new RefTypeAbsence();
			rta.setIdRefTypeAbsence(idRefTypeAbsence);
			histo.setType(rta);
		
		if(null != minutes) {
			arc.setTotalMinutes(arc.getTotalMinutes() + minutes);
		}
		if(null != minutesAnneeN1) {
			arc.setTotalMinutesAnneeN1(arc.getTotalMinutesAnneeN1() + minutesAnneeN1);
		}
		arc.setLastModification(helperService.getCurrentDate());

		counterRepository.persistEntity(arc);
		counterRepository.persistEntity(histo);

		return srm;
	}
	
	protected void controlCompteurPositif(Integer minutes, Integer totalMinutes, ReturnMessageDto srm) {
		if(null != minutes
				&& 0 > totalMinutes + minutes) {
			logger.warn(SOLDE_COMPTEUR_NEGATIF);
			srm.getErrors().add(String.format(SOLDE_COMPTEUR_NEGATIF));
		}
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
		
		// selon la SFD, compteur annee en cours = solde annee en cours + modulo 4 en heures de l annee precedente
		int modulo4 = arc.getTotalMinutesAnneeN1() % (4*60);
		
		AgentHistoAlimManuelle histo = new AgentHistoAlimManuelle();
			histo.setIdAgent(arc.getIdAgent());
			histo.setMinutes(modulo4);
			histo.setMinutesAnneeN1(0 - arc.getTotalMinutesAnneeN1());
			histo.setDateModification(helperService.getCurrentDate());
			histo.setMotifCompteur(null);
			histo.setMotifTechnique(RESET_COMPTEUR_ANNEE_PRECEDENTE);
		
		RefTypeAbsence rta = new RefTypeAbsence();
			rta.setIdRefTypeAbsence(RefTypeAbsenceEnum.REPOS_COMP.getValue());
			histo.setType(rta);
		
		arc.setTotalMinutes(arc.getTotalMinutes() + modulo4);
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
			histo.setMinutes(0 - arc.getTotalMinutes());
			histo.setMinutesAnneeN1(arc.getTotalMinutes());
			histo.setDateModification(helperService.getCurrentDate());
			histo.setMotifCompteur(null);
			histo.setMotifTechnique(RESET_COMPTEUR_ANNEE_EN_COURS);
		
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
}
