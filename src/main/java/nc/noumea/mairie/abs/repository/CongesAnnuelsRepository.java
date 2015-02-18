package nc.noumea.mairie.abs.repository;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import nc.noumea.mairie.abs.domain.AgentWeekCongeAnnuel;
import nc.noumea.mairie.abs.domain.CongeAnnuelAlimAutoHisto;
import nc.noumea.mairie.abs.domain.CongeAnnuelRestitutionMassiveHisto;
import nc.noumea.mairie.abs.domain.DemandeCongesAnnuels;
import nc.noumea.mairie.abs.domain.RefAlimCongeAnnuel;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.dto.RestitutionMassiveDto;

import org.springframework.stereotype.Repository;

@Repository
public class CongesAnnuelsRepository implements ICongesAnnuelsRepository {

	@PersistenceContext(unitName = "absPersistenceUnit")
	private EntityManager absEntityManager;

	@Override
	public void persistEntity(Object obj) {
		absEntityManager.persist(obj);
	}

	@Override
	public Double getSommeDureeDemandeCongeAnnuelEnCoursSaisieouViseeouAValider(Integer idAgent, Integer idDemande) {
		StringBuilder sb = new StringBuilder();
		sb.append("select dr from DemandeCongesAnnuels dr inner join dr.etatsDemande ed where dr.idAgent = :idAgent ");
		sb.append(" and ed.idEtatDemande in ( select max(ed2.idEtatDemande) from EtatDemande ed2 inner join ed2.demande d2 where d2.idAgent = :idAgent group by ed2.demande ) ");
		sb.append("and ed.etat in ( :SAISIE, :VISEE_F, :VISEE_D, :A_VALIDER ) ");
		if (null != idDemande) {
			sb.append("and dr.idDemande <> :idDemande ");
		}

		TypedQuery<DemandeCongesAnnuels> q = absEntityManager.createQuery(sb.toString(), DemandeCongesAnnuels.class);

		q.setParameter("idAgent", idAgent);
		q.setParameter("SAISIE", RefEtatEnum.SAISIE);
		q.setParameter("VISEE_F", RefEtatEnum.VISEE_FAVORABLE);
		q.setParameter("VISEE_D", RefEtatEnum.VISEE_DEFAVORABLE);
		q.setParameter("A_VALIDER", RefEtatEnum.A_VALIDER);
		if (null != idDemande) {
			q.setParameter("idDemande", idDemande);
		}

		List<DemandeCongesAnnuels> r = q.getResultList();
		double somme = 0.0;

		if (null != r) {
			for (DemandeCongesAnnuels conge : r) {
				somme += conge.getDuree();
			}
		}
		return somme;
	}

	@Override
	public AgentWeekCongeAnnuel getWeekHistoForAgentAndDate(Integer idAgent, Date dateMonth) {

		TypedQuery<AgentWeekCongeAnnuel> query = absEntityManager.createNamedQuery(
				"findAgentWeekCongeAnnuelByIdAgentAndDateMonth", AgentWeekCongeAnnuel.class);

		query.setParameter("idAgent", idAgent);
		query.setParameter("dateMonth", dateMonth);

		// Exec query
		List<AgentWeekCongeAnnuel> result = query.getResultList();

		return (result.size() == 1 ? result.get(0) : null);
	}

	@Override
	public List<Date> getListeMoisAlimAutoCongeAnnuel() {

		StringBuilder sb = new StringBuilder();
		sb.append("select distinct(c.dateMonth) from CongeAnnuelAlimAutoHisto c ");
		sb.append("order by c.dateMonth desc ");

		TypedQuery<Date> query = absEntityManager.createQuery(sb.toString(), Date.class);

		return query.getResultList();
	}

	@Override
	public List<CongeAnnuelAlimAutoHisto> getListeAlimAutoCongeAnnuelByMois(Date dateMois) {

		StringBuilder sb = new StringBuilder();
		sb.append("select c from CongeAnnuelAlimAutoHisto c ");
		sb.append("where c.dateMonth = :date ");
		sb.append("order by c.idAgent ");

		TypedQuery<CongeAnnuelAlimAutoHisto> query = absEntityManager.createQuery(sb.toString(),
				CongeAnnuelAlimAutoHisto.class);
		query.setParameter("date", dateMois);

		return query.getResultList();
	}

	@Override
	public List<DemandeCongesAnnuels> getListeDemandesCongesAnnuelsPrisesByAgent(Integer idAgentConcerne,
			Date fromDate, Date toDate) {

		StringBuilder sb = new StringBuilder();
		sb.append("select d from DemandeCongesAnnuels d ");
		sb.append("inner join d.etatsDemande ed ");
		sb.append("where d.idAgent = :idAgentConcerne ");
		sb.append("and d.dateDebut <= :fromDate and d.dateFin >= :toDate ");
		sb.append("and ed.etat in ( :PRISE ) ");
		sb.append("and ed.idEtatDemande in ( select max(ed2.idEtatDemande) from EtatDemande ed2 group by ed2.demande.idDemande ) ");

		TypedQuery<DemandeCongesAnnuels> query = absEntityManager
				.createQuery(sb.toString(), DemandeCongesAnnuels.class);

		query.setParameter("idAgentConcerne", idAgentConcerne);
		query.setParameter("fromDate", fromDate);
		query.setParameter("toDate", toDate);
		query.setParameter("PRISE", RefEtatEnum.PRISE);

		return query.getResultList();
	}

	@Override
	public List<CongeAnnuelRestitutionMassiveHisto> getRestitutionCAByAgentAndDate(RestitutionMassiveDto dto,
			Integer idAgentList) {

		StringBuilder sb = new StringBuilder();
		sb.append("select d from CongeAnnuelRestitutionMassiveHisto d ");
		sb.append("where d.idAgent = :idAgent ");
		sb.append("and d.dateRestitution = :dateRestitution ");
		sb.append("and d.status = 'OK' ");

		TypedQuery<CongeAnnuelRestitutionMassiveHisto> query = absEntityManager.createQuery(sb.toString(),
				CongeAnnuelRestitutionMassiveHisto.class);

		query.setParameter("idAgent", idAgentList);
		query.setParameter("dateRestitution", dto.getDateRestitution());

		return query.getResultList();
	}

	@Override
	public List<Integer> getListeDemandesCongesAnnuelsPrisesForDate(Date dateRestitution) {

		StringBuilder sb = new StringBuilder();
		sb.append("select distinct(d.idAgent) as idAgent from DemandeCongesAnnuels d ");
		sb.append("inner join d.etatsDemande ed ");
		sb.append("where :date between d.dateDebut and d.dateFin ");
		sb.append("and ed.etat in ( :PRISE ) ");
		sb.append("and ed.idEtatDemande in ( select max(ed2.idEtatDemande) from EtatDemande ed2 group by ed2.demande.idDemande ) ");
		sb.append("order by idAgent ");

		TypedQuery<Integer> query = absEntityManager.createQuery(sb.toString(), Integer.class);

		query.setParameter("date", dateRestitution);
		query.setParameter("PRISE", RefEtatEnum.PRISE);

		return query.getResultList();
	}

	@Override
	public List<RefAlimCongeAnnuel> getListeRefAlimCongeAnnuelByBaseConge(Integer idRefTypeSaisiCongeAnnuel) {
		StringBuilder sb = new StringBuilder();
		sb.append("select r from RefAlimCongeAnnuel r ");
		sb.append("where r.id.idRefTypeSaisiCongeAnnuel = :idRefTypeSaisiCongeAnnuel ");
		sb.append("order by r.id.annee desc ");

		TypedQuery<RefAlimCongeAnnuel> query = absEntityManager.createQuery(sb.toString(), RefAlimCongeAnnuel.class);

		query.setParameter("idRefTypeSaisiCongeAnnuel", idRefTypeSaisiCongeAnnuel);

		return query.getResultList();
	}

	@Override
	public RefAlimCongeAnnuel getRefAlimCongeAnnuelByMois(Integer idRefTypeSaisiCongeAnnuel, Integer year) {
		StringBuilder sb = new StringBuilder();
		sb.append("select r from RefAlimCongeAnnuel r ");
		sb.append("where r.idRefTypeSaisiCongeAnnuel = :idRefTypeSaisiCongeAnnuel ");
		sb.append("and by r.annee =:annee ");

		TypedQuery<RefAlimCongeAnnuel> query = absEntityManager.createQuery(sb.toString(), RefAlimCongeAnnuel.class);

		query.setParameter("idRefTypeSaisiCongeAnnuel", idRefTypeSaisiCongeAnnuel);
		query.setParameter("annee", year);
		
		return query.getSingleResult() ;
	}
}
