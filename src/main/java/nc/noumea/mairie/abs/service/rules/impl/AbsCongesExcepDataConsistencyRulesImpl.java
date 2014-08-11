package nc.noumea.mairie.abs.service.rules.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeCongesExceptionnels;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeSaisi;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.ICongesExceptionnelsRepository;
import nc.noumea.mairie.abs.service.impl.HelperService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("AbsCongesExcepDataConsistencyRulesImpl")
public class AbsCongesExcepDataConsistencyRulesImpl extends AbstractAbsenceDataConsistencyRules {

	public static final String CHAMP_COMMENTAIRE_OBLIGATOIRE = "Le champ Commentaire est obligatoire.";
	
	@Autowired
	protected ICongesExceptionnelsRepository congesExceptionnelsRepository;
	
	@Override
	public void processDataConsistencyDemande(ReturnMessageDto srm, Integer idAgent, Demande demande, Date dateLundi, boolean isProvenanceSIRH) {
		checkEtatsDemandeAcceptes(srm, demande, Arrays.asList(RefEtatEnum.PROVISOIRE, RefEtatEnum.SAISIE));
		checkChampMotifDemandeSaisi(srm, (DemandeCongesExceptionnels)demande);
		super.processDataConsistencyDemande(srm, idAgent, demande, dateLundi, isProvenanceSIRH);
		checkMessageAlerteDepassementDroit(srm, (DemandeCongesExceptionnels)demande);
	}
	
	protected ReturnMessageDto checkChampMotifDemandeSaisi(ReturnMessageDto srm, DemandeCongesExceptionnels demande) {
		
		if(null != demande.getType().getTypeSaisi()) {
			if((null == demande.getCommentaire()
					|| "".equals(demande.getCommentaire().trim()))
					&& demande.getType().getTypeSaisi().isMotif()){
				logger.warn(String.format(CHAMP_COMMENTAIRE_OBLIGATOIRE, demande.getIdAgent()));
				srm.getErrors().add(String.format(CHAMP_COMMENTAIRE_OBLIGATOIRE, demande.getIdAgent()));
			}
		}

		return srm;
	}
	
	protected boolean isAfficherBoutonImprimer(DemandeDto demandeDto) {
		return demandeDto.getIdRefEtat().equals(RefEtatEnum.VALIDEE.getCodeEtat());
	}
	
	protected boolean isAfficherBoutonAnnuler(DemandeDto demandeDto, boolean isOperateur) {
		return demandeDto.getIdRefEtat().equals(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.APPROUVEE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.VALIDEE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.EN_ATTENTE.getCodeEtat())
				|| (isOperateur && demandeDto.getIdRefEtat().equals(RefEtatEnum.PRISE.getCodeEtat()));
	}
	
	@Override
	public ReturnMessageDto checkEtatsDemandeAnnulee(ReturnMessageDto srm, Demande demande,
			List<RefEtatEnum> listEtatsAcceptes) {

		List<RefEtatEnum> listEtats = new ArrayList<RefEtatEnum>();
		listEtats.addAll(listEtatsAcceptes);
		listEtats.addAll(Arrays.asList(RefEtatEnum.VALIDEE, RefEtatEnum.EN_ATTENTE, RefEtatEnum.PRISE));
		// dans le cas des ASA A48, on peut annuler en plus les demandes a l
		// etat VALIDEE et EN_ATTENTE
		return super.checkEtatsDemandeAnnulee(srm, demande, listEtats);
	}
	
	@Override
	public DemandeDto filtreDroitOfDemandeSIRH(DemandeDto demandeDto) {
		
		demandeDto.setAffichageBoutonAnnuler(isAfficherBoutonAnnuler(demandeDto, true));
		demandeDto.setAffichageValidation(demandeDto.getIdRefEtat().equals(
				RefEtatEnum.APPROUVEE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.EN_ATTENTE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.VALIDEE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.REJETE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.PRISE.getCodeEtat()));
		demandeDto.setModifierValidation(demandeDto.getIdRefEtat().equals(
				RefEtatEnum.APPROUVEE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.EN_ATTENTE.getCodeEtat()));
		demandeDto.setAffichageEnAttente(demandeDto.getIdRefEtat().equals(
				RefEtatEnum.APPROUVEE.getCodeEtat()));
		demandeDto.setAffichageBoutonDupliquer(demandeDto.getIdRefEtat().equals(
				RefEtatEnum.APPROUVEE.getCodeEtat()));
		
		return demandeDto;
	}
	
	@Override
	public boolean checkDepassementCompteurAgent(DemandeDto demandeDto){
		return checkDepassementCompteurAgent(
				demandeDto.getIdTypeDemande(), demandeDto.getDateDebut(), 
				demandeDto.getAgentWithServiceDto().getIdAgent(), demandeDto.getDuree());
	}
	
	private boolean checkDepassementCompteurAgent(Integer idTypeDemande, Date dateDebutDemande, Integer idAgent, Double duree) {
		
		RefTypeSaisi typeSaisi = demandeRepository.getEntity(RefTypeSaisi.class, idTypeDemande);
		
		// si le quota max pour ce type de demande est a zero
		// on renvoie une alerte de depassement dans tous les cas
		if(0 == typeSaisi.getQuotaMax())
			return true;
		
		Date dateDebut = helperService.getDateDebutByUnitePeriodeQuotaAndDebutDemande(
				typeSaisi.getRefUnitePeriodeQuota(), 
				dateDebutDemande);
		
		Double dureeDejaPris = congesExceptionnelsRepository.countDureeByPeriodeAndTypeDemande(
				idAgent, dateDebut, dateDebutDemande, idTypeDemande);
		
		if(dureeDejaPris + duree > typeSaisi.getQuotaMax()) {
			return true;
		}
		
		return false;
	}
	
	protected ReturnMessageDto checkMessageAlerteDepassementDroit(ReturnMessageDto srm, DemandeCongesExceptionnels demande) {
		
		// si la colonne alerte dans la table de parametre est a vrai
		if(demande.getType().getTypeSaisi().isAlerte()
				&& checkDepassementCompteurAgent(
						demande.getType().getIdRefTypeAbsence(), demande.getDateDebut(), 
						demande.getIdAgent(), demande.getDuree())) {
			
			logger.warn(String.format(demande.getType().getTypeSaisi().getMessageAlerte(), demande.getIdAgent()));
			srm.getInfos().add(String.format(demande.getType().getTypeSaisi().getMessageAlerte(), demande.getIdAgent()));
		}
		
		return srm;
	}
	
	@Override
	public ReturnMessageDto checkSaisiNewTypeAbsence(RefTypeSaisi typeSaisi, ReturnMessageDto srm) {
		
		if(!typeSaisi.isCalendarDateDebut()) 
			srm.getErrors().add(String.format("La date de début est obligatoire."));
		
		if(!typeSaisi.isFonctionnaire() && !typeSaisi.isContractuel() && !typeSaisi.isConventionCollective())
			srm.getErrors().add(String.format("Vous devez sélectionner au moins un statut d'agent."));
		
		if(typeSaisi.isMotif() && null == typeSaisi.getInfosComplementaires())
			srm.getErrors().add(String.format("Les informations complémentaires sont obligatoires si le champ Motif est coché."));
		
		if(typeSaisi.isAlerte() && null == typeSaisi.getMessageAlerte())
			srm.getErrors().add(String.format("Le message d'alerte est obligatoire si le champ Alerte est coché."));
		
		if(null != typeSaisi.getRefUnitePeriodeQuota() 
				&& (null == typeSaisi.getQuotaMax() || 0 == typeSaisi.getQuotaMax())) {
			srm.getErrors().add(String.format("Le Quota max est obligatoire si l'Unité de période pour le quota est sélectionnée."));
		}
		
		if(null == typeSaisi.getUniteDecompte() || "".equals(typeSaisi.getUniteDecompte().trim()))
			srm.getErrors().add(String.format("L'unité de décompte est obligatoire."));
		
		if (HelperService.UNITE_DECOMPTE_JOURS.equals(typeSaisi.getUniteDecompte())) {
			return checkSaisiNewTypeAbsenceWithJours(typeSaisi, srm);
		}
		if (HelperService.UNITE_DECOMPTE_MINUTES.equals(typeSaisi.getUniteDecompte())) {
			return checkSaisiNewTypeAbsenceWithMinutes(typeSaisi, srm);
		}
		
		return srm;
	}
	
	private ReturnMessageDto checkSaisiNewTypeAbsenceWithMinutes(RefTypeSaisi typeSaisi, ReturnMessageDto srm) {
		
		if((typeSaisi.isCalendarDateFin() && !typeSaisi.isCalendarHeureFin())
				|| (!typeSaisi.isCalendarDateFin() && typeSaisi.isCalendarHeureFin())) 
			srm.getErrors().add(String.format("Pour une unité de décompte en MINUTES, l'heure de fin est obligatoire si la date de fin est sélectionnée."));
		
		if(!typeSaisi.isCalendarHeureDebut()) 
			srm.getErrors().add(String.format("L'heure de début est obligatoire pour une unité de décompte en MINUTES."));
		
		if(typeSaisi.isChkDateDebut() || typeSaisi.isChkDateFin())
			srm.getErrors().add(String.format("Les radio boutons Matin/Après-midi ne sont pas valides pour une unité de décompte en MINUTES."));
		
		return srm;
	}
	
	private ReturnMessageDto checkSaisiNewTypeAbsenceWithJours(RefTypeSaisi typeSaisi, ReturnMessageDto srm) {
		
		if(typeSaisi.isCalendarHeureDebut() || typeSaisi.isCalendarHeureFin()) 
			srm.getErrors().add(String.format("L'heure de début ou de fin n'est pas valide pour une unité de décompte en JOURS."));
		
		if((typeSaisi.isChkDateDebut() && !typeSaisi.isChkDateFin())
				|| (!typeSaisi.isChkDateDebut() && typeSaisi.isChkDateFin())) 
			srm.getErrors().add(String.format("Les radio boutons Matin/Après-midi doivent être sélectionnés ensemble."));
		
		return srm;
	}
}
