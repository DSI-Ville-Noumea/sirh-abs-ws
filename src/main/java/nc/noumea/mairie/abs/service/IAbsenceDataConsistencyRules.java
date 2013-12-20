package nc.noumea.mairie.abs.service;

import java.util.Date;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;

public interface IAbsenceDataConsistencyRules {

	void processDataConsistencyDemande(ReturnMessageDto srm, Integer idAgent, Demande demande, Date dateLundi);
	
	ReturnMessageDto checkEtatDemandeIsProvisoireOuSaisie(ReturnMessageDto srm, Demande demande);
	ReturnMessageDto checkDepassementDroitsAcquis(ReturnMessageDto srm, Demande demande);
	ReturnMessageDto checkDemandeDejaSaisieSurMemePeriode(ReturnMessageDto srm, Demande demande);
	ReturnMessageDto checkAgentInactivity(ReturnMessageDto srm, Integer idAgent, Date dateLundi);
}
