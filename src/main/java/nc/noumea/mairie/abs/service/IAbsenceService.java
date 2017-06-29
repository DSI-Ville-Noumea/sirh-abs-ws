package nc.noumea.mairie.abs.service;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.ControleMedical;
import nc.noumea.mairie.abs.dto.ControleMedicalDto;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.abs.dto.MoisAlimAutoCongesAnnuelsDto;
import nc.noumea.mairie.abs.dto.RefAlimCongesAnnuelsDto;
import nc.noumea.mairie.abs.dto.RestitutionMassiveDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDemandeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;

public interface IAbsenceService {

	ReturnMessageDemandeDto saveDemande(Integer idAgent, DemandeDto demandeDto);

	DemandeDto getDemandeDto(Integer idDemande);

	List<DemandeDto> getListeDemandes(Integer idAgentConnecte, List<Integer> idAgentConcerne, String ongletDemande, Date fromDate, Date toDate,
			Date dateDemande, String listIdRefEtat, Integer idRefType, Integer idRefGroupeAbsence, boolean isAgent);

	ReturnMessageDto setDemandeEtat(Integer idAgent, DemandeEtatChangeDto demandeEtatChangeDto);

	ReturnMessageDto setDemandeEtatPris(Integer idDemande);

	ReturnMessageDto saveDemandeSIRH(Integer idAgent, DemandeDto demandeDto);

	List<DemandeDto> getListeDemandesSIRH(Date fromDate, Date toDate, Integer idRefEtat, Integer idRefType, Integer idAgentRecherche,
			Integer idRefGroupeAbsence, List<Integer> agentIds, String listIdRefEtat, String ongletDemande);

	List<DemandeDto> getListeDemandesSIRHAValider(Date fromDate, Date toDate, Integer idRefEtat, Integer idRefType, Integer idAgentRecherche,
			Integer idRefGroupeAbsence, List<Integer> agentIds);

	List<DemandeDto> getDemandesArchives(Integer idDemande);

	ReturnMessageDto setDemandeEtatSIRH(Integer idAgent, List<DemandeEtatChangeDto> dto);

	ReturnMessageDto checkRecuperations(Integer convertedIdAgent, Date fromDate, Date toDate);

	ReturnMessageDto checkReposCompensateurs(Integer convertedIdAgent, Date fromDate, Date toDate);

	ReturnMessageDto checkAbsencesSyndicales(Integer convertedIdAgent, Date fromDate, Date toDate);

	ReturnMessageDto checkCongesExceptionnels(Integer convertedIdAgent, Date fromDate, Date toDate);

	ReturnMessageDto checkCongesAnnuels(Integer convertedIdAgent, Date fromDate, Date toDate);

	List<MoisAlimAutoCongesAnnuelsDto> getListeMoisAlimAutoCongeAnnuel();

	List<MoisAlimAutoCongesAnnuelsDto> getListeAlimAutoCongeAnnuelByMois(Date dateMois, boolean onlyErreur);

	List<MoisAlimAutoCongesAnnuelsDto> getHistoAlimAutoCongeAnnuel(Integer idAgent);

	List<Integer> getListeIdAgentConcerneRestitutionMassive(RestitutionMassiveDto dto);

	List<RefAlimCongesAnnuelsDto> getListeRefAlimCongeAnnuelByBaseConge(Integer idRefTypeSaisiCongeAnnuel);

	ReturnMessageDto setRefAlimCongeAnnuel(Integer convertedIdAgent, RefAlimCongesAnnuelsDto refAlimCongesAnnuelsDto);

	ReturnMessageDto miseAJourSpsold(Integer idAgent);

	ReturnMessageDto miseAJourSpsorc(Integer idAgent);

	ReturnMessageDto createRefAlimCongeAnnuelAnnee(Integer anneeCreation);

	List<MoisAlimAutoCongesAnnuelsDto> getHistoAlimAutoRecup(Integer convertedIdAgent);

	List<MoisAlimAutoCongesAnnuelsDto> getHistoAlimAutoReposComp(Integer convertedIdAgent);

	ReturnMessageDto checkMaladies(Integer convertedIdAgent, Date fromDate, Date toDate);

	ReturnMessageDto checkAbsences(Integer convertedIdAgent, Date fromDate, Date toDate);

	ReturnMessageDto addPieceJointeSIRH(Integer idAgent, DemandeDto demandeDto);

	List<DemandeDto> getListDemandesCAToAddOrRemoveOnAgentCounter(Integer idAgent, Integer idAgentConcerne);

	ReturnMessageDto saveCommentaireDRH(Integer idDemande, String commentaire);

	List<DemandeDto> getListDemandeRejetDRHStatutVeille();

	ReturnMessageDto persistDemandeControleMedical(ControleMedicalDto dto);

	ControleMedical mapControleMedicalDtoToControleMedical(ControleMedicalDto dto);

	ControleMedicalDto getDemandeControleMedical(Integer idDemandeMaladie);
	
	ReturnMessageDto savePieceJointesWithStream(InputStream stream, Integer idAgent, Integer idAgentOperateur, Integer idDemande, String typeFile);
}
