package nc.noumea.mairie.abs.service;

import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;

public interface IAbsenceService {

	ReturnMessageDto saveDemande(Integer idAgent, DemandeDto demandeDto);

	DemandeDto getDemandeDto(Integer idDemande);

	List<DemandeDto> getListeDemandes(Integer idAgentConnecte, Integer idAgentConcerne, String ongletDemande,
			Date fromDate, Date toDate, Date dateDemande, Integer idRefEtat, Integer idRefType);

	ReturnMessageDto setDemandeEtat(Integer idAgent, DemandeEtatChangeDto demandeEtatChangeDto);

	ReturnMessageDto setDemandeEtatPris(Integer idDemande);
	
	ReturnMessageDto saveDemandeSIRH(Integer idAgent, DemandeDto demandeDto);
}
