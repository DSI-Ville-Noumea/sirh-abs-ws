package nc.noumea.mairie.abs.repository;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;

import nc.noumea.mairie.abs.domain.ControleMedical;

@Repository
public class ControleMedicalRepository implements IControleMedicalRepository {

	@PersistenceContext(unitName = "absPersistenceUnit")
	private EntityManager absEntityManager;

	@Override
	public void persistEntity(Object obj) {
		absEntityManager.persist(obj);
	}

	@Override
	public void removeEntity(Object obj) {
		absEntityManager.remove(obj);
	}

	@Override
	public void clear() {
		absEntityManager.clear();
	}

	@Override
	public void flush() {
		absEntityManager.flush();
	}

	@Override
	public void setFlushMode(FlushModeType flushMode) {
		absEntityManager.setFlushMode(flushMode);
	}

	@Override
	public ControleMedical findByDemandeId(Integer idDemande) {
		StringBuilder sb = new StringBuilder();
		sb.append("select c from ControleMedical c where c.idDemandeMaladie = :idDemande");

		TypedQuery<ControleMedical> query = absEntityManager.createQuery(sb.toString(), ControleMedical.class);
		query.setParameter("idDemande", idDemande);

		return query.getResultList().size() > 0 ? query.getResultList().get(0) : null;
	}

}
