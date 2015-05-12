package nc.noumea.mairie.abs.repository;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import nc.noumea.mairie.abs.domain.AgentWeekReposComp;
import nc.noumea.mairie.abs.domain.DemandeReposComp;
import nc.noumea.mairie.abs.domain.RefEtatEnum;

import org.joda.time.DateTime;
import org.springframework.stereotype.Repository;

@Repository
public class ReposCompensateurRepository implements IReposCompensateurRepository {

	@PersistenceContext(unitName = "absPersistenceUnit")
	private EntityManager absEntityManager;

	@Override
	public Integer getSommeDureeDemandeReposCompEnCoursSaisieouVisee(Integer idAgent, Integer idDemande) {

		StringBuilder sb = new StringBuilder();
		sb.append("select sum(dr.duree) from DemandeReposComp dr inner join dr.etatsDemande ed where dr.idAgent = :idAgent ");
		sb.append(" and ed.idEtatDemande in ( select max(ed2.idEtatDemande) from EtatDemande ed2 inner join ed2.demande d2 where d2.idAgent = :idAgent group by ed2.demande ) ");
		sb.append("and ed.etat in ( :SAISIE, :VISEE_F, :VISEE_D ) ");
		if (null != idDemande) {
			sb.append("and dr.idDemande <> :idDemande ");
		}

		TypedQuery<Long> q = absEntityManager.createQuery(sb.toString(), Long.class);

		q.setParameter("idAgent", idAgent);
		q.setParameter("SAISIE", RefEtatEnum.SAISIE);
		q.setParameter("VISEE_F", RefEtatEnum.VISEE_FAVORABLE);
		q.setParameter("VISEE_D", RefEtatEnum.VISEE_DEFAVORABLE);
		if (null != idDemande) {
			q.setParameter("idDemande", idDemande);
		}

		List<Long> r = q.getResultList();

		if (r.size() == 0 || null == r.get(0))
			return 0;

		return r.get(0).intValue();
	}

	@Override
	public Double getSommeDureeDemandePrises2Ans(Integer idAgent) {
		StringBuilder sb = new StringBuilder();
		sb.append("select dr from DemandeReposComp dr inner join dr.etatsDemande ed where dr.idAgent = :idAgent ");
		sb.append("and ed.idEtatDemande in ( select max(ed2.idEtatDemande) from EtatDemande ed2 inner join ed2.demande d2 where d2.idAgent = :idAgent group by ed2.demande ) ");
		sb.append("and ed.etat in ( :PRIS ) ");
		sb.append("and EXTRACT(year from dr.dateDebut) between :debut and :fin ");

		TypedQuery<DemandeReposComp> q = absEntityManager.createQuery(sb.toString(), DemandeReposComp.class);

		Integer year = new DateTime(new Date()).getYear();

		q.setParameter("idAgent", idAgent);
		q.setParameter("PRIS", RefEtatEnum.PRISE);
		q.setParameter("debut", (year - 1));
		q.setParameter("fin", year);

		List<DemandeReposComp> r = q.getResultList();
		double somme = 0.0;

		if (null != r) {
			for (DemandeReposComp repos : r) {
				somme += repos.getDuree() + repos.getDureeAnneeN1();
			}
		}
		return somme;
	}

	@Override
	public List<AgentWeekReposComp> getListeAlimAutoReposCompByAgent(Integer convertedIdAgent) {

		TypedQuery<AgentWeekReposComp> query = absEntityManager.createNamedQuery("findAgentWeekReposCompByIdAgent",
				AgentWeekReposComp.class);

		query.setParameter("idAgent", convertedIdAgent);

		return query.getResultList();
	}
}
