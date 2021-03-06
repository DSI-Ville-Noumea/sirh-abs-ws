package nc.noumea.mairie.abs.repository;

import java.util.List;

import nc.noumea.mairie.abs.domain.Droit;
import nc.noumea.mairie.abs.domain.DroitProfil;
import nc.noumea.mairie.abs.domain.DroitsAgent;
import nc.noumea.mairie.abs.domain.Profil;

public interface IAccessRightsRepository {

	void clear();

	Droit getAgentAccessRights(Integer idAgent);

	List<Droit> getAgentsApprobateurs();

	Droit getDroitByProfilAndAgent(String profil, Integer idAgent);

	boolean isUserApprobateur(Integer idAgent);

	boolean isUserOperateur(Integer idAgent);

	boolean isUserViseur(Integer idAgent);

	boolean isUserDelegataire(Integer idAgent);

	void persisEntity(Object obj);

	Profil getProfilByName(String profilName);

	List<Droit> getDroitSousApprobateur(Integer idAgentApprobateur);

	List<DroitsAgent> getListOfAgentsToInputOrApprove(Integer idAgent, Integer idDroitProfil);

	List<DroitsAgent> getListOfAgentsToInputOrApprove(Integer idAgent);

	void removeEntity(Object obj);

	boolean isUserDelegataireOfApprobateur(Integer idAgentApprobateur, Integer idAgent);

	boolean isUserOperateurOfApprobateur(Integer idAgentApprobateur, Integer idAgent);

	boolean isUserViseurOfApprobateur(Integer idAgentApprobateur, Integer idAgent);

	Droit getAgentDroitFetchAgents(Integer idAgent);

	List<DroitProfil> getDroitProfilByAgent(Integer idAgentApprobateur, Integer idAgent);

	DroitsAgent getDroitsAgent(Integer idAgent);

	List<DroitsAgent> getListDroitsAgent(List<Integer> listIdAgent);

	DroitProfil getDroitProfilApprobateur(Integer idAgentApprobateur);

	Droit getApprobateurOfAgent(DroitsAgent droitAgent);

	boolean isViseurOfAgent(Integer idAgentViseur, Integer IdAgent);

	boolean isApprobateurOrDelegataireOfAgent(Integer idAgentApprobateurOrDelegataire, Integer IdAgent);

	boolean isOperateurOfAgent(Integer idAgentOperateur, Integer IdAgent);

	List<DroitProfil> getDroitProfilByAgentAndLibelle(Integer idAgent, String libelleProfil);

	List<DroitsAgent> getListOfAgentsForListDemandes(List<Integer> idAgent);

	List<DroitsAgent> getListeActeursOfAgent(Integer idAgent);

	DroitProfil getUserOperateurOfApprobateur(Integer idAgentApprobateur, Integer idAgent);

	DroitProfil getUserViseurOfApprobateur(Integer idAgentApprobateur, Integer idAgent);
}
