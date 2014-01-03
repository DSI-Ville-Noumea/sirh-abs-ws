package nc.noumea.mairie.abs.service;

import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.abs.dto.RefEtatDto;
import nc.noumea.mairie.abs.dto.RefTypeAbsenceDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;

public interface IAbsenceService {

	List<RefEtatDto> getRefEtats();

	List<RefTypeAbsenceDto> getRefTypesAbsence();

	ReturnMessageDto saveDemande(Integer idAgent, DemandeDto demandeDto);

	DemandeDto getDemandeDto(Integer idDemande, Integer idTypeDemande);

	List<DemandeDto> getListeDemandes(Integer idAgentConnecte, Integer idAgentConcerne, String ongletDemande, Date fromDate, Date toDate,
			Date dateDemande, Integer idRefEtat, Integer idRefType);

	boolean verifAccessRightDemande(Integer idAgent, Integer idAgentOfDemande, ReturnMessageDto returnDto);
	
	ReturnMessageDto setDemandeEtat(Integer idAgent, DemandeEtatChangeDto demandeEtatChangeDto);

	ReturnMessageDto setDemandesEtatPris(String csvListIdDemande);
	
	ReturnMessageDto supprimerDemande(Integer idAgent, Integer idDemande, Integer idTypeDemande);
}
