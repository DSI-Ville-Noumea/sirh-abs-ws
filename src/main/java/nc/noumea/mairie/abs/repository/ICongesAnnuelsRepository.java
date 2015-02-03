package nc.noumea.mairie.abs.repository;

import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentWeekCongeAnnuel;
import nc.noumea.mairie.abs.domain.CongeAnnuelAlimAutoHisto;
import nc.noumea.mairie.abs.domain.CongeAnnuelRestitutionMassiveHisto;
import nc.noumea.mairie.abs.domain.DemandeCongesAnnuels;
import nc.noumea.mairie.abs.dto.RestitutionMassiveDto;

public interface ICongesAnnuelsRepository {

	Double getSommeDureeDemandeCongeAnnuelEnCoursSaisieouViseeouAValider(Integer idAgent, Integer idDemande);

	AgentWeekCongeAnnuel getWeekHistoForAgentAndDate(Integer idAgent, Date dateMonth);

	void persistEntity(Object obj);

	List<Date> getListeMoisAlimAutoCongeAnnuel();

	List<DemandeCongesAnnuels> getListeDemandesCongesAnnuelsPrisesByAgent(Integer idAgentConcerne, Date fromDate,
			Date toDate);

	List<CongeAnnuelRestitutionMassiveHisto> getRestitutionCAByAgentAndDate(RestitutionMassiveDto dto,
			Integer idAgentList);

	List<CongeAnnuelAlimAutoHisto> getListeAlimAutoCongeAnnuel(Date dateMois);

	List<Integer> getListeDemandesCongesAnnuelsPrisesForDate(Date dateRestitution);
}
