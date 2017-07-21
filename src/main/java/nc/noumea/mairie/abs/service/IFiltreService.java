package nc.noumea.mairie.abs.service;

import java.util.List;

import nc.noumea.mairie.abs.domain.RefEtat;
import nc.noumea.mairie.abs.domain.RefTypeGenerique;
import nc.noumea.mairie.abs.dto.RefEtatDto;
import nc.noumea.mairie.abs.dto.RefGroupeAbsenceDto;
import nc.noumea.mairie.abs.dto.RefTypeAbsenceDto;
import nc.noumea.mairie.abs.dto.RefTypeDto;
import nc.noumea.mairie.abs.dto.RefTypeSaisiDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.dto.UnitePeriodeQuotaDto;

public interface IFiltreService {

	List<RefEtatDto> getRefEtats(String ongletDemande);

	List<RefTypeAbsenceDto> getRefTypesAbsence(Integer idAgentConcerne);

	List<RefTypeAbsenceDto> getRefTypesAbsenceSaisieKiosque(Integer idRefGroupeAbsence, Integer idAgent);

	List<RefEtat> getListeEtatsByOnglet(String ongletDemande, List<Integer> idRefEtat);

	List<RefTypeSaisiDto> getRefTypeSaisi(Integer idRefTypeAbsence);

	List<RefGroupeAbsenceDto> getRefGroupeAbsence(Integer idRefGroupeAbsence);

	List<UnitePeriodeQuotaDto> getUnitePeriodeQuota();

	List<RefTypeAbsenceDto> getRefTypesAbsenceCompteurKiosque();

	List<RefTypeAbsenceDto> getAllRefTypesAbsence();

	List<RefTypeDto> getAllRefTypeAccidentTravail();

	List<RefTypeDto> getAllRefTypeSiegeLesion();

	List<RefTypeDto> getAllRefTypeMaladiePro();

	List<RefGroupeAbsenceDto> getRefGroupeAbsenceForAgent(
			Integer idRefGroupeAbsence);
	
	<T extends RefTypeGenerique> ReturnMessageDto setTypeGenerique(Integer idAgentConnecte, Class<? extends RefTypeGenerique> T, RefTypeDto dto);

	<T extends RefTypeGenerique> ReturnMessageDto deleteTypeGenerique(Integer idAgentConnecte,
			Class<? extends RefTypeGenerique> T, RefTypeDto dto);

	ReturnMessageDto setTypeMaladiePro(Integer idAgentConnecte, RefTypeDto dto);

	List<RefTypeAbsenceDto> getAllRefTypesAbsenceFiltre(Integer idRefGroupeAbsence, Integer idAgent);
}
