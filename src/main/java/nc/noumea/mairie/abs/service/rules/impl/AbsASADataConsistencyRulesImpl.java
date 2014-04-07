package nc.noumea.mairie.abs.service.rules.impl;

import java.util.Arrays;
import java.util.Date;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;

import org.springframework.stereotype.Service;

@Service("AbsASADataConsistencyRulesImpl")
public class AbsASADataConsistencyRulesImpl extends AbstractAbsenceDataConsistencyRules {
	
	@Override
	public void processDataConsistencyDemande(ReturnMessageDto srm, Integer idAgent, Demande demande,
			Date dateLundi) {
		checkEtatsDemandeAcceptes(srm, demande, Arrays.asList(RefEtatEnum.SAISIE));
		
	}

}
