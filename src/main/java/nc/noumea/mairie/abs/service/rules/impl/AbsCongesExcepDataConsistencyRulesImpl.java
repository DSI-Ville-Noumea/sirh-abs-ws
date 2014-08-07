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
}
