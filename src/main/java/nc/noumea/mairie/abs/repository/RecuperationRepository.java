package nc.noumea.mairie.abs.repository;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import nc.noumea.mairie.abs.domain.AgentRecupCount;
import nc.noumea.mairie.abs.domain.AgentWeekRecup;
import nc.noumea.mairie.abs.domain.RefEtatEnum;

import org.springframework.stereotype.Repository;

@Repository
public class RecuperationRepository implements IRecuperationRepository {

	@PersistenceContext(unitName = "absPersistenceUnit")
    private EntityManager absEntityManager;

	@Override
	public AgentRecupCount getAgentRecupCount(Integer idAgent) {
		TypedQuery<AgentRecupCount> q = absEntityManager.createNamedQuery("findAgentRecupCountByIdAgent", AgentRecupCount.class);
		q.setParameter("idAgent", idAgent);
		q.setMaxResults(1);

		List<AgentRecupCount> r = q.getResultList();
		
		return (r.size() == 1 ? r.get(0) : null);
	}

	@Override
	public AgentWeekRecup getWeekRecupForAgentAndDate(Integer idAgent, Date dateMonday) {
		TypedQuery<AgentWeekRecup> q = absEntityManager.createNamedQuery("findAgentWeekRecupByIdAgentAndDateMonday", AgentWeekRecup.class);
		q.setParameter("idAgent", idAgent);
		q.setParameter("dateMonday", dateMonday);
		q.setMaxResults(1);
		List<AgentWeekRecup> r = q.getResultList();
		
		return (r.size() == 1 ? r.get(0) : null);
	}
	
	@Override
	public void persistEntity(Object entity) {
		absEntityManager.persist(entity);
	}
	
	@Override
    public Integer getSommeDureeDemandeRecupEnCoursSaisieouVisee(Integer idAgent) {
		
		TypedQuery<Integer> q = absEntityManager.createQuery("select sum(dr.duree) from DemandeRecup dr inner join dr.etatsDemande ed where dr.idAgent = :idAgent "
				+ "and ed.idEtatDemande in ( select max(ed2.idEtatDemande) from EtatDemande ed2 inner join ed2.demande d2 where d2.idAgent = :idAgent ) "
				+ "and (ed.etat = :SAISIE or ed.etat = :VISEE_F ed.etat = :VISEE_D )", 
				Integer.class);
		
		q.setParameter("idAgent", idAgent);
		q.setParameter("SAISIE", RefEtatEnum.SAISIE.getCodeEtat());
		q.setParameter("VISEE_F", RefEtatEnum.VISEE_FAVORABLE.getCodeEtat());
		q.setParameter("VISEE_D", RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat());

		List<Integer> r = q.getResultList();
		
		if (r.size() == 0)
			return null;

		return r.get(0);
	}
}
