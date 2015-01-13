package nc.noumea.mairie.abs.service;

import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.dto.AgentJoursFeriesReposDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;

public interface ISaisieJoursFeriesReposService {

	List<AgentJoursFeriesReposDto> getListAgentsWithJoursFeriesEnRepos(Integer idAgent, String codeService,
			Date dateDebut, Date dateFin);

	ReturnMessageDto setListAgentsWithJoursFeriesEnRepos(Integer idAgent,
			List<AgentJoursFeriesReposDto> listDto, Date dateDebut, Date dateFin);
}
