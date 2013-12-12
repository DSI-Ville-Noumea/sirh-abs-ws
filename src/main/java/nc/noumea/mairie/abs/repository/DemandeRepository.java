package nc.noumea.mairie.abs.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import nc.noumea.mairie.abs.domain.EtatDemande;

import org.springframework.stereotype.Repository;

@Repository
public class DemandeRepository implements IDemandeRepository {

	@PersistenceContext(unitName = "absPersistenceUnit")
	private EntityManager absEntityManager;
	
	public void persisEntity(Object obj) {
		absEntityManager.persist(obj);
	}
	
	@Override
    public <T> T getEntity(Class<T> Tclass, Object Id) {
        return absEntityManager.find(Tclass, Id);
    }
	
	@Override
    public EtatDemande getLastEtatDemandeByIdDemande(Integer idDemande){
		
		TypedQuery<EtatDemande> q = absEntityManager.createQuery("select ed from EtatDemande ed inner join ed.demande d where d.idDemande = :idDemande and ed.idEtatDemande in ( select max(ed2.idEtatDemande) from EtatDemande ed2 inner join ed2.demande d2 where d2.idDemande = :idDemande ) ", 
				EtatDemande.class);
		
		q.setParameter("idDemande", idDemande);

		List<EtatDemande> r = q.getResultList();

		if (r.size() == 0)
			return null;

		return r.get(0);
	}
}
