package nc.noumea.mairie.abs.repository;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import nc.noumea.mairie.abs.domain.Droit;
import nc.noumea.mairie.abs.domain.DroitProfil;
import nc.noumea.mairie.abs.domain.DroitsAgent;
import nc.noumea.mairie.abs.domain.Profil;
import nc.noumea.mairie.abs.domain.ProfilEnum;

import org.springframework.stereotype.Repository;

@Repository
public class AccessRightsRepository implements IAccessRightsRepository {

	@PersistenceContext(unitName = "absPersistenceUnit")
	private EntityManager absEntityManager;

	@Override
	public void clear() {
		absEntityManager.clear();
	}

	@Override
	public Droit getAgentAccessRights(Integer idAgent) throws NoResultException {

		TypedQuery<Droit> q = absEntityManager.createNamedQuery("getAgentAccessRights", Droit.class);
		q.setParameter("idAgent", idAgent);
		List<Droit> ds = q.getResultList();

		return ds.size() == 0 ? null : ds.get(0);
	}

	@Override
	public List<Droit> getAgentsApprobateurs() {

		TypedQuery<Droit> q = absEntityManager.createNamedQuery("getAgentsApprobateurs", Droit.class);

		return q.getResultList();
	}

	@Override
	public Droit getDroitByProfilAndAgent(String profil, Integer idAgent) {

		TypedQuery<Droit> q = absEntityManager.createNamedQuery("getDroitByProfilAndAgent", Droit.class);

		q.setParameter("idAgent", idAgent);
		q.setParameter("libelle", profil);
		List<Droit> list = q.getResultList();
		if (null == list || list.size() == 0) {
			return null;
		} else {
			return list.get(0);
		}

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
		TypedQuery<Profil> q = absEntityManager.createQuery("from Profil p where p.libelle = :profilName", Profil.class);
		q.setParameter("profilName", profilName);

		Profil p = q.getSingleResult();

		return p;
	}

	@Override
	public List<Droit> getDroitSousApprobateur(Integer idAgentApprobateur) {
		TypedQuery<Droit> q = absEntityManager.createQuery(
				"select dp.droit from DroitProfil dp where dp.droitApprobateur.idDroit in (select idDroit from Droit where idAgent = :idAgentApprobateur) and dp.droit.idAgent != :idAgentApprobateur",
				Droit.class);
		q.setParameter("idAgentApprobateur", idAgentApprobateur);

		List<Droit> listeFinale = q.getResultList();

		return listeFinale;
	}

	@Override
	public void removeEntity(Object obj) {
		absEntityManager.remove(obj);
	}

	@Override
	public List<DroitsAgent> getListOfAgentsToInputOrApprove(Integer idAgent) {

		TypedQuery<DroitsAgent> q = absEntityManager.createNamedQuery("getListOfAgentsToInputOrApproveWithoutProfil", DroitsAgent.class);

		q.setParameter("idAgent", idAgent);

		return q.getResultList();
	}

	@Override
	public List<DroitsAgent> getListOfAgentsForListDemandes(List<Integer> idAgent) {

		TypedQuery<DroitsAgent> q = absEntityManager.createNamedQuery("getListOfAgentsToInputOrApproveWithProfil", DroitsAgent.class);

		q.setParameter("idAgent", idAgent);

		return q.getResultList();
	}

	@Override
	public List<DroitsAgent> getListOfAgentsToInputOrApprove(Integer idAgent, Integer idDroitProfil) {

		TypedQuery<DroitsAgent> q = absEntityManager.createNamedQuery("getListOfAgentsToInputOrApprove", DroitsAgent.class);

		q.setParameter("idAgent", idAgent);
		q.setParameter("idDroitProfil", idDroitProfil);

		return q.getResultList();
	}

	@Override
	public Droit getAgentDroitFetchAgents(Integer idAgent) {

		TypedQuery<Droit> q = absEntityManager.createQuery("from Droit d LEFT JOIN FETCH d.droitDroitsAgent dda where d.idAgent = :idAgent ", Droit.class);
		q.setParameter("idAgent", idAgent);

		List<Droit> r = q.getResultList();

		if (r.size() == 0)
			return null;

		return r.get(0);
	}

	@Override
	public List<DroitProfil> getDroitProfilByAgent(Integer idAgentApprobateur, Integer idAgent) {

		TypedQuery<DroitProfil> q = absEntityManager.createNamedQuery("getDroitProfilByAgent", DroitProfil.class);

		q.setParameter("idAgentApprobateur", idAgentApprobateur);
		q.setParameter("idAgent", idAgent);

		return q.getResultList();
	}

	@Override
	public boolean isUserDelegataireOfApprobateur(Integer idAgentApprobateur, Integer idAgent) {
		TypedQuery<DroitProfil> q = absEntityManager.createNamedQuery("getInputterDroitProfilOfApprobateurByLibelle", DroitProfil.class);

		q.setParameter("idAgent", idAgent);
		q.setParameter("idAgentApprobateur", idAgentApprobateur);
		q.setParameter("libelle", ProfilEnum.DELEGATAIRE.toString());

		try {
			q.getSingleResult();
			return true;
		} catch (Exception e) {
			return false;
		}

	}

	@Override
	public boolean isUserOperateurOfApprobateur(Integer idAgentApprobateur, Integer idAgent) {

		TypedQuery<DroitProfil> q = absEntityManager.createNamedQuery("getInputterDroitProfilOfApprobateurByLibelle", DroitProfil.class);

		q.setParameter("idAgent", idAgent);
		q.setParameter("idAgentApprobateur", idAgentApprobateur);
		q.setParameter("libelle", ProfilEnum.OPERATEUR.toString());

		try {
			q.getSingleResult();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public boolean isUserViseurOfApprobateur(Integer idAgentApprobateur, Integer idAgent) {

		TypedQuery<DroitProfil> q = absEntityManager.createNamedQuery("getInputterDroitProfilOfApprobateurByLibelle", DroitProfil.class);

		q.setParameter("idAgent", idAgent);
		q.setParameter("idAgentApprobateur", idAgentApprobateur);
		q.setParameter("libelle", ProfilEnum.VISEUR.toString());

		try {
			q.getSingleResult();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public DroitProfil getUserOperateurOfApprobateur(Integer idAgentApprobateur, Integer idAgent) {

		TypedQuery<DroitProfil> q = absEntityManager.createNamedQuery("getInputterDroitProfilOfApprobateurByLibelle", DroitProfil.class);

		q.setParameter("idAgent", idAgent);
		q.setParameter("idAgentApprobateur", idAgentApprobateur);
		q.setParameter("libelle", ProfilEnum.OPERATEUR.toString());

		try {
			return q.getSingleResult();
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public DroitProfil getUserViseurOfApprobateur(Integer idAgentApprobateur, Integer idAgent) {

		TypedQuery<DroitProfil> q = absEntityManager.createNamedQuery("getInputterDroitProfilOfApprobateurByLibelle", DroitProfil.class);

		q.setParameter("idAgent", idAgent);
		q.setParameter("idAgentApprobateur", idAgentApprobateur);
		q.setParameter("libelle", ProfilEnum.VISEUR.toString());

		try {
			return q.getSingleResult();
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public DroitsAgent getDroitsAgent(Integer idAgent) {

		TypedQuery<DroitsAgent> q = absEntityManager.createNamedQuery("getDroitsAgent", DroitsAgent.class);
		q.setParameter("idAgent", idAgent);
		List<DroitsAgent> ds = q.getResultList();

		return ds.size() == 0 ? null : ds.get(0);
	}

	@Override
	public DroitProfil getDroitProfilApprobateur(Integer idAgentApprobateur) {

		TypedQuery<DroitProfil> q = absEntityManager.createNamedQuery("getDroitProfilApprobateur", DroitProfil.class);
		q.setParameter("idAgentApprobateur", idAgentApprobateur);

		List<DroitProfil> r = q.getResultList();

		if (r.size() == 0)
			return null;

		return r.get(0);
	}

	@Override
	public Droit getApprobateurOfAgent(DroitsAgent droitAgent) {
		StringBuilder sb = new StringBuilder();
		sb.append("select d.* from abs_droits_agent da ");
		sb.append("inner join abs_droit_droits_agent dda on da.id_droits_agent=dda.id_droits_agent ");
		sb.append("inner join abs_droit_profil dp on dda.id_droit_profil = dp.id_droit_profil ");
		sb.append("inner join abs_profil p on dp.id_profil = p.id_profil ");
		sb.append("inner join abs_droit d on d.id_droit = dp.id_droit_approbateur ");
		sb.append("where da.id_droits_agent = :idDroitAgent ");
		sb.append("and p.libelle = :libelle ");

		Query q = absEntityManager.createNativeQuery(sb.toString(), Droit.class);
		q.setParameter("idDroitAgent", droitAgent.getIdDroitsAgent());
		q.setParameter("libelle", ProfilEnum.APPROBATEUR.toString());

		@SuppressWarnings("unchecked")
		List<Droit> listeFinale = q.getResultList();

		return listeFinale.size() == 0 ? null : listeFinale.get(0);
	}

	@Override
	public boolean isViseurOfAgent(Integer idAgentViseur, Integer IdAgent) {
		StringBuilder sb = new StringBuilder();
		sb.append("select d.* from abs_droits_agent da ");
		sb.append("inner join abs_droit_droits_agent dda on da.id_droits_agent=dda.id_droits_agent ");
		sb.append("inner join abs_droit_profil dp on dda.id_droit_profil = dp.id_droit_profil ");
		sb.append("inner join abs_profil p on dp.id_profil = p.id_profil ");
		sb.append("inner join abs_droit d on d.id_droit = dp.id_droit ");
		sb.append("where da.id_agent = :idAgent ");
		sb.append("and d.id_agent = :idAgentViseur ");
		sb.append("and p.libelle = :libelle ");

		Query q = absEntityManager.createNativeQuery(sb.toString(), Droit.class);
		q.setParameter("idAgent", IdAgent);
		q.setParameter("idAgentViseur", idAgentViseur);
		q.setParameter("libelle", ProfilEnum.VISEUR.toString());

		try {
			q.getSingleResult();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public boolean isOperateurOfAgent(Integer idAgentOperateur, Integer IdAgent) {
		StringBuilder sb = new StringBuilder();
		sb.append("select d.* from abs_droits_agent da ");
		sb.append("inner join abs_droit_droits_agent dda on da.id_droits_agent=dda.id_droits_agent ");
		sb.append("inner join abs_droit_profil dp on dda.id_droit_profil = dp.id_droit_profil ");
		sb.append("inner join abs_profil p on dp.id_profil = p.id_profil ");
		sb.append("inner join abs_droit d on d.id_droit = dp.id_droit ");
		sb.append("where da.id_agent = :idAgent ");
		sb.append("and d.id_agent = :idAgentViseur ");
		sb.append("and p.libelle = :libelle ");

		Query q = absEntityManager.createNativeQuery(sb.toString(), Droit.class);
		q.setParameter("idAgent", IdAgent);
		q.setParameter("idAgentViseur", idAgentOperateur);
		q.setParameter("libelle", ProfilEnum.OPERATEUR.toString());

		try {
			q.getSingleResult();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public boolean isApprobateurOrDelegataireOfAgent(Integer idAgentApprobateurOrDelegataire, Integer IdAgent) {
		StringBuilder sb = new StringBuilder();

		sb.append("select d.* from abs_droits_agent da  ");
		sb.append("inner join abs_droit_droits_agent dda on da.id_droits_agent=dda.id_droits_agent   ");
		sb.append("inner join abs_droit_profil dp on dda.id_droit_profil = dp.id_droit_profil   ");
		sb.append("inner join abs_profil p on dp.id_profil = p.id_profil   ");
		sb.append("inner join abs_droit d on d.id_droit = dp.id_droit   ");
		sb.append("where da.id_agent = :idAgent  ");
		sb.append("and p.libelle = :approbateur ");
		sb.append("and ( d.id_agent = :idAgentApprobateurOrDelegataire  ");
		sb.append("or d.id_droit in ( ");
		sb.append("select dp2.id_droit_approbateur from abs_droit d2  ");
		sb.append("inner join abs_droit_profil dp2 on d2.id_droit = dp2.id_droit  ");
		sb.append("inner join abs_profil p2 on dp2.id_profil = p2.id_profil  ");
		sb.append("where d2.id_agent = :idAgentApprobateurOrDelegataire ");
		sb.append("and p2.libelle = :delegataire ) )  ");

		Query q = absEntityManager.createNativeQuery(sb.toString(), Droit.class);
		q.setParameter("idAgent", IdAgent);
		q.setParameter("idAgentApprobateurOrDelegataire", idAgentApprobateurOrDelegataire);
		q.setParameter("approbateur", ProfilEnum.APPROBATEUR.toString());
		q.setParameter("delegataire", ProfilEnum.DELEGATAIRE.toString());

		try {
			q.getSingleResult();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public List<DroitProfil> getDroitProfilByAgentAndLibelle(Integer idAgent, String libelleProfil) {

		TypedQuery<DroitProfil> q = absEntityManager.createNamedQuery("getDroitProfilByLibelleProfil", DroitProfil.class);

		q.setParameter("idAgent", idAgent);
		q.setParameter("libelle", libelleProfil);

		try {
			return q.getResultList();
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public List<DroitsAgent> getListeActeursOfAgent(Integer idAgent) {

		TypedQuery<DroitsAgent> q = absEntityManager.createQuery("from DroitsAgent da LEFT JOIN FETCH da.droitDroitsAgent dda " + "LEFT JOIN FETCH dda.droitProfil dp "
				+ "LEFT JOIN FETCH dp.profil p " + "LEFT JOIN FETCH dp.droit d " + "where da.idAgent = :idAgent ", DroitsAgent.class);
		q.setParameter("idAgent", idAgent);

		return q.getResultList();
	}

	@Override
	public List<DroitsAgent> getListDroitsAgent(List<Integer> listIdAgent) {
		if (listIdAgent == null || listIdAgent.size() == 0) {
			return new ArrayList<DroitsAgent>();
		} else {
			TypedQuery<DroitsAgent> q = absEntityManager.createNamedQuery("getListDroitsAgent", DroitsAgent.class);
			q.setParameter("listIdAgent", listIdAgent);

			return q.getResultList();
		}
	}
}
