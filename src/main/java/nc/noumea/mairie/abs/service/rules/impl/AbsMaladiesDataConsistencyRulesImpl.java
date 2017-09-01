package nc.noumea.mairie.abs.service.rules.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import nc.noumea.mairie.abs.RefTypeGroupeAbsenceEnum;
import nc.noumea.mairie.abs.domain.AuthorizedPAForMaladieEnum;
import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeMaladies;
import nc.noumea.mairie.abs.domain.Profil;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.domain.RefTypeSaisi;
import nc.noumea.mairie.abs.domain.RefTypeSaisiCongeAnnuel;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.dto.SoldeEnfantMaladeDto;
import nc.noumea.mairie.abs.repository.IMaladiesRepository;
import nc.noumea.mairie.abs.service.ICounterService;
import nc.noumea.mairie.abs.vo.CalculDroitsMaladiesVo;
import nc.noumea.mairie.abs.vo.CheckCompteurAgentVo;
import nc.noumea.mairie.domain.Spadmn;

@Service("AbsMaladiesDataConsistencyRulesImpl")
public class AbsMaladiesDataConsistencyRulesImpl extends AbstractAbsenceDataConsistencyRules {

	public static final String	CHAMP_AT_REFERENCE_OBLIGATOIRE	= "Le champ Accident de travail de référence est obligatoire.";

	@Autowired
	@Qualifier("MaladieCounterServiceImpl")
	private ICounterService			maladieCounterServiceImpl;

	@Autowired
	protected IMaladiesRepository	maladiesRepository;

	@Override
	public void processDataConsistencyDemande(ReturnMessageDto srm, Integer idAgent, Demande demande, boolean isProvenanceSIRH) {
		List<RefEtatEnum> listEtatsAcceptes = new ArrayList<RefEtatEnum>();
		listEtatsAcceptes.addAll(Arrays.asList(RefEtatEnum.PROVISOIRE, RefEtatEnum.SAISIE, RefEtatEnum.A_VALIDER));
		// #
		if (isProvenanceSIRH) {
			listEtatsAcceptes.addAll(Arrays.asList(RefEtatEnum.VALIDEE, RefEtatEnum.PRISE));
		}

		checkEtatsDemandeAcceptes(srm, demande, listEtatsAcceptes);
		checkATReferencePourTypeRechute(srm, (DemandeMaladies) demande);
		// #31607 : on verifie la cohérence date debut/date fin ave le nombre de jours ITT
		checkNombreJoursITT(srm, (DemandeMaladies) demande);
		// #31605 puis #39427 : on ne permet aucune date dans le futur
		checkDateFutur(srm, (DemandeMaladies) demande);
		
		// Pour les AT, on empêche la création si une demande existe avec la même date d'accident
		checkDateAccidentTravailUnique(srm, (DemandeMaladies) demande);
		
		// un agent peut avoir fait des heures supp le matin et avoir un AT à 12h => on ne check pas les pointages
		checkDateDebutInferieurDateFin(srm, demande.getDateDebut(), demande.getDateFin());
		checkSaisieKiosqueAutorisee(srm, demande.getType().getTypeSaisi(), isProvenanceSIRH);
		if (srm.getErrors().size() == 0)
			checkDemandeDejaSaisieSurMemePeriode(srm, demande);
		checkStatutAgent(srm, demande);
		// #39402 : On vérifie que la PA de l'agent se situe parmi celles autorisées (AuthorizedPAForMaladieEnum.java), pour toutes les maladies.
		checkPAAgentForMaladie(srm, demande);
		super.checkChampMotif(srm, demande);
		
		// #39320 : Pour les enfants malades, on affiche une alerte si le solde est dépassé
		if (demande.getType().getIdRefTypeAbsence().equals(RefTypeAbsenceEnum.ENFANT_MALADE.getValue())) {
			DemandeDto dto = new DemandeDto(demande, false);
			if (checkDepassementCompteurAgent(dto, null)) {
				logger.warn(DEPASSEMENT_QUOTA_ENFANT_MALADE);
				srm.getInfos().add(DEPASSEMENT_QUOTA_ENFANT_MALADE);
			}
		}
	}

	protected ReturnMessageDto checkDateFutur(ReturnMessageDto srm, DemandeMaladies demande) {
		if (demande.getDateDebut() != null && demande.getDateDebut().compareTo(new Date()) > 0) {
			logger.warn(DEMANDE_DATE_FUTUR_MSG);
			srm.getErrors().add(DEMANDE_DATE_FUTUR_MSG);
		}
		if (demande.getDateAccidentTravail() != null && demande.getDateAccidentTravail().compareTo(new Date()) > 0) {
			logger.warn(DEMANDE_DATE_AT_FUTUR_MSG);
			srm.getErrors().add(DEMANDE_DATE_AT_FUTUR_MSG);
		}
		if (demande.getDateAccidentTravail() != null && demande.getDateDebut() != null && demande.getDateAccidentTravail().after(demande.getDateDebut())) {
			logger.warn(DEMANDE_DATE_AT_APRES_DATE_DEBUT);
			srm.getErrors().add(DEMANDE_DATE_AT_APRES_DATE_DEBUT);
		}

		return srm;
	}

	protected ReturnMessageDto checkDateAccidentTravailUnique(ReturnMessageDto srm, DemandeMaladies demande) {
		
		// Si la demande est nouvelle, on vérifie que la date de l'accident du travail n'existe pas pour une autre demande.
		if (demande.getIdDemande() == null && demande.getDateAccidentTravail() != null && !demande.isProlongation()) {
			if (maladiesRepository.getInitialATByAgent(demande.getIdAgent(), demande.getDateAccidentTravail())) {
				logger.warn(DATE_ACCIDENT_TRAVAIL_EXISTANTE);
				srm.getErrors().add(DATE_ACCIDENT_TRAVAIL_EXISTANTE);
			}
		}

		return srm;
	}

	protected ReturnMessageDto checkNombreJoursITT(ReturnMessageDto srm, DemandeMaladies demande) {
		if (null != demande.getType().getTypeSaisi() && demande.getType().getTypeSaisi().isNombreITT() && demande.getDateDebut() != null
				&& demande.getDateFin() != null && demande.getNombreITT() != null) {
			double nbJour = helperService.calculNombreJoursITT(demande);
			if (nbJour > demande.getNombreITT()) {
				logger.info(NB_JOURS_ITT_INCOHERENT);
				srm.getInfos().add(NB_JOURS_ITT_INCOHERENT);
			} else if (nbJour < demande.getNombreITT()) {
				logger.info(NB_JOURS_ITT_TROP_ELEVE);
				srm.getErrors().add(NB_JOURS_ITT_TROP_ELEVE);
			}
		}
		return srm;
	}

	protected ReturnMessageDto checkATReferencePourTypeRechute(ReturnMessageDto srm, DemandeMaladies demande) {

		if (null != demande.getType().getTypeSaisi() && demande.getType().getTypeSaisi().isAtReference()
				&& (null == demande.getAccidentTravailReference() || null == demande.getAccidentTravailReference().getIdDemande())) {

			logger.warn(String.format(CHAMP_AT_REFERENCE_OBLIGATOIRE));
			srm.getErrors().add(String.format(CHAMP_AT_REFERENCE_OBLIGATOIRE));
		}

		return srm;
	}

	protected boolean isAfficherBoutonAnnuler(DemandeDto demandeDto, boolean isOperateur, boolean isFromSIRH) {
		return (isFromSIRH && (demandeDto.getIdRefEtat().equals(RefEtatEnum.VALIDEE.getCodeEtat()) || demandeDto.getIdRefEtat().equals(
				RefEtatEnum.REJETE.getCodeEtat()) || demandeDto.getIdRefEtat().equals(RefEtatEnum.PRISE.getCodeEtat())));
	}

	@Override
	public ReturnMessageDto checkEtatsDemandeAnnulee(ReturnMessageDto srm, Demande demande, List<RefEtatEnum> listEtatsAcceptes) {

		List<RefEtatEnum> listEtats = new ArrayList<RefEtatEnum>();
		listEtats.addAll(listEtatsAcceptes);
		listEtats.addAll(Arrays.asList(RefEtatEnum.VALIDEE, RefEtatEnum.REJETE));
		return super.checkEtatsDemandeAnnulee(srm, demande, listEtats);
	}

	@Override
	public DemandeDto filtreDroitOfDemandeSIRH(DemandeDto demandeDto) {

		demandeDto.setAffichageBoutonAnnuler(isAfficherBoutonAnnuler(demandeDto, true, true));
		demandeDto.setAffichageValidation(demandeDto.getIdRefEtat().equals(RefEtatEnum.A_VALIDER.getCodeEtat()));
		demandeDto.setAffichageBoutonRejeter(demandeDto.getIdRefEtat().equals(RefEtatEnum.A_VALIDER.getCodeEtat()));
		demandeDto.setAffichageEnAttente(false);
		demandeDto.setAffichageBoutonDupliquer(demandeDto.getIdRefEtat().equals(RefEtatEnum.ANNULEE.getCodeEtat()));

		return demandeDto;
	}

	@Override
	public boolean checkDepassementCompteurAgent(DemandeDto demandeDto, CheckCompteurAgentVo checkCompteurAgentVo) {

		// on verifie d abord l etat de la demande
		// si ANNULE PRIS VALIDE ou REFUSE, on n affiche pas d alerte de depassement de compteur
		if (!checkEtatDemandePourDepassementCompteurAgent(demandeDto))
			return false;

		return checkDepassementCompteurAgent(demandeDto.getAgentWithServiceDto().getIdAgent(), demandeDto);
	}

	protected boolean checkEtatDemandePourDepassementCompteurAgent(DemandeDto demandeDto) {

		if (demandeDto.getIdDemande() != null && (demandeDto.getIdRefEtat().equals(RefEtatEnum.PROVISOIRE.getCodeEtat()) || demandeDto.getIdRefEtat().equals(RefEtatEnum.REJETE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.PRISE.getCodeEtat()) || demandeDto.getIdRefEtat().equals(RefEtatEnum.ANNULEE.getCodeEtat()))) {
			return false;
		}

		return true;
	}

	private boolean checkDepassementCompteurAgent(Integer idAgent, DemandeDto demandeDto) {

		// #36518 : si enfant malade, l'agent a droit à 3 jours par année civile
		if (demandeDto.getIdTypeDemande() != null && demandeDto.getIdTypeDemande() == RefTypeAbsenceEnum.ENFANT_MALADE.getValue()) {
			// #40182 : On n'affiche pas le dépassement pour les premières demandes
			List<DemandeMaladies> listMaladiesEnfantSurAnneeCivile = maladiesRepository.getListEnfantMaladeAnneeCivileByAgent(
					demandeDto.getAgentWithServiceDto().getIdAgent(), helperService.getDateDebutAnneeForOneDate(demandeDto.getDateDebut(), 1),
					demandeDto.getDateFin());
			Integer duree = maladieCounterServiceImpl.getNombeJourMaladies(demandeDto.getAgentWithServiceDto().getIdAgent(),
					helperService.getDateDebutAnneeForOneDate(demandeDto.getDateDebut(), 1),
					demandeDto.getDateFin(), listMaladiesEnfantSurAnneeCivile);
			// #39320 : Pour une demande créée, si elle dépasse le quota, on en informe l'utilisateur.
			if (demandeDto.getIdDemande() == null) {
				if (demandeDto.getDuree() + duree > SoldeEnfantMaladeDto.QUOTA_ENFANT_MALADE)
					return true;
			}
			else {
				if (duree > SoldeEnfantMaladeDto.QUOTA_ENFANT_MALADE)
					return true;
			}
		}

		CalculDroitsMaladiesVo vo = maladieCounterServiceImpl.calculDroitsMaladiesForDemandeMaladies(idAgent, demandeDto);
		if ((null != vo.getNombreJoursCoupePleinSalaire() && 0 < vo.getNombreJoursCoupePleinSalaire())
				|| (null != vo.getNombreJoursCoupeDemiSalaire() && 0 < vo.getNombreJoursCoupeDemiSalaire())) {
			return true;
		}

		return false;
	}

	@Override
	public boolean checkDepassementITT(DemandeDto demandeDto) {
		if (demandeDto.getDateDebut() != null && demandeDto.getDateFin() != null && demandeDto.getNombreITT() != null
				&& demandeDto.getTypeSaisi().isNombreITT()) {
			double nbJour = helperService.calculNombreJoursITT(demandeDto);
			if (nbJour != demandeDto.getNombreITT()) {
				return true;
			}
		}
		return false;
	}

	public ReturnMessageDto checkPAAgentForMaladie(ReturnMessageDto srm, Demande demande) {
		// on recherche sa PA actuelle
		Spadmn pa = sirhRepository.getAgentCurrentPosition(agentMatriculeService.fromIdAgentToSIRHNomatrAgent(demande.getIdAgent()),
				helperService.getCurrentDate());
		
		if (pa != null) {
			for (AuthorizedPAForMaladieEnum authorizedPA : Arrays.asList(AuthorizedPAForMaladieEnum.values())) {
				if (authorizedPA.getCode().equals(pa.getCdpadm()))
					return srm;
			}
		}

		logger.warn(String.format(INACTIVITE_MSG));
		srm.getErrors().add(String.format(INACTIVITE_MSG));
		return srm;
	}

	protected boolean isAfficherBoutonModifier(DemandeDto demandeDto, boolean isAgentLuiMeme, Profil currentProfil) {

		if (isAgentLuiMeme) {
			return false;
		}

		if (!isAgentLuiMeme) {
			if (demandeDto.isAffichageBoutonModifier()
					|| ((demandeDto.getIdRefEtat().equals(RefEtatEnum.PROVISOIRE.getCodeEtat())
							|| demandeDto.getIdRefEtat().equals(RefEtatEnum.SAISIE.getCodeEtat()) || demandeDto.getIdRefEtat().equals(
							RefEtatEnum.A_VALIDER.getCodeEtat())) && currentProfil.isModification()))
				return true;
		}

		return false;
	}

	protected boolean isAfficherBoutonSupprimer(DemandeDto demandeDto, boolean isAgentLuiMeme, Profil currentProfil) {

		if (isAgentLuiMeme)
			return false;

		boolean etatOkPourSuppression = demandeDto.getGroupeAbsence() != null && demandeDto.getGroupeAbsence().getIdRefGroupeAbsence().equals(RefTypeGroupeAbsenceEnum.MALADIES.getValue()) ? 
				// #39690 : Un approbateur ne peut supprimer une demande de maladie à l'état "En attente de validation par la DRH"
				(demandeDto.getIdRefEtat().equals(RefEtatEnum.PROVISOIRE.getCodeEtat())
						|| demandeDto.getIdRefEtat().equals(RefEtatEnum.SAISIE.getCodeEtat()))
				:
				// Pour les autres absences, on ne touche pas au controle précédent.
				(demandeDto.getIdRefEtat().equals(RefEtatEnum.PROVISOIRE.getCodeEtat())
						|| demandeDto.getIdRefEtat().equals(RefEtatEnum.SAISIE.getCodeEtat())
						|| demandeDto.getIdRefEtat().equals(RefEtatEnum.A_VALIDER.getCodeEtat()));
		
				
		if (demandeDto.isAffichageBoutonSupprimer() || (etatOkPourSuppression && currentProfil.isSuppression()))
			return true;

		return false;
	}

	@Override
	public ReturnMessageDto checkSaisiNewTypeAbsence(RefTypeSaisi typeSaisi, RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel, ReturnMessageDto srm) {

		if (!typeSaisi.isCalendarDateDebut())
			srm.getErrors().add(String.format("La date de début est obligatoire."));

		if (!typeSaisi.isFonctionnaire() && !typeSaisi.isContractuel() && !typeSaisi.isConventionCollective())
			srm.getErrors().add(String.format("Vous devez sélectionner au moins un statut d'agent."));

		if (typeSaisi.isAlerte() && null == typeSaisi.getMessageAlerte())
			srm.getErrors().add(String.format("Le message d'alerte est obligatoire si le champ Alerte est coché."));

		return srm;
	}
}
