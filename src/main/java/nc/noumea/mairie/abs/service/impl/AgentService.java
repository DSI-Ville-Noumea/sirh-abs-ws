package nc.noumea.mairie.abs.service.impl;

import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.dto.AgentGeneriqueDto;
import nc.noumea.mairie.abs.dto.AgentWithServiceDto;
import nc.noumea.mairie.abs.service.IAgentService;
import nc.noumea.mairie.ws.ISirhWSConsumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AgentService implements IAgentService {

	@Autowired
	protected ISirhWSConsumer sirhWSConsumer;
	
	/**
	 * cette methode permet d optimiser les appels a SIRH-WS
	 * afin d ameliorer les temps de reponse
	 */
	@Override
	public AgentGeneriqueDto getAgentOptimise(
			List<AgentGeneriqueDto> listAgentsExistants, Integer idAgent) {
		
		if (null == idAgent) {
			return null;
		}

		// on regarde dans les agents deja retournes par sirh-ws
		for (AgentGeneriqueDto agentExistant : listAgentsExistants) {
			if (agentExistant.getIdAgent().equals(idAgent)) {
				return agentExistant;
			}
		}

		AgentGeneriqueDto result = sirhWSConsumer.getAgent(idAgent);
		if (result != null && result.getIdAgent() != null) {
			listAgentsExistants.add(result);
		}

		return result;
	}
	

	@Override
	public AgentWithServiceDto getAgentOptimise(
			List<AgentWithServiceDto> listAgentsExistants, Integer idAgent, Date currentDate) {
		
		if (null == idAgent) {
			return null;
		}

		// on regarde dans les agents deja retournes par sirh-ws
		for (AgentWithServiceDto agentExistant : listAgentsExistants) {
			if (agentExistant.getIdAgent().equals(idAgent)) {
				return agentExistant;
			}
		}

		AgentWithServiceDto result = sirhWSConsumer.getAgentService(idAgent, currentDate);
		if (result != null && result.getIdAgent() != null) {
			listAgentsExistants.add(result);
		}

		return result;
	}

}
