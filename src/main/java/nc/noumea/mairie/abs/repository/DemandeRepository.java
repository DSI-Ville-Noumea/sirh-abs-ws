package nc.noumea.mairie.abs.repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.ProfilEnum;
import nc.noumea.mairie.abs.domain.RefEtat;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;

import org.springframework.stereotype.Repository;

@Repository
public class DemandeRepository implements IDemandeRepository {

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
	public void removeEntity(Object obj) {
		absEntityManager.remove(obj);
	}
	
	@Override
	public void clear() {
		absEntityManager.clear();
	}
	
	@Override
	public void flush() {
		absEntityManager.flush();
	}
	
	@Override
	public void setFlushMode(FlushModeType flushMode) {
		absEntityManager.setFlushMode(flushMode);
	}
	
	@Override
	public List<Demande> listeDemandesAgent(Integer idAgentConnecte, Integer idAgentConcerne, Date fromDate, Date toDate, Integer idRefType) {
		StringBuilder sb = new StringBuilder();
		sb.append("select d from Demande d ");
		sb.append("where 1=1 ");

		if (idAgentConcerne != null) {
			sb.append("and d.idAgent = :idAgentConcerne ");
		}else {
			sb.append("and d.idAgent in ( select da.idAgent from DroitsAgent da inner join da.droitDroitsAgent dda inner join dda.droit d where d.idAgent = :idAgentConnecte ) ");
		}

		if (idRefType != null) {
			sb.append("and d.type.idRefTypeAbsence = :idRefTypeAbsence ");
		}

		if (fromDate != null && toDate == null) {
			sb.append("and d.dateDebut = :fromDate ");
		} else if (fromDate == null && toDate != null) {
			sb.append("and d.dateDebut = :toDate ");
		} else if (fromDate != null && toDate != null) {
			sb.append("and d.dateDebut >= :fromDate and d.dateDebut <= :toDate ");
		}

		sb.append("order by d.idDemande desc ");

		TypedQuery<Demande> query = absEntityManager.createQuery(sb.toString(), Demande.class);

		if (idAgentConcerne != null) {
			query.setParameter("idAgentConcerne", idAgentConcerne);
		}else{
			query.setParameter("idAgentConnecte", idAgentConnecte);
		}

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
		return res;
	}
	
	@Override
	public List<RefEtat> findAllRefEtats() {
		
	    return absEntityManager.createQuery("SELECT o FROM RefEtat o", RefEtat.class).getResultList();
	}
	
	@Override
	public List<Integer> getListViseursDemandesSaisiesJourDonne(Integer type) {
		
		StringBuilder sb = new StringBuilder();
			sb.append("select droit.id_agent as idAgent from abs_droit droit ");
			sb.append("inner join abs_droit_profil dp on droit.id_droit = dp.id_droit ");
			sb.append("inner join abs_profil p on dp.id_profil = p.id_profil ");
			sb.append("inner join abs_droit_droits_agent dda on dp.id_droit_profil = dda.id_droit_profil ");
			sb.append("inner join abs_droits_agent da on dda.id_droits_agent = da.id_droits_agent ");
			sb.append("where p.libelle = :LIBELLE ");
			sb.append("and da.id_agent in ( ");
					sb.append("select d.id_agent from abs_demande d ");
					sb.append("inner join abs_etat_demande ed on d.id_demande = ed.id_demande ");
					sb.append("where d.id_type_demande = :TYPE ");
					sb.append("and ed.id_ref_etat = :SAISIE ");
					sb.append("and date_trunc('day', ed.date) = current_date - interval '1 day' ");
					sb.append("and ed.id_etat_demande in ( select max(ed2.id_etat_demande) from abs_etat_demande ed2 group by ed2.id_demande ) ");
			sb.append(" ) GROUP BY idAgent ");
		
		@SuppressWarnings("unchecked")
		List<Integer> result = absEntityManager.createNativeQuery(sb.toString())
			.setParameter("LIBELLE", ProfilEnum.VISEUR.toString())
			.setParameter("TYPE", type)
			.setParameter("SAISIE", RefEtatEnum.SAISIE.getCodeEtat())
			.getResultList();
		
		return result;
	}
	
	@Override
	public List<Integer> getListApprobateursDemandesSaisiesViseesJourDonne(Integer type) {
		
		StringBuilder sb = new StringBuilder();
			sb.append("select droit.id_agent as idAgent from abs_droit droit ");
			sb.append("inner join abs_droit_profil dp on droit.id_droit = dp.id_droit ");
			sb.append("inner join abs_profil p on dp.id_profil = p.id_profil ");
			sb.append("inner join abs_droit_droits_agent dda on dp.id_droit_profil = dda.id_droit_profil ");
			sb.append("inner join abs_droits_agent da on dda.id_droits_agent = da.id_droits_agent ");
			sb.append("where p.libelle = :LIBELLE ");
			sb.append("and da.id_agent in ( ");
					sb.append("select d.id_agent from abs_demande d ");
					sb.append("inner join abs_etat_demande ed on d.id_demande = ed.id_demande ");
					sb.append("where d.id_type_demande = :TYPE ");
					sb.append("and ed.id_ref_etat in( :SAISIE , :VISEE_F , :VISEE_D ) ");
					sb.append("and date_trunc('day', ed.date) = current_date - interval '1 day' ");
					sb.append("and ed.id_etat_demande in ( select max(ed2.id_etat_demande) from abs_etat_demande ed2 group by ed2.id_demande ) ");
			sb.append(" ) GROUP BY idAgent ");
		
		@SuppressWarnings("unchecked")
		List<Integer> result = absEntityManager.createNativeQuery(sb.toString())
			.setParameter("LIBELLE", ProfilEnum.APPROBATEUR.toString())
			.setParameter("TYPE", type)
			.setParameter("SAISIE", RefEtatEnum.SAISIE.getCodeEtat())
			.setParameter("VISEE_F", RefEtatEnum.VISEE_FAVORABLE.getCodeEtat())
			.setParameter("VISEE_D", RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat())
			.getResultList();
		
		return result;
	}
	
	
	@Override
	public List<RefTypeAbsence> findAllRefTypeAbsences() {
		
	    return absEntityManager.createQuery("SELECT o FROM RefTypeAbsence o", RefTypeAbsence.class).getResultList();
	}
}
