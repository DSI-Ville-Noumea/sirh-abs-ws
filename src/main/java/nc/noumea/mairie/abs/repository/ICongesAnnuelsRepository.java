package nc.noumea.mairie.abs.repository;

import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentWeekCongeAnnuel;

public interface ICongesAnnuelsRepository {

	Double getSommeDureeDemandeCongeAnnuelEnCoursSaisieouViseeouAValider(Integer idAgent, Integer idDemande);

	AgentWeekCongeAnnuel getWeekHistoForAgentAndDate(Integer idAgent,
			Date dateMonth);
	
	void persistEntity(Object obj);

	List<Date> getListeMoisAlimAutoCongeAnnuel();
}
