package nc.noumea.mairie.abs.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import nc.noumea.mairie.abs.domain.MotifCompteur;
import nc.noumea.mairie.abs.domain.MotifRefus;

import org.springframework.stereotype.Repository;

@Repository
public class MotifRepository implements IMotifRepository {

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
	public List<MotifRefus> getListeMotifRefus(Integer idType) {
		StringBuilder sb = new StringBuilder();
		sb.append("select m from MotifRefus m ");
		sb.append("where m.refTypeAbsence.idRefTypeAbsence =:idType ");

		TypedQuery<MotifRefus> query = absEntityManager.createQuery(sb.toString(), MotifRefus.class);
		query.setParameter("idType", idType);

		return query.getResultList();
	}

	@Override
	public List<MotifCompteur> getListeMotifCompteur(Integer idType) {
		StringBuilder sb = new StringBuilder();
		sb.append("select m from MotifCompteur m ");
		sb.append("where m.refTypeAbsence.idRefTypeAbsence =:idType ");

		TypedQuery<MotifCompteur> query = absEntityManager.createQuery(sb.toString(), MotifCompteur.class);
		query.setParameter("idType", idType);

		return query.getResultList();
	}
}
