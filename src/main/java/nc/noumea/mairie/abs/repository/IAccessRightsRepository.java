package nc.noumea.mairie.abs.repository;

import java.util.List;

import nc.noumea.mairie.abs.domain.Droit;
import nc.noumea.mairie.abs.domain.Profil;

public interface IAccessRightsRepository {

	Droit getAgentAccessRights(int idAgent);

	List<Droit> getAgentsApprobateurs();

	boolean isUserApprobator(Integer idAgent);

	boolean isUserOperator(Integer idAgent);

	boolean isUserViseur(Integer idAgent);

	boolean isUserDelegataire(Integer idAgent);

	void persisEntity(Object obj);

	Profil getProfilByName(String profilName);

	List<Droit> getDroitSousApprobateur(Integer idAgentApprobateur);
}
