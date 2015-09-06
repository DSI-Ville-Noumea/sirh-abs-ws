package nc.noumea.mairie.abs.service;

import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.dto.AgentJoursFeriesGardeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.dto.SaisieGardeDto;

public interface ISaisieJoursFeriesGardeService {

	SaisieGardeDto getListAgentsWithJoursFeriesEnGarde(Integer idAgent, Integer idServiveADS, Date dateDebut,
			Date dateFin);

	ReturnMessageDto setListAgentsWithJoursFeriesEnGarde(Integer idAgent, List<AgentJoursFeriesGardeDto> listDto,
			Date dateDebut, Date dateFin);
}
