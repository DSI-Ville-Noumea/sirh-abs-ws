package nc.noumea.mairie.abs.service.impl;

import nc.noumea.mairie.abs.dto.SoldeDto;
import nc.noumea.mairie.abs.repository.ISirhRepository;
import nc.noumea.mairie.abs.service.ISoldeService;
import nc.noumea.mairie.domain.SpSold;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SoldeService implements ISoldeService {

	private Logger logger = LoggerFactory.getLogger(SoldeService.class);

	@Autowired
	private ISirhRepository sirhRepository;

	@Override
	public SoldeDto getAgentSoldeCongeAnnee(Integer idAgent) {
		SpSold soldeCongeAnnee = sirhRepository.getSpsold(idAgent);
		SoldeDto dto = new SoldeDto();
		dto.setSolde(soldeCongeAnnee == null ? 0 : soldeCongeAnnee.getSoldeAnneeEnCours());
		return dto;
	}

	@Override
	public SoldeDto getAgentSoldeCongeAnneePrec(Integer idAgent) {
		SpSold soldeCongeAnnePrece = sirhRepository.getSpsold(idAgent);
		SoldeDto dto = new SoldeDto();
		dto.setSolde(soldeCongeAnnePrece == null ? 0 : soldeCongeAnnePrece.getSoldeAnneeEnCours());
		return dto;
	}

}
