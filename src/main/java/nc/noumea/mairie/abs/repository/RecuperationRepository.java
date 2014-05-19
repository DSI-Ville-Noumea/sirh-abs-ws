package nc.noumea.mairie.abs.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import nc.noumea.mairie.abs.domain.RefEtatEnum;

import org.springframework.stereotype.Repository;

@Repository
public class RecuperationRepository implements IRecuperationRepository {

	@PersistenceContext(unitName = "absPersistenceUnit")
    private EntityManager absEntityManager;

	@Override
    public Integer getSommeDureeDemandeRecupEnCoursSaisieouVisee(Integer idAgent, Integer idDemande) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("select sum(dr.duree) from DemandeRecup dr inner join dr.etatsDemande ed where dr.idAgent = :idAgent ");
		sb.append(" and ed.idEtatDemande in ( select max(ed2.idEtatDemande) from EtatDemande ed2 inner join ed2.demande d2 where d2.idAgent = :idAgent group by ed2.demande ) ");
		sb.append("and ed.etat in ( :SAISIE, :VISEE_F, :VISEE_D ) ");
		if(null != idDemande){
			sb.append("and dr.idDemande <> :idDemande ");
		}
		
		TypedQuery<Long> q = absEntityManager.createQuery(sb.toString(), Long.class);
		
		q.setParameter("idAgent", idAgent);
		q.setParameter("SAISIE", RefEtatEnum.SAISIE);
		q.setParameter("VISEE_F", RefEtatEnum.VISEE_FAVORABLE);
		q.setParameter("VISEE_D", RefEtatEnum.VISEE_DEFAVORABLE);
		if(null != idDemande){
			q.setParameter("idDemande", idDemande);
		}
		
		List<Long> r = q.getResultList();
		
		if (r.size() == 0 || null ==  r.get(0))
			return 0;

		return r.get(0).intValue();
	}
}
