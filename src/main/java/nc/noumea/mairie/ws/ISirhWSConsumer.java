package nc.noumea.mairie.ws;

import java.util.Date;

import nc.noumea.mairie.abs.dto.AgentGeneriqueDto;
import nc.noumea.mairie.abs.dto.AgentWithServiceDto;
import nc.noumea.mairie.abs.dto.RefTypeSaisiCongeAnnuelDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;

public interface ISirhWSConsumer {

	AgentWithServiceDto getAgentService(Integer idAgent, Date date);

	ReturnMessageDto isUtilisateurSIRH(Integer idAgent);

	AgentGeneriqueDto getAgent(Integer idAgent);

	boolean isJourHoliday(Date date);

	RefTypeSaisiCongeAnnuelDto getBaseHoraireAbsence(Integer idAgent, Date date);
}
