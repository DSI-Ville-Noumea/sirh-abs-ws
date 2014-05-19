package nc.noumea.mairie.abs.asa.service.rules.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import nc.noumea.mairie.abs.asa.domain.DemandeAsa;
import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.service.rules.impl.AbstractAbsenceDataConsistencyRules;

@Service("AbsAsaDataConsistencyRulesImpl")
public class AbsAsaDataConsistencyRulesImpl extends AbstractAbsenceDataConsistencyRules {

	public static final String DEPASSEMENT_DROITS_ASA_MSG = "Les droits pour ce type d'absence ASA sont dépassés.";
	public static final String AUCUN_DROITS_ASA_MSG = "L'agent [%d] ne possède pas de droit ASA.";
	public static final String OS_INEXISTANT = "L'organisation syndicale n'existe pas.";
	public static final String OS_INACTIVE = "L'organisation syndicale n'est pas active.";

	@Override
	public void processDataConsistencyDemande(ReturnMessageDto srm, Integer idAgent, Demande demande, Date dateLundi) {
		checkEtatsDemandeAcceptes(srm, demande, Arrays.asList(RefEtatEnum.PROVISOIRE, RefEtatEnum.SAISIE));
		super.processDataConsistencyDemande(srm, idAgent, demande, dateLundi);
	}
	
	public boolean isAfficherBoutonImprimer(DemandeDto demandeDto) {
		
		return demandeDto.getIdRefEtat().equals(RefEtatEnum.VALIDEE.getCodeEtat());
	}
	
	public boolean isAfficherBoutonAnnuler(DemandeDto demandeDto) {
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
	
	public void checkOrganisationSyndicale(ReturnMessageDto srm, DemandeAsa demande) {
		
		if (null == demande.getOrganisationSyndicale()) {
			logger.warn(OS_INEXISTANT);
			srm.getErrors().add(String.format(OS_INEXISTANT));
		} else if(!demande.getOrganisationSyndicale().isActif()) {
			logger.warn(OS_INACTIVE);
			srm.getErrors().add(String.format(OS_INACTIVE));
		}
	}
	
	public boolean checkEtatDemandePourDepassementCompteurAgent(DemandeDto demandeDto) {
		
		if(demandeDto.getIdRefEtat().equals(RefEtatEnum.VALIDEE.getCodeEtat())
			|| demandeDto.getIdRefEtat().equals(RefEtatEnum.REJETE.getCodeEtat())
			|| demandeDto.getIdRefEtat().equals(RefEtatEnum.PRISE.getCodeEtat())
			|| demandeDto.getIdRefEtat().equals(RefEtatEnum.ANNULEE.getCodeEtat())) {
			return false;
		}
		
		return true;
	}

}
