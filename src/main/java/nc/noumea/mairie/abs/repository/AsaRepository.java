package nc.noumea.mairie.abs.repository;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import nc.noumea.mairie.abs.domain.DemandeAsa;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;

import org.springframework.stereotype.Repository;

@Repository
public class AsaRepository implements IAsaRepository {

	@PersistenceContext(unitName = "absPersistenceUnit")
	private EntityManager absEntityManager;

	@Override
	public List<DemandeAsa> getListDemandeAsaEnCours(Integer idAgent, Integer idDemande, RefTypeAbsenceEnum type) {

		StringBuilder sb = new StringBuilder();
		sb.append("select da from DemandeAsa da inner join da.etatsDemande ed where da.idAgent = :idAgent ");
		sb.append(" and da.type.idRefTypeAbsence = :type ");
		sb.append(" and ed.idEtatDemande in ( select max(ed2.idEtatDemande) from EtatDemande ed2 inner join ed2.demande d2 where d2.idAgent = :idAgent group by ed2.demande ) ");
		sb.append("and ed.etat in ( :SAISIE, :VISEE_F, :VISEE_D, :APPROUVE, :EN_ATTENTE ) ");
		if (null != idDemande) {
			sb.append("and da.idDemande <> :idDemande ");
		}

		TypedQuery<DemandeAsa> q = absEntityManager.createQuery(sb.toString(), DemandeAsa.class);

		q.setParameter("idAgent", idAgent);
		q.setParameter("type", type.getValue());
		q.setParameter("SAISIE", RefEtatEnum.SAISIE);
		q.setParameter("VISEE_F", RefEtatEnum.VISEE_FAVORABLE);
		q.setParameter("VISEE_D", RefEtatEnum.VISEE_DEFAVORABLE);
		q.setParameter("APPROUVE", RefEtatEnum.APPROUVEE);
		q.setParameter("EN_ATTENTE", RefEtatEnum.EN_ATTENTE);
		if (null != idDemande) {
			q.setParameter("idDemande", idDemande);
		}

		return q.getResultList();
	}

	@Override
	public List<DemandeAsa> getListDemandeAsaPourMois(Integer idAgent, Integer idDemande, Date dateDeb, Date dateFin,
			RefTypeAbsenceEnum type) {

		StringBuilder sb = new StringBuilder();
		sb.append("select da from DemandeAsa da inner join da.etatsDemande ed where da.idAgent = :idAgent ");
		sb.append(" and da.type.idRefTypeAbsence = :type ");
		sb.append(" and ed.idEtatDemande in ( select max(ed2.idEtatDemande) from EtatDemande ed2 inner join ed2.demande d2 where d2.idAgent = :idAgent group by ed2.demande ) ");
		sb.append("and ed.etat in ( :SAISIE, :VISEE_F, :VISEE_D, :APPROUVE, :EN_ATTENTE, :PRISE, :VALIDEE ) ");
		sb.append("and da.dateDebut BETWEEN :dateDebut and :dateFin ");
		if (null != idDemande) {
			sb.append("and da.idDemande <> :idDemande ");
		}

		TypedQuery<DemandeAsa> q = absEntityManager.createQuery(sb.toString(), DemandeAsa.class);

		q.setParameter("idAgent", idAgent);
		q.setParameter("type", type.getValue());
		q.setParameter("SAISIE", RefEtatEnum.SAISIE);
		q.setParameter("VISEE_F", RefEtatEnum.VISEE_FAVORABLE);
		q.setParameter("VISEE_D", RefEtatEnum.VISEE_DEFAVORABLE);
		q.setParameter("APPROUVE", RefEtatEnum.APPROUVEE);
		q.setParameter("PRISE", RefEtatEnum.PRISE);
		q.setParameter("VALIDEE", RefEtatEnum.VALIDEE);
		q.setParameter("EN_ATTENTE", RefEtatEnum.EN_ATTENTE);
		q.setParameter("dateDebut", dateDeb);
		q.setParameter("dateFin", dateFin);
		if (null != idDemande) {
			q.setParameter("idDemande", idDemande);
		}

		return q.getResultList();
	}

}
