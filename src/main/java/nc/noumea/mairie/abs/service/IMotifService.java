package nc.noumea.mairie.abs.service;

import java.util.List;

import nc.noumea.mairie.abs.dto.MotifCompteurDto;
import nc.noumea.mairie.abs.dto.MotifDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;

public interface IMotifService {

	List<MotifDto> getListeMotif();

	List<MotifCompteurDto> getListeMotifCompteur(Integer idRefType);

	ReturnMessageDto setMotif(MotifDto motifDto);

	ReturnMessageDto setMotifCompteur(MotifCompteurDto motifCompteurDto);
}
