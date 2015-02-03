package nc.noumea.mairie.abs.service;

import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.abs.dto.MoisAlimAutoCongesAnnuelsDto;
import nc.noumea.mairie.abs.dto.RestitutionMassiveDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;

public interface IAbsenceService {

	ReturnMessageDto saveDemande(Integer idAgent, DemandeDto demandeDto);

	DemandeDto getDemandeDto(Integer idDemande);

	List<DemandeDto> getListeDemandes(Integer idAgentConnecte, Integer idAgentConcerne, String ongletDemande,
			Date fromDate, Date toDate, Date dateDemande, Integer idRefEtat, Integer idRefType,
			Integer idRefGroupeAbsence);

	ReturnMessageDto setDemandeEtat(Integer idAgent, DemandeEtatChangeDto demandeEtatChangeDto);

	ReturnMessageDto setDemandeEtatPris(Integer idDemande);

	ReturnMessageDto saveDemandeSIRH(Integer idAgent, DemandeDto demandeDto);

	List<DemandeDto> getListeDemandesSIRH(Date fromDate, Date toDate, Integer idRefEtat, Integer idRefType,
			Integer idAgentRecherche, Integer idRefGroupeAbsence);

	List<DemandeDto> getDemandesArchives(Integer idDemande);

	ReturnMessageDto setDemandeEtatSIRH(Integer idAgent, List<DemandeEtatChangeDto> dto);

	List<DemandeDto> getListeDemandesSIRHAValider();

	ReturnMessageDto checkRecuperations(Integer convertedIdAgent, Date fromDate, Date toDate);

	ReturnMessageDto checkReposCompensateurs(Integer convertedIdAgent, Date fromDate, Date toDate);

	ReturnMessageDto checkAbsencesSyndicales(Integer convertedIdAgent, Date fromDate, Date toDate);

	ReturnMessageDto checkCongesExceptionnels(Integer convertedIdAgent, Date fromDate, Date toDate);

	ReturnMessageDto checkCongesAnnuels(Integer convertedIdAgent, Date fromDate, Date toDate);

	List<MoisAlimAutoCongesAnnuelsDto> getListeMoisAlimAutoCongeAnnuel();

	List<MoisAlimAutoCongesAnnuelsDto> getListeAlimAutoCongeAnnuel(Date dateMois);

	List<Integer> getListeIdAgentConcerneRestitutionMassive(RestitutionMassiveDto dto);
}
