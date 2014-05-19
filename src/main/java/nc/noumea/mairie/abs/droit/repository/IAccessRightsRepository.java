package nc.noumea.mairie.abs.droit.repository;

import java.util.List;

import nc.noumea.mairie.abs.droit.domain.Droit;
import nc.noumea.mairie.abs.droit.domain.DroitProfil;
import nc.noumea.mairie.abs.droit.domain.DroitsAgent;
import nc.noumea.mairie.abs.droit.domain.Profil;

public interface IAccessRightsRepository {

	void clear();
	
	Droit getAgentAccessRights(Integer idAgent);

	List<Droit> getAgentsApprobateurs();

	boolean isUserApprobateur(Integer idAgent);

	boolean isUserOperateur(Integer idAgent);

	boolean isUserViseur(Integer idAgent);

	boolean isUserDelegataire(Integer idAgent);

	void persisEntity(Object obj);

	Profil getProfilByName(String profilName);

	List<Droit> getDroitSousApprobateur(Integer idAgentApprobateur);

	List<DroitsAgent> getListOfAgentsToInputOrApprove(Integer idAgent, String codeService, Integer idDroitProfil);
	
	List<DroitsAgent> getListOfAgentsToInputOrApprove(Integer idAgent, String codeService);

	void removeEntity(Object obj);

	boolean isUserDelegataireOfApprobateur(Integer idAgentApprobateur, Integer idAgent);

	boolean isUserOperateurOfApprobateur(Integer idAgentApprobateur, Integer idAgent);

	boolean isUserViseurOfApprobateur(Integer idAgentApprobateur, Integer idAgent);
	
	Droit getAgentDroitFetchAgents(Integer idAgent);
	
	DroitProfil getDroitProfilByAgent(Integer idAgentApprobateur, Integer idAgent);

	DroitsAgent getDroitsAgent(Integer idAgent);

	DroitProfil getDroitProfilApprobateur(Integer idAgentApprobateur);

	Droit getApprobateurOfAgent(DroitsAgent droitAgent);
	
	boolean isViseurOfAgent(Integer idAgentViseur, Integer IdAgent);
	
	boolean isApprobateurOrDelegataireOfAgent(Integer idAgentApprobateurOrDelegataire, Integer IdAgent);
	
	boolean isOperateurOfAgent(Integer idAgentViseur, Integer IdAgent);
	
	DroitProfil getDroitProfilByAgentAndLibelle(Integer idAgent, String libelleProfil);
}
