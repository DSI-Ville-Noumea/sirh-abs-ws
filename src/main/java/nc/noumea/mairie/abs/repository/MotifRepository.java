package nc.noumea.mairie.abs.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import nc.noumea.mairie.abs.domain.Motif;
import nc.noumea.mairie.abs.domain.MotifCompteur;

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
	public void flush() {
		absEntityManager.flush();
	}

	@Override
	public <T> T getEntity(Class<T> Tclass, Object Id) {
		return absEntityManager.find(Tclass, Id);
	}

	@Override
	public List<Motif> getListeMotif() {
		StringBuilder sb = new StringBuilder();
		sb.append("select m from Motif m ");

		TypedQuery<Motif> query = absEntityManager.createQuery(sb.toString(), Motif.class);

		return query.getResultList();
	}

	@Override
	public List<MotifCompteur> getListeMotifCompteur(Integer idType) {
		StringBuilder sb = new StringBuilder();
		sb.append("select m from MotifCompteur m ");
		if (idType != null) {
			sb.append("where m.refTypeAbsence.idRefTypeAbsence =:idType ");
		}
		// #14737 tri par ordre alpha
		sb.append(" order by m.libelle ");
		
		TypedQuery<MotifCompteur> query = absEntityManager.createQuery(sb.toString(), MotifCompteur.class);
		if (idType != null) {
			query.setParameter("idType", idType);
		}

		return query.getResultList();
	}
}
