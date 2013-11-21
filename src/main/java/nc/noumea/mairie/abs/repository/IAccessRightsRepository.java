package nc.noumea.mairie.abs.repository;

import java.util.List;

import nc.noumea.mairie.abs.domain.Droit;
import nc.noumea.mairie.abs.domain.Profil;

public interface IAccessRightsRepository {

	List<Droit> getAgentAccessRights(int idAgent);

	List<Droit> getAgentsApprobateurs();

	boolean isUserOperator(Integer idAgent);

	boolean isUserViseur(Integer idAgent);

	void persisEntity(Object obj);

	Profil getProfilByName(String profilName);
}
