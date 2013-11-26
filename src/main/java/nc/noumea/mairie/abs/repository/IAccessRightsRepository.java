package nc.noumea.mairie.abs.repository;

import java.util.List;

import nc.noumea.mairie.abs.domain.Droit;
import nc.noumea.mairie.abs.domain.DroitsAgent;
import nc.noumea.mairie.abs.domain.Profil;

public interface IAccessRightsRepository {

	Droit getAgentAccessRights(int idAgent);

	List<Droit> getAgentsApprobateurs();

	boolean isUserApprobateur(Integer idAgent);

	boolean isUserOperateur(Integer idAgent);

	boolean isUserViseur(Integer idAgent);

	boolean isUserDelegataire(Integer idAgent);

	void persisEntity(Object obj);

	Profil getProfilByName(String profilName);

	List<Droit> getDroitSousApprobateur(Integer idAgentApprobateur);

	List<DroitsAgent> getListOfAgentsToInputOrApprove(Integer idAgent, String codeService);
	
	void deleteDroitProfilByIdDroitAndIdProfil(Integer idDroitProfil);
	
	void removeEntity(Object obj);
}
