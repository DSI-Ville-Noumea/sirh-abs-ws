package nc.noumea.mairie.abs.service.counter.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentCongeAnnuelCount;
import nc.noumea.mairie.abs.domain.AgentHistoAlimManuelle;
import nc.noumea.mairie.abs.domain.AgentJoursFeriesRepos;
import nc.noumea.mairie.abs.domain.AgentWeekCongeAnnuel;
import nc.noumea.mairie.abs.domain.CongeAnnuelRestitutionMassiveHisto;
import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeCongesAnnuels;
import nc.noumea.mairie.abs.domain.MotifCompteur;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.domain.RefTypeGroupeAbsenceEnum;
import nc.noumea.mairie.abs.domain.RefTypeSaisiCongeAnnuel;
import nc.noumea.mairie.abs.dto.CompteurDto;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.abs.dto.InfosAlimAutoCongesAnnuelsDto;
import nc.noumea.mairie.abs.dto.RefTypeSaisiCongeAnnuelDto;
import nc.noumea.mairie.abs.dto.RestitutionMassiveDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.IAgentJoursFeriesReposRepository;
import nc.noumea.mairie.abs.repository.ICongesAnnuelsRepository;
import nc.noumea.mairie.abs.repository.IDemandeRepository;
import nc.noumea.mairie.abs.repository.ITypeAbsenceRepository;
import nc.noumea.mairie.abs.service.AgentNotFoundException;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.springframework.beans.factory.annotation.Autowired;
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
	
	protected static final String BASE_CONGES_ALIM_AUTO_INEXISTANT = "La base congés [%d] n'existe pas dans ABS_REF_ALIM_CONGE_ANNUEL.";
	protected static final String PA_INEXISTANT = "Pas de PA active pour l'agent : [%d].";
	protected static final String COMPTEUR_DEJA_A_JOUR = "Compteur de congés annuels déjà mis à jour ce mois-ci pour l'agent : [%d].";
	protected static final String AGENT_AUCUN_CA = "L'agent [%d] n'était pas en congé à cette date.";
	protected static final String BASE_CA_NON_TROUVEE = "Base congé non trouvée pour l'agent [%d].";
	protected static final String MAUVAIS_BASE_CA = "Mauvaise base congé pour l'agent [%d].";
	protected static final String MOTIF_OBLIGATOIRE = "Le motif est obligatoire.";
	protected static final String AUCUN_AGENT = "Pas d'agent sélectionné.";
	protected static final String TYPE_RESTITUTION_OBLIGATOIRE = "Le type de restitution est obligatoire.";
	protected static final String DATE_JOUR_RESTITUER_KO = "La date du jour à restituer doit être antérieure à aujourd'hui.";
	
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
				&& (demande.getLatestEtatDemande().getEtat().equals(RefEtatEnum.APPROUVEE) || demande
						.getLatestEtatDemande().getEtat().equals(RefEtatEnum.VALIDEE))) {
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

		// on verifie que le solde est positif seulement si on debite le
		// compteur
		if (0 > jours && 0 > arc.getTotalJours() + arc.getTotalJoursAnneeN1() + jours) {
			logger.warn(SOLDE_COMPTEUR_NEGATIF);
			srm.getErrors().add(String.format(SOLDE_COMPTEUR_NEGATIF));
			return srm;
		}

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

		arc.setTotalJours(arc.getTotalJours() + joursAnneeEnCours);
		arc.setTotalJoursAnneeN1(arc.getTotalJoursAnneeN1() + joursAnneeN1);
		arc.setLastModification(helperService.getCurrentDate());

		counterRepository.persistEntity(arc);
		counterRepository.persistEntity(demande);

		return srm;
	}
	


	/**
	 * appeler depuis SIRH l historique ABS_AGENT_WEEK_ALIM_MANUELLE
	 * mise a jour
	 */
	@Override
	protected ReturnMessageDto majManuelleCompteurToAgent(Integer idAgent, CompteurDto compteurDto, ReturnMessageDto srm,
			MotifCompteur motifCompteur) {

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
			return majManuelleCompteurToAgent(idAgent, compteurDto, jours, joursAnneeN1, RefTypeAbsenceEnum.CONGE_ANNUEL.getValue(), srm, motifCompteur);
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
			Double jours, Double joursAnneeN1, Integer idRefTypeAbsence, ReturnMessageDto srm, MotifCompteur motifCompteur)
			throws InstantiationException, IllegalAccessException {

		if (sirhWSConsumer.getAgent(compteurDto.getIdAgent()) == null) {
			logger.error("There is no Agent [{}]. Impossible to update its counters.", compteurDto.getIdAgent());
			throw new AgentNotFoundException();
		}

		logger.info("updating counters for Agent [{}] with {} minutes...", compteurDto.getIdAgent(), jours);

		AgentCongeAnnuelCount arc = (AgentCongeAnnuelCount) counterRepository.getAgentCounter(AgentCongeAnnuelCount.class,
				compteurDto.getIdAgent());

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
		majAgentHistoAlimManuelle(idAgentOperateur, compteurDto.getIdAgent(), motifCompteur, textLog, arc, idRefTypeAbsence);
		
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
		List<InfosAlimAutoCongesAnnuelsDto> listPA = sirhWSConsumer.getListPAPourAlimAutoCongesAnnuels(arc.getIdAgent(), dateDebut, dateFin);
		
		if(null == listPA
				|| (null != listPA && 0 == listPA.size())) {
			logger.error(PA_INEXISTANT, arc.getIdAgent());
			srm.getErrors().add(String.format(PA_INEXISTANT, idAgent));
			return srm;
		}
		
		Double joursAAjouter = 0.0;
		// on calcule le nombre de jours conges à ajouter sur le mois
		for(InfosAlimAutoCongesAnnuelsDto PA : listPA) {
			if(PA.isDroitConges()) {
				RefTypeSaisiCongeAnnuel typeCongeAnnuel = typeAbsenceRepository.getEntity(RefTypeSaisiCongeAnnuel.class, PA.getIdBaseCongeAbsence());
				
				if(null == typeCongeAnnuel) {
					logger.error(BASE_CONGES_ALIM_AUTO_INEXISTANT, PA.getIdBaseCongeAbsence());
					srm.getErrors().add(String.format(BASE_CONGES_ALIM_AUTO_INEXISTANT, PA.getIdBaseCongeAbsence()));
					return srm;
				}
				
				Double quotaMois = typeCongeAnnuel.getRefAlimCongeAnnuel().getQuotaCongesByMois(new DateTime(dateDebut).getMonthOfYear());
				Double nombreJoursPA = helperService.calculNombreJours(PA.getDateDebut(), PA.getDateFin());
				
				Calendar calendar = GregorianCalendar.getInstance();
				calendar.setTime(PA.getDateDebut());
				Integer dernierJourMois = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
				
				joursAAjouter += getNombreJoursDonnantDroitsAConges(dernierJourMois, quotaMois, nombreJoursPA);
				
				// cas partiuclier de la base C :
				// pour la base C, on rajoute le quota mensuel – le nombre de jours fériés/chômés cochés (= en repos) sur le mois
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
		
		List<AgentJoursFeriesRepos> listJoursReposAgent = agentJoursFeriesReposRepository.getAgentJoursFeriesReposByIdAgentAndPeriode(idAgent, dateDebut, dateFin);
		
		Integer nombreJoursRepos = 0;
		if(null != listJoursReposAgent
				&& 0 < listJoursReposAgent.size()) {
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
	
	public ReturnMessageDto restitutionMassiveCA(RestitutionMassiveDto dto) {
		
		logger.info("Start restitutionMassiveCA for idAgent {} ...", dto.getIdAgent());
		
		ReturnMessageDto srm = new ReturnMessageDto();
		
		/////////////////////////////////////
		// on check le DTO
		checkRestitutionMassiveDto(dto, srm);
		if(0 < srm.getErrors().size()) {
			return srm;
		}
		
		/////////////////////////////////////
		// Ne concerne QUE les agents en base congé A et D
		checkAgentIsBaseCongeAOrC(dto.getIdAgent(), dto.getDateRestitution(), srm);
		if(0 < srm.getErrors().size())
			return srm;
		
		//////////////////////////////////////
		// on recherche le compteur de l agent
		AgentCongeAnnuelCount arc = (AgentCongeAnnuelCount) counterRepository.getAgentCounter(
				AgentCongeAnnuelCount.class, dto.getIdAgent());
		
		if (arc == null) {
			logger.error(COMPTEUR_INEXISTANT);
			srm.getErrors().add(String.format(COMPTEUR_INEXISTANT));
			persistCongeAnnuelRestitutionMassiveHisto(dto, COMPTEUR_INEXISTANT, new Date());
			return srm;
		}
		
		/////////////////////////////////////
		// la personne a pose un conge? 
		RefTypeSaisiCongeAnnuel refTypeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
			refTypeSaisiCongeAnnuel.setCalendarDateDebut(true);
			refTypeSaisiCongeAnnuel.setCalendarDateFin(true);
			refTypeSaisiCongeAnnuel.setChkDateDebut(true);
			refTypeSaisiCongeAnnuel.setChkDateFin(true);
		
		List<Demande> listCongesAgentpris = demandeRepository.listeDemandesAgent(
				null, dto.getIdAgent(), 
				helperService.getDateDebutCongeAnnuel(refTypeSaisiCongeAnnuel, dto.getDateRestitution(), dto.isMatin(), dto.isApresMidi()), 
				helperService.getDateFinCongeAnnuel(refTypeSaisiCongeAnnuel, dto.getDateRestitution(), null, dto.isMatin(), dto.isApresMidi(), null), 
				null, 
				RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());
		
		if(null == listCongesAgentpris
				|| listCongesAgentpris.isEmpty()) {
			logger.error(String.format(AGENT_AUCUN_CA, dto.getIdAgent()));
			srm.getErrors().add(String.format(AGENT_AUCUN_CA, dto.getIdAgent()));
			persistCongeAnnuelRestitutionMassiveHisto(dto, AGENT_AUCUN_CA, new Date());
			return srm;
		}
		
		///////////////////////////////
		// compte les samedis decomptes a rendre
		DemandeCongesAnnuels demandeCA = (DemandeCongesAnnuels) listCongesAgentpris.get(0);
		
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
		if(demandeCA.getDureeAnneeN1() > 0) {
			if(demandeCA.getDureeAnneeN1() < jourTmp) {
				joursAAjouterN1 = demandeCA.getDureeAnneeN1();
				jourTmp -= demandeCA.getDureeAnneeN1();
			}else{
				joursAAjouterN1 = jourTmp;
				jourTmp = 0.0;
			}
		}
		if(demandeCA.getDuree() > 0) {
			joursAAjouter = jourTmp;
		}
		
		//////////////////////////////////////
		// redonner le samedi offert si besoin
		Double samediOffert = getSamediOffertARendre(demandeCA, dto);
		demandeCA.setNbSamediOffert(demandeCA.getNbSamediOffert() - samediOffert);
		
		/////////////////////
		// on enregistre
		Date dernierModif = new Date();
		
		AgentWeekCongeAnnuel weekCA = new AgentWeekCongeAnnuel();
		weekCA.setIdAgent(dto.getIdAgent());
		weekCA.setDateMonth(dto.getDateRestitution());
		weekCA.setLastModification(dernierModif);
		weekCA.setJours(joursAAjouter + joursAAjouterN1);
		
		arc.setLastModification(dernierModif);
		arc.setTotalJours(arc.getTotalJours() + joursAAjouter);
		arc.setTotalJoursAnneeN1(arc.getTotalJoursAnneeN1() + joursAAjouterN1);
		
		persistCongeAnnuelRestitutionMassiveHisto(dto, "OK", dernierModif);
		
		counterRepository.persistEntity(weekCA);
		
		logger.info("Finally restitutionMassiveCA for idAgent {} ...", dto.getIdAgent());
		
		return srm;
	}
	
	protected void checkRestitutionMassiveDto(RestitutionMassiveDto dto, ReturnMessageDto srm) {
		
		if(null == dto.getDateRestitution() || !dto.getDateRestitution().before(new Date())) {
			srm.getErrors().add(DATE_JOUR_RESTITUER_KO);
		}
		if(!dto.isApresMidi() && !dto.isMatin() && !dto.isJournee()) {
			srm.getErrors().add(TYPE_RESTITUTION_OBLIGATOIRE);
		}
		if(null == dto.getIdAgent()) {
			srm.getErrors().add(AUCUN_AGENT);
		}
		if(null == dto.getMotif() || "".equals(dto.getMotif().trim())) {
			srm.getErrors().add(MOTIF_OBLIGATOIRE);
		}
	}
	
	private void persistCongeAnnuelRestitutionMassiveHisto(RestitutionMassiveDto dto, String status, Date dernierModif){
		CongeAnnuelRestitutionMassiveHisto histo = new CongeAnnuelRestitutionMassiveHisto();
			histo.setIdAgent(dto.getIdAgent());
			histo.setDateModification(dernierModif);
			histo.setDateRestitution(dto.getDateRestitution());
			histo.setStatus(status);
		counterRepository.persistEntity(histo);
	}
	
	protected void checkAgentIsBaseCongeAOrC(Integer idAgent, Date dateRestitution, ReturnMessageDto srm) {
		
		RefTypeSaisiCongeAnnuelDto dtoBase = sirhWSConsumer.getBaseHoraireAbsence(idAgent, dateRestitution);
		if (dtoBase.getIdRefTypeSaisiCongeAnnuel() != null) {
			RefTypeSaisiCongeAnnuel typeConge = typeAbsenceRepository.getEntity(RefTypeSaisiCongeAnnuel.class,
					dtoBase.getIdRefTypeSaisiCongeAnnuel());
			
			if(null == typeConge
					|| null == typeConge.getCodeBaseHoraireAbsence()
					|| (!"A".equals(typeConge.getCodeBaseHoraireAbsence().trim())
							&& !"D".equals(typeConge.getCodeBaseHoraireAbsence().trim()))) {
				srm.getErrors().add(String.format(MAUVAIS_BASE_CA, idAgent));
			}
		}else{
			srm.getErrors().add(String.format(BASE_CA_NON_TROUVEE, idAgent));
		}
	}
	
	protected Double getSamediDecompteARendre(DemandeCongesAnnuels demandeCA, RestitutionMassiveDto dto) {
		
		DateTime dateARestituer = new DateTime(dto.getDateRestitution());
		if(dateARestituer.getDayOfWeek() == DateTimeConstants.FRIDAY
				&& 0 < demandeCA.getNbSamediDecompte()){
			if(dto.isJournee() || dto.isApresMidi()) {
				return 1.0;
			}
			if(dto.isMatin()) {
				return 0.5;
			}
		}
		return 0.0;
	}
	
	protected Double getSamediOffertARendre(DemandeCongesAnnuels demandeCA, RestitutionMassiveDto dto) {
		
		DateTime dateARestituer = new DateTime(dto.getDateRestitution());
		if(dateARestituer.getDayOfWeek() == DateTimeConstants.FRIDAY
				&& 0 < demandeCA.getNbSamediOffert()){
			if(dto.isJournee() || dto.isApresMidi()) {
				return 1.0;
			}
		}
		return 0.0;
	}
}
