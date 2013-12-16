package nc.noumea.mairie.abs.repository;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.EtatDemande;

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
    public EtatDemande getLastEtatDemandeByIdDemande(Integer idDemande){
		
		TypedQuery<EtatDemande> q = absEntityManager.createQuery("select ed from EtatDemande ed inner join ed.demande d where d.idDemande = :idDemande "
				+ "and ed.idEtatDemande in ( select max(ed2.idEtatDemande) from EtatDemande ed2 inner join ed2.demande d2 where d2.idDemande = :idDemande ) ", 
				EtatDemande.class);
		
		q.setParameter("idDemande", idDemande);

		List<EtatDemande> r = q.getResultList();

		if (r.size() == 0)
			return null;

		return r.get(0);
	}

	@Override
	public List<Demande> listeDemandesAgentNonPrises(Integer idAgentConnecte, Date fromDate, Date toDate,
			Date dateDemande, Integer idRefEtat, Integer idRefType) {

		StringBuilder sb = new StringBuilder();
		sb.append("select d from Demande d ");
		//sb.append("where ptg.dateDebut >= :fromDate and ptg.dateDebut < :toDate ");

		if (idRefType != null) {
			sb.append("and d.type.idRefTypeAbsence = :idRefTypeAbsence ");
		}

		sb.append("order by d.idDemande desc ");

		TypedQuery<Demande> query = absEntityManager.createQuery(sb.toString(), Demande.class);
		//query.setParameter("fromDate", fromDate);
		//query.setParameter("toDate", toDate);

		if (idRefType != null) {
			query.setParameter("idRefTypeAbsence", idRefType);
		}

		return query.getResultList();
	}
}
