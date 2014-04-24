package nc.noumea.mairie.abs.service;

import java.util.List;

import nc.noumea.mairie.abs.domain.RefEtat;
import nc.noumea.mairie.abs.dto.RefEtatDto;
import nc.noumea.mairie.abs.dto.RefTypeAbsenceDto;
import nc.noumea.mairie.abs.dto.RefTypeSaisiDto;

public interface IFiltreService {

	List<RefEtatDto> getRefEtats(String ongletDemande);

	List<RefTypeAbsenceDto> getRefTypesAbsence(Integer idAgentConcerne);

	List<RefEtat> getListeEtatsByOnglet(String ongletDemande, Integer idRefEtat);
	
	List<RefTypeSaisiDto> getRefTypeSaisi(Integer idRefTypeAbsence);
}