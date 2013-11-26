package nc.noumea.mairie.abs.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import nc.noumea.mairie.abs.domain.Droit;
import nc.noumea.mairie.abs.domain.DroitsAgent;
import nc.noumea.mairie.abs.domain.Profil;
import nc.noumea.mairie.abs.domain.ProfilEnum;

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
	public boolean isUserOperateur(Integer idAgent) {

		TypedQuery<Droit> q = absEntityManager.createNamedQuery("getDroitByProfilAndAgent", Droit.class);

		q.setParameter("idAgent", idAgent);
		q.setParameter("libelle", ProfilEnum.OPERATEUR.toString());

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
		q.setParameter("libelle", ProfilEnum.VISEUR.toString());

		Boolean result = null;
		if (null != q.getResultList() && 0 < q.getResultList().size()) {
			result = true;
		}

		return (result != null && result);
	}

	@Override
	public boolean isUserApprobateur(Integer idAgent) {

		TypedQuery<Droit> q = absEntityManager.createNamedQuery("getDroitByProfilAndAgent", Droit.class);

		q.setParameter("idAgent", idAgent);
		q.setParameter("libelle", ProfilEnum.APPROBATEUR.toString());

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
		q.setParameter("libelle", ProfilEnum.DELEGATAIRE.toString());

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
	public List<Droit> getDroitSousApprobateur(Integer idAgentApprobateur) {
		TypedQuery<Droit> q = absEntityManager
				.createQuery(
						"from Droit d LEFT JOIN FETCH d.droitProfils dp where dp.droitApprobateur.idAgent = :idAgent and dp.droitApprobateur.idAgent != dp.droit.idAgent ",
						Droit.class);
		q.setParameter("idAgent", idAgentApprobateur);

		List<Droit> r = q.getResultList();

		if (r.size() == 0)
			return null;

		return r;
	}
	
	@Override
	public void deleteDroitProfilByIdDroitAndIdProfil(Integer idDroitProfil) {
		
		Query q = absEntityManager.createQuery("delete from DroitProfil where idDroitProfil = :idDroitProfil ");
		q.setParameter("idDroitProfil", idDroitProfil);
		
		q.executeUpdate();
		absEntityManager.flush();
	}
	
	@Override
	public void removeEntity(Object obj) {
		absEntityManager.remove(obj);
	}

	@Override
	public List<DroitsAgent> getListOfAgentsToInputOrApprove(Integer idAgent, String codeService) {

		TypedQuery<DroitsAgent> q = absEntityManager.createNamedQuery(
				codeService == null ? "getListOfAgentsToInputOrApprove" : "getListOfAgentsToInputOrApproveByService",
				DroitsAgent.class);

		q.setParameter("idAgent", idAgent);

		if (codeService != null)
			q.setParameter("codeService", codeService);

		return q.getResultList();
	}

}
