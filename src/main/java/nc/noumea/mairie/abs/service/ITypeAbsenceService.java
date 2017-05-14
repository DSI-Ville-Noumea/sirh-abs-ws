package nc.noumea.mairie.abs.service;

import java.util.List;

import nc.noumea.mairie.abs.dto.RefTypeAbsenceDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;

public interface ITypeAbsenceService {

	List<RefTypeAbsenceDto> getListeTypAbsence(Integer idRefGroupeAbsence);

	List<RefTypeAbsenceDto> getListeTypAbsenceCongeAnnuel();

	ReturnMessageDto setTypAbsence(Integer idAgent, RefTypeAbsenceDto typeAbsenceDto);

	ReturnMessageDto inactiveTypeAbsence(Integer idAgent, Integer idRefTypeAbsence);

	RefTypeAbsenceDto getTypeAbsenceByBaseHoraire(Integer idBaseHoraireAbsence);
}
