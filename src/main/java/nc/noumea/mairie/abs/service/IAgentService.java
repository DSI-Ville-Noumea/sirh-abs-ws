package nc.noumea.mairie.abs.service;

import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.dto.AgentGeneriqueDto;
import nc.noumea.mairie.abs.dto.AgentWithServiceDto;

public interface IAgentService {

	AgentGeneriqueDto getAgentOptimise(List<AgentGeneriqueDto> listAgentsExistants, Integer idAgent);

	AgentWithServiceDto getAgentOptimise(
			List<AgentWithServiceDto> listAgentsExistants, Integer idAgent,
			Date currentDate);
}
