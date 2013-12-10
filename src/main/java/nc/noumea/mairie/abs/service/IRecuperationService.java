package nc.noumea.mairie.abs.service;

import java.util.Date;

import nc.noumea.mairie.abs.dto.SoldeDto;

public interface IRecuperationService {

	int addRecuperationToAgent(Integer idAgent, Date dateMonday, Integer minutes);

	SoldeDto getAgentSoldeRecuperation(Integer idAgent);
}
