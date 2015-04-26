package nc.noumea.mairie.abs.repository;

import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentJoursFeriesGarde;

public interface IAgentJoursFeriesGardeRepository {

	AgentJoursFeriesGarde getAgentJoursFeriesGardeByIdAgentAndJourFerie(Integer idAgent, Date jourFerieChome);

	List<AgentJoursFeriesGarde> getAgentJoursFeriesGardeByIdAgentAndPeriode(Integer idAgent, Date dateDebut,
			Date dateFin);

	List<AgentJoursFeriesGarde> getAgentJoursFeriesGardeByPeriode(Date dateDebut, Date dateFin);

	void persistEntity(Object obj);

	void removeEntity(Object obj);
}
