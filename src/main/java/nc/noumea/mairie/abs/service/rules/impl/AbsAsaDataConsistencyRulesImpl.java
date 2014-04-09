package nc.noumea.mairie.abs.service.rules.impl;

import java.util.Arrays;
import java.util.Date;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;

public class AbsAsaDataConsistencyRulesImpl extends AbstractAbsenceDataConsistencyRules {
	
	public static final String AUCUN_DROITS_ASA_MSG = "Vous ne possédez pas de droit ASA.";
	public static final String DEPASSEMENT_DROITS_ASA_MSG = "Vos droits pour ce type d'absence ASA sont dépassés.";
	
	@Override
	public void processDataConsistencyDemande(ReturnMessageDto srm, Integer idAgent, Demande demande,
			Date dateLundi) {
		checkEtatsDemandeAcceptes(srm, demande, Arrays.asList(RefEtatEnum.SAISIE));
		super.processDataConsistencyDemande(srm, idAgent, demande, dateLundi);
	}

}
