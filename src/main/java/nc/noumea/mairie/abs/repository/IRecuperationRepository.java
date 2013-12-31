package nc.noumea.mairie.abs.repository;


public interface IRecuperationRepository {
	
	Integer getSommeDureeDemandeRecupEnCoursSaisieouVisee(Integer idAgent, Integer idDemande);
}
