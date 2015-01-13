package nc.noumea.mairie.abs.repository;

import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentJoursFeriesRepos;

public interface IAgentJoursFeriesReposRepository {

	AgentJoursFeriesRepos getAgentJoursFeriesReposByIdAgentAndJourFerie(Integer idAgent, Date jourFerieChome);

	List<AgentJoursFeriesRepos> getAgentJoursFeriesReposByIdAgentAndPeriode(Integer idAgent, Date dateDebut, Date dateFin);
	
	List<AgentJoursFeriesRepos> getAgentJoursFeriesReposByPeriode(Date dateDebut, Date dateFin);

	void persistEntity(Object obj);

	void removeEntity(Object obj);
}
