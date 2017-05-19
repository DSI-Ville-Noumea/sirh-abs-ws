package nc.noumea.mairie.abs.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeSaisiCongeAnnuel;

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
	public List<RefTypeAbsence> getListeTypAbsence(Integer idRefGroupeAbsence) {
		StringBuilder sb = new StringBuilder();
		sb.append("select a from RefTypeAbsence a ");
		sb.append("left outer join a.typeSaisi d ");
		sb.append("inner join a.groupe g ");
		sb.append("where a.actif = true ");

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

	@Override
	public List<RefTypeAbsence> getListeAllTypeAbsence() {
		StringBuilder sb = new StringBuilder();
		sb.append("select a from RefTypeAbsence a ");
		sb.append("left outer join a.typeSaisi d ");
		sb.append("inner join a.groupe g ");
		sb.append("order by g.code, a.label ");

		TypedQuery<RefTypeAbsence> query = null;

		query = absEntityManager.createQuery(sb.toString(), RefTypeAbsence.class);

		return query.getResultList();
	}

	@Override
	public List<RefTypeSaisiCongeAnnuel> getListeTypeSaisiCongeAnnuel() {
		StringBuilder sb = new StringBuilder();
		sb.append("select a from RefTypeSaisiCongeAnnuel a ");
		sb.append("order by a.codeBaseHoraireAbsence ");

		TypedQuery<RefTypeSaisiCongeAnnuel> query = null;

		query = absEntityManager.createQuery(sb.toString(), RefTypeSaisiCongeAnnuel.class);

		return query.getResultList();
	}

}
