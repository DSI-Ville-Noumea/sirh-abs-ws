package nc.noumea.mairie.abs.service;

import java.util.List;

import nc.noumea.mairie.abs.dto.AccessRightsDto;
import nc.noumea.mairie.abs.dto.AgentDto;
import nc.noumea.mairie.abs.dto.AgentWithServiceDto;
import nc.noumea.mairie.abs.dto.InputterDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.dto.ServiceDto;
import nc.noumea.mairie.abs.dto.ViseursDto;

public interface IAccessRightsService {

	AccessRightsDto getAgentAccessRights(Integer idAgent);

	boolean canUserAccessAccessRights(Integer idAgent);

	List<AgentWithServiceDto> getApprobateurs();

	List<AgentWithServiceDto> setApprobateurs(List<AgentWithServiceDto> dto);

	InputterDto getInputter(int idAgent);

	ViseursDto getViseurs(int idAgent);

	ReturnMessageDto setInputter(Integer idAgentAppro, InputterDto dto);

	ReturnMessageDto setViseurs(Integer idAgentAppro, ViseursDto dto);

	List<AgentDto> getAgentsToApproveOrInput(Integer idAgentApprobateur, Integer idAgent);

	List<AgentDto> getAgentsToApproveOrInput(Integer idAgentApprobateur, Integer idAgent, String codeService);

	ReturnMessageDto setAgentsToInput(Integer idAgentApprobateur, Integer idAgentOperateur, List<AgentDto> agents);

	ReturnMessageDto setAgentsToApprove(Integer idAgentApprobateur, List<AgentDto> agents);

	AgentWithServiceDto getApprobateurOfAgent(Integer convertedIdAgent);
	
	boolean verifAccessRightListDemande(Integer idAgentConnecte, Integer idAgentOfDemande, ReturnMessageDto returnDto);

	List<ServiceDto> getAgentsServicesToApproveOrInput(Integer idAgent);
	
	List<AgentDto> getAgentsToApproveOrInput(Integer idAgent, String codeService);

	ReturnMessageDto verifAccessRightDemande(Integer idAgent, Integer idAgentOfDemande, ReturnMessageDto returnDto);
	
	Integer getIdApprobateurOfDelegataire(Integer idAgentConnecte, Integer idAgentConcerne);
}
