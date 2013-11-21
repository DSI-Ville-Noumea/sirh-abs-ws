package nc.noumea.mairie.abs.service;

import java.util.List;

import nc.noumea.mairie.abs.dto.AccessRightsDto;
import nc.noumea.mairie.abs.dto.AgentWithServiceDto;

public interface IAccessRightsService {

	AccessRightsDto getAgentAccessRights(Integer idAgent);

	List<AgentWithServiceDto> listAgentsApprobateurs();

	List<AgentWithServiceDto> setApprobateurs(List<AgentWithServiceDto> dto);

}
