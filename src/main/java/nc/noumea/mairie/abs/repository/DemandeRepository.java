package nc.noumea.mairie.abs.repository;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.hibernate.envers.configuration.internal.metadata.reader.AuditedPropertiesHolder;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeCongesAnnuels;
import nc.noumea.mairie.abs.domain.ProfilEnum;
import nc.noumea.mairie.abs.domain.RefEtat;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.domain.RefTypeGroupeAbsenceEnum;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.enums.AuthorizedMaladiesForProlongationEnum;
import nc.noumea.mairie.abs.service.impl.AbsenceService;

@Repository
public class DemandeRepository implements IDemandeRepository {
	
	private Logger logger =  LoggerFactory.getLogger(AbsenceService.class);

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
	public List<Demande> listeDemandesAgent(Integer idAgentConnecte, Integer idAgentConcerne, Date fromDate,
			Date toDate, Integer idRefType, Integer idRefGroupeAbsence) {
		StringBuilder sb = new StringBuilder();
		sb.append("select d from Demande d inner join fetch d.etatsDemande ed ");
		sb.append("where 1=1 ");

		if (idAgentConcerne != null) {
			sb.append("and d.idAgent = :idAgentConcerne ");
		} else {
			sb.append("and d.idAgent in ( select da.idAgent from DroitsAgent da inner join da.droitDroitsAgent dda inner join dda.droit d where d.idAgent = :idAgentConnecte ) ");
		}

		if (idRefType != null) {
			sb.append("and d.type.idRefTypeAbsence = :idRefTypeAbsence ");
		}

		if (idRefGroupeAbsence != null) {
			sb.append("and d.type.groupe.idRefGroupeAbsence = :idRefGroupeAbsence ");
		}

		if (fromDate != null && toDate == null) {
			sb.append("and d.dateDebut >= :fromDate ");
		} else if (fromDate == null && toDate != null) {
			sb.append("and d.dateDebut <= :toDate ");
		} else if (fromDate != null && toDate != null) {
			sb.append("and d.dateDebut >= :fromDate and d.dateDebut <= :toDate ");
		}

		sb.append("order by d.dateDebut desc "); 

		TypedQuery<Demande> query = absEntityManager.createQuery(sb.toString(), Demande.class);

		if (idAgentConcerne != null) {
			query.setParameter("idAgentConcerne", idAgentConcerne);
		} else {
			query.setParameter("idAgentConnecte", idAgentConnecte);
		}

		if (idRefType != null) {
			query.setParameter("idRefTypeAbsence", idRefType);
		}

		if (idRefGroupeAbsence != null) {
			query.setParameter("idRefGroupeAbsence", idRefGroupeAbsence);
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

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> listeIdsDemandesForListAgent(Integer idAgentConnecte, List<Integer> idAgentConcerne, Date fromDate,
			Date toDate, Integer idRefType, Integer idRefGroupeAbsence, List<RefEtat> listEtats, Integer limitResultMax) {
		
		StringBuilder sb = new StringBuilder();
		sb.append(" select distinct(d.id_demande) from abs_demande d ");
		sb.append(" inner join abs_etat_demande ed on d.id_demande=ed.id_demande ");
		sb.append(" where 1=1 ");

		if (idAgentConcerne != null) {
			sb.append("and d.id_agent in ( :idAgentConcerne ) ");
		} else {
			sb.append("and d.id_agent in ( ");
				sb.append("select da.id_agent ");
				sb.append("from abs_droits_agent da ");
				sb.append("inner join abs_droit_droits_agent dda on da.id_droits_agent=dda.id_droits_agent ");
				sb.append("inner join abs_droit d on dda.id_droit=d.id_droit ");
				sb.append("where d.id_agent = :idAgentConnecte ");
			sb.append(") ");
		}
		
		if(null != listEtats && !listEtats.isEmpty()) {
			sb.append("and ed.id_etat_demande in ( ");
			sb.append("select max(ed2.id_etat_demande) from abs_etat_demande ed2 ");
			sb.append(" inner join abs_demande d2 on ed2.id_demande=d2.id_demande ");
					
			if (idAgentConcerne != null) {
				sb.append("where d2.id_agent in ( :idAgentConcerne ) ");
			} else {
				sb.append("where d2.id_agent in ( ");
					sb.append("select da2.id_agent ");
					sb.append("from abs_droits_agent da2 ");
					sb.append("inner join abs_droit_droits_agent dda2 on da2.id_droits_agent=dda2.id_droits_agent ");
					sb.append("inner join abs_droit d2 on dda2.id_droit=d2.id_droit ");
					sb.append("where d2.id_agent = :idAgentConnecte ");
				sb.append(") ");
			}
					
			sb.append("group by ed2.id_demande ) ");
			sb.append(" and ed.id_ref_etat in :listRefEtat ");
		}

		if (idRefType != null) {
			sb.append("and d.id_type_demande = :idRefTypeAbsence ");
		}

		if (idRefGroupeAbsence != null) {
			sb.append("and d.id_type_demande in ( select id_ref_type_absence from abs_ref_type_absence where id_ref_groupe_absence = :idRefGroupeAbsence ) ");
		}

		if (fromDate != null && toDate == null) {
			sb.append("and d.date_fin >= :fromDate ");
		} else if (fromDate == null && toDate != null) {
			sb.append("and d.date_debut <= :toDate ");
		} else if (fromDate != null && toDate != null) {
			sb.append("and d.date_fin >= :fromDate and d.date_debut <= :toDate ");
		}

		Query query = absEntityManager.createNativeQuery(sb.toString());
		
		if(null != limitResultMax) {
			query.setFirstResult(0);
			query.setMaxResults(limitResultMax);
		}

		if (idAgentConcerne != null) {
			query.setParameter("idAgentConcerne", idAgentConcerne);
		} else {
			query.setParameter("idAgentConnecte", idAgentConnecte);
		}
		
		if(null != listEtats && !listEtats.isEmpty()) {
			List<Integer> listRefEtatEnum = new ArrayList<Integer>();
			for(RefEtat refEtat : listEtats) {
				listRefEtatEnum.add(refEtat.getIdRefEtat());
			}
			
			query.setParameter("listRefEtat", listRefEtatEnum);
		}

		if (idRefType != null) {
			query.setParameter("idRefTypeAbsence", idRefType);
		}

		if (idRefGroupeAbsence != null) {
			query.setParameter("idRefGroupeAbsence", idRefGroupeAbsence);
		}

		if (fromDate != null && toDate == null) {
			query.setParameter("fromDate", fromDate);
		} else if (fromDate == null && toDate != null) {
			query.setParameter("toDate", toDate);
		} else if (fromDate != null && toDate != null) {
			query.setParameter("fromDate", fromDate);
			query.setParameter("toDate", toDate);
		}

		return (List<Integer>) query.getResultList();
	}
	
	@Override
	public List<Demande> listeDemandesByListIdsDemande(List<Integer> listIdsDemande) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct d from Demande d inner join fetch d.etatsDemande ed ");
		sb.append("where d.idDemande in :listIdsDemande ");		
		sb.append("order by d.dateDebut desc ");

		TypedQuery<Demande> query = absEntityManager.createQuery(sb.toString(), Demande.class);

		query.setParameter("listIdsDemande", listIdsDemande);

		return query.getResultList();
	}

	@Override
	public int countListeDemandesForListAgent(Integer idAgentConnecte, List<Integer> idAgentConcerne, Date fromDate,
			Date toDate, Integer idRefType, Integer idRefGroupeAbsence, List<RefEtat> listEtats) {
		
		StringBuilder sb = new StringBuilder();
		sb.append(" select count(d.id_demande) from abs_demande d ");
		sb.append(" inner join abs_etat_demande ed on d.id_demande=ed.id_demande ");
		sb.append(" where 1=1 ");

		if (idAgentConcerne != null) {
			sb.append("and d.id_agent in ( :idAgentConcerne ) ");
		} else {
			sb.append("and d.id_agent in ( ");
				sb.append("select da.id_agent ");
				sb.append("from abs_droits_agent da ");
				sb.append("inner join abs_droit_droits_agent dda on da.id_droits_agent=dda.id_droits_agent ");
				sb.append("inner join abs_droit d on dda.id_droit=d.id_droit ");
				sb.append("where d.id_agent = :idAgentConnecte ");
			sb.append(") ");
		}
		
		if(null != listEtats && !listEtats.isEmpty()) {
			sb.append("and ed.id_etat_demande in ( ");
			sb.append("select max(ed2.id_etat_demande) from abs_etat_demande ed2 ");
			sb.append(" inner join abs_demande d2 on ed2.id_demande=d2.id_demande ");
					
			if (idAgentConcerne != null) {
				sb.append("where d2.id_agent in ( :idAgentConcerne ) ");
			} else {
				sb.append("where d2.id_agent in ( ");
					sb.append("select da2.id_agent ");
					sb.append("from abs_droits_agent da2 ");
					sb.append("inner join abs_droit_droits_agent dda2 on da2.id_droits_agent=dda2.id_droits_agent ");
					sb.append("inner join abs_droit d2 on dda2.id_droit=d2.id_droit ");
					sb.append("where d2.id_agent = :idAgentConnecte ");
				sb.append(") ");
			}
					
			sb.append("group by ed2.id_demande ) ");
			sb.append(" and ed.id_ref_etat in :listRefEtat ");
		}

		if (idRefType != null) {
			sb.append("and d.id_type_demande = :idRefTypeAbsence ");
		}

		if (idRefGroupeAbsence != null) {
			sb.append("and d.id_type_demande in ( select id_ref_type_absence from abs_ref_type_absence where id_ref_groupe_absence = :idRefGroupeAbsence ) ");
		}

		if (fromDate != null && toDate == null) {
			sb.append("and d.date_fin >= :fromDate ");
		} else if (fromDate == null && toDate != null) {
			sb.append("and d.date_debut <= :toDate ");
		} else if (fromDate != null && toDate != null) {
			sb.append("and d.date_fin >= :fromDate and d.date_debut <= :toDate ");
		}

		Query query = absEntityManager.createNativeQuery(sb.toString());

		if (idAgentConcerne != null) {
			query.setParameter("idAgentConcerne", idAgentConcerne);
		} else {
			query.setParameter("idAgentConnecte", idAgentConnecte);
		}
		
		if(null != listEtats && !listEtats.isEmpty()) {
			List<Integer> listRefEtatEnum = new ArrayList<Integer>();
			for(RefEtat refEtat : listEtats) {
				listRefEtatEnum.add(refEtat.getIdRefEtat());
			}
			
			query.setParameter("listRefEtat", listRefEtatEnum);
		}

		if (idRefType != null) {
			query.setParameter("idRefTypeAbsence", idRefType);
		}

		if (idRefGroupeAbsence != null) {
			query.setParameter("idRefGroupeAbsence", idRefGroupeAbsence);
		}

		if (fromDate != null && toDate == null) {
			query.setParameter("fromDate", fromDate);
		} else if (fromDate == null && toDate != null) {
			query.setParameter("toDate", toDate);
		} else if (fromDate != null && toDate != null) {
			query.setParameter("fromDate", fromDate);
			query.setParameter("toDate", toDate);
		}

		BigInteger nbResults = (BigInteger) query.getSingleResult();
		return nbResults.intValue();
	}

	@Override
	public List<Integer> getListViseursDemandesSaisiesJourDonne(List<Integer> listeTypesGroupe) {

		StringBuilder sb = new StringBuilder();
		sb.append("select distinct(droit.id_agent) as idAgent from abs_droit droit ");
		sb.append("inner join abs_droit_profil dp on droit.id_droit = dp.id_droit ");
		sb.append("inner join abs_profil p on dp.id_profil = p.id_profil ");
		sb.append("inner join abs_droit_droits_agent dda on dp.id_droit_profil = dda.id_droit_profil ");
		sb.append("inner join abs_droits_agent da on dda.id_droits_agent = da.id_droits_agent ");
		sb.append("where p.libelle = :LIBELLE ");
		sb.append("and da.id_agent in ( ");
		sb.append("select d.id_agent from abs_demande d ");
		sb.append("inner join abs_etat_demande ed on d.id_demande = ed.id_demande ");
		sb.append("inner join abs_ref_type_absence rta on d.id_type_demande = rta.id_ref_type_absence "
				+ "and rta.id_ref_groupe_absence in ( :TYPE ) ");
		sb.append("left outer join abs_ref_type_saisi rts on rta.id_ref_type_absence = rts.id_ref_type_absence "
				+ "and rts.saisie_kiosque is true ");
		sb.append("where ed.id_ref_etat = :SAISIE ");
		sb.append("and date_trunc('day', ed.date) <= (current_date - interval '1 day') ");
		sb.append("and ed.id_etat_demande in ( select max(ed2.id_etat_demande) from abs_etat_demande ed2 group by ed2.id_demande ) ");
		sb.append(" ) GROUP BY idAgent ");

		@SuppressWarnings("unchecked")
		List<Integer> result = absEntityManager.createNativeQuery(sb.toString())
				.setParameter("LIBELLE", ProfilEnum.VISEUR.toString()).setParameter("TYPE", listeTypesGroupe)
				.setParameter("SAISIE", RefEtatEnum.SAISIE.getCodeEtat()).getResultList();

		return result;
	}

	@Override
	public List<Integer> getListApprobateursForAgent(Integer idAgent) {

		StringBuilder sb = new StringBuilder();
		sb.append("select distinct(droit.id_agent) as idAgent from abs_droit droit ");
		sb.append("inner join abs_droit_profil dp on droit.id_droit = dp.id_droit ");
		sb.append("inner join abs_profil p on dp.id_profil = p.id_profil ");
		sb.append("inner join abs_droit_droits_agent dda on dp.id_droit_profil = dda.id_droit_profil ");
		sb.append("inner join abs_droits_agent da on dda.id_droits_agent = da.id_droits_agent ");
		sb.append("where p.libelle = :LIBELLE ");
		sb.append("and da.id_agent = :idAgent GROUP BY idAgent ");

		@SuppressWarnings("unchecked")
		List<Integer> result = absEntityManager
		.createNativeQuery(sb.toString())
				.setParameter("LIBELLE", ProfilEnum.APPROBATEUR.toString())
				.setParameter("idAgent", idAgent)
				.getResultList();

		return result;
	}

	@Override
	public List<Integer> getListApprobateursDemandesSaisiesViseesJourDonne(List<Integer> listeTypesGroupe) {

		StringBuilder sb = new StringBuilder();
		sb.append("select distinct(droit.id_agent) as idAgent from abs_droit droit ");
		sb.append("inner join abs_droit_profil dp on droit.id_droit = dp.id_droit ");
		sb.append("inner join abs_profil p on dp.id_profil = p.id_profil ");
		sb.append("inner join abs_droit_droits_agent dda on dp.id_droit_profil = dda.id_droit_profil ");
		sb.append("inner join abs_droits_agent da on dda.id_droits_agent = da.id_droits_agent ");
		sb.append("where p.libelle = :LIBELLE ");
		sb.append("and da.id_agent in ( ");
		sb.append("select d.id_agent from abs_demande d ");
		sb.append("inner join abs_etat_demande ed on d.id_demande = ed.id_demande ");
		sb.append("inner join abs_ref_type_absence rta on d.id_type_demande = rta.id_ref_type_absence "
				+ "and rta.id_ref_groupe_absence in ( :TYPE ) ");
		sb.append("left outer join abs_ref_type_saisi rts on rta.id_ref_type_absence = rts.id_ref_type_absence "
				+ "and rts.saisie_kiosque is true ");
		sb.append("where ed.id_ref_etat in( :SAISIE , :VISEE_F , :VISEE_D ) ");
		sb.append("and date_trunc('day', ed.date) <= (current_date - interval '1 day') ");
		sb.append("and ed.id_etat_demande in ( select max(ed2.id_etat_demande) from abs_etat_demande ed2 group by ed2.id_demande ) ");
		sb.append(" ) GROUP BY idAgent ");

		@SuppressWarnings("unchecked")
		List<Integer> result = absEntityManager.createNativeQuery(sb.toString())
				.setParameter("LIBELLE", ProfilEnum.APPROBATEUR.toString()).setParameter("TYPE", listeTypesGroupe)
				.setParameter("SAISIE", RefEtatEnum.SAISIE.getCodeEtat())
				.setParameter("VISEE_F", RefEtatEnum.VISEE_FAVORABLE.getCodeEtat())
				.setParameter("VISEE_D", RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat()).getResultList();

		return result;
	}

	@Override
	public List<Integer> getAllMaladiesSaisiesVeille() {

		StringBuilder sb = new StringBuilder();
		sb.append("select distinct d.id_agent from abs_demande d ");
		sb.append("inner join abs_etat_demande ed on d.id_demande = ed.id_demande ");
		sb.append("inner join abs_ref_type_absence rta on d.id_type_demande = rta.id_ref_type_absence "
				+ "and rta.id_ref_groupe_absence in ( :TYPE ) ");
		sb.append("left outer join abs_ref_type_saisi rts on rta.id_ref_type_absence = rts.id_ref_type_absence "
				+ "and rts.saisie_kiosque is true ");
		sb.append("where ed.id_ref_etat = :SAISIE ");
		sb.append("and date_trunc('day', ed.date) = date_trunc('day', current_date - interval '1 day') ");

		@SuppressWarnings("unchecked")
		List<Integer> result = absEntityManager.createNativeQuery(sb.toString())
				.setParameter("SAISIE", RefEtatEnum.SAISIE.getCodeEtat())
				.setParameter("TYPE", RefTypeGroupeAbsenceEnum.MALADIES.getValue())
				.getResultList();

		return result;
	}

	@Override
	public List<Integer> getListApprobateursMaladiesSaisiesViseesVeille(List<Integer> listeTypesGroupe) {

		StringBuilder sb = new StringBuilder();
		sb.append("select distinct(droit.id_agent) as idAgent from abs_droit droit ");
		sb.append("inner join abs_droit_profil dp on droit.id_droit = dp.id_droit ");
		sb.append("inner join abs_profil p on dp.id_profil = p.id_profil ");
		sb.append("inner join abs_droit_droits_agent dda on dp.id_droit_profil = dda.id_droit_profil ");
		sb.append("inner join abs_droits_agent da on dda.id_droits_agent = da.id_droits_agent ");
		sb.append("where p.libelle = :LIBELLE ");
		sb.append("and da.id_agent in ( ");
		sb.append("select distinct d.id_agent from abs_demande d ");
		sb.append("inner join abs_etat_demande ed on d.id_demande = ed.id_demande ");
		sb.append("inner join abs_ref_type_absence rta on d.id_type_demande = rta.id_ref_type_absence "
				+ "and rta.id_ref_groupe_absence in ( :TYPE ) ");
		sb.append("left outer join abs_ref_type_saisi rts on rta.id_ref_type_absence = rts.id_ref_type_absence "
				+ "and rts.saisie_kiosque is true ");
		sb.append("where ed.id_ref_etat = :SAISIE ");
		sb.append("and date_trunc('day', ed.date) = date_trunc('day', current_date - interval '1 day') ");
		sb.append(" ) GROUP BY idAgent ");

		@SuppressWarnings("unchecked")
		List<Integer> result = absEntityManager.createNativeQuery(sb.toString())
				.setParameter("LIBELLE", ProfilEnum.APPROBATEUR.toString())
				.setParameter("SAISIE", RefEtatEnum.SAISIE.getCodeEtat())
				.setParameter("TYPE", listeTypesGroupe)
				.getResultList();

		return result;
	}

	/**
	 * #30120
	 * 
	 */
	@Override
	public List<Demande> listeDemandesSIRH(Date fromDate, Date toDate, Integer idRefEtat, Integer idRefType,
			List<Integer> listIdAgentRecherche, Integer idRefGroupeAbsence) {
		
		List<Integer> listDemandeIds = listeDemandesSIRHIdsWithtouEtatDemandeFetch(
				fromDate, toDate, idRefEtat, idRefType, listIdAgentRecherche, idRefGroupeAbsence);
		
		return listeDemandesSIRHWithEtatDemandeFetch(listDemandeIds);
	}

	private List<Integer> listeDemandesSIRHIdsWithtouEtatDemandeFetch(Date fromDate, Date toDate, Integer idRefEtat, Integer idRefType,
			List<Integer> listIdAgentRecherche, Integer idRefGroupeAbsence) {

		StringBuilder sql = new StringBuilder();
		sql.append(" select d.id_demande from abs_demande d ");
		
		if (idRefGroupeAbsence != null) {
			sql.append(" inner join abs_ref_type_absence ref on d.id_type_demande=ref.id_ref_type_absence ");
			if (idRefGroupeAbsence.equals(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue()))
				sql.append(" inner join abs_demande_conges_annuels ca on d.id_demande=ca.id_demande ");
		}
		if (idRefEtat != null)
			sql.append(" inner join abs_etat_demande ed on d.id_demande=ed.id_demande ");

		sql.append(" where 1=1 ");

		if (idRefEtat != null) {
			sql.append(" and ed.id_etat_demande in ( ");
			sql.append("select max(ed2.id_etat_demande) from abs_etat_demande ed2 ");
			sql.append(" inner join abs_demande d2 on ed2.id_demande=d2.id_demande ");
			sql.append("group by ed2.id_demande ) ");
			sql.append(" and ed.id_ref_etat = :ID_REF_ETAT ");
		}
		
		if (idRefGroupeAbsence != null)
			sql.append(" and ref.id_ref_groupe_absence = :ID_REF_GROUP ");
		if (idRefType != null) {
			if (idRefGroupeAbsence == null || !idRefGroupeAbsence.equals(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue()))
				sql.append(" and d.id_type_demande = :ID_REF_TYPE ");
			else if (idRefGroupeAbsence.equals(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue()))
				sql.append(" and ca.id_ref_type_saisi_conge_annuel = :ID_REF_TYPE ");
		}
		
		// date
		if (fromDate != null && toDate == null) {
			sql.append("and d.date_fin >= :fromDate ");
		} else if (fromDate == null && toDate != null) {
			sql.append("and d.date_Debut <= :toDate ");
		} else if (fromDate != null && toDate != null) {
			sql.append("and d.date_fin >= :fromDate and d.date_Debut <= :toDate ");
		}

		// agent
		if (listIdAgentRecherche != null && !listIdAgentRecherche.isEmpty()) {
			sql.append("and d.id_Agent in :idAgentRecherche ");
		}
		sql.append("order by d.date_Debut desc limit :LIMIT");
		
		Query query = absEntityManager.createNativeQuery(sql.toString());

		// Parameters
		if (idRefType != null)
			query.setParameter("ID_REF_TYPE", idRefType);
		if (idRefEtat != null)
			query.setParameter("ID_REF_ETAT", idRefEtat);
		if (idRefGroupeAbsence != null)
			query.setParameter("ID_REF_GROUP", idRefGroupeAbsence);
		
		query.setParameter("LIMIT", 300);

		// date
		if (fromDate != null && toDate == null) {
			query.setParameter("fromDate", fromDate);
		} else if (fromDate == null && toDate != null) {
			query.setParameter("toDate", toDate);
		} else if (fromDate != null && toDate != null) {
			query.setParameter("fromDate", fromDate);
			query.setParameter("toDate", toDate);
		}
		// agent
		if (listIdAgentRecherche != null && !listIdAgentRecherche.isEmpty()) {
			query.setParameter("idAgentRecherche", listIdAgentRecherche);
		}

		return (List<Integer>) query.getResultList();
	}
	
	private List<Demande> listeDemandesSIRHWithEtatDemandeFetch(List<Integer> listIdDemande) {
		
		if(null != listIdDemande && !listIdDemande.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append("select d from Demande d inner join fetch d.etatsDemande ed ");
			sb.append("where d.idDemande in :listIdDemande ");
			sb.append("order by d.dateDebut desc ");
			
			TypedQuery<Demande> query = absEntityManager.createQuery(sb.toString(), Demande.class);

			query.setParameter("listIdDemande", listIdDemande);
			
			return query.getResultList();
		}
		return Lists.newArrayList();
	}

	@Override
	public List<Demande> listeDemandesASAAndCongesExcepAndMaladiesSIRHAValider(Date fromDate, Date toDate,
			List<Integer> listIdRefGroupe, Integer idRefTypeFamille, List<Integer> listIdAgentRecherche, Integer maxResult) {
		// Récupérer la liste des Id limités pour faire ensuite le fetch
		List<Integer> listIds = listeIdDemandesASAAndCongesExcepAndMaladiesSIRHAValider(fromDate, toDate, listIdRefGroupe, idRefTypeFamille, listIdAgentRecherche, maxResult);
		if (listIds.isEmpty())
			return Lists.newArrayList();
		
		// pour le moment la DRH ne doit valider que les congés excep, congé annuel et les ASA
		StringBuilder sb = new StringBuilder();
		sb.append("select d from Demande d inner join fetch d.etatsDemande ed ");
		sb.append("where d.idDemande in ( :LIST_ID_DEMANDE ) ");
		sb.append("order by d.dateDebut desc ");

		TypedQuery<Demande> query = absEntityManager.createQuery(sb.toString(), Demande.class);
		query.setParameter("LIST_ID_DEMANDE", listIds);

		return query.getResultList();
	}

	@Override
	public List<Demande> listeDemandesCongesAnnuelsSIRHAValider(Date fromDate, Date toDate, List<Integer> listIdAgentRecherche, Integer idTypeCA, Integer maxResult) {
		List<Integer> listIds = listeIdDemandesCongesAnnuelsSIRHAValider(fromDate, toDate, listIdAgentRecherche, idTypeCA, maxResult);
		if (listIds.isEmpty())
			return Lists.newArrayList();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select d from Demande d inner join fetch d.etatsDemande ed ");
		sb.append("where d.idDemande in :LIST_ID_DEMANDE ");
		sb.append("order by d.dateDebut desc ");

		TypedQuery<Demande> query = absEntityManager.createQuery(sb.toString(), Demande.class);
		query.setParameter("LIST_ID_DEMANDE", listIds);

		return query.getResultList();
	}

	@Override
	public Integer getNombreSamediOffertSurAnnee(Integer idAgent, Integer year, Integer idDemande) {

		StringBuilder sb = new StringBuilder();
		sb.append("select d from DemandeCongesAnnuels d inner join d.etatsDemande ed ");
		sb.append("where d.idAgent = :idAgent ");
		sb.append("and d.type.groupe.idRefGroupeAbsence in(:CONGE_ANNUEL ) ");
		sb.append("and d.dateDebut >= :fromDate and d.dateDebut <= :toDate ");
		sb.append("and d.nbSamediOffert > 0 ");
		if (null != idDemande) {
			sb.append("and d.idDemande <> :idDemande ");
		}
		sb.append("and ed.idEtatDemande in ( select max(ed2.idEtatDemande) from EtatDemande ed2 inner join ed2.demande d2 where d2.idAgent = :idAgent group by ed2.demande ) ");
		sb.append("and ed.etat not in ( :REJETE, :REFUSEE, :ANNULEE ) ");

		TypedQuery<DemandeCongesAnnuels> query = absEntityManager
				.createQuery(sb.toString(), DemandeCongesAnnuels.class);

		query.setParameter("idAgent", idAgent);
		query.setParameter("CONGE_ANNUEL", RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());
		query.setParameter("fromDate", new DateTime(year, 1, 1, 0, 0, 0).toDate());
		query.setParameter("toDate", new DateTime(year, 12, 31, 23, 59, 0).toDate());
		if (null != idDemande) {
			query.setParameter("idDemande", idDemande);
		}
		query.setParameter("REJETE", RefEtatEnum.REJETE);
		query.setParameter("REFUSEE", RefEtatEnum.REFUSEE);
		query.setParameter("ANNULEE", RefEtatEnum.ANNULEE);

		List<DemandeCongesAnnuels> res = query.getResultList();
		return res.size();
	}

	@Override
	public List<Demande> listeDemandesAgentVerification(Integer idAgentConcerne, Date fromDate, Date toDate,
			Integer idRefGroupeAbsence) {
		StringBuilder sb = new StringBuilder();
		sb.append("select d from Demande d inner join d.etatsDemande ed ");
		sb.append("where 1=1 ");
		sb.append("and d.idAgent = :idAgentConcerne ");
		
		if(null != idRefGroupeAbsence)
			sb.append("and d.type.groupe.idRefGroupeAbsence = :idRefGroupeAbsence ");
		
		sb.append("and((:fromDate between  d.dateDebut and d.dateFin or :toDate between d.dateDebut and d.dateFin) or (d.dateDebut between :fromDate and :toDate or d.dateFin between :fromDate and :toDate))");
		sb.append("and ed.idEtatDemande in ( select max(ed2.idEtatDemande) from EtatDemande ed2 inner join ed2.demande d2 where d2.idAgent = :idAgentConcerne group by ed2.demande ) ");
		sb.append("and ed.etat in ( :SAISI, :VISEE_FAVORABLE, :VISEE_DEFAVORABLE, :APPROUVEE, :A_VALIDER, :EN_ATTENTE, :PRISE, :VALIDEE ) ");
		sb.append("order by d.idDemande desc ");

		TypedQuery<Demande> query = absEntityManager.createQuery(sb.toString(), Demande.class);

		query.setParameter("idAgentConcerne", idAgentConcerne);
		
		if(null != idRefGroupeAbsence)
			query.setParameter("idRefGroupeAbsence", idRefGroupeAbsence);
		
		query.setParameter("fromDate", fromDate);
		query.setParameter("toDate", toDate);
		// #31896
		query.setParameter("SAISI", RefEtatEnum.SAISIE);
		query.setParameter("VISEE_FAVORABLE", RefEtatEnum.VISEE_FAVORABLE);
		query.setParameter("VISEE_DEFAVORABLE", RefEtatEnum.VISEE_DEFAVORABLE);
		query.setParameter("APPROUVEE", RefEtatEnum.APPROUVEE);
		query.setParameter("A_VALIDER", RefEtatEnum.A_VALIDER);
		query.setParameter("EN_ATTENTE", RefEtatEnum.EN_ATTENTE);
		query.setParameter("PRISE", RefEtatEnum.PRISE);
		query.setParameter("VALIDEE", RefEtatEnum.VALIDEE);

		return query.getResultList();
	}

	@Override
	public List<Demande> listerDemandeCongeUnique(Integer idAgent, Integer annee) {

		StringBuilder sb = new StringBuilder();
		sb.append("select d from Demande d inner join fetch d.etatsDemande ed ");
		sb.append("where d.idAgent = :idAgentConcerne ");
		sb.append("and d.type.idRefTypeAbsence in (:listIdRefTypeAbsence) ");
		sb.append("and year(d.dateDebut) = :year ");
		sb.append("and ed.idEtatDemande in ( select max(ed2.idEtatDemande) from EtatDemande ed2 inner join ed2.demande d2 group by ed2.demande ) ");
		sb.append("and ed.etat in ( :VALIDEE, :PRISE ) ");

		TypedQuery<Demande> query = absEntityManager.createQuery(sb.toString(), Demande.class);
		query.setParameter("idAgentConcerne", idAgent);
		query.setParameter("year", annee);
		// ID des CONGES UNIQUE
		query.setParameter("listIdRefTypeAbsence", Arrays.asList(44, 45));
		query.setParameter("VALIDEE", RefEtatEnum.VALIDEE);
		query.setParameter("PRISE", RefEtatEnum.PRISE);

		return query.getResultList();
	}

	@Override
	public List<Demande> getListDemandeRejetDRHStatutVeille(List<Integer> listeTypesGroupe) {

		StringBuilder sb = new StringBuilder();
		sb.append("select d from Demande d inner join d.etatsDemande ed ");
		sb.append("where d.type.groupe.idRefGroupeAbsence in ( :TYPE ) ");
		sb.append("and ed.idEtatDemande in ( select max(ed2.idEtatDemande) from EtatDemande ed2 inner join ed2.demande d2 group by ed2.demande ) ");
		sb.append("and ed.etat in ( :REJET_DRH ) ");
		sb.append("and ed.date between :dateHistoMatin and :dateHistoSoir ");
		
		TypedQuery<Demande> q = absEntityManager.createQuery(sb.toString(), Demande.class);
		q.setParameter("TYPE", listeTypesGroupe);
		q.setParameter("REJET_DRH", RefEtatEnum.REJETE);

		DateTime hierMatin = new DateTime(new Date());
		DateTime hierSoir = new DateTime(new Date());
		hierMatin = hierMatin.minusDays(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(1);
		hierSoir = hierSoir.minusDays(1).withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59);
		q.setParameter("dateHistoMatin", hierMatin.toDate());
		q.setParameter("dateHistoSoir", hierSoir.toDate());
		
		return q.getResultList();
	}
	
	/**
	 * #44736 : Il doit être possible de saisir une prolongation "inter-types" pour certaines maladies.
	 * @param demande
	 * @return
	 */
	private List<Integer> getListOfAuthorizedProlongation(DemandeDto demande) {
		List<Integer> resultList = Lists.newArrayList();
		boolean isAuthorized = false;
		
		for (AuthorizedMaladiesForProlongationEnum e: AuthorizedMaladiesForProlongationEnum.values()) {
			resultList.add(e.getCode());
			if (e.getCode().equals(demande.getIdTypeDemande()))
				isAuthorized = true;
		}
		
		return isAuthorized ? resultList : Arrays.asList(demande.getIdTypeDemande());
	}
	
	@Override
	public boolean initialDemandeForProlongationExists(DemandeDto demande) {
		StringBuilder sb = new StringBuilder();
		sb.append("select d from Demande d inner join fetch d.etatsDemande ed ");
		sb.append("where d.type.idRefTypeAbsence in (:TYPES_MALADIES) ");
		sb.append("and ed.idEtatDemande in ( select max(ed2.idEtatDemande) from EtatDemande ed2 inner join ed2.demande d2 group by ed2.demande ) ");
		sb.append("and ed.etat in ( :EN_ATTENTE, :PRISE, :SAISIE, :VALIDEE) ");
		sb.append("and d.dateFin between :dateFin1 and :dateFin2 ");
		sb.append("and d.idAgent = :idAgent ");
		
		TypedQuery<Demande> q = absEntityManager.createQuery(sb.toString(), Demande.class);
		
		// #44736 : On va chercher les types de maladies pris en compte.
		q.setParameter("TYPES_MALADIES", getListOfAuthorizedProlongation(demande));
		
		// Liste des états possible : #39417
		q.setParameter("EN_ATTENTE", RefEtatEnum.EN_ATTENTE);
		q.setParameter("PRISE", RefEtatEnum.PRISE);
		q.setParameter("SAISIE", RefEtatEnum.SAISIE);
		q.setParameter("VALIDEE", RefEtatEnum.VALIDEE);

		DateTime veilleProlongationStart = new DateTime(demande.getDateDebut());
		
		// #40396 : On encadre la date de fin sur toute la journée, entre minuit et minuit
		veilleProlongationStart = veilleProlongationStart.minusDays(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0);
		q.setParameter("dateFin1", veilleProlongationStart.toDate());
		DateTime veilleProlongationEnd = veilleProlongationStart.withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59).withMillisOfSecond(999);
		q.setParameter("dateFin2", veilleProlongationEnd.toDate());
		q.setParameter("idAgent", demande.getAgentWithServiceDto().getIdAgent());
		
		logger.debug("Recherche de maladie pour l'agent id {} entre le {} et le {} pour une demande de prolongation.", demande.getAgentWithServiceDto().getIdAgent(), veilleProlongationStart, veilleProlongationEnd);
		
		return q.getResultList().isEmpty() ? false : true;
	}

	@Override
	public List<Demande> getListeATReferenceForAgent(Integer idAgent) {

		StringBuilder sb = new StringBuilder();
		sb.append("select d from Demande d inner join fetch d.etatsDemande ed ");
		sb.append("where d.idAgent = :idAgentConcerne ");
		sb.append("and d.type.idRefTypeAbsence = :idAT ");
		sb.append("and ed.idEtatDemande in ( select max(ed2.idEtatDemande) from EtatDemande ed2 inner join ed2.demande d2 group by ed2.demande ) ");
		sb.append("and ed.etat in ( :VALIDEE, :PRISE ) ");

		TypedQuery<Demande> query = absEntityManager.createQuery(sb.toString(), Demande.class);
		query.setParameter("idAgentConcerne", idAgent);
		query.setParameter("idAT", RefTypeAbsenceEnum.MALADIE_AT.getValue());
		// Uniquement les AT validés ou pris.
		query.setParameter("VALIDEE", RefEtatEnum.VALIDEE);
		query.setParameter("PRISE", RefEtatEnum.PRISE);

		return query.getResultList();
	}

	@Override
	public List<Integer> listeIdDemandesCongesAnnuelsSIRHAValider(Date fromDate, Date toDate,
			List<Integer> listIdAgentRecherche, Integer idTypeCA, Integer maxResult) {

		StringBuilder sql = new StringBuilder();
		sql.append(" select d.id_demande from abs_demande d ");
		sql.append(" inner join abs_etat_demande ed on d.id_demande=ed.id_demande ");
		if (idTypeCA != null)
			sql.append(" inner join abs_demande_conges_annuels ca on d.id_demande=ca.id_demande ");
		sql.append(" inner join abs_ref_type_absence ref on d.id_type_demande=ref.id_ref_type_absence ");
		sql.append(" where ed.id_etat_demande in ( ");
		sql.append("select max(ed2.id_etat_demande) from abs_etat_demande ed2 ");
		sql.append(" inner join abs_demande d2 on ed2.id_demande=d2.id_demande ");
		sql.append("group by ed2.id_demande ) ");
		sql.append(" and ed.id_ref_etat in ( :EN_ATTENTE, :A_VALIDER ) ");
		sql.append(" and ref.id_ref_groupe_absence = :CONGE_ANNUEL ");

		// date
		if (fromDate != null && toDate == null) {
			sql.append("and d.date_fin >= :fromDate ");
		} else if (fromDate == null && toDate != null) {
			sql.append("and d.date_Debut <= :toDate ");
		} else if (fromDate != null && toDate != null) {
			sql.append("and d.date_fin >= :fromDate and d.date_Debut <= :toDate ");
		}
		if (idTypeCA != null)
			sql.append("and ca.id_ref_type_saisi_conge_annuel = :idTypeCA ");

		// agent
		if (listIdAgentRecherche != null && !listIdAgentRecherche.isEmpty())
			sql.append("and d.id_Agent in :idAgentRecherche ");

		sql.append("order by d.date_Debut desc limit :LIMIT");
		Query query = absEntityManager.createNativeQuery(sql.toString());

		query.setParameter("CONGE_ANNUEL", RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());
		query.setParameter("EN_ATTENTE", RefEtatEnum.EN_ATTENTE.getCodeEtat());
		query.setParameter("A_VALIDER", RefEtatEnum.A_VALIDER.getCodeEtat());
		query.setParameter("LIMIT", maxResult);

		// date
		if (fromDate != null && toDate == null) {
			query.setParameter("fromDate", fromDate);
		} else if (fromDate == null && toDate != null) {
			query.setParameter("toDate", toDate);
		} else if (fromDate != null && toDate != null) {
			query.setParameter("fromDate", fromDate);
			query.setParameter("toDate", toDate);
		}
		if (idTypeCA != null)
			query.setParameter("idTypeCA", idTypeCA);
		// agent
		if (listIdAgentRecherche != null && !listIdAgentRecherche.isEmpty()) {
			query.setParameter("idAgentRecherche", listIdAgentRecherche);
		}

		return (List<Integer>) query.getResultList();
	}

	@Override
	public List<Integer> listeIdDemandesASAAndCongesExcepAndMaladiesSIRHAValider(Date fromDate, Date toDate,
			List<Integer> listIdRefGroupe, Integer idRefTypeFamille, List<Integer> listIdAgentRecherche,
			Integer maxResult) {

		StringBuilder sql = new StringBuilder();
		sql.append(" select d.id_demande from abs_demande d ");
		sql.append(" inner join abs_etat_demande ed on d.id_demande=ed.id_demande ");
		sql.append(" inner join abs_ref_type_absence ref on d.id_type_demande=ref.id_ref_type_absence ");
		sql.append(" where ed.id_etat_demande in ( ");
		sql.append("select max(ed2.id_etat_demande) from abs_etat_demande ed2 ");
		sql.append(" inner join abs_demande d2 on ed2.id_demande=d2.id_demande ");
		sql.append("group by ed2.id_demande ) ");
		sql.append(" and ed.id_ref_etat in ( :APPROUVEE, :EN_ATTENTE, :A_VALIDER ) ");
		sql.append(" and ref.id_ref_groupe_absence in ( :LIST_ID_REF_GROUP ) ");

		// date
		if (fromDate != null && toDate == null) {
			sql.append("and d.date_fin >= :fromDate ");
		} else if (fromDate == null && toDate != null) {
			sql.append("and d.date_Debut <= :toDate ");
		} else if (fromDate != null && toDate != null) {
			sql.append("and d.date_fin >= :fromDate and d.date_Debut <= :toDate ");
		}

		// agent
		if (listIdAgentRecherche != null && !listIdAgentRecherche.isEmpty()) {
			sql.append("and d.id_Agent in :idAgentRecherche ");
		}
		sql.append("order by d.date_Debut desc limit :LIMIT");
		Query query = absEntityManager.createNativeQuery(sql.toString());

		query.setParameter("LIST_ID_REF_GROUP", listIdRefGroupe);
		query.setParameter("EN_ATTENTE", RefEtatEnum.EN_ATTENTE.getCodeEtat());
		query.setParameter("A_VALIDER", RefEtatEnum.A_VALIDER.getCodeEtat());
		query.setParameter("APPROUVEE", RefEtatEnum.APPROUVEE.getCodeEtat());
		query.setParameter("LIMIT", maxResult);

		// date
		if (fromDate != null && toDate == null) {
			query.setParameter("fromDate", fromDate);
		} else if (fromDate == null && toDate != null) {
			query.setParameter("toDate", toDate);
		} else if (fromDate != null && toDate != null) {
			query.setParameter("fromDate", fromDate);
			query.setParameter("toDate", toDate);
		}
		// agent
		if (listIdAgentRecherche != null && !listIdAgentRecherche.isEmpty()) {
			query.setParameter("idAgentRecherche", listIdAgentRecherche);
		}

		return (List<Integer>) query.getResultList();
	}
}
