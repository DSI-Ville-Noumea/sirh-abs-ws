package nc.noumea.mairie.abs.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeCongesAnnuels;
import nc.noumea.mairie.abs.domain.ProfilEnum;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeGroupeAbsenceEnum;

import org.joda.time.DateTime;
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

	@Override
	public List<Demande> listeDemandesForListAgent(Integer idAgentConnecte, List<Integer> idAgentConcerne, Date fromDate,
			Date toDate, Integer idRefType, Integer idRefGroupeAbsence) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("select d from Demande d inner join fetch d.etatsDemande ed ");
		sb.append("where 1=1 ");

		if (idAgentConcerne != null) {
			sb.append("and d.idAgent in :idAgentConcerne ");
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

	/**
	 * #30120 
	 * 
	 */
	@Override
	public List<Demande> listeDemandesSIRH(Date fromDate, Date toDate, Integer idRefEtat, Integer idRefType,
			List<Integer> listIdAgentRecherche, Integer idRefGroupeAbsence) {
		
		List<Demande> listDemande = listeDemandesSIRHWithtouEtatDemandeFetch(
				fromDate, toDate, idRefEtat, idRefType, listIdAgentRecherche, idRefGroupeAbsence);
		
		if(null == listDemande
				|| listDemande.isEmpty()) {
			return listDemande;
		}
		
		List<Integer> listIdDemande = new ArrayList<Integer>();
		for(Demande demande : listDemande) {
			listIdDemande.add(demande.getIdDemande());
		}
		
		return listeDemandesSIRHWithEtatDemandeFetch(fromDate, toDate, idRefEtat, idRefType, listIdAgentRecherche, idRefGroupeAbsence, listIdDemande);
	}

	private List<Demande> listeDemandesSIRHWithtouEtatDemandeFetch(Date fromDate, Date toDate, Integer idRefEtat, Integer idRefType,
			List<Integer> listIdAgentRecherche, Integer idRefGroupeAbsence) {

		StringBuilder sb = new StringBuilder();
		sb.append("select d from Demande d ");
		sb.append("where 1=1 ");
		// date
		if (fromDate != null && toDate == null) {
			sb.append("and d.dateDebut >= :fromDate ");
		} else if (fromDate == null && toDate != null) {
			sb.append("and d.dateDebut <= :toDate ");
		} else if (fromDate != null && toDate != null) {
			sb.append("and d.dateDebut >= :fromDate and d.dateDebut <= :toDate ");
		}
		// agent
		if (listIdAgentRecherche != null && !listIdAgentRecherche.isEmpty()) {
			sb.append("and d.idAgent in :idAgentRecherche ");
		}
		// type
		if (idRefType != null) {
			sb.append("and d.type.idRefTypeAbsence = :idRefTypeAbsence ");
		}
		// groupe
		if (idRefGroupeAbsence != null) {
			sb.append("and d.type.groupe.idRefGroupeAbsence = :idRefGroupeAbsence ");
		}

		sb.append("order by d.dateDebut desc ");

		TypedQuery<Demande> query = absEntityManager.createQuery(sb.toString(), Demande.class);

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
		// type
		if (idRefType != null) {
			query.setParameter("idRefTypeAbsence", idRefType);
		}
		// groupe
		if (idRefGroupeAbsence != null) {
			query.setParameter("idRefGroupeAbsence", idRefGroupeAbsence);
		}
		query.setFirstResult(0);
		query.setMaxResults(300);
		return query.getResultList();
	}
	
	private List<Demande> listeDemandesSIRHWithEtatDemandeFetch(Date fromDate, Date toDate, Integer idRefEtat, Integer idRefType,
			List<Integer> listIdAgentRecherche, Integer idRefGroupeAbsence, List<Integer> listIdDemande) {

		StringBuilder sb = new StringBuilder();
		sb.append("select d from Demande d inner join fetch d.etatsDemande ed ");
		sb.append("where 1=1 ");
		// id demande recupere de la 1er requete
		if(null != listIdDemande) {
			sb.append("and d.idDemande in :listIdDemande ");
		}
		// date
		if (fromDate != null && toDate == null) {
			sb.append("and d.dateDebut >= :fromDate ");
		} else if (fromDate == null && toDate != null) {
			sb.append("and d.dateDebut <= :toDate ");
		} else if (fromDate != null && toDate != null) {
			sb.append("and d.dateDebut >= :fromDate and d.dateDebut <= :toDate ");
		}
		// agent
		if (listIdAgentRecherche != null && !listIdAgentRecherche.isEmpty()) {
			sb.append("and d.idAgent in :idAgentRecherche ");
		}
		// type
		if (idRefType != null) {
			sb.append("and d.type.idRefTypeAbsence = :idRefTypeAbsence ");
		}
		// groupe
		if (idRefGroupeAbsence != null) {
			sb.append("and d.type.groupe.idRefGroupeAbsence = :idRefGroupeAbsence ");
		}

		sb.append("order by d.dateDebut desc ");

		TypedQuery<Demande> query = absEntityManager.createQuery(sb.toString(), Demande.class);

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
		// type
		if (idRefType != null) {
			query.setParameter("idRefTypeAbsence", idRefType);
		}
		// groupe
		if (idRefGroupeAbsence != null) {
			query.setParameter("idRefGroupeAbsence", idRefGroupeAbsence);
		}
		if(null != listIdDemande) {
			query.setParameter("listIdDemande", listIdDemande);
		}
		// ne fonctionne pas avec un inner join FETCH dans la requete
		// query.setMaxResults(300);
		
		return query.getResultList();
	}

	@Override
	public List<Demande> listeDemandesASAAndCongesExcepAndMaladiesSIRHAValider(Date fromDate, Date toDate,
			List<Integer> listIdRefGroupe, Integer idRefTypeFamille, List<Integer> listIdAgentRecherche) {
		// pour le moment la DRH ne doit valider que les congés excep, congé
		// annuel et les ASA
		StringBuilder sb = new StringBuilder();
		sb.append("select d from Demande d inner join fetch d.etatsDemande ed ");
		sb.append("where d.type.groupe.idRefGroupeAbsence in( :LISTREFGROUPE ) ");
		sb.append("and ed.idEtatDemande in ( select max(ed2.idEtatDemande) from EtatDemande ed2 inner join ed2.demande d2 group by ed2.demande ) ");
		sb.append("and ed.etat in ( :APPROUVEE, :EN_ATTENTE, :A_VALIDER ) ");

		// type famille
		if (idRefTypeFamille != null) {
			sb.append("and d.type.idRefTypeAbsence = :idRefType ");
		}

		// date
		if (fromDate != null && toDate == null) {
			sb.append("and d.dateDebut >= :fromDate ");
		} else if (fromDate == null && toDate != null) {
			sb.append("and d.dateDebut <= :toDate ");
		} else if (fromDate != null && toDate != null) {
			sb.append("and d.dateDebut >= :fromDate and d.dateDebut <= :toDate ");
		}

		// agent
		if (listIdAgentRecherche != null && !listIdAgentRecherche.isEmpty()) {
			sb.append("and d.idAgent in :idAgentRecherche ");
		}
		sb.append("order by d.dateDebut desc ");

		TypedQuery<Demande> query = absEntityManager.createQuery(sb.toString(), Demande.class);
		query.setParameter("LISTREFGROUPE", listIdRefGroupe);
		query.setParameter("APPROUVEE", RefEtatEnum.APPROUVEE);
		query.setParameter("EN_ATTENTE", RefEtatEnum.EN_ATTENTE);
		query.setParameter("A_VALIDER", RefEtatEnum.A_VALIDER);

		// type famille
		if (idRefTypeFamille != null) {
			query.setParameter("idRefType", idRefTypeFamille);
		}

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

		return query.getResultList();
	}

	@Override
	public List<Demande> listeDemandesCongesAnnuelsSIRHAValider(Date fromDate, Date toDate,
			List<Integer> listIdAgentRecherche) {
		// pour le moment la DRH ne doit valider que les congés excep, congé
		// annuel et les ASA
		StringBuilder sb = new StringBuilder();
		sb.append("select d from Demande d inner join fetch d.etatsDemande ed ");
		sb.append("where d.type.groupe.idRefGroupeAbsence in( :CONGE_ANNUEL ) ");
		sb.append("and ed.idEtatDemande in ( select max(ed2.idEtatDemande) from EtatDemande ed2 inner join ed2.demande d2 group by ed2.demande ) ");
		sb.append("and ed.etat in ( :EN_ATTENTE, :A_VALIDER ) ");

		// date
		if (fromDate != null && toDate == null) {
			sb.append("and d.dateDebut >= :fromDate ");
		} else if (fromDate == null && toDate != null) {
			sb.append("and d.dateDebut <= :toDate ");
		} else if (fromDate != null && toDate != null) {
			sb.append("and d.dateDebut >= :fromDate and d.dateDebut <= :toDate ");
		}

		// agent
		if (listIdAgentRecherche != null && !listIdAgentRecherche.isEmpty()) {
			sb.append("and d.idAgent in :idAgentRecherche ");
		}
		sb.append("order by d.dateDebut desc ");

		TypedQuery<Demande> query = absEntityManager.createQuery(sb.toString(), Demande.class);
		query.setParameter("CONGE_ANNUEL", RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());
		query.setParameter("EN_ATTENTE", RefEtatEnum.EN_ATTENTE);
		query.setParameter("A_VALIDER", RefEtatEnum.A_VALIDER);

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
		sb.append("select d from Demande d inner join fetch d.etatsDemande ed ");
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
}
