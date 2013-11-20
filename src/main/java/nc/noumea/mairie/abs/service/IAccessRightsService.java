package nc.noumea.mairie.abs.service;

import nc.noumea.mairie.abs.dto.AccessRightsDto;

public interface IAccessRightsService {

	AccessRightsDto getAgentAccessRights(Integer idAgent);

	

}
