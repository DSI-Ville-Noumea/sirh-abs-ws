package nc.noumea.mairie.abs.repository;

import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.DemandeAsa;

public interface IAsaRepository {

	List<DemandeAsa> getListDemandeAsaEnCours(Integer idAgent, Integer idDemande, Date dateDeb, Date dateFin, Integer type);

	List<DemandeAsa> getListDemandeAsaPourMoisByAgent(Integer idAgent, Integer idDemande, Date dateDeb, Date dateFin, Integer type);

	List<DemandeAsa> getListDemandeAsaPourMoisByOS(Integer idOrganisation, Integer idDemande, Date dateDebut, Date dateFin, Integer type);
}
