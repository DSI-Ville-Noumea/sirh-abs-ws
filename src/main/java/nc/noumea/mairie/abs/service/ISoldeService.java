package nc.noumea.mairie.abs.service;

import nc.noumea.mairie.abs.dto.SoldeDto;

public interface ISoldeService {

	SoldeDto getAgentSoldeCongeAnnee(Integer idAgent);

	SoldeDto getAgentSoldeCongeAnneePrec(Integer idAgent);

	SoldeDto getAgentSoldeRecuperation(Integer idAgent);

}
