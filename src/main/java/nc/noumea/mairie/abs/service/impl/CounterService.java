package nc.noumea.mairie.abs.service.impl;

import java.util.Date;

import nc.noumea.mairie.abs.domain.AgentRecupCount;
import nc.noumea.mairie.abs.domain.AgentReposCompCount;
import nc.noumea.mairie.abs.domain.AgentWeekAlimManuelle;
import nc.noumea.mairie.abs.domain.AgentWeekRecup;
import nc.noumea.mairie.abs.domain.AgentWeekReposComp;
import nc.noumea.mairie.abs.domain.BaseAgentCount;
import nc.noumea.mairie.abs.domain.BaseAgentWeekHisto;
import nc.noumea.mairie.abs.domain.MotifCompteur;
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
	public ReturnMessageDto majManuelleCompteurRecupToAgent(Integer idAgent, CompteurDto compteurDto) {

		logger.info("Trying to update manually recuperation counters for Agent {} ...", compteurDto.getIdAgent());
		
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
		
		int minutes = helperService.calculMinutesAlimManuelleCompteur(compteurDto);

		try {
			return majManuelleCompteurToAgent(AgentRecupCount.class, compteurDto.getIdAgent(), minutes, compteurDto.getIdMotifCompteur(), result);
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
	protected <T1, T2> ReturnMessageDto majManuelleCompteurToAgent(Class<T1> T1, Integer idAgent, Integer minutes, Integer idMotifCompteur, ReturnMessageDto srm) 
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
		if(0 > arc.getTotalMinutes() + minutes) {
			logger.warn(SOLDE_COMPTEUR_NEGATIF);
			srm.getErrors().add(String.format(SOLDE_COMPTEUR_NEGATIF));
			return srm;
		}
		
		MotifCompteur motifCompteur = counterRepository.getEntity(MotifCompteur.class, idMotifCompteur);
		if(null == motifCompteur) {
			logger.warn(MOTIF_COMPTEUR_INEXISTANT);
			srm.getErrors().add(String.format(MOTIF_COMPTEUR_INEXISTANT));
			return srm;
		}
		
		AgentWeekAlimManuelle histo = new AgentWeekAlimManuelle();
			histo.setIdAgent(idAgent);
			histo.setMinutes(minutes);
			histo.setDateModification(helperService.getCurrentDate());
			histo.setMotifCompteur(motifCompteur);
		
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
	
}
