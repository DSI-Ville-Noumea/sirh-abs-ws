package nc.noumea.mairie.abs.service;

import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.dto.CompteurAsaDto;
import nc.noumea.mairie.abs.dto.CompteurDto;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;

public interface ICounterService {

	int addToAgentForPTG(Integer idAgent, Date dateMonday, Integer minutes);
	
	ReturnMessageDto majCompteurToAgent(ReturnMessageDto srm, Demande demande, DemandeEtatChangeDto demandeEtatChangeDto);

	ReturnMessageDto majManuelleCompteurToAgent(Integer idAgent, CompteurDto compteurDto);

	ReturnMessageDto resetCompteurRCAnneePrecedente(Integer idAgentReposCompCount);

	ReturnMessageDto resetCompteurRCAnneenCours(Integer idAgentReposCompCount);

	List<Integer> getListAgentReposCompCountForResetAnneePrcd();

	List<Integer> getListAgentReposCompCountForResetAnneeEnCours();

	int calculMinutesCompteur(DemandeEtatChangeDto demandeEtatChangeDto, Demande demande);

	List<CompteurAsaDto> getListeCompteur();
}
