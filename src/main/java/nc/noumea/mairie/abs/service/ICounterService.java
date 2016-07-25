package nc.noumea.mairie.abs.service;

import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.dto.AgentOrganisationSyndicaleDto;
import nc.noumea.mairie.abs.dto.CompteurDto;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.abs.dto.OrganisationSyndicaleDto;
import nc.noumea.mairie.abs.dto.RestitutionMassiveDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.dto.SoldeSpecifiqueDto;

public interface ICounterService {

	int addToAgentForPTG(Integer idAgent, Date dateMonday, Integer minutes);

	ReturnMessageDto majCompteurToAgent(ReturnMessageDto srm, Demande demande, DemandeEtatChangeDto demandeEtatChangeDto);

	ReturnMessageDto majManuelleCompteurToAgent(Integer idAgent, CompteurDto compteurDto,boolean compteurExistantBloquant);

	ReturnMessageDto resetCompteurRCAnneePrecedente(Integer idAgentReposCompCount);

	ReturnMessageDto resetCompteurRCAnneenCours(Integer idAgentReposCompCount);

	List<Integer> getListAgentReposCompCountForResetAnneePrcd();

	List<Integer> getListAgentReposCompCountForResetAnneeEnCours();

	List<CompteurDto> getListeCompteur(Integer idOrganisationSyndicale,Integer annee);

	List<SoldeSpecifiqueDto> getListAgentCounterByDate(Integer idAgent, Date dateDebut, Date dateFin);

	ReturnMessageDto initCompteurCongeAnnuel(Integer idAgent, Integer idAgentConcerne);

	List<Integer> getListAgentCongeAnnuelCountForReset();

	ReturnMessageDto resetCompteurCongeAnnuel(Integer idAgentCongeAnnuelCount);

	ReturnMessageDto saveRepresentantA52(Integer idOrganisationSyndicale,
			List<AgentOrganisationSyndicaleDto> listeAgentDto);

	ReturnMessageDto alimentationAutoCompteur(Integer idAgent, Date dateDebut, Date dateFin);

	ReturnMessageDto restitutionMassiveCA(Integer idAgent, RestitutionMassiveDto dto, List<Integer> listIdAgent);

	ReturnMessageDto checkRestitutionMassiveDto(RestitutionMassiveDto dto, ReturnMessageDto srm);

	List<RestitutionMassiveDto> getHistoRestitutionMassiveCA(Integer idAgentConnecte);

	RestitutionMassiveDto getDetailsHistoRestitutionMassive(Integer idAgentConnecte, RestitutionMassiveDto dto);

	List<RestitutionMassiveDto> getHistoRestitutionMassiveCAByAgent(Integer idAgent);

	int addToAgentForPTG(Integer idAgent, Date date, Integer minutes, Integer idPointage, Integer idPointageParent);

	List<OrganisationSyndicaleDto> getlisteOrganisationSyndicaleA52();

	List<AgentOrganisationSyndicaleDto> listeRepresentantA52(Integer idOrganisationSyndicale);

	ReturnMessageDto majManuelleCompteurToListAgent(Integer idAgent, List<CompteurDto> listeCompteurDto, boolean compteurExistantBloquant);

	List<AgentOrganisationSyndicaleDto> listeRepresentantA54(Integer idOrganisationSyndicale);

	ReturnMessageDto saveRepresentantA54(Integer idOrganisationSyndicale, List<AgentOrganisationSyndicaleDto> listeAgentDto);

	List<AgentOrganisationSyndicaleDto> listeRepresentantA48(Integer idOrganisationSyndicale);

	ReturnMessageDto saveRepresentantA48(Integer idOrganisationSyndicale, List<AgentOrganisationSyndicaleDto> listeAgentDto);
}
