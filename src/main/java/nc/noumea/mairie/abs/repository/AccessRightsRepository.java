package nc.noumea.mairie.abs.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import nc.noumea.mairie.abs.domain.Droit;
import nc.noumea.mairie.abs.domain.Profil;

import org.springframework.stereotype.Repository;

@Repository
public class AccessRightsRepository implements IAccessRightsRepository {

	@PersistenceContext(unitName = "absPersistenceUnit")
	private EntityManager absEntityManager;

	@Override
	public Droit getAgentAccessRights(int idAgent) throws NoResultException {

		TypedQuery<Droit> q = absEntityManager.createNamedQuery("getAgentAccessRights", Droit.class);
		q.setParameter("idAgent", idAgent);

		return q.getSingleResult();
	}

	@Override
	public List<Droit> getAgentsApprobateurs() {

		TypedQuery<Droit> q = absEntityManager.createNamedQuery("getAgentsApprobateurs", Droit.class);

		return q.getResultList();
	}

	@Override
	public boolean isUserOperator(Integer idAgent) {

		TypedQuery<Droit> q = absEntityManager.createNamedQuery("getDroitByProfilAndAgent", Droit.class);

		q.setParameter("idAgent", idAgent);
		q.setParameter("libelle", "OPERATEUR");

		Boolean result = null;
		if (null != q.getResultList() && 0 < q.getResultList().size()) {
			result = true;
		}

		return (result != null && result);
	}

	@Override
	public void persisEntity(Object obj) {
		absEntityManager.persist(obj);
	}

	@Override
	public boolean isUserViseur(Integer idAgent) {

		TypedQuery<Droit> q = absEntityManager.createNamedQuery("getDroitByProfilAndAgent", Droit.class);

		q.setParameter("idAgent", idAgent);
		q.setParameter("libelle", "VISEUR");

		Boolean result = null;
		if (null != q.getResultList() && 0 < q.getResultList().size()) {
			result = true;
		}

		return (result != null && result);
	}

	@Override
	public Profil getProfilByName(String profilName) {
		TypedQuery<Profil> q = absEntityManager
				.createQuery("from Profil p where p.libelle = :profilName", Profil.class);
		q.setParameter("profilName", profilName);

		Profil p = q.getSingleResult();

		return p;
	}

	@Override
	public boolean isUserApprobator(Integer idAgent) {

		TypedQuery<Droit> q = absEntityManager.createNamedQuery("getDroitByProfilAndAgent", Droit.class);

		q.setParameter("idAgent", idAgent);
		q.setParameter("libelle", "APPROBATEUR");

		Boolean result = null;
		if (null != q.getResultList() && 0 < q.getResultList().size()) {
			result = true;
		}

		return (result != null && result);
	}

	@Override
	public boolean isUserDelegataire(Integer idAgent) {

		TypedQuery<Droit> q = absEntityManager.createNamedQuery("getDroitByProfilAndAgent", Droit.class);

		q.setParameter("idAgent", idAgent);
		q.setParameter("libelle", "DELEGATAIRE");

		Boolean result = null;
		if (null != q.getResultList() && 0 < q.getResultList().size()) {
			result = true;
		}

		return (result != null && result);
	}

	@Override
	public List<Droit> getDroitSousApprobateur(Integer idAgentApprobateur) {
		TypedQuery<Droit> q = absEntityManager
				.createQuery(
						"from Droit d LEFT JOIN FETCH d.droitProfils dp LEFT JOIN FETCH dp.profil p where dp.droitApprobateur.idAgent = :idAgent",
						Droit.class);
		q.setParameter("idAgent", idAgentApprobateur);

		List<Droit> r = q.getResultList();

		if (r.size() == 0)
			return null;

		return r;
	}

}
