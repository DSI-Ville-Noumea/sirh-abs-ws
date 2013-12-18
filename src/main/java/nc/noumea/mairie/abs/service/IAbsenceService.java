package nc.noumea.mairie.abs.service;

import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.RefEtatDto;
import nc.noumea.mairie.abs.dto.RefTypeAbsenceDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;

public interface IAbsenceService {

	List<RefEtatDto> getRefEtats();

	List<RefTypeAbsenceDto> getRefTypesAbsence();

	ReturnMessageDto saveDemande(Integer idAgent, DemandeDto demandeDto);

	DemandeDto getDemande(Integer idDemande, Integer idTypeDemande);

	List<DemandeDto> getListeDemandes(Integer idAgentConnecte, String ongletDemande, Date fromDate, Date toDate,
			Date dateDemande, Integer idRefEtat, Integer idRefType);

	boolean verifAccessRightDemande(Integer idAgent, DemandeDto demandeDto, ReturnMessageDto returnDto);
}
