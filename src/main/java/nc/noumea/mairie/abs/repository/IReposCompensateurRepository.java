package nc.noumea.mairie.abs.repository;

import java.util.List;

import nc.noumea.mairie.abs.domain.AgentWeekReposComp;

public interface IReposCompensateurRepository {

	Integer getSommeDureeDemandeReposCompEnCoursSaisieouVisee(Integer idAgent, Integer idDemande);

	List<AgentWeekReposComp> getListeAlimAutoReposCompByAgent(Integer convertedIdAgent);
}
