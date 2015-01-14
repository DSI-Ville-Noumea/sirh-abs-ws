package nc.noumea.mairie.ws;

import java.util.Date;

import nc.noumea.mairie.abs.dto.ReturnMessageDto;

public interface IPtgWsConsumer {

	ReturnMessageDto checkPointage(Integer idAgent, Date dateDebut, Date dateFin);
}
