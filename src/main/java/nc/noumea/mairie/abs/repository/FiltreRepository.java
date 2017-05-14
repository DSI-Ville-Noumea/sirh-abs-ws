package nc.noumea.mairie.abs.repository;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import nc.noumea.mairie.abs.domain.RefEtat;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefGroupeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAccidentTravail;
import nc.noumea.mairie.abs.domain.RefTypeMaladiePro;
import nc.noumea.mairie.abs.domain.RefTypeSaisi;
import nc.noumea.mairie.abs.domain.RefTypeSiegeLesion;
import nc.noumea.mairie.abs.domain.RefUnitePeriodeQuota;

import org.springframework.stereotype.Repository;

@Repository
public class FiltreRepository implements IFiltreRepository {

	@PersistenceContext(unitName = "absPersistenceUnit")
	private EntityManager absEntityManager;

	@Override
	public void persist(Object entity) {
		absEntityManager.persist(entity);
	}

	@Override
	public <T> T findObject(Class<T> T, Integer id) {
		return (T) absEntityManager.find(T, id);
	}

	@Override
	public void remove(Object entity) {
		absEntityManager.remove(entity);
	}
	
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
	public List<RefEtat> findRefEtatPlanning() {
		List<RefEtat> res = new ArrayList<RefEtat>();
		res.add(absEntityManager.find(RefEtat.class, (RefEtatEnum.PROVISOIRE.getCodeEtat())));
		res.add(absEntityManager.find(RefEtat.class, (RefEtatEnum.SAISIE.getCodeEtat())));
		res.add(absEntityManager.find(RefEtat.class, (RefEtatEnum.VISEE_FAVORABLE.getCodeEtat())));
		res.add(absEntityManager.find(RefEtat.class, (RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat())));
		res.add(absEntityManager.find(RefEtat.class, (RefEtatEnum.APPROUVEE.getCodeEtat())));
		res.add(absEntityManager.find(RefEtat.class, (RefEtatEnum.VALIDEE.getCodeEtat())));
		res.add(absEntityManager.find(RefEtat.class, (RefEtatEnum.EN_ATTENTE.getCodeEtat())));
		res.add(absEntityManager.find(RefEtat.class, (RefEtatEnum.A_VALIDER.getCodeEtat())));
		res.add(absEntityManager.find(RefEtat.class, (RefEtatEnum.PRISE.getCodeEtat())));
		return res;
	}

	@Override
	public List<RefEtat> findAllRefEtats() {

		return absEntityManager.createQuery("SELECT o FROM RefEtat o", RefEtat.class).getResultList();
	}

	@Override
	public List<RefTypeAbsence> findAllRefTypeAbsences() {
		return absEntityManager.createQuery("SELECT o FROM RefTypeAbsence o where o.actif = true order by o.label ", RefTypeAbsence.class).getResultList();
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

	@Override
	public List<RefGroupeAbsence> findAllRefGroupeAbsence() {
		return absEntityManager.createNamedQuery("getAllRefGroupeAbsence", RefGroupeAbsence.class).getResultList();
	}

	@Override
	public RefGroupeAbsence findRefGroupeAbsence(Integer idRefGroupeAbsence) {

		TypedQuery<RefGroupeAbsence> query = null;
		query = absEntityManager.createNamedQuery("getRefGroupeAbsenceById", RefGroupeAbsence.class);
		query.setParameter("idRefGroupeAbsence", idRefGroupeAbsence);
		query.setMaxResults(1);

		return query.getSingleResult();
	}

	@Override
	public List<RefUnitePeriodeQuota> findAllRefUnitePeriodeQuota() {
		return absEntityManager.createNamedQuery("findAllRefUnitePeriodeQuota", RefUnitePeriodeQuota.class)
				.getResultList();
	}

	@Override
	public List<RefTypeAbsence> findAllRefTypeAbsencesWithGroup(Integer idRefGroupeAbsence) {
		String sql = "SELECT o FROM RefTypeAbsence o where o.groupe.idRefGroupeAbsence =:idRefGroupeAbsence and o.actif =true order by o.label ";

		TypedQuery<RefTypeAbsence> query = absEntityManager.createQuery(sql, RefTypeAbsence.class);
		query.setParameter("idRefGroupeAbsence", idRefGroupeAbsence);
		return query.getResultList();
	}

	@Override
	public List<RefEtat> findRefEtatAValider() {
		List<RefEtat> res = new ArrayList<RefEtat>();
		res.add(absEntityManager.find(RefEtat.class, RefEtatEnum.APPROUVEE.getCodeEtat()));
		res.add(absEntityManager.find(RefEtat.class, RefEtatEnum.EN_ATTENTE.getCodeEtat()));
		res.add(absEntityManager.find(RefEtat.class, RefEtatEnum.A_VALIDER.getCodeEtat()));
		return res;
	}

	@Override
	public RefTypeSiegeLesion findRefTypeSiegeLesion(Integer idRefSiegeLesion) {

		TypedQuery<RefTypeSiegeLesion> query = null;
		query = absEntityManager.createNamedQuery("getRefTypeSiegeLesionByIdType", RefTypeSiegeLesion.class);
		query.setParameter("idRefSiegeLesion", idRefSiegeLesion);
		query.setMaxResults(1);

		return query.getSingleResult();
	}

	@Override
	public List<RefTypeSiegeLesion> findAllRefTypeSiegeLesion() {
		return absEntityManager.createNamedQuery("getAllRefTypeSiegeLesion", RefTypeSiegeLesion.class)
				.getResultList();
	}

	@Override
	public RefTypeMaladiePro findRefTypeMaladiePro(Integer idRefMaladiePro) {

		TypedQuery<RefTypeMaladiePro> query = null;
		query = absEntityManager.createNamedQuery("getRefTypeMaladieProByIdType", RefTypeMaladiePro.class);
		query.setParameter("idRefMaladiePro", idRefMaladiePro);
		query.setMaxResults(1);

		return query.getSingleResult();
	}

	@Override
	public List<RefTypeMaladiePro> findAllRefTypeMaladiePro() {
		return absEntityManager.createNamedQuery("getAllRefTypeMaladiePro", RefTypeMaladiePro.class)
				.getResultList();
	}

	@Override
	public RefTypeAccidentTravail findRefTypeAccidentTravail(Integer idRefAccidentTravail) {

		TypedQuery<RefTypeAccidentTravail> query = null;
		query = absEntityManager.createNamedQuery("getRefTypeAccidentTravailByIdType", RefTypeAccidentTravail.class);
		query.setParameter("idRefAccidentTravail", idRefAccidentTravail);
		query.setMaxResults(1);

		return query.getSingleResult();
	}

	@Override
	public List<RefTypeAccidentTravail> findAllRefTypeAccidentTravail() {
		return absEntityManager.createNamedQuery("getAllRefTypeAccidentTravail", RefTypeAccidentTravail.class)
				.getResultList();
	}
}
