package nc.noumea.mairie.abs.repository;

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
		sb.append("select d from Demande d ");
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

		sb.append("order by d.idDemande desc ");

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
		sb.append("and date_trunc('day', ed.date) = current_date - interval '1 day' ");
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
		sb.append("and date_trunc('day', ed.date) = current_date - interval '1 day' ");
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
	public List<Demande> listeDemandesSIRH(Date fromDate, Date toDate, Integer idRefEtat, Integer idRefType,
			Integer idAgentRecherche, Integer idRefGroupeAbsence) {
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
		if (idAgentRecherche != null) {
			sb.append("and d.idAgent = :idAgentRecherche ");
		}
		// type
		if (idRefType != null) {
			sb.append("and d.type.idRefTypeAbsence = :idRefTypeAbsence ");
		}
		// groupe
		if (idRefGroupeAbsence != null) {
			sb.append("and d.type.groupe.idRefGroupeAbsence = :idRefGroupeAbsence ");
		}

		sb.append("order by d.idDemande desc ");

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
		if (idAgentRecherche != null) {
			query.setParameter("idAgentRecherche", idAgentRecherche);
		}
		// type
		if (idRefType != null) {
			query.setParameter("idRefTypeAbsence", idRefType);
		}
		// groupe
		if (idRefGroupeAbsence != null) {
			query.setParameter("idRefGroupeAbsence", idRefGroupeAbsence);
		}
		return query.getResultList();
	}

	@Override
	public List<Demande> listeDemandesSIRHAValider() {
		// pour le moment la DRH ne doit valider que les congés excep, congé
		// annuel et les ASA
		StringBuilder sb = new StringBuilder();
		sb.append("select d from Demande d ");
		sb.append("where d.type.groupe.idRefGroupeAbsence in( :ASA , :CONGE_EXCEP, :CONGE_ANNUEL ) ");
		sb.append("order by d.idDemande desc ");

		TypedQuery<Demande> query = absEntityManager.createQuery(sb.toString(), Demande.class);
		query.setParameter("ASA", RefTypeGroupeAbsenceEnum.ASA.getValue());
		query.setParameter("CONGE_EXCEP", RefTypeGroupeAbsenceEnum.CONGES_EXCEP.getValue());
		query.setParameter("CONGE_ANNUEL", RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());

		return query.getResultList();
	}

	@Override
	public Integer getNombreSamediOffertSurAnnee(DemandeCongesAnnuels demande, Integer year) {
		StringBuilder sb = new StringBuilder();
		sb.append("select d from DemandeCongesAnnuels d ");
		sb.append("where d.idAgent = :idAgent ");
		sb.append("and d.type.groupe.idRefGroupeAbsence in(:CONGE_ANNUEL ) ");
		sb.append("and d.dateDebut >= :fromDate and d.dateDebut <= :toDate ");
		sb.append("and d.samediOffert is true ");

		TypedQuery<DemandeCongesAnnuels> query = absEntityManager
				.createQuery(sb.toString(), DemandeCongesAnnuels.class);

		query.setParameter("idAgent", demande.getIdAgent());
		query.setParameter("CONGE_ANNUEL", RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());
		query.setParameter("fromDate", new DateTime(year, 1, 1, 0, 0, 0).toDate());
		query.setParameter("toDate", new DateTime(year, 12, 31, 23, 59, 0).toDate());
		List<DemandeCongesAnnuels> res = query.getResultList();
		return res.size();
	}
}
