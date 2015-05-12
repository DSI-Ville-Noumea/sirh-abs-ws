package nc.noumea.mairie.abs.repository;

import java.util.List;

import nc.noumea.mairie.abs.domain.AgentWeekReposComp;

public interface IReposCompensateurRepository {

	Integer getSommeDureeDemandeReposCompEnCoursSaisieouVisee(Integer idAgent, Integer idDemande);

	Double getSommeDureeDemandePrises2Ans(Integer idAgent);

	List<AgentWeekReposComp> getListeAlimAutoReposCompByAgent(Integer convertedIdAgent);
}
