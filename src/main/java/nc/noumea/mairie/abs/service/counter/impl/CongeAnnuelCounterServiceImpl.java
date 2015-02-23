package nc.noumea.mairie.abs.service.counter.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentCongeAnnuelCount;
import nc.noumea.mairie.abs.domain.AgentHistoAlimManuelle;
import nc.noumea.mairie.abs.domain.AgentJoursFeriesRepos;
import nc.noumea.mairie.abs.domain.AgentWeekCongeAnnuel;
import nc.noumea.mairie.abs.domain.CongeAnnuelRestitutionMassive;
import nc.noumea.mairie.abs.domain.CongeAnnuelRestitutionMassiveHisto;
import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeCongesAnnuels;
import nc.noumea.mairie.abs.domain.EtatDemandeCongesAnnuels;
import nc.noumea.mairie.abs.domain.MotifCompteur;
import nc.noumea.mairie.abs.domain.RefAlimCongeAnnuel;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.domain.RefTypeSaisiCongeAnnuel;
import nc.noumea.mairie.abs.dto.CompteurDto;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.abs.dto.InfosAlimAutoCongesAnnuelsDto;
import nc.noumea.mairie.abs.dto.RestitutionMassiveDto;
import nc.noumea.mairie.abs.dto.RestitutionMassiveHistoDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.IAgentJoursFeriesReposRepository;
import nc.noumea.mairie.abs.repository.ICongesAnnuelsRepository;
import nc.noumea.mairie.abs.repository.IDemandeRepository;
import nc.noumea.mairie.abs.repository.ITypeAbsenceRepository;
import nc.noumea.mairie.abs.service.AgentNotFoundException;
import nc.noumea.mairie.abs.service.IAbsenceDataConsistencyRules;
import nc.noumea.mairie.abs.web.AccessForbiddenException;
import nc.noumea.mairie.abs.web.NotFoundException;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("CongeAnnuelCounterServiceImpl")
public class CongeAnnuelCounterServiceImpl extends AbstractCounterService {

	@Autowired
	private ITypeAbsenceRepository typeAbsenceRepository;

	@Autowired
	private ICongesAnnuelsRepository congesAnnuelsRepository;
	
	@Autowired
	private IAgentJoursFeriesReposRepository agentJoursFeriesReposRepository;
	
	@Autowired
	private IDemandeRepository demandeRepository;
	
	@Autowired
	@Qualifier("AbsCongesAnnuelsDataConsistencyRulesImpl")
	private IAbsenceDataConsistencyRules absCongesAnnuelsDataConsistencyRulesImpl;
	
	protected static final String BASE_CONGES_ALIM_AUTO_INEXISTANT = "La base congé [%d] n'existe pas dans ABS_REF_ALIM_CONGE_ANNUEL.";
	protected static final String PA_INEXISTANT = "Pas de PA active pour l'agent : [%d].";
	protected static final String COMPTEUR_DEJA_A_JOUR = "Compteur de congés annuels déjà mis à jour ce mois-ci pour l'agent : [%d].";
	protected static final String AGENT_AUCUN_CA = "L'agent [%d] n'était pas en congé à cette date.";
	protected static final String MOTIF_OBLIGATOIRE = "Le motif est obligatoire.";
	protected static final String TYPE_RESTITUTION_OBLIGATOIRE = "Le type de restitution est obligatoire.";
	protected static final String DATE_JOUR_RESTITUER_KO = "La date du jour à restituer doit être antérieure à aujourd'hui.";
	protected static final String RESTITUTION_EXISTANTE = "Une restitution a déjà eu lieu pour ce même jour et un autre type.";
	protected static final String COMPTEUR_CA_RESTITUTION_INEXISTANT = "Le compteur n'existe pas pour l'agent [%d].";
	protected static final String AGENT_NON_HABILITE = "L'agent n'est pas habilité.";
	
	@Override
	@Transactional(value = "absTransactionManager")
	public ReturnMessageDto initCompteurCongeAnnuel(Integer idAgent, Integer idAgentConcerne) {

		logger.info("Trying to initiate manually counters for Agent {} ...", idAgentConcerne);

		// tester si agent est un utilisateur SIRH
		ReturnMessageDto result = sirhWSConsumer.isUtilisateurSIRH(idAgent);
		if (!result.getErrors().isEmpty()) {
			// seuls les utilisateurs de SIRH ont le droit de faire cette action
			return result;
		}

		// on verifie que l'agent n'a pas deja un compteur
		AgentCongeAnnuelCount arcExistant = (AgentCongeAnnuelCount) counterRepository.getAgentCounter(
				AgentCongeAnnuelCount.class, idAgentConcerne);

		if (arcExistant != null) {
			logger.warn(COMPTEUR_EXISTANT);
			result.getErrors().add(String.format(COMPTEUR_EXISTANT));
			return result;
		} else {
			AgentCongeAnnuelCount arc = new AgentCongeAnnuelCount();
			arc.setIdAgent(idAgentConcerne);

			arc.setTotalJours(0.0);
			arc.setTotalJoursAnneeN1(0.0);
			arc.setLastModification(helperService.getCurrentDate());

			AgentHistoAlimManuelle histo = new AgentHistoAlimManuelle();
			histo.setIdAgent(idAgent);
			histo.setIdAgentConcerne(arc.getIdAgent());
			histo.setDateModification(helperService.getCurrentDate());
			histo.setMotifCompteur(null);
			histo.setMotifTechnique(INITIATE_COMPTEUR);
			histo.setText(INITIATE_COMPTEUR);
			histo.setCompteurAgent(arc);

			RefTypeAbsence rta = new RefTypeAbsence();
			rta.setIdRefTypeAbsence(RefTypeAbsenceEnum.CONGE_ANNUEL.getValue());
			histo.setType(rta);

			counterRepository.persistEntity(arc);
			counterRepository.persistEntity(histo);
		}

		return result;
	}

	@Override
	@Transactional(readOnly = true)
	public List<Integer> getListAgentCongeAnnuelCountForReset() {
		return counterRepository.getListAgentCongeAnnuelCountForReset();
	}

	@Override
	@Transactional(value = "absTransactionManager")
	public ReturnMessageDto resetCompteurCongeAnnuel(Integer idAgentCount) {

		logger.info("reset CompteurCongeAnnuel for idAgentCount {} ...", idAgentCount);

		ReturnMessageDto srm = new ReturnMessageDto();

		AgentCongeAnnuelCount arc = counterRepository.getEntity(AgentCongeAnnuelCount.class, idAgentCount);

		if (arc == null) {
			logger.warn(COMPTEUR_INEXISTANT);
			srm.getErrors().add(String.format(COMPTEUR_INEXISTANT));
			return srm;
		}

		// selon la SFD, compteur annee en cours à ajouter au compteur de
		// l'année precedente
		// et on remet le compteur de l'année à 0

		AgentHistoAlimManuelle histo = new AgentHistoAlimManuelle();
		histo.setIdAgent(arc.getIdAgent());
		histo.setIdAgentConcerne(arc.getIdAgent());
		histo.setDateModification(helperService.getCurrentDate());
		histo.setMotifCompteur(null);
		histo.setMotifTechnique(RESET_COMPTEUR_ANNEE_EN_COURS);		
		String textLog = "Retrait de " + (0 - arc.getTotalJours()) + " jours sur la nouvelle année.";
		histo.setText(textLog);
		histo.setCompteurAgent(arc);

		RefTypeAbsence rta = new RefTypeAbsence();
		rta.setIdRefTypeAbsence(RefTypeAbsenceEnum.CONGE_ANNUEL.getValue());
		histo.setType(rta);

		arc.setTotalJoursAnneeN1(arc.getTotalJoursAnneeN1() + arc.getTotalJours());
		arc.setTotalJours(0.0);

		counterRepository.persistEntity(arc);
		counterRepository.persistEntity(histo);

		return srm;
	}

	@Override
	public ReturnMessageDto majCompteurToAgent(ReturnMessageDto srm, Demande demande,
			DemandeEtatChangeDto demandeEtatChangeDto) {

		logger.info("Trying to update conge annuel counters for Agent [{}] ...", demande.getIdAgent());
		
		Double jours = calculJoursCompteur(demandeEtatChangeDto, demande);
		if (0 != jours) {
			try {
				return majCompteurToAgent((DemandeCongesAnnuels) demande, jours, srm);
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException("An error occured while trying to update conge annuel counters :", e);
			}
		}
		return srm;
	}

	protected Double calculJoursCompteur(DemandeEtatChangeDto demandeEtatChangeDto, Demande demande) {
		Double jours = 0.0;
		
		// si on approuve, et que le compteur est en depassement
		// pas de mise a jour du compteur
		// la DRH doit d abord valider
		ReturnMessageDto srm = new ReturnMessageDto();
		srm = absCongesAnnuelsDataConsistencyRulesImpl.checkDepassementDroitsAcquis(srm, demande);
		if (srm.getInfos().size() > 0 
				&& demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.APPROUVEE.getCodeEtat())) { 
			return jours;
		}
		
		// si on approuve, le compteur decremente
		if (demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.APPROUVEE.getCodeEtat())
				|| demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.VALIDEE.getCodeEtat())) {
			jours = 0 - ((DemandeCongesAnnuels) demande).getDuree()
					- ((DemandeCongesAnnuels) demande).getDureeAnneeN1();
		}
		// si on passe de Approuve a Refuse, le compteur incremente
		if (demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.REFUSEE.getCodeEtat())
				&& demande.getLatestEtatDemande().getEtat().equals(RefEtatEnum.APPROUVEE)) {
			jours = ((DemandeCongesAnnuels) demande).getDuree() + ((DemandeCongesAnnuels) demande).getDureeAnneeN1();
		}
		// si on passe de Approuve a Annulé ou de Validée à annulé, le compteur
		// incremente
		if (demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.ANNULEE.getCodeEtat())
				&& (demande.getLatestEtatDemande().getEtat().equals(RefEtatEnum.APPROUVEE)
						|| demande.getLatestEtatDemande().getEtat().equals(RefEtatEnum.VALIDEE) || demande
						.getLatestEtatDemande().getEtat().equals(RefEtatEnum.PRISE))) {
			jours = ((DemandeCongesAnnuels) demande).getDuree() + ((DemandeCongesAnnuels) demande).getDureeAnneeN1();
		}

		return jours;
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
	protected <T1, T2> ReturnMessageDto majCompteurToAgent(DemandeCongesAnnuels demande, Double jours,
			ReturnMessageDto srm) throws InstantiationException, IllegalAccessException {

		if (sirhWSConsumer.getAgent(demande.getIdAgent()) == null) {
			logger.error("There is no Agent [{}]. Impossible to update its counters.", demande.getIdAgent());
			throw new AgentNotFoundException();
		}

		logger.info("updating counters for Agent [{}] with {} jours...", demande.getIdAgent(), jours);

		AgentCongeAnnuelCount arc = (AgentCongeAnnuelCount) counterRepository.getAgentCounter(
				AgentCongeAnnuelCount.class, demande.getIdAgent());

		if (arc == null) {
			logger.warn(COMPTEUR_INEXISTANT);
			srm.getErrors().add(String.format(COMPTEUR_INEXISTANT));
			return srm;
		}
		
		// on ne bloque pas si le compteur est negatif

		Double joursAnneeN1 = 0.0;
		Double joursAnneeEnCours = 0.0;
		// dans le cas ou on débite
		if (0 > jours) {
			if (0 > arc.getTotalJoursAnneeN1() + jours) {
				joursAnneeN1 = 0 - arc.getTotalJoursAnneeN1();
				joursAnneeEnCours = arc.getTotalJoursAnneeN1() + jours;
				demande.setDuree(0 - joursAnneeEnCours);
				demande.setDureeAnneeN1(arc.getTotalJoursAnneeN1());
			} else {
				joursAnneeN1 = jours;
				demande.setDuree(0.0);
				demande.setDureeAnneeN1(0 - joursAnneeN1);
			}
		}
		// dans le cas ou on recredite
		if (0 < jours) {
			joursAnneeEnCours = null != demande.getDuree() ? demande.getDuree() : 0;
			joursAnneeN1 = null != demande.getDureeAnneeN1() ? demande.getDureeAnneeN1() : 0;
			demande.setDuree(joursAnneeN1 + joursAnneeEnCours);
			demande.setDureeAnneeN1(0.0);
		}

		Double joursOld = arc.getTotalJours();
		Double joursAnneeN1Old = arc.getTotalJoursAnneeN1();
		
		arc.setTotalJours(arc.getTotalJours() + joursAnneeEnCours);
		arc.setTotalJoursAnneeN1(arc.getTotalJoursAnneeN1() + joursAnneeN1);
		arc.setLastModification(helperService.getCurrentDate());

		updateDemandeWithNewSolde(demande, joursOld, arc.getTotalJours(), joursAnneeN1Old, arc.getTotalJoursAnneeN1());
		
		counterRepository.persistEntity(arc);
		counterRepository.persistEntity(demande);

		return srm;
	}

	/**
	 * #13519 maj solde sur la demande
	 * 
	 * @param demande
	 *            demande a mettre a jour
	 * @param joursOld
	 *            ancien solde en jours
	 * @param JoursNew
	 *            nouveau solde en jours
	 * @param minutesOld
	 *            ancien solde en minutes
	 * @param minutesNew
	 *            nouveau solde en minutes
	 */
	protected void updateDemandeWithNewSolde(DemandeCongesAnnuels demande, Double joursOld, Double JoursNew,
			Double joursAnneeN1Old, Double joursAnneeN1New) {
		
		demande.setTotalJoursNew(JoursNew);
		demande.setTotalJoursOld(joursOld);
		demande.setTotalJoursAnneeN1New(joursAnneeN1New);
		demande.setTotalJoursAnneeN1Old(joursAnneeN1Old);
	}
	
	/**
	 * appeler depuis SIRH l historique ABS_AGENT_WEEK_ALIM_MANUELLE mise a jour
	 */
	@Override
	protected ReturnMessageDto majManuelleCompteurToAgent(Integer idAgent, CompteurDto compteurDto,
			ReturnMessageDto srm, MotifCompteur motifCompteur) {

		logger.info("Trying to update manually conge annuel counters for Agent {} ...", compteurDto.getIdAgent());

		Double jours = null;
		Double joursAnneeN1 = null;

		if (compteurDto.isAnneePrecedente()) {
			Double dJoursAnneeN1 = helperService.calculAlimManuelleCompteur(compteurDto);
			joursAnneeN1 = null != dJoursAnneeN1 ? dJoursAnneeN1 : 0;
		} else {
			Double dJours = helperService.calculAlimManuelleCompteur(compteurDto);
			jours = null != dJours ? dJours : 0;
		}

		try {
			return majManuelleCompteurToAgent(idAgent, compteurDto, jours, joursAnneeN1,
					RefTypeAbsenceEnum.CONGE_ANNUEL.getValue(), srm, motifCompteur);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException("An error occured while trying to update conge annuel counters :", e);
		}
	}

	/**
	 * Mise à jour manuelle du compteur de congé annuel
	 * 
	 * @param T1
	 *            inherits BaseAgentCount
	 * @param idAgent
	 * @param jours
	 *            : negatif pour debiter, positif pour crediter
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private ReturnMessageDto majManuelleCompteurToAgent(Integer idAgentOperateur, CompteurDto compteurDto,
			Double jours, Double joursAnneeN1, Integer idRefTypeAbsence, ReturnMessageDto srm,
			MotifCompteur motifCompteur) throws InstantiationException, IllegalAccessException {

		if (sirhWSConsumer.getAgent(compteurDto.getIdAgent()) == null) {
			logger.error("There is no Agent [{}]. Impossible to update its counters.", compteurDto.getIdAgent());
			throw new AgentNotFoundException();
		}

		logger.info("updating counters for Agent [{}] with {} minutes...", compteurDto.getIdAgent(), jours);

		AgentCongeAnnuelCount arc = (AgentCongeAnnuelCount) counterRepository.getAgentCounter(
				AgentCongeAnnuelCount.class, compteurDto.getIdAgent());

		if (arc == null) {
			logger.warn(COMPTEUR_INEXISTANT);
			srm.getErrors().add(String.format(COMPTEUR_INEXISTANT));
			return srm;
		}

		// on verifie que le solde est positif
		controlCompteurPositif(jours, arc.getTotalJours(), srm);
		controlCompteurPositif(joursAnneeN1, arc.getTotalJoursAnneeN1(), srm);
		if (!srm.getErrors().isEmpty()) {
			return srm;
		}

		String textLog = "";
		if (null != compteurDto.getDureeAAjouter()) {
			if (compteurDto.isAnneePrecedente()) {
				textLog = "Ajout de " + joursAnneeN1 + " jours sur le compteur de l'année précédente.";
			} else {
				textLog = "Ajout de " + jours + " jours sur le compteur de l'année.";
			}
		}
		if (null != compteurDto.getDureeARetrancher()) {
			if (compteurDto.isAnneePrecedente()) {
				textLog = "Retrait de " + joursAnneeN1 + " jours sur le compteur de l'année précédente.";
			} else {
				textLog = "Retrait de " + jours + " jours sur le compteur de l'année.";
			}
		}

		if (null != jours) {
			arc.setTotalJours(arc.getTotalJours() + jours);
		}
		if (null != joursAnneeN1) {
			arc.setTotalJoursAnneeN1(arc.getTotalJoursAnneeN1() + joursAnneeN1);
		}
		arc.setLastModification(helperService.getCurrentDate());

		counterRepository.persistEntity(arc);
		majAgentHistoAlimManuelle(idAgentOperateur, compteurDto.getIdAgent(), motifCompteur, textLog, arc,
				idRefTypeAbsence);
		
		return srm;
	}
	
	@Override
	@Transactional(value = "absTransactionManager")
	public ReturnMessageDto alimentationAutoCompteur(Integer idAgent, Date dateDebut, Date dateFin) {
		
		logger.info("Start Alimentation auto CompteurCongeAnnuel for idAgent {} ...", idAgent);

		ReturnMessageDto srm = new ReturnMessageDto();

		// on recherche le compteur de l agent
		AgentCongeAnnuelCount arc = (AgentCongeAnnuelCount) counterRepository.getAgentCounter(
				AgentCongeAnnuelCount.class, idAgent);
		
		if (arc == null) {
			logger.error(COMPTEUR_INEXISTANT);
			srm.getErrors().add(String.format(COMPTEUR_INEXISTANT));
			return srm;
		}
		
		AgentWeekCongeAnnuel awca = congesAnnuelsRepository.getWeekHistoForAgentAndDate(idAgent, dateDebut);
		
		// si compteur deja mis a jour
		if (awca != null) {
			logger.error(COMPTEUR_DEJA_A_JOUR, idAgent);
			srm.getErrors().add(String.format(COMPTEUR_DEJA_A_JOUR, idAgent));
			return srm;
		}
		
		// on recupere la PA de l agent
		List<InfosAlimAutoCongesAnnuelsDto> listPA = sirhWSConsumer.getListPAPourAlimAutoCongesAnnuels(
				arc.getIdAgent(), dateDebut, dateFin);
		
		if (null == listPA || (null != listPA && 0 == listPA.size())) {
			logger.error(PA_INEXISTANT, arc.getIdAgent());
			srm.getErrors().add(String.format(PA_INEXISTANT, idAgent));
			return srm;
		}
		
		Double joursAAjouter = 0.0;
		// on calcule le nombre de jours conges à ajouter sur le mois
		for(InfosAlimAutoCongesAnnuelsDto PA : listPA) {
			if(PA.isDroitConges()) {
				RefTypeSaisiCongeAnnuel typeCongeAnnuel = typeAbsenceRepository.getEntity(
						RefTypeSaisiCongeAnnuel.class, PA.getIdBaseCongeAbsence());
				
				if(null == typeCongeAnnuel) {
					logger.error(BASE_CONGES_ALIM_AUTO_INEXISTANT, PA.getIdBaseCongeAbsence());
					srm.getErrors().add(String.format(BASE_CONGES_ALIM_AUTO_INEXISTANT, PA.getIdBaseCongeAbsence()));
					return srm;
				}
				
				RefAlimCongeAnnuel refAlim = congesAnnuelsRepository.getRefAlimCongeAnnuel(
						typeCongeAnnuel.getIdRefTypeSaisiCongeAnnuel(), new DateTime(dateDebut).getYear());

				Double quotaMois = getQuotaCongesByMois(refAlim, new DateTime(dateDebut).getMonthOfYear());

				Double nombreJoursPA = helperService.calculNombreJours(PA.getDateDebut(), PA.getDateFin());
				
				Calendar calendar = GregorianCalendar.getInstance();
				calendar.setTime(PA.getDateDebut());
				Integer dernierJourMois = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
				
				joursAAjouter += getNombreJoursDonnantDroitsAConges(dernierJourMois, quotaMois, nombreJoursPA);
				
				// cas partiuclier de la base C :
				// pour la base C, on rajoute le quota mensuel – le nombre de
				// jours fériés/chômés cochés (= en repos) sur le mois
				if(null != typeCongeAnnuel.getCodeBaseHoraireAbsence()
						&& "C".equals(typeCongeAnnuel.getCodeBaseHoraireAbsence().trim())) {
					joursAAjouter -= getJoursReposFeriesbyAgent(idAgent, dateDebut, dateFin);
				}
			}
		}
		
		Date dernierModif = new Date();
		// on enregistre
		
		awca = new AgentWeekCongeAnnuel();
		awca.setIdAgent(idAgent);
		awca.setDateMonth(dateDebut);
		
		awca.setLastModification(dernierModif);
		awca.setJours(joursAAjouter);
		
		arc.setTotalJours(arc.getTotalJours() + joursAAjouter);
		arc.setLastModification(dernierModif);
		
		congesAnnuelsRepository.persistEntity(awca);
		
		logger.info("Finally Alimentation auto CompteurCongeAnnuel for idAgent {} ...", idAgent);
		
		return srm;
	}
	
	protected Integer getJoursReposFeriesbyAgent(Integer idAgent, Date dateDebut, Date dateFin) {
		
		List<AgentJoursFeriesRepos> listJoursReposAgent = agentJoursFeriesReposRepository
				.getAgentJoursFeriesReposByIdAgentAndPeriode(idAgent, dateDebut, dateFin);
		
		Integer nombreJoursRepos = 0;
		if (null != listJoursReposAgent && 0 < listJoursReposAgent.size()) {
			nombreJoursRepos = listJoursReposAgent.size();
		}
		return nombreJoursRepos;
	}
	
	protected Double getNombreJoursDonnantDroitsAConges(Integer dernierJourMois, Double quotaMois, Double nombreJoursPA) {
		if(nombreJoursPA >= dernierJourMois) {
			return quotaMois;
		}else{
			return Math.ceil((quotaMois * nombreJoursPA / 30) * 2) /2;
		}
	}
	
	/**
	 * #12474 et #12496 : voir ces redmines pour les règles mises en place
	 * concernant la restitution du samedi offert ou decompte
	 */
	@Override
	@Transactional(value = "absTransactionManager")
	public ReturnMessageDto restitutionMassiveCA(Integer idAgentConnecte, RestitutionMassiveDto dto,
			List<Integer> listIdAgent) {
		
		ReturnMessageDto srm = new ReturnMessageDto();
		
		// verification des droits SIRH
		ReturnMessageDto isUtilisateurSIRH = sirhWSConsumer.isUtilisateurSIRH(idAgentConnecte);
		if (!isUtilisateurSIRH.getErrors().isEmpty()) {
			logger.warn(AGENT_NON_HABILITE);
			srm.getErrors().add(String.format(AGENT_NON_HABILITE));
			return srm;
		}

		//////////////////////////////////////
		Date dernierModif = new Date();
		
		// on verifie si une restitution n a pas deja eu lieu pour le meme jour
		srm = checkAutreRestitutionMemeJour(dto, srm);
		if(srm.getErrors().size() > 0) 
			return srm;
		
		CongeAnnuelRestitutionMassive restitutionMassive = congesAnnuelsRepository.getCongeAnnuelRestitutionMassiveByDate(dto);
		
		if(null == restitutionMassive) {
			restitutionMassive = new CongeAnnuelRestitutionMassive();
			restitutionMassive.setDateRestitution(getDateResitution(dto));
			restitutionMassive.setMotif(dto.getMotif());
			restitutionMassive.setJournee(dto.isJournee());
			restitutionMassive.setMatin(dto.isMatin());
			restitutionMassive.setApresMidi(dto.isApresMidi());
		}
		
		restitutionMassive.setDateModification(dernierModif);
		
		boolean isError = false;
		
		for (Integer idAgentList : listIdAgent) {
		
			logger.info("Start restitutionMassiveCA for idAgent {} ...", idAgentList);

			/////////////////////////////////////
			// on teste s il n y a pas deja eu une restitution massive
			// pour l agent sur le meme jour
			if(checkCADejaRestitue(srm, restitutionMassive, idAgentList))
				continue;

			// ////////////////////////////////////
			// on recherche le compteur de l agent
			AgentCongeAnnuelCount arc = (AgentCongeAnnuelCount) counterRepository.getAgentCounter(
						AgentCongeAnnuelCount.class, idAgentList);
			
			if (arc == null) {
				logger.error(String.format(COMPTEUR_CA_RESTITUTION_INEXISTANT, idAgentList));
				srm.getErrors().add(String.format(COMPTEUR_CA_RESTITUTION_INEXISTANT, idAgentList));
				persistCongeAnnuelRestitutionMassiveHisto(
						restitutionMassive, idAgentList, dto, COMPTEUR_CA_RESTITUTION_INEXISTANT,
						new Date(), null);
				isError = true;
				continue;
			}
		
			/////////////////////////////////////
			// la personne a pose un conge? 
			RefTypeSaisiCongeAnnuel refTypeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
				refTypeSaisiCongeAnnuel.setCalendarDateDebut(true);
				refTypeSaisiCongeAnnuel.setCalendarDateFin(true);
				refTypeSaisiCongeAnnuel.setChkDateDebut(true);
				refTypeSaisiCongeAnnuel.setChkDateFin(true);
			
			List<DemandeCongesAnnuels> listCongesAgentpris = congesAnnuelsRepository
					.getListeDemandesCongesAnnuelsPrisesByAgent(idAgentList, helperService.getDateDebutCongeAnnuel(
							refTypeSaisiCongeAnnuel, dto.getDateRestitution(), dto.isMatin(), dto.isApresMidi()),
							helperService.getDateFinCongeAnnuel(refTypeSaisiCongeAnnuel, dto.getDateRestitution(),
									null, dto.isMatin(), dto.isApresMidi(), null));

			if (null == listCongesAgentpris || listCongesAgentpris.isEmpty()) {
				logger.error(String.format(AGENT_AUCUN_CA, idAgentList));
				srm.getErrors().add(String.format(AGENT_AUCUN_CA, idAgentList));
				persistCongeAnnuelRestitutionMassiveHisto(
						restitutionMassive, idAgentList, dto, AGENT_AUCUN_CA, new Date(), null);
				isError = true;
				continue;
			}
		
			///////////////////////////////
			// compte les samedis decomptes a rendre
			DemandeCongesAnnuels demandeCA = listCongesAgentpris.get(0);
			
			Double samediAAjouter = getSamediDecompteARendre(demandeCA, dto);

			/////////////////////////////////////
			// nombre de jour a redonner au total 
			Double jourTmp = 0.0;
			if(dto.isJournee()) {
				jourTmp = 1.0;
			}else{
				jourTmp = 0.5;
			}
			jourTmp += samediAAjouter;

			/////////////////////////////////////////////
			// quels compteurs réalimenter? N-1 et/ou N?
			Double joursAAjouter = 0.0;
			Double joursAAjouterN1 = 0.0;
				if (null != demandeCA.getDureeAnneeN1() && demandeCA.getDureeAnneeN1() > 0) {
				if(demandeCA.getDureeAnneeN1() < jourTmp) {
					joursAAjouterN1 = demandeCA.getDureeAnneeN1();
					jourTmp -= demandeCA.getDureeAnneeN1();
				}else{
					joursAAjouterN1 = jourTmp;
					jourTmp = 0.0;
				}
			}
				if (null != demandeCA.getDuree() && demandeCA.getDuree() > 0) {
				joursAAjouter = jourTmp;
			}
		
			//////////////////////////////////////
			// redonner le samedi offert si besoin
			// on enregistre la demande de Conge Annuel si samedi offert modifie
			Double samediOffert = getSamediOffertARendre(demandeCA, dto);
			demandeCA.setNbSamediOffert(demandeCA.getNbSamediOffert() - samediOffert);

			//////////////////////////////////////
			// et on ajoute une ligne d'historique a la demande 
			// si le samedi offert est modifie
			if(0.0 != samediOffert) {
				EtatDemandeCongesAnnuels etatDemande = new EtatDemandeCongesAnnuels();
					etatDemande.setMotif(dto.getMotif());
					etatDemande.setNbSamediOffert(demandeCA.getNbSamediOffert());
					etatDemande.setCommentaire(demandeCA.getCommentaire());
					etatDemande.setDate(dernierModif);
					etatDemande.setDateDebut(demandeCA.getDateDebut());
					etatDemande.setDateDebutAM(demandeCA.isDateDebutAM());
					etatDemande.setDateDebutPM(demandeCA.isDateDebutPM());
					etatDemande.setDateFin(demandeCA.getDateFin());
					etatDemande.setDateFinAM(demandeCA.isDateFinAM());
					etatDemande.setDateFinPM(demandeCA.isDateFinPM());
					etatDemande.setDemande(demandeCA);
					etatDemande.setDuree(demandeCA.getDuree());
					etatDemande.setDureeAnneeN1(demandeCA.getDureeAnneeN1());
					etatDemande.setEtat(RefEtatEnum.PRISE);
					etatDemande.setIdAgent(idAgentConnecte);
					etatDemande.setNbSamediDecompte(demandeCA.getNbSamediDecompte());
					etatDemande.setTypeSaisiCongeAnnuel(demandeCA.getTypeSaisiCongeAnnuel());
	
				counterRepository.persistEntity(etatDemande);
			}
		
			// on enregistre une ligne historique du compteur
			AgentWeekCongeAnnuel weekCA = new AgentWeekCongeAnnuel();
				weekCA.setIdAgent(idAgentList);
			weekCA.setDateMonth(getDateResitution(dto));
			weekCA.setLastModification(dernierModif);
			weekCA.setJours(joursAAjouter + joursAAjouterN1);
			
			// on enregistre le compteur
			arc.setLastModification(dernierModif);
			arc.setTotalJours(arc.getTotalJours() + joursAAjouter);
			arc.setTotalJoursAnneeN1(arc.getTotalJoursAnneeN1() + joursAAjouterN1);
		
			// on enregistre l'historique de la restitution massive
			persistCongeAnnuelRestitutionMassiveHisto(restitutionMassive, idAgentList, dto, "OK", dernierModif, joursAAjouter
					+ joursAAjouterN1);
		
			counterRepository.persistEntity(weekCA);
		
			srm.getInfos().add("Restitution massive enregistrée pour l'agent " + idAgentList);
		
			logger.info("Finally restitutionMassiveCA for idAgent {} ...", idAgentList);
		}
		
		if(isError) {
			restitutionMassive.setStatus("Erreur");
		}else{
			restitutionMassive.setStatus("OK");
		}
		counterRepository.persistEntity(restitutionMassive);
		
		return srm;
	}
	
	protected ReturnMessageDto checkAutreRestitutionMemeJour(RestitutionMassiveDto dto, ReturnMessageDto srm) {
		
		List<CongeAnnuelRestitutionMassive> listRestitutionsMemeJours = congesAnnuelsRepository.getListCongeAnnuelRestitutionMassiveByDate(dto);
		if(null != listRestitutionsMemeJours) {
			for(CongeAnnuelRestitutionMassive restitutionExistante : listRestitutionsMemeJours) {
				if(dto.isJournee() && (restitutionExistante.isApresMidi() || restitutionExistante.isMatin())) {
					logger.warn(RESTITUTION_EXISTANTE);
					srm.getErrors().add(String.format(RESTITUTION_EXISTANTE));
					return srm;
				}
				if(dto.isMatin() && restitutionExistante.isJournee()) {
					logger.warn(RESTITUTION_EXISTANTE);
					srm.getErrors().add(String.format(RESTITUTION_EXISTANTE));
					return srm;
				}
				if(dto.isApresMidi() && restitutionExistante.isJournee()) {
					logger.warn(RESTITUTION_EXISTANTE);
					srm.getErrors().add(String.format(RESTITUTION_EXISTANTE));
					return srm;
				}
			}
		}
		
		return srm;
	}
	
	private Date getDateResitution(RestitutionMassiveDto dto){
		if(dto.isApresMidi()){
			return new DateTime(dto.getDateRestitution()).withHourOfDay(12).withMinuteOfHour(0).withSecondOfMinute(0)
					.toDate();
		}
		return dto.getDateRestitution();
	}
	
	@Override
	public ReturnMessageDto checkRestitutionMassiveDto(RestitutionMassiveDto dto, ReturnMessageDto srm) {
		
		if(null == dto.getDateRestitution() || !dto.getDateRestitution().before(new Date())) {
			srm.getErrors().add(DATE_JOUR_RESTITUER_KO);
		}
		if(!dto.isApresMidi() && !dto.isMatin() && !dto.isJournee()) {
			srm.getErrors().add(TYPE_RESTITUTION_OBLIGATOIRE);
		}
		if(null == dto.getMotif() || "".equals(dto.getMotif().trim())) {
			srm.getErrors().add(MOTIF_OBLIGATOIRE);
		}
		return srm;
	}
	
	private void persistCongeAnnuelRestitutionMassiveHisto(
			CongeAnnuelRestitutionMassive restitutionMassive, 
			Integer idAgentList, RestitutionMassiveDto dto,
			String status, Date dernierModif, Double jours) {
		CongeAnnuelRestitutionMassiveHisto histo = new CongeAnnuelRestitutionMassiveHisto();
			histo.setIdAgent(idAgentList);
			histo.setStatus(status);
			histo.setJours(jours);
			histo.setRestitutionMassive(restitutionMassive);
		restitutionMassive.getRestitutionMassiveHisto().add(histo);
	}
	
	protected Double getSamediDecompteARendre(DemandeCongesAnnuels demandeCA, RestitutionMassiveDto dto) {
		
		DateTime dateARestituer = new DateTime(dto.getDateRestitution());
		if (dateARestituer.getDayOfWeek() == DateTimeConstants.FRIDAY && 0 < demandeCA.getNbSamediDecompte()
				&& !sirhWSConsumer.isJourHoliday(dateARestituer.plusDays(1).toDate())){
			if(dto.isJournee() || dto.isApresMidi()) {
				return 1.0;
			}
			if(dto.isMatin()) {
				return 0.5;
			}
		}
		// si le jour a restitue est un jeudi et que le vendredi est ferie
		if (dateARestituer.getDayOfWeek() == DateTimeConstants.THURSDAY && 0 < demandeCA.getNbSamediDecompte()
				&& sirhWSConsumer.isJourHoliday(dateARestituer.plusDays(1).toDate())
				&& !sirhWSConsumer.isJourHoliday(dateARestituer.plusDays(2).toDate())){
			if(dto.isJournee() || dto.isApresMidi()) {
				return 1.0;
			}
			if(dto.isMatin()) {
				return 0.5;
			}
		}
		return 0.0;
	}
	
	/**
	 * on teste si le jour a rendre est un vendredi ET qu au moins un samedi est
	 * offert dans la demande de CA ET qu auncun samedi n est decomtpe de la
	 * demande CA ET que le samedi n est pas un jour ferie ou chome
	 */
	protected Double getSamediOffertARendre(DemandeCongesAnnuels demandeCA, RestitutionMassiveDto dto) {
		
		DateTime dateARestituer = new DateTime(dto.getDateRestitution());
		if (dateARestituer.getDayOfWeek() == DateTimeConstants.FRIDAY && 0 < demandeCA.getNbSamediOffert()
				&& 0 == demandeCA.getNbSamediDecompte()
				&& !sirhWSConsumer.isJourHoliday(dateARestituer.plusDays(1).toDate())){
			if(dto.isJournee()) {
				return 1.0;
			}
		}
		return 0.0;
	}
	
	protected boolean checkCADejaRestitue(ReturnMessageDto srm, CongeAnnuelRestitutionMassive restitutionMassive, Integer idAgentList) {
		
		if(null != restitutionMassive.getRestitutionMassiveHisto()) {
			for(CongeAnnuelRestitutionMassiveHisto detailsHisto : restitutionMassive.getRestitutionMassiveHisto()) {
				if(detailsHisto.getIdAgent().equals(idAgentList)) {
					logger.error(String.format(RESTITUTION_EXISTANTE, idAgentList));
					return true;
				}
			}
		}

		return false;
	}
	
	/**
	 * #13580 retroune l'historique des restitutions massives 
	 */
	@Override
	@Transactional(readOnly = true)
	public List<RestitutionMassiveDto> getHistoRestitutionMassiveCA(Integer idAgentConnecte) {
		
		// verification des droits SIRH
		ReturnMessageDto isUtilisateurSIRH = sirhWSConsumer.isUtilisateurSIRH(idAgentConnecte);
		if (!isUtilisateurSIRH.getErrors().isEmpty()) {
			logger.warn(AGENT_NON_HABILITE);
			throw new AccessForbiddenException();
		}
		
		List<RestitutionMassiveDto> result = new ArrayList<RestitutionMassiveDto>();
		
		List<CongeAnnuelRestitutionMassive> listRestitutionCA = congesAnnuelsRepository
				.getHistoRestitutionMassiveOrderByDate();
		
		if(null != listRestitutionCA
				&& !listRestitutionCA.isEmpty()) {
			for(CongeAnnuelRestitutionMassive histo : listRestitutionCA) {
				RestitutionMassiveDto dto = new RestitutionMassiveDto(histo);
				result.add(dto);
			}
		}
		
		return result;
	}
	
	/**
	 * #13580 retroune l'historique des restitutions massives 
	 */
	@Override
	@Transactional(readOnly = true)
	public RestitutionMassiveDto getDetailsHistoRestitutionMassive(Integer idAgentConnecte,
			RestitutionMassiveDto dto) {
		
		// verification des droits SIRH
		ReturnMessageDto isUtilisateurSIRH = sirhWSConsumer.isUtilisateurSIRH(idAgentConnecte);
		if (!isUtilisateurSIRH.getErrors().isEmpty()) {
			logger.warn(AGENT_NON_HABILITE);
			throw new AccessForbiddenException();
		}
		
		CongeAnnuelRestitutionMassive restitutionMassive = counterRepository.getEntity(CongeAnnuelRestitutionMassive.class, dto.getIdRestitutionMassive());
		
		if(null == restitutionMassive)
			throw new NotFoundException();
			
		List<RestitutionMassiveHistoDto> listHistoAgents = new ArrayList<RestitutionMassiveHistoDto>();
		for(CongeAnnuelRestitutionMassiveHisto histo : restitutionMassive.getRestitutionMassiveHisto()) {
			RestitutionMassiveHistoDto dtoHisto = new RestitutionMassiveHistoDto(histo);
			listHistoAgents.add(dtoHisto);
		}
		dto.setListHistoAgents(listHistoAgents);
		
		return dto;
	}

	public Double getQuotaCongesByMois(RefAlimCongeAnnuel ref, Integer mois) {

		if (mois == null || ref == null)
			return null;

		switch (mois) {
			case 1:
				return ref.getJanvier();
			case 2:
				return ref.getFevrier();
			case 3:
				return ref.getMars();
			case 4:
				return ref.getAvril();
			case 5:
				return ref.getMai();
			case 6:
				return ref.getJuin();
			case 7:
				return ref.getJuillet();
			case 8:
				return ref.getAout();
			case 9:
				return ref.getSeptembre();
			case 10:
				return ref.getOctobre();
			case 11:
				return ref.getNovembre();
			case 12:
				return ref.getDecembre();
			default:
				return null;
		}
	}
}
