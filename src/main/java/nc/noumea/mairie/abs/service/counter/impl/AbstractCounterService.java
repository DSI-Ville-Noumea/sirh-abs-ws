package nc.noumea.mairie.abs.service.counter.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentHistoAlimManuelle;
import nc.noumea.mairie.abs.domain.BaseAgentCount;
import nc.noumea.mairie.abs.domain.BaseAgentWeekHisto;
import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeRecup;
import nc.noumea.mairie.abs.domain.MotifCompteur;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.dto.CompteurDto;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.IAccessRightsRepository;
import nc.noumea.mairie.abs.repository.ICounterRepository;
import nc.noumea.mairie.abs.repository.ISirhRepository;
import nc.noumea.mairie.abs.service.AgentNotFoundException;
import nc.noumea.mairie.abs.service.ICounterService;
import nc.noumea.mairie.abs.service.NotAMondayException;
import nc.noumea.mairie.abs.service.impl.HelperService;
import nc.noumea.mairie.ws.ISirhWSConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractCounterService implements ICounterService {

	protected Logger logger = LoggerFactory.getLogger(AbstractCounterService.class);

	@Autowired
	protected ISirhWSConsumer sirhWSConsumer;
	
	@Autowired
	protected ICounterRepository counterRepository;

	@Autowired
	protected ISirhRepository sirhRepository;

	@Autowired
	protected HelperService helperService;

	@Autowired
	protected IAccessRightsRepository accessRightsRepository;

	protected static final String MOTIF_COMPTEUR_INEXISTANT = "Le motif n'existe pas.";
	protected static final String SOLDE_COMPTEUR_NEGATIF = "Le solde du compteur de l'agent ne peut pas être négatif.";
	protected static final String OPERATEUR_INEXISTANT = "Vous n'êtes pas habilité à mettre à jour le compteur de cet agent.";
	protected static final String DUREE_A_SAISIR = "La durée à ajouter ou retrancher n'est pas saisie.";
	protected static final String ERREUR_DUREE_SAISIE = "Un seul des champs Durée à ajouter ou Durée à retrancher doit être saisi.";
	protected static final String COMPTEUR_INEXISTANT = "Le compteur n'existe pas.";
	
	protected static final String RESET_COMPTEUR_ANNEE_PRECEDENTE = "Remise à 0 du compteur Année précédente";
	protected static final String RESET_COMPTEUR_ANNEE_EN_COURS = "Remise à 0 du compteur Année en cours";
	
	protected static final String ERROR_TECHNIQUE = "Erreur technique : ICounterService défaut d'implémentation";
	
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
	public abstract int addToAgentForPTG(Integer idAgent, Date dateMonday, Integer minutes);

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
	public abstract ReturnMessageDto majCompteurToAgent(ReturnMessageDto srm, Demande demande, Integer minutes);
	
	/**
	 * appeler depuis Kiosque ou SIRH
	 * l historique ABS_AGENT_WEEK_ALIM_MANUELLE mise a jour
	 */
	@Override
	public ReturnMessageDto majManuelleCompteurToAgent(Integer idAgent, CompteurDto compteurDto) {
		
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
		
		return result;
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
	
	public void controlSaisieAlimManuelleCompteur(CompteurDto compteurDto, ReturnMessageDto result) {
		
		if(null == compteurDto.getDureeAAjouter() && null == compteurDto.getDureeARetrancher()) {
			logger.debug(DUREE_A_SAISIR);
			result.getErrors().add(String.format(DUREE_A_SAISIR));
		}
		
		if(null != compteurDto.getDureeAAjouter() && null != compteurDto.getDureeARetrancher()) {
			logger.debug(ERREUR_DUREE_SAISIE);
			result.getErrors().add(String.format(ERREUR_DUREE_SAISIE));
		}
	}
	
	protected void controlCompteurPositif(Integer minutes, Integer totalMinutes, ReturnMessageDto srm) {
		if(null != minutes
				&& 0 > totalMinutes + minutes) {
			logger.warn(SOLDE_COMPTEUR_NEGATIF);
			srm.getErrors().add(String.format(SOLDE_COMPTEUR_NEGATIF));
		}
	}
	
	@Override
	public ReturnMessageDto resetCompteurRCAnneePrecedente(Integer idAgentReposCompCount) {
		
		ReturnMessageDto srm = new ReturnMessageDto();
			srm.getErrors().add(String.format(ERROR_TECHNIQUE));
		
		return srm;
	}
	
	@Override
	public ReturnMessageDto resetCompteurRCAnneenCours(Integer idAgentReposCompCount) {
		
		ReturnMessageDto srm = new ReturnMessageDto();
			srm.getErrors().add(String.format(ERROR_TECHNIQUE));
		
		return srm;
	}
	
	@Override
	public List<Integer> getListAgentReposCompCountForResetAnneePrcd() {
		return new  ArrayList<Integer>();
	}
	
	@Override
	public List<Integer> getListAgentReposCompCountForResetAnneeEnCours() {
		return new  ArrayList<Integer>();
	}
	
	@Override
	public int calculMinutesCompteur(DemandeEtatChangeDto demandeEtatChangeDto, Demande demande) {
		int minutes = 0;
		// si on approuve, le compteur decremente
		if (demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.APPROUVEE.getCodeEtat())) {
			minutes = 0 - ((DemandeRecup) demande).getDuree();
		}
		// si on passe de Approuve a Refuse, le compteur incremente
		if ((demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.REFUSEE.getCodeEtat()) || demandeEtatChangeDto
				.getIdRefEtat().equals(RefEtatEnum.ANNULEE.getCodeEtat()))
				&& demande.getLatestEtatDemande().getEtat().equals(RefEtatEnum.APPROUVEE)) {
			minutes = ((DemandeRecup) demande).getDuree();
		}

		return minutes;
	}
}
