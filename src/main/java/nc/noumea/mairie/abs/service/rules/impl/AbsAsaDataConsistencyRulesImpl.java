package nc.noumea.mairie.abs.service.rules.impl;

import java.util.Arrays;
import java.util.Date;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;

public class AbsAsaDataConsistencyRulesImpl extends AbstractAbsenceDataConsistencyRules {

	public static final String DEPASSEMENT_DROITS_ASA_MSG = "Les droits pour ce type d'absence ASA sont dépassés.";
	public static final String AUCUN_DROITS_ASA_MSG = "L'agent [%d] ne possède pas de droit ASA.";

	@Override
	public void processDataConsistencyDemande(ReturnMessageDto srm, Integer idAgent, Demande demande, Date dateLundi) {
		checkEtatsDemandeAcceptes(srm, demande, Arrays.asList(RefEtatEnum.PROVISOIRE, RefEtatEnum.SAISIE));
		super.processDataConsistencyDemande(srm, idAgent, demande, dateLundi);
	}
	
	protected boolean isAfficherBoutonImprimer(DemandeDto demandeDto) {
		
		return demandeDto.getIdRefEtat().equals(RefEtatEnum.VALIDEE.getCodeEtat());
	}
	
	protected boolean isAfficherBoutonAnnuler(DemandeDto demandeDto) {
		return demandeDto.getIdRefEtat().equals(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.APPROUVEE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.VALIDEE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.EN_ATTENTE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.PRISE.getCodeEtat());
	}
	
	@Override
	public DemandeDto filtreDroitOfDemandeSIRH(DemandeDto demandeDto) {
		
		demandeDto.setAffichageBoutonAnnuler(isAfficherBoutonAnnuler(demandeDto));
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

}
