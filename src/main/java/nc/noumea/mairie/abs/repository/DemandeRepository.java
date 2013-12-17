package nc.noumea.mairie.abs.repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.EtatDemande;
import nc.noumea.mairie.abs.domain.RefEtat;
import nc.noumea.mairie.abs.domain.RefEtatEnum;

import org.springframework.stereotype.Repository;

@Repository
public class DemandeRepository implements IDemandeRepository {

	@PersistenceContext(unitName = "absPersistenceUnit")
	private EntityManager absEntityManager;

	@Override
	public void persisEntity(Object obj) {
		absEntityManager.persist(obj);
	}

	@Override
	public <T> T getEntity(Class<T> Tclass, Object Id) {
		return absEntityManager.find(Tclass, Id);
	}

	@Override
	public EtatDemande getLastEtatDemandeByIdDemande(Integer idDemande) {

		TypedQuery<EtatDemande> q = absEntityManager
				.createQuery(
						"select ed from EtatDemande ed inner join ed.demande d where d.idDemande = :idDemande "
								+ "and ed.idEtatDemande in ( select max(ed2.idEtatDemande) from EtatDemande ed2 inner join ed2.demande d2 where d2.idDemande = :idDemande ) ",
						EtatDemande.class);

		q.setParameter("idDemande", idDemande);

		List<EtatDemande> r = q.getResultList();

		if (r.size() == 0)
			return null;

		return r.get(0);
	}

	@Override
	public List<Demande> listeDemandesAgent(Integer idAgentConnecte, Date fromDate, Date toDate, Integer idRefType) {
		StringBuilder sb = new StringBuilder();
		sb.append("select d from Demande d ");
		sb.append("where d.idAgent = :idAgent ");

		if (idRefType != null) {
			sb.append("and d.type.idRefTypeAbsence = :idRefTypeAbsence ");
		}

		if (fromDate != null && toDate == null) {
			sb.append("and d.dateDebut = :fromDate ");
		} else if (fromDate == null && toDate != null) {
			sb.append("and d.dateDebut = :toDate ");
		} else if (fromDate != null && toDate != null) {
			sb.append("and d.dateDebut >= :fromDate and d.dateDebut < :toDate ");
		}

		sb.append("order by d.idDemande desc ");

		TypedQuery<Demande> query = absEntityManager.createQuery(sb.toString(), Demande.class);
		query.setParameter("idAgent", idAgentConnecte);

		if (idRefType != null) {
			query.setParameter("idRefTypeAbsence", idRefType);
		}

		if (fromDate != null) {
		}

		if (fromDate != null && toDate == null) {
			query.setParameter("fromDate", fromDate);
		} else if (fromDate == null && toDate != null) {
			query.setParameter("toDate", toDate);
		} else if (fromDate != null && toDate != null) {
			query.setParameter("fromDate", fromDate);
			query.setParameter("toDate", toDate);
		}

		return query.getResultList();
	}

	@Override
	public List<RefEtat> findRefEtatNonPris() {
		List<RefEtat> res = new ArrayList<RefEtat>();
		res = RefEtat.findAllRefEtats();
		RefEtat etatPris = RefEtat.findRefEtat(RefEtatEnum.PRISE.getCodeEtat());
		res.remove(etatPris);
		return res;
	}

	@Override
	public List<RefEtat> findRefEtatEnCours() {
		List<RefEtat> res = new ArrayList<RefEtat>();
		res.add(RefEtat.findRefEtat(RefEtatEnum.SAISIE.getCodeEtat()));
		res.add(RefEtat.findRefEtat(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat()));
		res.add(RefEtat.findRefEtat(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat()));
		res.add(RefEtat.findRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat()));
		return res;
	}
}
