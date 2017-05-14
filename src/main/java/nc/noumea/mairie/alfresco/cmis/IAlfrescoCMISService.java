package nc.noumea.mairie.alfresco.cmis;

import org.apache.chemistry.opencmis.client.api.Session;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;

public interface IAlfrescoCMISService {

	ReturnMessageDto uploadDocument(Integer idAgentOperateur, DemandeDto demandeDto, Demande demande, 
			ReturnMessageDto returnDto, boolean isFromKiosqueRH, boolean isFromHSCT);

	ReturnMessageDto removeDocument(Session session, ReturnMessageDto returnDto, Demande demande, DemandeDto demandeDto);

	ReturnMessageDto removeAllDocument(ReturnMessageDto returnDto,
			Demande demande);

}
