package nc.noumea.mairie.abs.service;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeMaladies;
import nc.noumea.mairie.abs.dto.AgentGeneriqueDto;
import nc.noumea.mairie.abs.dto.AgentOrganisationSyndicaleDto;
import nc.noumea.mairie.abs.dto.CompteurDto;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.abs.dto.OrganisationSyndicaleDto;
import nc.noumea.mairie.abs.dto.RestitutionMassiveDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.dto.SoldeEnfantMaladeDto;
import nc.noumea.mairie.abs.dto.SoldeMaladiesDto;
import nc.noumea.mairie.abs.dto.SoldeSpecifiqueDto;
import nc.noumea.mairie.abs.vo.CalculDroitsMaladiesVo;

public interface ICounterService {

	int addToAgentForPTG(Integer idAgent, Date dateMonday, Integer minutes);

	ReturnMessageDto majCompteurToAgent(ReturnMessageDto srm, Demande demande, DemandeEtatChangeDto demandeEtatChangeDto);

	ReturnMessageDto majManuelleCompteurToAgent(Integer idAgent, CompteurDto compteurDto,boolean compteurExistantBloquant);

	ReturnMessageDto resetCompteurRCAnneePrecedente(Integer idAgentReposCompCount);

	ReturnMessageDto resetCompteurRCAnneenCours(Integer idAgentReposCompCount);

	List<Integer> getListAgentReposCompCountForResetAnneePrcd();

	List<Integer> getListAgentReposCompCountForResetAnneeEnCours();

	List<CompteurDto> getListeCompteurAmicale(Integer idAgentRecherche, Integer annee, Boolean actif);

	List<CompteurDto> getListeCompteur(Integer idOrganisationSyndicale, Integer annee);

	List<CompteurDto> getListeCompteur(Integer idOrganisationSyndicale,Integer annee, Integer pageSize, Integer pageNumber);
	
	List<CompteurDto> getListeCompteur(Integer pageSize, Integer pageNumber, Integer idAgentRecherche, String dateMin, String dateMax) throws ParseException;

	Integer countAllByYear(Integer annee, Integer idOS, Integer idAgentRecherche, Date dateMin, Date dateMax);

	Integer countAllByYear(Integer annee, Integer idOS);

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

	ReturnMessageDto saveRepresentantA54(Integer idOrganisationSyndicale, Integer idAgent);

	ReturnMessageDto saveRepresentantA48(Integer idOrganisationSyndicale, Integer idAgent);

	SoldeMaladiesDto getSoldeByAgent(Integer idAgent, Date dateFinAnneeGlissante, AgentGeneriqueDto agentDto);

	SoldeEnfantMaladeDto getSoldeEnfantMalade(Integer idAgent);

	CalculDroitsMaladiesVo calculDroitsMaladiesForDemandeMaladies(Integer idAgent, DemandeDto demandeMaladie);

	Integer getNombeJourMaladies(Integer idAgent, Date dateDebutAnneeGlissante, Date dateFinAnneeGlissante, List<DemandeMaladies> listMaladies, Integer idDemande);

	List<DemandeMaladies> getHistoriqueMaladiesWithDroits(Integer idAgent, Date date);
}
