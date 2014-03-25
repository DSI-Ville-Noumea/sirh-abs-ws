package nc.noumea.mairie.abs.service;

import java.util.List;

import nc.noumea.mairie.abs.dto.HistoriqueSoldeDto;
import nc.noumea.mairie.abs.dto.SoldeDto;

public interface ISoldeService {

	SoldeDto getAgentSolde(Integer idAgent);

	List<HistoriqueSoldeDto> getHistoriqueSoldeAgentByTypeAbsence(Integer idAgent, Integer codeRefTypeAbsence);

}
