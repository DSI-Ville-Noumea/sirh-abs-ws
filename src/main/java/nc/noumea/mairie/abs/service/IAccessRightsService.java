package nc.noumea.mairie.abs.service;

import java.util.List;

import nc.noumea.mairie.abs.domain.Droit;
import nc.noumea.mairie.abs.dto.AccessRightsDto;
import nc.noumea.mairie.abs.dto.ActeursDto;
import nc.noumea.mairie.abs.dto.AgentDto;
import nc.noumea.mairie.abs.dto.AgentGeneriqueDto;
import nc.noumea.mairie.abs.dto.AgentWithServiceDto;
import nc.noumea.mairie.abs.dto.ApprobateurDto;
import nc.noumea.mairie.abs.dto.InputterDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.dto.ServiceDto;
import nc.noumea.mairie.abs.dto.ViseursDto;

public interface IAccessRightsService {

	AccessRightsDto getAgentAccessRights(Integer idAgent);

	boolean canUserAccessAccessRights(Integer idAgent);

	List<ApprobateurDto> getApprobateurs(Integer idAgent, String codeService);

	ReturnMessageDto setApprobateur(AgentWithServiceDto dto);

	ReturnMessageDto deleteApprobateur(AgentWithServiceDto dto);

	InputterDto getDelegator(int idAgent);

	InputterDto getInputter(int idAgent);

	ViseursDto getViseurs(int idAgent);

	ReturnMessageDto setInputter(Integer idAgentAppro, InputterDto dto);

	ReturnMessageDto setViseurs(Integer idAgentAppro, ViseursDto dto);

	ReturnMessageDto setAgentsToApprove(Integer idAgentApprobateur, List<AgentDto> agents);

	AgentWithServiceDto getApprobateurOfAgent(Integer convertedIdAgent);

	boolean verifAccessRightListDemande(Integer idAgentConnecte, Integer idAgentOfDemande, ReturnMessageDto returnDto);

	List<ServiceDto> getAgentsServicesToApproveOrInput(Integer idAgent);

	List<AgentDto> getAgentsToApproveOrInput(Integer idAgent, String codeService);

	ReturnMessageDto verifAccessRightDemande(Integer idAgent, Integer idAgentOfDemande, ReturnMessageDto returnDto);

	List<Integer> getIdApprobateurOfDelegataire(Integer idAgentConnecte, Integer idAgentConcerne);

	ReturnMessageDto setDelegataire(Integer idAgentAppro, InputterDto inputterDto, ReturnMessageDto returnDto);

	ActeursDto getListeActeurs(Integer idAgent);

	List<AgentDto> getAgentsToInputByOperateur(Integer idAgentApprobateur, Integer idAgent, String codeService);

	List<AgentDto> getAgentsToInputByViseur(Integer idAgentApprobateur, Integer idAgentViseur);

	ReturnMessageDto setAgentsToInputByOperateur(Integer idAgentApprobateur, Integer idAgentOperateur,
			List<AgentDto> agents);

	List<AgentDto> getAgentsToApproveOrInput(Integer idAgentApprobateur, Integer idAgent);

	ReturnMessageDto setAgentsToInputByViseur(Integer idAgentApprobateur, Integer idAgentOperateur,
			List<AgentDto> agents);

	ReturnMessageDto setOperateur(Integer idAgentAppro, AgentDto operateurDto);

	ReturnMessageDto deleteOperateur(Integer idAgentAppro, AgentDto operateurDto);

	ReturnMessageDto deleteViseur(Integer idAgentAppro, AgentDto viseurDto);

	ReturnMessageDto setViseur(Integer idAgentAppro, AgentDto viseurDto);

	AgentGeneriqueDto findAgent(Integer idAgent);

	boolean isUserApprobateur(Integer idAgent);

	boolean isUserOperateur(Integer idAgent);

	boolean isUserViseur(Integer idAgent);

	List<Integer> getListAgentByService(String codeService);

	List<ServiceDto> getAgentsServicesForOperateur(Integer idAgentOperateur);

	List<Droit> getListApprobateursOfOperateur(Integer idAgentOperateur);
}
