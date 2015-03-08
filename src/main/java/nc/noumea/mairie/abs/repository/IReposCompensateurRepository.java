package nc.noumea.mairie.abs.repository;

public interface IReposCompensateurRepository {

	Integer getSommeDureeDemandeReposCompEnCoursSaisieouVisee(Integer idAgent, Integer idDemande);

	Double getSommeDureeDemandePrises2Ans(Integer idAgent);
}
