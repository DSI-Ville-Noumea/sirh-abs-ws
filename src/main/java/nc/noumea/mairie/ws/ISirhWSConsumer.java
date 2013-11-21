package nc.noumea.mairie.ws;

import java.util.Date;

import nc.noumea.mairie.abs.dto.AgentWithServiceDto;

public interface ISirhWSConsumer {

	AgentWithServiceDto getAgentService(Integer idAgent, Date date);
}
