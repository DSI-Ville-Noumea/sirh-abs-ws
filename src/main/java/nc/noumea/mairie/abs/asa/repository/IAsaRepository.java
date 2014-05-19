package nc.noumea.mairie.abs.asa.repository;

import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.asa.domain.DemandeAsa;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;

public interface IAsaRepository {

	List<DemandeAsa> getListDemandeAsaEnCours(Integer idAgent, Integer idDemande, RefTypeAbsenceEnum type);

	List<DemandeAsa> getListDemandeAsaPourMois(Integer idAgent, Integer idDemande, Date dateDeb, Date dateFin,
			RefTypeAbsenceEnum type);
}
