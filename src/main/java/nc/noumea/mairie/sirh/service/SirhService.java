package nc.noumea.mairie.sirh.service;

import nc.noumea.mairie.sirh.domain.Agent;
import nc.noumea.mairie.sirh.repository.IAgentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SirhService implements ISirhService {

	@Autowired
	private IAgentRepository agentRepository;
	
	@Override
	public Agent findAgent(Integer idAgent){
		return agentRepository.findAgent(idAgent);
	}
}
