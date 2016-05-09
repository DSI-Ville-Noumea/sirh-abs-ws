package nc.noumea.mairie.abs.repository;

import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentWeekCongeAnnuel;
import nc.noumea.mairie.abs.domain.CongeAnnuelAlimAutoHisto;
import nc.noumea.mairie.abs.domain.CongeAnnuelRestitutionMassive;
import nc.noumea.mairie.abs.domain.CongeAnnuelRestitutionMassiveHisto;
import nc.noumea.mairie.abs.domain.DemandeCongesAnnuels;
import nc.noumea.mairie.abs.domain.EtatDemandeCongesAnnuels;
import nc.noumea.mairie.abs.domain.RefAlimCongeAnnuel;
import nc.noumea.mairie.abs.dto.RestitutionMassiveDto;
import nc.noumea.mairie.abs.vo.CheckCompteurAgentVo;

public interface ICongesAnnuelsRepository {

	Double getSommeDureeDemandeCongeAnnuelEnCoursSaisieouViseeouAValider(Integer idAgent, Integer idDemande);

	AgentWeekCongeAnnuel getWeekHistoForAgentAndDate(Integer idAgent, Date dateMonth);

	void persistEntity(Object obj);

	List<Date> getListeMoisAlimAutoCongeAnnuel();

	List<DemandeCongesAnnuels> getListeDemandesCongesAnnuelsPrisesByAgent(Integer idAgentConcerne, Date fromDate,
			Date toDate);

	List<CongeAnnuelAlimAutoHisto> getListeAlimAutoCongeAnnuelByMois(Date dateMois, boolean onlyErreur);

	List<CongeAnnuelAlimAutoHisto> getListeAlimAutoCongeAnnuelByAgent(Integer idAgent);

	List<Integer> getListeDemandesCongesAnnuelsPrisesForDate(Date dateRestitution);

	List<RefAlimCongeAnnuel> getListeRefAlimCongeAnnuelByBaseConge(Integer idRefTypeSaisiCongeAnnuel);

	RefAlimCongeAnnuel getRefAlimCongeAnnuel(Integer idRefTypeSaisiCongeAnnuel, Integer year);

	List<CongeAnnuelRestitutionMassive> getHistoRestitutionMassiveOrderByDate();

	CongeAnnuelRestitutionMassive getCongeAnnuelRestitutionMassiveByDate(RestitutionMassiveDto dto);

	List<CongeAnnuelRestitutionMassive> getListCongeAnnuelRestitutionMassiveByDate(RestitutionMassiveDto dto);

	List<RefAlimCongeAnnuel> getListeRefAlimCongeAnnuelByYear(Integer year);

	List<CongeAnnuelRestitutionMassiveHisto> getListRestitutionMassiveByIdAgent(
			List<Integer> idAgent, Date fromDate, Date toDate);

	List<CheckCompteurAgentVo> getSommeDureeDemandeCongeAnnuelEnCoursSaisieouViseeOuAValiderForListAgent(
			List<Integer> listIdsAgent);
	
	List<EtatDemandeCongesAnnuels> getListEtatDemandeCongesAnnuelsApprouveValideAndAnnuleByIdAgent(Integer idAgent);

}
