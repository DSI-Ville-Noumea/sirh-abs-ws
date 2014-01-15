package nc.noumea.mairie.abs.service;

import java.util.List;

import nc.noumea.mairie.abs.dto.MotifCompteurDto;
import nc.noumea.mairie.abs.dto.MotifRefusDto;

public interface IMotifService {

	List<MotifRefusDto> getListeMotifRefus(Integer idRefType);

	List<MotifCompteurDto> getListeMotifCompteur(Integer idRefType);
}
