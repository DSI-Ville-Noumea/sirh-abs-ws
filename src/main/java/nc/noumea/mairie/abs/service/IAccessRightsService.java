package nc.noumea.mairie.abs.service;

import java.util.List;

import nc.noumea.mairie.abs.dto.AccessRightsDto;
import nc.noumea.mairie.abs.dto.AgentDto;
import nc.noumea.mairie.abs.dto.AgentWithServiceDto;
import nc.noumea.mairie.abs.dto.InputterDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;

public interface IAccessRightsService {

	AccessRightsDto getAgentAccessRights(Integer idAgent);

	boolean canUserAccessAccessRights(Integer idAgent);

	List<AgentWithServiceDto> getApprobateurs();

	List<AgentWithServiceDto> setApprobateurs(List<AgentWithServiceDto> dto);

	InputterDto getInputter(int idAgent);

	ReturnMessageDto setInputter(Integer idAgent, InputterDto dto);

	List<AgentDto> getAgentsToApproveOrInput(Integer idAgentApprobateur, Integer idAgent);

	List<AgentDto> getAgentsToApproveOrInput(Integer idAgentApprobateur, Integer idAgent, String codeService);

	void setAgentsToInput(Integer idAgentApprobateur, Integer idAgentOperateur, List<AgentDto> agents);

	void setAgentsToApprove(Integer idAgentApprobateur, List<AgentDto> agents);

}
