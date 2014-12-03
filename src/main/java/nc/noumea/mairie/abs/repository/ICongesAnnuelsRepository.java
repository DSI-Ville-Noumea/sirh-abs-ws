package nc.noumea.mairie.abs.repository;

public interface ICongesAnnuelsRepository {

	Double getSommeDureeDemandeCongeAnnuelEnCoursSaisieouViseeouAValider(Integer idAgent, Integer idDemande);
}
