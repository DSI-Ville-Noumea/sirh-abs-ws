package nc.noumea.mairie.ws;

import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.dto.AgentGeneriqueDto;
import nc.noumea.mairie.abs.dto.AgentWithServiceDto;
import nc.noumea.mairie.abs.dto.EntiteDto;
import nc.noumea.mairie.abs.dto.InfosAlimAutoCongesAnnuelsDto;
import nc.noumea.mairie.abs.dto.JourDto;
import nc.noumea.mairie.abs.dto.RefTypeSaisiCongeAnnuelDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;

public interface ISirhWSConsumer {

	AgentWithServiceDto getAgentService(Integer idAgent, Date date);

	ReturnMessageDto isUtilisateurSIRH(Integer idAgent);

	AgentGeneriqueDto getAgent(Integer idAgent);

	boolean isJourHoliday(Date date);

	RefTypeSaisiCongeAnnuelDto getBaseHoraireAbsence(Integer idAgent, Date date);

	List<InfosAlimAutoCongesAnnuelsDto> getListPAPourAlimAutoCongesAnnuels(Integer idAgent, Date dateDebut, Date dateFin);

	List<JourDto> getListeJoursFeries(Date dateDebut, Date dateFin);

	ReturnMessageDto isPaieEnCours();

	RefTypeSaisiCongeAnnuelDto getOldBaseHoraireAbsence(Integer idAgent);

	List<InfosAlimAutoCongesAnnuelsDto> getListPAByAgentSansPAFuture(Integer idAgent, Date dateFin);

	EntiteDto getAgentDirection(Integer idAgent, Date date);

	List<AgentWithServiceDto> getListAgentsWithService(List<Integer> listAgentDto, Date date,
			boolean withoutLibelleService);

	List<JourDto> getListeJoursFeriesForSaisiDPM(Date dateDebut, Date dateFin);

	List<AgentGeneriqueDto> getListAgents(List<Integer> listAgentDto);

	List<AgentWithServiceDto> getListAgentServiceWithParent(Integer idServiceADS, Date date);

	List<AgentWithServiceDto> getListAgentsWithServiceOldAffectation(List<Integer> listAgentSansAffectation, 
			boolean withoutLibelleService);
}
