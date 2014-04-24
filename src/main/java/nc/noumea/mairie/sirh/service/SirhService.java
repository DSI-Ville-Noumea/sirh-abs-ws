package nc.noumea.mairie.sirh.service;

import nc.noumea.mairie.abs.repository.ISirhRepository;
import nc.noumea.mairie.sirh.domain.Agent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SirhService implements ISirhService {

	@Autowired
	private ISirhRepository sirhRepository;

	@Override
	@Transactional(readOnly = true)
	public Agent findAgent(Integer idAgent) {
		return sirhRepository.getAgent(idAgent);
	}
}
