package nc.noumea.mairie.abs.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import nc.noumea.mairie.abs.domain.RefTypeAbsence;

import org.springframework.stereotype.Repository;

@Repository
public class TypeAbsenceRepository implements ITypeAbsenceRepository {

	@PersistenceContext(unitName = "absPersistenceUnit")
	private EntityManager absEntityManager;
	
	@Override
	public void persistEntity(Object obj) {
		absEntityManager.persist(obj);
	}

	@Override
	public <T> T getEntity(Class<T> Tclass, Object Id) {
		return absEntityManager.find(Tclass, Id);
	}
	
	@Override
	public void removeEntity(Object obj) {
		absEntityManager.remove(obj);
	}

	@Override
	public List<RefTypeAbsence> getListeTypAbsence() {

		TypedQuery<RefTypeAbsence> query = null;
		
		query = absEntityManager.createQuery("select a from RefTypeAbsence a inner join a.typeSaisi d inner join a.groupe g order by g.code, a.label ", RefTypeAbsence.class);

		return query.getResultList();
	}

}
