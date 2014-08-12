package nc.noumea.mairie.abs.repository;

import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.DemandeAsa;

public interface IAsaRepository {

	List<DemandeAsa> getListDemandeAsaEnCours(Integer idAgent, Integer idDemande, Integer type);

	List<DemandeAsa> getListDemandeAsaPourMois(Integer idAgent, Integer idDemande, Date dateDeb, Date dateFin,
			Integer type);
}
