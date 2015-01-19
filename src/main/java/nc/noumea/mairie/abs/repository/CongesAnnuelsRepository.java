package nc.noumea.mairie.abs.repository;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import nc.noumea.mairie.abs.domain.AgentWeekCongeAnnuel;
import nc.noumea.mairie.abs.domain.DemandeCongesAnnuels;
import nc.noumea.mairie.abs.domain.RefEtatEnum;

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

		TypedQuery<AgentWeekCongeAnnuel> query = absEntityManager
				.createNamedQuery("findAgentWeekCongeAnnuelByIdAgentAndDateMonth", AgentWeekCongeAnnuel.class);

		query.setParameter("idAgent", idAgent);
		query.setParameter("dateMonth", dateMonth);
		
		// Exec query
		List<AgentWeekCongeAnnuel> result = query.getResultList();

		return (result.size() == 1 ? result.get(0) : null);
	}
}
