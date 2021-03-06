package nc.noumea.mairie.abs.service.rules.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentCongeAnnuelCount;
import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeCongesAnnuels;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeSaisi;
import nc.noumea.mairie.abs.domain.RefTypeSaisiCongeAnnuel;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.abs.dto.RefTypeSaisiCongeAnnuelDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.ICongesAnnuelsRepository;
import nc.noumea.mairie.abs.vo.CheckCompteurAgentVo;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("AbsCongesAnnuelsDataConsistencyRulesImpl")
public class AbsCongesAnnuelsDataConsistencyRulesImpl extends AbstractAbsenceDataConsistencyRules {

	public static final String DEPASSEMENT_DROITS_ACQUIS_MSG = "Votre solde congé est en dépassement de %s jours.";
	public static final String COMPTEUR_INEXISTANT = "Le compteur de congés annuels n'existe pas.";
	public static final String DUREE_SUPERIEURE_0 = "La durée de la demande doit être supérieure à 0.";
	public static final String DUREE_SUPERIEURE_A = "La durée de la demande ne pas être supérieure à %s.";

	@Autowired
	protected ICongesAnnuelsRepository congesAnnuelsRepository;

	@Override
	public void processDataConsistencyDemande(ReturnMessageDto srm, Integer idAgent, Demande demande,
			boolean isProvenanceSIRH) {
		checkEtatsDemandeAcceptes(srm, demande, Arrays.asList(RefEtatEnum.PROVISOIRE, RefEtatEnum.SAISIE, RefEtatEnum.A_VALIDER));
		checkBaseHoraireAbsenceAgent(srm, demande.getIdAgent(), demande.getDateDebut());
		checkDepassementDroitsAcquis(srm, demande, null);
		checkDuree(srm, (DemandeCongesAnnuels) demande);
		checkMultipleCycle(srm, (DemandeCongesAnnuels) demande, idAgent);

		super.processDataConsistencyDemande(srm, idAgent, demande, isProvenanceSIRH);
	}
	
	protected ReturnMessageDto checkDuree(ReturnMessageDto srm, DemandeCongesAnnuels demande) {
		
		if (demande.getTypeSaisiCongeAnnuel().getQuotaMultiple() != null
				&& demande.getTypeSaisiCongeAnnuel().getCodeBaseHoraireAbsence().equals("C")) {
			if(0 == demande.getDuree()) {
				logger.warn(DUREE_SUPERIEURE_0);
				srm.getErrors().add(DUREE_SUPERIEURE_0);
			}
			
			Date dateReprise = new DateTime(demande.getDateFin()).plusDays(1).toDate();
			Double dureeTheorique = helperService.getDureeCongeAnnuel(demande, dateReprise, false, null);
			if(null == dureeTheorique
					|| demande.getDuree() > dureeTheorique) {
				logger.warn(String.format(DUREE_SUPERIEURE_A, dureeTheorique));
				srm.getErrors().add(
						String.format(DUREE_SUPERIEURE_A, dureeTheorique));
			}
		}
		
		return srm;
	}

	protected ReturnMessageDto checkMultipleCycle(ReturnMessageDto srm, DemandeCongesAnnuels demande, Integer idAgent) {
		double nbJours = 0.0;
		if (demande.getTypeSaisiCongeAnnuel().getQuotaMultiple() != null) {
			switch (demande.getTypeSaisiCongeAnnuel().getCodeBaseHoraireAbsence()) {
				case "C":
					nbJours = helperService.calculNombreJours(demande.getDateDebut(), demande.getDateFin());
					if (nbJours % demande.getTypeSaisiCongeAnnuel().getQuotaMultiple() != 0) {
						if ((accessRightsRepository.isOperateurOfAgent(idAgent, demande.getIdAgent())
								|| accessRightsRepository.isApprobateurOrDelegataireOfAgent(idAgent, demande.getIdAgent()) 
								|| sirhWSConsumer.isUtilisateurSIRH(idAgent).getErrors().size() == 0)) {
							logger.warn(String.format(SAISIE_NON_MULTIPLE, demande.getTypeSaisiCongeAnnuel()
									.getCodeBaseHoraireAbsence(), demande.getTypeSaisiCongeAnnuel().getQuotaMultiple()));
							srm.getInfos().add(
									String.format(SAISIE_NON_MULTIPLE, demande.getTypeSaisiCongeAnnuel()
											.getCodeBaseHoraireAbsence(), demande.getTypeSaisiCongeAnnuel()
											.getQuotaMultiple()));
						} else {
							logger.warn(String.format(SAISIE_NON_MULTIPLE, demande.getTypeSaisiCongeAnnuel()
									.getCodeBaseHoraireAbsence(), demande.getTypeSaisiCongeAnnuel().getQuotaMultiple()));
							srm.getErrors().add(
									String.format(SAISIE_NON_MULTIPLE, demande.getTypeSaisiCongeAnnuel()
											.getCodeBaseHoraireAbsence(), demande.getTypeSaisiCongeAnnuel()
											.getQuotaMultiple()));
						}
					}
					break;
				case "E":
				case "F":
					nbJours = helperService.calculNombreJours(demande.getDateDebut(), demande.getDateFin());
					if (nbJours % demande.getTypeSaisiCongeAnnuel().getQuotaMultiple() != 0) {
						if (accessRightsRepository.isOperateurOfAgent(idAgent, demande.getIdAgent())
								|| sirhWSConsumer.isUtilisateurSIRH(idAgent).getErrors().size() == 0) {
							logger.warn(String.format(SAISIE_NON_MULTIPLE, demande.getTypeSaisiCongeAnnuel()
									.getCodeBaseHoraireAbsence(), demande.getTypeSaisiCongeAnnuel().getQuotaMultiple()));
							srm.getInfos().add(
									String.format(SAISIE_NON_MULTIPLE, demande.getTypeSaisiCongeAnnuel()
											.getCodeBaseHoraireAbsence(), demande.getTypeSaisiCongeAnnuel()
											.getQuotaMultiple()));
						} else {
							logger.warn(String.format(SAISIE_NON_MULTIPLE, demande.getTypeSaisiCongeAnnuel()
									.getCodeBaseHoraireAbsence(), demande.getTypeSaisiCongeAnnuel().getQuotaMultiple()));
							srm.getErrors().add(
									String.format(SAISIE_NON_MULTIPLE, demande.getTypeSaisiCongeAnnuel()
											.getCodeBaseHoraireAbsence(), demande.getTypeSaisiCongeAnnuel()
											.getQuotaMultiple()));
						}
					}
					break;

				default:
					break;
			}
		}

		return srm;
	}

	protected ReturnMessageDto checkBaseHoraireAbsenceAgent(ReturnMessageDto srm, Integer idAgent, Date dateDemande) {
		// on recherche sa base horaire d'absence
		RefTypeSaisiCongeAnnuelDto dtoBase = sirhWSConsumer.getBaseHoraireAbsence(idAgent, dateDemande);
		if (dtoBase.getIdRefTypeSaisiCongeAnnuel() == null) {
			logger.warn(String.format(BASE_HORAIRE_AGENT, idAgent));
			srm.getErrors().add(String.format(BASE_HORAIRE_AGENT, idAgent));
		}

		return srm;
	}

	@Override
	public ReturnMessageDto checkDepassementDroitsAcquis(ReturnMessageDto srm, Demande demande, CheckCompteurAgentVo checkCompteurAgentVo) {
		
		// on recupere le solde de l agent
		Double sommeDemandeEnCours = 0.0;
		Double soldeCongeAnnuelNEtN1 = 0.0; 
		
		if(null != checkCompteurAgentVo
				&& null != checkCompteurAgentVo.getDureeDemandeEnCoursCongesAnnuels()
						&& null != checkCompteurAgentVo.getCompteurCongesAnnuels()) {
			
			// on prend toutes les demandes en cours avec notamment la demande concernee en parametre
			// il faut donc a deduire
			sommeDemandeEnCours = checkCompteurAgentVo.getDureeDemandeEnCoursCongesAnnuels() - ((DemandeCongesAnnuels) demande).getDuree();
			soldeCongeAnnuelNEtN1 = checkCompteurAgentVo.getCompteurCongesAnnuels();
			
		}else{
			
			if(null != checkCompteurAgentVo
					&& null != checkCompteurAgentVo.getCompteurCongesAnnuels()) {
				soldeCongeAnnuelNEtN1 = checkCompteurAgentVo.getCompteurCongesAnnuels();
			} else {
				AgentCongeAnnuelCount soldeCongeAnnuel = counterRepository.getAgentCounter(AgentCongeAnnuelCount.class,
						demande.getIdAgent());
				
				if (soldeCongeAnnuel == null) {
					logger.debug(COMPTEUR_INEXISTANT);
					srm.getErrors().add(COMPTEUR_INEXISTANT);
				}else{
					soldeCongeAnnuelNEtN1 = soldeCongeAnnuel.getTotalJours() + soldeCongeAnnuel.getTotalJoursAnneeN1();
					
					if(null != checkCompteurAgentVo)
						checkCompteurAgentVo.setCompteurCongesAnnuels(soldeCongeAnnuelNEtN1);
				}
			}

			sommeDemandeEnCours = congesAnnuelsRepository
					.getSommeDureeDemandeCongeAnnuelEnCoursSaisieouViseeouAValider(demande.getIdAgent(),
							demande.getIdDemande());
		}

		if (soldeCongeAnnuelNEtN1 - sommeDemandeEnCours
						- ((DemandeCongesAnnuels) demande).getDuree() < -5) {
			
			double solde = soldeCongeAnnuelNEtN1
						- sommeDemandeEnCours - ((DemandeCongesAnnuels) demande).getDuree();

			logger.debug(String.format(DEPASSEMENT_DROITS_ACQUIS_MSG, String.valueOf(solde)));
			srm.getInfos().add(String.format(DEPASSEMENT_DROITS_ACQUIS_MSG, String.valueOf(solde)));
		}

		return srm;
	}

	@Override
	public ReturnMessageDto checkSaisiNewTypeAbsence(RefTypeSaisi typeSaisi,
			RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel, ReturnMessageDto srm) {
		if (typeSaisiCongeAnnuel == null || typeSaisiCongeAnnuel.getIdRefTypeSaisiCongeAnnuel() == null) {
			logger.warn(String.format(SAISIE_TYPE_ABSENCE_NON_AUTORISEE));
			srm.getErrors().add(String.format(SAISIE_TYPE_ABSENCE_NON_AUTORISEE));
			return srm;
		}
		if (!typeSaisiCongeAnnuel.isCalendarDateDebut())
			srm.getErrors().add(String.format("La date de début est obligatoire."));

		if (typeSaisiCongeAnnuel.isCalendarDateFin() && typeSaisiCongeAnnuel.isCalendarDateReprise())
			srm.getErrors().add(String.format("Si date de reprise est à oui, alors date de fin doit être à non."));

		if (!typeSaisiCongeAnnuel.isCalendarDateFin() && !typeSaisiCongeAnnuel.isCalendarDateReprise())
			srm.getErrors().add(String.format("Si date de reprise est à non, alors date de fin doit être à oui."));

		return srm;
	}

	@Override
	public boolean checkDepassementMultipleAgent(DemandeDto demandeDto) {
		return checkDepassementMultipleAgent(demandeDto.getTypeSaisiCongeAnnuel(), demandeDto.getDuree());
	}

	private boolean checkDepassementMultipleAgent(RefTypeSaisiCongeAnnuelDto refTypeSaisiCongeAnnuelDto, Double duree) {

		RefTypeSaisiCongeAnnuel typeSaisi = demandeRepository.getEntity(RefTypeSaisiCongeAnnuel.class,
				refTypeSaisiCongeAnnuelDto.getIdRefTypeSaisiCongeAnnuel());

		switch (typeSaisi.getCodeBaseHoraireAbsence()) {
			case "E":
			case "F":

				// si le quota max pour ce type de demande est a zero
				// on renvoie une alerte de depassement dans tous les cas
				if (typeSaisi.getQuotaMultiple() == null)
					return false;

				if (duree % typeSaisi.getQuotaMultiple() != 0) {
					return true;
				}

				break;
			case "C":

				// si le quota max pour ce type de demande est a zero
				// on renvoie une alerte de depassement dans tous les cas
				if (typeSaisi.getQuotaMultiple() == null)
					return false;

				if (duree % 3 != 0) {
					return true;
				}
				break;

			default:
				break;
		}

		return false;
	}

	protected boolean isAfficherBoutonAnnuler(DemandeDto demandeDto, boolean isOperateur, boolean isFromSIRH) {
		return demandeDto.getIdRefEtat().equals(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat())
				|| (isOperateur && demandeDto.getIdRefEtat().equals(RefEtatEnum.APPROUVEE.getCodeEtat()))
				|| (isOperateur && demandeDto.getIdRefEtat().equals(RefEtatEnum.VALIDEE.getCodeEtat()))
				|| (isOperateur && demandeDto.getIdRefEtat().equals(RefEtatEnum.EN_ATTENTE.getCodeEtat()))
				|| (isOperateur && demandeDto.getIdRefEtat().equals(RefEtatEnum.A_VALIDER.getCodeEtat()))
				|| (isOperateur && demandeDto.getIdRefEtat().equals(RefEtatEnum.PRISE.getCodeEtat()));
	}

	@Override
	public ReturnMessageDto checkEtatsDemandeAnnulee(ReturnMessageDto srm, Demande demande,
			List<RefEtatEnum> listEtatsAcceptes) {

		List<RefEtatEnum> listEtats = new ArrayList<RefEtatEnum>();
		listEtats.addAll(listEtatsAcceptes);
		listEtats.addAll(Arrays.asList(RefEtatEnum.VISEE_FAVORABLE, RefEtatEnum.VISEE_DEFAVORABLE,
				RefEtatEnum.APPROUVEE, RefEtatEnum.PRISE, RefEtatEnum.VALIDEE, RefEtatEnum.EN_ATTENTE,
				RefEtatEnum.A_VALIDER));
		// dans le cas des CONGES ANNUELS, on peut tout annuler sauf
		// saisie,provisoire,refuse,rejeté et annulé
		return super.checkEtatsDemandeAnnulee(srm, demande, listEtats);
	}

	@Override
	public DemandeDto filtreDroitOfDemandeSIRH(DemandeDto demandeDto) {
                
		demandeDto.setAffichageBoutonAnnuler(isAfficherBoutonAnnuler(demandeDto, true, true));
		demandeDto.setAffichageValidation(demandeDto.getIdRefEtat().equals(RefEtatEnum.SAISIE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.A_VALIDER.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.EN_ATTENTE.getCodeEtat()));
		demandeDto.setAffichageBoutonRejeter(demandeDto.getIdRefEtat().equals(RefEtatEnum.SAISIE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.APPROUVEE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.A_VALIDER.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.EN_ATTENTE.getCodeEtat()));
		demandeDto.setAffichageEnAttente(demandeDto.getIdRefEtat().equals(RefEtatEnum.A_VALIDER.getCodeEtat()));
		demandeDto.setAffichageBoutonDupliquer(demandeDto.getIdRefEtat().equals(RefEtatEnum.ANNULEE.getCodeEtat()));

		return demandeDto;
	}

	@Override
	public boolean checkDepassementCompteurAgent(DemandeDto demandeDto, CheckCompteurAgentVo checkCompteurAgentVo) {

		// on verifie d abord l etat de la demande
		// si ANNULE PRIS VALIDE ou REFUSE, on n affiche pas d alerte de
		// depassement de compteur
		if (!checkEtatDemandePourDepassementCompteurAgent(demandeDto))
			return false;

		ReturnMessageDto dtoErreur = new ReturnMessageDto();
		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setIdAgent(demandeDto.getAgentWithServiceDto().getIdAgent());
		demande.setIdDemande(demandeDto.getIdDemande());
		demande.setDuree(demandeDto.getDuree());

		dtoErreur = checkDepassementDroitsAcquis(dtoErreur, demande, checkCompteurAgentVo);
		if (dtoErreur.getInfos().size() > 0) {
			return true;
		} else {
			return false;
		}
	}

	protected boolean checkEtatDemandePourDepassementCompteurAgent(DemandeDto demandeDto) {

		if (demandeDto.getIdRefEtat().equals(RefEtatEnum.VALIDEE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.REJETE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.REFUSEE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.PRISE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.ANNULEE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.APPROUVEE.getCodeEtat())) {
			return false;
		}

		return true;
	}

	/**
	 * #13362 si une demande passe de REFUSEE a APPROUVEE, il faut verifier qu
	 * une autre demande n a pas utilisee le samedi offert pendant que la
	 * demande etait REFUSEE sinon on retire le samedi offert de la demande
	 */
	@Override
	public void checkSamediOffertToujoursOk(DemandeEtatChangeDto demandeEtatChangeDto, Demande demande) {

		// si on passe de REFUSEE a APPROUVEE
		if (demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.APPROUVEE.getCodeEtat())
				&& demande.getLatestEtatDemande().getEtat().equals(RefEtatEnum.REFUSEE)) {
			// si un samedi offert etait utilise
			if (((DemandeCongesAnnuels) demande).getNbSamediOffert() > 0) {
				Double nombreSamediOffert = helperService.getNombreSamediOffert((DemandeCongesAnnuels) demande);

				if (0.0 == nombreSamediOffert) {
					((DemandeCongesAnnuels) demande).setNbSamediOffert(0.0);
					
					Double duree = helperService.getDureeCongeAnnuel(
							(DemandeCongesAnnuels) demande, null, false, null);
					
					((DemandeCongesAnnuels) demande).setDuree(null == duree || duree < 0 ? 0.0 : duree);
					((DemandeCongesAnnuels) demande).setDureeAnneeN1(0.0);
				}
			}
		}
	}

	protected boolean isAfficherBoutonImprimer(DemandeDto demandeDto) {
		// cf redmine #13378
		if (demandeDto.getIdRefEtat().equals(RefEtatEnum.APPROUVEE.getCodeEtat()) 
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.VALIDEE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.PRISE.getCodeEtat())) {
			return true;
		}
		return false;
	}
	
	@Override
	public HashMap<Integer, CheckCompteurAgentVo> checkDepassementCompteurForListAgentsOrDemandes(List<DemandeDto> listDemande, 
			HashMap<Integer, CheckCompteurAgentVo> mapCheckCompteurAgentVo) {
		
		if(null == listDemande
				|| listDemande.isEmpty()) {
			return mapCheckCompteurAgentVo;
		}
		
		if(null == mapCheckCompteurAgentVo)
			mapCheckCompteurAgentVo = new HashMap<Integer, CheckCompteurAgentVo>();
		
		List<Integer> listIdsAgentWithCA = new ArrayList<Integer>();
		for(DemandeDto demandeDto : listDemande) {
			listIdsAgentWithCA.add(demandeDto.getAgentWithServiceDto().getIdAgent());
		}
		
		List<CheckCompteurAgentVo> listCheckCompteurAgentVo = congesAnnuelsRepository.getSommeDureeDemandeCongeAnnuelEnCoursSaisieouViseeOuAValiderForListAgent(listIdsAgentWithCA);
		List<AgentCongeAnnuelCount> listAgentCongeAnnuelCount = counterRepository.getListAgentCongeAnnuelCountWithListAgents(listIdsAgentWithCA);

		if (null != listAgentCongeAnnuelCount && !listAgentCongeAnnuelCount.isEmpty()) {
			for (AgentCongeAnnuelCount agentCongeAnnuelCount : listAgentCongeAnnuelCount) {

				CheckCompteurAgentVo vo = mapCheckCompteurAgentVo.get(agentCongeAnnuelCount.getIdAgent());
				
				if (null == vo)
					vo = new CheckCompteurAgentVo();
				
				for (CheckCompteurAgentVo voTmp : listCheckCompteurAgentVo) {
					if (voTmp.getIdAgent().equals(agentCongeAnnuelCount.getIdAgent())) {
						vo.setDureeDemandeEnCoursCongesAnnuels(voTmp.getDureeDemandeEnCoursCongesAnnuels());
						vo.setIdAgent(voTmp.getIdAgent());
						break;
					}
				}

				vo.setCompteurCongesAnnuels(agentCongeAnnuelCount.getTotalJours() + agentCongeAnnuelCount.getTotalJoursAnneeN1());
				mapCheckCompteurAgentVo.put(agentCongeAnnuelCount.getIdAgent(), vo);
			}
		}
		
		return mapCheckCompteurAgentVo;
	}
}
