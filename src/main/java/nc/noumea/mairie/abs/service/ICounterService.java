package nc.noumea.mairie.abs.service;

import java.util.Date;

import nc.noumea.mairie.abs.dto.ReturnMessageDto;

public interface ICounterService {

	int addRecuperationToAgentForPTG(Integer idAgent, Date dateMonday, Integer minutes);
	
	int addReposCompensateurToAgentForPTG(Integer idAgent, Date dateMonday, Integer minutes);
	
	ReturnMessageDto majCompteurRecupToAgent(ReturnMessageDto srm, Integer idAgent, Integer minutes);
}
