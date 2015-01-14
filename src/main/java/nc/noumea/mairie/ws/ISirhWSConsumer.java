package nc.noumea.mairie.ws;

import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.dto.AgentGeneriqueDto;
import nc.noumea.mairie.abs.dto.AgentWithServiceDto;
import nc.noumea.mairie.abs.dto.InfosAlimAutoCongesAnnuelsDto;
import nc.noumea.mairie.abs.dto.JourDto;
import nc.noumea.mairie.abs.dto.RefTypeSaisiCongeAnnuelDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.dto.SirhWsServiceDto;

public interface ISirhWSConsumer {

	AgentWithServiceDto getAgentService(Integer idAgent, Date date);

	ReturnMessageDto isUtilisateurSIRH(Integer idAgent);

	AgentGeneriqueDto getAgent(Integer idAgent);

	boolean isJourHoliday(Date date);

	RefTypeSaisiCongeAnnuelDto getBaseHoraireAbsence(Integer idAgent, Date date);

	List<InfosAlimAutoCongesAnnuelsDto> getListPAPourAlimAutoCongesAnnuels(
			Integer idAgent, Date dateDebut, Date dateFin);

	List<JourDto> getListeJoursFeries(Date dateDebut, Date dateFin);

	SirhWsServiceDto getAgentDirection(Integer idAgent, Date date);
}
