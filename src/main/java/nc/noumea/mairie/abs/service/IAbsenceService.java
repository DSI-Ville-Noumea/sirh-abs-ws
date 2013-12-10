package nc.noumea.mairie.abs.service;

import java.util.List;

import nc.noumea.mairie.abs.dto.RefEtatDto;
import nc.noumea.mairie.abs.dto.RefTypeAbsenceDto;

public interface IAbsenceService {

	List<RefEtatDto> getRefEtats();
	
	List<RefTypeAbsenceDto> getRefTypesAbsence();
}
