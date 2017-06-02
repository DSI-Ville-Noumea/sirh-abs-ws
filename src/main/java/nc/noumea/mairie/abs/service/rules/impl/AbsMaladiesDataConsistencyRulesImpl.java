package nc.noumea.mairie.abs.service.rules.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

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
		// #31607 : on verifie la cohérence date debut/date fin ave le nombre de
		// jours ITT
		checkNombreJoursITT(srm, (DemandeMaladies) demande);
		// #31605 : on ne permet pas de date dans le futur sauf si c'est une prolongation
		checkDateFutur(srm, (DemandeMaladies) demande);

		// un agent peut avoir fait des heures supp le matin et avoir un AT à
		// 12h
		// on ne check pas sa PA, car il peut avoir une PA AT le meme jour qu un
		// AT
		if (demande.getType().getIdRefTypeAbsence().equals(RefTypeAbsenceEnum.MALADIE_AT.getValue())
				|| demande.getType().getIdRefTypeAbsence().equals(RefTypeAbsenceEnum.MALADIE_AT_RECHUTE.getValue())) {
			checkDateDebutInferieurDateFin(srm, demande.getDateDebut(), demande.getDateFin());
			checkSaisieKiosqueAutorisee(srm, demande.getType().getTypeSaisi(), isProvenanceSIRH);
			if (srm.getErrors().size() == 0)
				checkDemandeDejaSaisieSurMemePeriode(srm, demande);
			checkStatutAgent(srm, demande);
			super.checkChampMotif(srm, demande);
		} else {
			super.processDataConsistencyDemande(srm, idAgent, demande, isProvenanceSIRH);
		}
	}

	protected ReturnMessageDto checkDateFutur(ReturnMessageDto srm, DemandeMaladies demande) {
		if (demande.getDateDebut() != null && demande.getDateDebut().compareTo(new Date()) > 0 && !demande.isProlongation()) {
			logger.warn(DEMANDE_DATE_FUTUR_MSG);
			srm.getErrors().add(DEMANDE_DATE_FUTUR_MSG);
		}

		return srm;
	}

	protected ReturnMessageDto checkNombreJoursITT(ReturnMessageDto srm, DemandeMaladies demande) {
		if (null != demande.getType().getTypeSaisi() && demande.getType().getTypeSaisi().isNombreITT() && demande.getDateDebut() != null
				&& demande.getDateFin() != null && demande.getNombreITT() != null) {
			double nbJour = helperService.calculNombreJours(demande.getDateDebut(), demande.getDateFin());
			if (nbJour != demande.getNombreITT()) {
				logger.info(NB_JOURS_ITT_INCOHERENT);
				srm.getInfos().add(NB_JOURS_ITT_INCOHERENT);
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
		// si ANNULE PRIS VALIDE ou REFUSE, on n affiche pas d alerte de
		// depassement de compteur
		if (!checkEtatDemandePourDepassementCompteurAgent(demandeDto))
			return false;

		return checkDepassementCompteurAgent(demandeDto.getAgentWithServiceDto().getIdAgent(), demandeDto);
	}

	protected boolean checkEtatDemandePourDepassementCompteurAgent(DemandeDto demandeDto) {

		if (demandeDto.getIdRefEtat().equals(RefEtatEnum.PROVISOIRE.getCodeEtat()) || demandeDto.getIdRefEtat().equals(RefEtatEnum.REJETE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.PRISE.getCodeEtat()) || demandeDto.getIdRefEtat().equals(RefEtatEnum.ANNULEE.getCodeEtat())) {
			return false;
		}

		return true;
	}

	private boolean checkDepassementCompteurAgent(Integer idAgent, DemandeDto demandeDto) {

		// #36518 : si enfant malade, l'agent a droit à 3 jours par année civile
		if (demandeDto.getIdTypeDemande() != null && demandeDto.getIdTypeDemande() == RefTypeAbsenceEnum.ENFANT_MALADE.getValue()) {
			List<DemandeMaladies> listMaladiesEnfantSurAnneeCivile = maladiesRepository.getListEnfantMaladeAnneeCivileByAgent(
					demandeDto.getAgentWithServiceDto().getIdAgent(), helperService.getDateDebutAnneeForOneDate(demandeDto.getDateDebut(), 1),
					helperService.getDateFinAnneeForOneDate(demandeDto.getDateDebut(), 1));
			Integer duree = maladieCounterServiceImpl.getNombeJourMaladies(demandeDto.getAgentWithServiceDto().getIdAgent(),
					helperService.getDateDebutAnneeForOneDate(demandeDto.getDateDebut(), 1),
					helperService.getDateFinAnneeForOneDate(demandeDto.getDateDebut(), 1), listMaladiesEnfantSurAnneeCivile);
			if (duree > SoldeEnfantMaladeDto.QUOTA_ENFANT_MALADE) {
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
			double nbJour = helperService.calculNombreJours(demandeDto.getDateDebut(), demandeDto.getDateFin());
			if (nbJour != demandeDto.getNombreITT()) {
				return true;
			}
		}
		return false;
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

		if (isAgentLuiMeme) {
			return false;
		}

		if (!isAgentLuiMeme) {
			if (demandeDto.isAffichageBoutonSupprimer()
					|| ((demandeDto.getIdRefEtat().equals(RefEtatEnum.PROVISOIRE.getCodeEtat())
							|| demandeDto.getIdRefEtat().equals(RefEtatEnum.SAISIE.getCodeEtat()) || demandeDto.getIdRefEtat().equals(
							RefEtatEnum.A_VALIDER.getCodeEtat())) && currentProfil.isSuppression()))
				return true;
		}

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
