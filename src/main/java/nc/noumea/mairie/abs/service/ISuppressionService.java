package nc.noumea.mairie.abs.service;

import nc.noumea.mairie.abs.dto.ReturnMessageDto;

public interface ISuppressionService {
	ReturnMessageDto supprimerDemandeEtatProvisoire(Integer idDemande);

	ReturnMessageDto supprimerDemande(Integer idAgent, Integer idDemande);
}
