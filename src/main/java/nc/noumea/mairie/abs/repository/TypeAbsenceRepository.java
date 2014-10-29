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
	public List<RefTypeAbsence> getListeTypAbsence(Integer idRefGroupeAbsence) {
		StringBuilder sb = new StringBuilder();
		sb.append("select a from RefTypeAbsence a ");
		sb.append("inner join a.typeSaisi d ");
		sb.append("inner join a.groupe g ");
		sb.append("where 1=1 ");

		if (idRefGroupeAbsence != null) {
			sb.append("and a.groupe.idRefGroupeAbsence = :idRefGroupeAbsence ");
		}
		sb.append("order by g.code, a.label ");

		TypedQuery<RefTypeAbsence> query = null;

		query = absEntityManager.createQuery(sb.toString(), RefTypeAbsence.class);

		if (idRefGroupeAbsence != null) {
			query.setParameter("idRefGroupeAbsence", idRefGroupeAbsence);
		}

		return query.getResultList();
	}

}
