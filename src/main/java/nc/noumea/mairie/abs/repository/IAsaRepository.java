package nc.noumea.mairie.abs.repository;

import java.util.List;

import nc.noumea.mairie.abs.domain.DemandeAsa;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;

public interface IAsaRepository {

	List<DemandeAsa> getListDemandeAsaEnCours(Integer idAgent, Integer idDemande, RefTypeAbsenceEnum type);
}
