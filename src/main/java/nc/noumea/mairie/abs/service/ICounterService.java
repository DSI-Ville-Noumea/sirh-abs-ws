package nc.noumea.mairie.abs.service;

import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.dto.CompteurDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;

public interface ICounterService {

	int addRecuperationToAgentForPTG(Integer idAgent, Date dateMonday, Integer minutes);
	
	int addReposCompensateurToAgentForPTG(Integer idAgent, Date dateMonday, Integer minutes);
	
	ReturnMessageDto majCompteurRecupToAgent(ReturnMessageDto srm, Integer idAgent, Integer minutes);
	
	ReturnMessageDto majManuelleCompteurToAgent(Integer idAgent, CompteurDto compteurDto, RefTypeAbsenceEnum refTypeAbsence);
	
	ReturnMessageDto resetCompteurRCAnneePrecedente(Integer idAgentReposCompCount);
	
	ReturnMessageDto resetCompteurRCAnneenCours(Integer idAgentReposCompCount);
	
	List<Integer> getListAgentReposCompCountForResetAnneePrcd();
	
	List<Integer> getListAgentReposCompCountForResetAnneeEnCours();
}
