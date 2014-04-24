package nc.noumea.mairie.abs.repository;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import nc.noumea.mairie.abs.domain.RefEtat;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeSaisi;

import org.springframework.stereotype.Repository;

@Repository
public class FiltreRepository implements IFiltreRepository {

	@PersistenceContext(unitName = "absPersistenceUnit")
	private EntityManager absEntityManager;

	@Override
	public List<RefEtat> findRefEtatNonPris() {
		List<RefEtat> res = new ArrayList<RefEtat>();
		res = findAllRefEtats();
		RefEtat etatPris = absEntityManager.find(RefEtat.class, (RefEtatEnum.PRISE.getCodeEtat()));
		res.remove(etatPris);
		return res;
	}

	@Override
	public List<RefEtat> findRefEtatEnCours() {
		List<RefEtat> res = new ArrayList<RefEtat>();
		res.add(absEntityManager.find(RefEtat.class, (RefEtatEnum.SAISIE.getCodeEtat())));
		res.add(absEntityManager.find(RefEtat.class, (RefEtatEnum.VISEE_FAVORABLE.getCodeEtat())));
		res.add(absEntityManager.find(RefEtat.class, (RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat())));
		res.add(absEntityManager.find(RefEtat.class, (RefEtatEnum.APPROUVEE.getCodeEtat())));
		res.add(absEntityManager.find(RefEtat.class, (RefEtatEnum.VALIDEE.getCodeEtat())));
		return res;
	}

	@Override
	public List<RefEtat> findAllRefEtats() {

		return absEntityManager.createQuery("SELECT o FROM RefEtat o", RefEtat.class).getResultList();
	}

	@Override
	public List<RefTypeAbsence> findAllRefTypeAbsences() {
		return absEntityManager.createQuery("SELECT o FROM RefTypeAbsence o", RefTypeAbsence.class).getResultList();
	}

	@Override
	public RefTypeSaisi findRefTypeSaisi(Integer idRefTypeAbsence) {

		TypedQuery<RefTypeSaisi> query = null;
		query = absEntityManager.createNamedQuery("getRefTypeSaisiByIdTypeDemande", RefTypeSaisi.class);
		query.setParameter("idRefTypeAbsence", idRefTypeAbsence);
		query.setMaxResults(1);

		return query.getSingleResult();
	}

	@Override
	public List<RefTypeSaisi> findAllRefTypeSaisi() {

		TypedQuery<RefTypeSaisi> query = null;
		query = absEntityManager.createNamedQuery("getAllRefTypeSaisi", RefTypeSaisi.class);

		return query.getResultList();
	}

	@Override
	public <T> T getEntity(Class<T> Tclass, Object Id) {
		return absEntityManager.find(Tclass, Id);
	}
}