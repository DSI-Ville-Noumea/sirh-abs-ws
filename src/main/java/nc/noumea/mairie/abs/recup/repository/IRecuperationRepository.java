package nc.noumea.mairie.abs.recup.repository;


public interface IRecuperationRepository {
	
	Integer getSommeDureeDemandeRecupEnCoursSaisieouVisee(Integer idAgent, Integer idDemande);
}
