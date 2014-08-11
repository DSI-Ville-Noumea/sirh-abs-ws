package nc.noumea.mairie.abs.service;

import java.util.List;

import nc.noumea.mairie.abs.dto.RefTypeAbsenceDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;

public interface ITypeAbsenceService {

	List<RefTypeAbsenceDto> getListeTypAbsence();

	ReturnMessageDto setTypAbsence(Integer idAgent, RefTypeAbsenceDto typeAbsenceDto);
	
	ReturnMessageDto deleteTypeAbsence(Integer idAgent, Integer idTypeDemande);
}