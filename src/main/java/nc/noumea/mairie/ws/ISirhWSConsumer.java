package nc.noumea.mairie.ws;

import java.util.Date;

import nc.noumea.mairie.abs.dto.AgentWithServiceDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;

public interface ISirhWSConsumer {

	AgentWithServiceDto getAgentService(Integer idAgent, Date date);
	
	ReturnMessageDto isUtilisateurSIRH(Integer idAgent);
}
