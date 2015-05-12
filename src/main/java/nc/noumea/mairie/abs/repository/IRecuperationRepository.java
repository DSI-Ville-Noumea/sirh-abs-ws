package nc.noumea.mairie.abs.repository;

import java.util.List;

import nc.noumea.mairie.abs.domain.AgentWeekRecup;

public interface IRecuperationRepository {

	Integer getSommeDureeDemandeRecupEnCoursSaisieouVisee(Integer idAgent, Integer idDemande);

	List<AgentWeekRecup> getListeAlimAutoRecupByAgent(Integer convertedIdAgent);
}
