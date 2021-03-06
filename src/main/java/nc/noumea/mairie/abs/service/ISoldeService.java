package nc.noumea.mairie.abs.service;

import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.dto.HistoriqueSoldeDto;
import nc.noumea.mairie.abs.dto.SoldeDto;

public interface ISoldeService {

	SoldeDto getAgentSolde(Integer idAgent, Date dateDeb, Date dateFin, Integer typeDemande, Date dateJour);

	List<HistoriqueSoldeDto> getHistoriqueSoldeAgent(Integer idAgent, Integer codeRefTypeAbsence, Date dateDeb,
			Date dateFin, boolean isSirh);

}
