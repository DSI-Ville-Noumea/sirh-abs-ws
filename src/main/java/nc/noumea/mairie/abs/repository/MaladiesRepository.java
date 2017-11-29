package nc.noumea.mairie.abs.repository;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;

import nc.noumea.mairie.abs.domain.DemandeMaladies;
import nc.noumea.mairie.abs.domain.RefDroitsMaladies;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;

@Repository
public class MaladiesRepository implements IMaladiesRepository {

	@PersistenceContext(unitName = "absPersistenceUnit")
	private EntityManager absEntityManager;

	@Override
	public List<DemandeMaladies> getListMaladiesAnneGlissanteByAgent(
			Integer idAgent, Date dateDebutAnneeGlissante, Date dateFinAnneeGlissante) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("select d from DemandeMaladies d "
				+ "inner join d.etatsDemande ed "
				+ "where d.idAgent = :idAgent ");
		sb.append(" and d.type.idRefTypeAbsence in ( :MALADIE , :MALADIE_ENFANT , :MALADIE_CONVALESCENCE , :MALADIE_EVASAN , :MALADIE_HOSPITALISATION ) ");
		sb.append(" and d.dateDebut <= :dateFin ");
		
		if(null != dateDebutAnneeGlissante) 
			sb.append(" and d.dateFin >= :dateDebut ");
				
		sb.append(" and ed.idEtatDemande in ( select max(ed2.idEtatDemande) from EtatDemande ed2 "
				+ "inner join ed2.demande d2 where d2.idAgent = :idAgent ");
		sb.append("and d2.dateDebut <= :dateFin ");
		
		if(null != dateDebutAnneeGlissante) 
			sb.append("and d2.dateFin >= :dateDebut ");
		
		sb.append("group by ed2.demande ) ");
		sb.append("and ed.etat in ( :PRISE, :VALIDEE ) ");
		sb.append("order by d.dateDebut asc ");

		TypedQuery<DemandeMaladies> q = absEntityManager.createQuery(sb.toString(), DemandeMaladies.class);

		q.setParameter("idAgent", idAgent);
		q.setParameter("PRISE", RefEtatEnum.PRISE);
		q.setParameter("VALIDEE", RefEtatEnum.VALIDEE);
		
		if(null != dateDebutAnneeGlissante)
			q.setParameter("dateDebut", dateDebutAnneeGlissante);
		
		q.setParameter("dateFin", dateFinAnneeGlissante);
		q.setParameter("MALADIE", RefTypeAbsenceEnum.MALADIE.getValue());
		q.setParameter("MALADIE_ENFANT", RefTypeAbsenceEnum.MALADIE_ENFANT_MALADE.getValue());
		q.setParameter("MALADIE_CONVALESCENCE", RefTypeAbsenceEnum.MALADIE_CONVALESCENCE.getValue());
		q.setParameter("MALADIE_EVASAN", RefTypeAbsenceEnum.MALADIE_EVASAN.getValue());
		q.setParameter("MALADIE_HOSPITALISATION", RefTypeAbsenceEnum.MALADIE_HOSPITALISATION.getValue());

		return q.getResultList();
	}

	@Override
	public List<DemandeMaladies> getListMaladiesAnneGlissanteRetroactiveByAgent(
			Integer idAgent, Date dateDebutAnneeGlissante, Date dateFinAnneeGlissante, Integer idDemande, boolean isCancel, boolean isRetroactif) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("select d from DemandeMaladies d "
				+ "inner join d.etatsDemande ed "
				+ "where d.idAgent = :idAgent ");
		sb.append(" and d.type.idRefTypeAbsence in ( :MALADIE , :MALADIE_ENFANT , :MALADIE_CONVALESCENCE , :MALADIE_EVASAN , :MALADIE_HOSPITALISATION ) ");
		sb.append(" and d.dateDebut <= :dateFin ");
		
		if(null != dateDebutAnneeGlissante) 
			sb.append(" and d.dateFin >= :dateDebut ");
				
		sb.append(" and ed.idEtatDemande in ( select max(ed2.idEtatDemande) from EtatDemande ed2 "
				+ "inner join ed2.demande d2 where d2.idAgent = :idAgent ");
		sb.append("and d2.dateDebut <= :dateFin ");
		
		if(null != dateDebutAnneeGlissante) 
			sb.append("and d2.dateFin >= :dateDebut ");
		
		sb.append("group by ed2.demande ) ");
		sb.append("and (ed.etat in ( :PRISE, :VALIDEE ) ");
		
		if (idDemande != null && isRetroactif) {
			if (isCancel)
				sb.append("AND d.idDemande != :idDemande) ");
			else
				sb.append("OR d.idDemande = :idDemande) ");
		}
		
		sb.append(") ");
		sb.append("order by d.dateDebut asc ");

		TypedQuery<DemandeMaladies> q = absEntityManager.createQuery(sb.toString(), DemandeMaladies.class);
		
		q.setParameter("idAgent", idAgent);
		q.setParameter("PRISE", RefEtatEnum.PRISE);
		q.setParameter("VALIDEE", RefEtatEnum.VALIDEE);
		
		if(null != dateDebutAnneeGlissante)
			q.setParameter("dateDebut", dateDebutAnneeGlissante);
		if (idDemande != null && isRetroactif)
			q.setParameter("idDemande", idDemande);
		
		q.setParameter("dateFin", dateFinAnneeGlissante);
		q.setParameter("MALADIE", RefTypeAbsenceEnum.MALADIE.getValue());
		q.setParameter("MALADIE_ENFANT", RefTypeAbsenceEnum.MALADIE_ENFANT_MALADE.getValue());
		q.setParameter("MALADIE_CONVALESCENCE", RefTypeAbsenceEnum.MALADIE_CONVALESCENCE.getValue());
		q.setParameter("MALADIE_EVASAN", RefTypeAbsenceEnum.MALADIE_EVASAN.getValue());
		q.setParameter("MALADIE_HOSPITALISATION", RefTypeAbsenceEnum.MALADIE_HOSPITALISATION.getValue());

		return q.getResultList();
	}
	
	@Override
	public RefDroitsMaladies getDroitsMaladies(boolean isFonctionnaire, boolean isContractuel, boolean isConvColl, Integer anneeAnciennete) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("select d from RefDroitsMaladies d ");
		sb.append("where ");
		if(isFonctionnaire)
			sb.append("d.fonctionnaire = true ");
		if(isContractuel)
			sb.append("d.contractuel = true ");
		if(isConvColl)
			sb.append("d.conventionCollective = true ");
		
		if(null != anneeAnciennete)
			sb.append("and d.anneeAnciennete > :anneeAnciennete ");
		
		sb.append("order by d.anneeAnciennete ");

		TypedQuery<RefDroitsMaladies> q = absEntityManager.createQuery(sb.toString(), RefDroitsMaladies.class);
		
		if(null != anneeAnciennete)
			q.setParameter("anneeAnciennete", anneeAnciennete);

		List<RefDroitsMaladies> result = q.getResultList();
		
		if(null != result
				&& !result.isEmpty()) {
			return result.get(0);
		}
		
		return null;
	}

	@Override
	public List<DemandeMaladies> getListEnfantMaladeAnneeCivileByAgent(Integer idAgent, Date dateDebutAnneeForOneDate, Date dateFinAnneeForOneDate) {

		StringBuilder sb = new StringBuilder();
		sb.append("select d from DemandeMaladies d " + "inner join d.etatsDemande ed " + "where d.idAgent = :idAgent ");
		sb.append(" and d.type.idRefTypeAbsence in ( :ENFANT_MALADE ) ");
		sb.append(" and d.dateDebut <= :dateFin ");
		sb.append(" and d.dateFin >= :dateDebut ");
		sb.append(" and ed.idEtatDemande in ( select max(ed2.idEtatDemande) from EtatDemande ed2 " + "inner join ed2.demande d2 where d2.idAgent = :idAgent ");
		sb.append("and d2.dateDebut <= :dateFin ");
		sb.append("and d2.dateFin >= :dateDebut ");
		sb.append("group by ed2.demande ) ");
		sb.append("and ed.etat in ( :PRISE, :VALIDEE , :A_VALIDER ) ");
		sb.append("order by d.dateDebut asc ");

		TypedQuery<DemandeMaladies> q = absEntityManager.createQuery(sb.toString(), DemandeMaladies.class);

		q.setParameter("idAgent", idAgent);
		q.setParameter("PRISE", RefEtatEnum.PRISE);
		q.setParameter("VALIDEE", RefEtatEnum.VALIDEE);
		q.setParameter("A_VALIDER", RefEtatEnum.A_VALIDER);
		q.setParameter("dateDebut", dateDebutAnneeForOneDate);
		q.setParameter("dateFin", dateFinAnneeForOneDate);
		q.setParameter("ENFANT_MALADE", RefTypeAbsenceEnum.ENFANT_MALADE.getValue());

		return q.getResultList();
	}

	@Override
	public boolean getInitialATByAgent(Integer idAgent, Date dateAT) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("select d from DemandeMaladies d inner join d.etatsDemande ed where d.idAgent = :idAgent");
		sb.append(" and d.type.idRefTypeAbsence in ( :AT ) ");
		sb.append(" and ed.idEtatDemande in ( select max(ed2.idEtatDemande) from EtatDemande ed2 " + "inner join ed2.demande d2 where d2.idAgent = :idAgent ");
		sb.append("group by ed2.demande ) ");
		sb.append("and ed.etat not in ( :REFUSEE, :REJETE , :ANNULEE ) ");
		sb.append("and d.prolongation is false ");
		sb.append("and d.dateAccidentTravail = :dateAT ");
		sb.append("order by d.dateDebut asc ");

		TypedQuery<DemandeMaladies> q = absEntityManager.createQuery(sb.toString(), DemandeMaladies.class);

		q.setParameter("idAgent", idAgent);
		q.setParameter("REFUSEE", RefEtatEnum.REFUSEE);
		q.setParameter("REJETE", RefEtatEnum.REJETE);
		q.setParameter("ANNULEE", RefEtatEnum.ANNULEE);
		q.setParameter("dateAT", dateAT);
		q.setParameter("AT", RefTypeAbsenceEnum.MALADIE_AT.getValue());
		
		return !q.getResultList().isEmpty();
	}

	@Override
	public List<DemandeMaladies> getAllATByDateATAndAgentId(Date dateAT, Integer idAgent) {

		StringBuilder sb = new StringBuilder();
		sb.append("select d from DemandeMaladies d inner join fetch d.etatsDemande ed ");
		sb.append("where d.idAgent = :idAgentConcerne ");
		sb.append("and d.type.idRefTypeAbsence = :idAT ");
		sb.append("and d.dateAccidentTravail = :dateAT ");
		sb.append("and ed.idEtatDemande in ( select max(ed2.idEtatDemande) from EtatDemande ed2 inner join ed2.demande d2 group by ed2.demande ) ");
		sb.append("and ed.etat not in ( :REJETE, :REFUSEE, :ANNULEE ) ");

		TypedQuery<DemandeMaladies> query = absEntityManager.createQuery(sb.toString(), DemandeMaladies.class);
		query.setParameter("idAgentConcerne", idAgent);
		query.setParameter("idAT", RefTypeAbsenceEnum.MALADIE_AT.getValue());
		query.setParameter("dateAT", dateAT);
		query.setParameter("ANNULEE", RefEtatEnum.ANNULEE);
		query.setParameter("REFUSEE", RefEtatEnum.REFUSEE);
		query.setParameter("REJETE", RefEtatEnum.REJETE);

		return query.getResultList();
	}

	@Override
	public List<DemandeMaladies> getListMaladiesFuturesForDemande(Integer idAgent, Date dateDebut) {

		StringBuilder sb = new StringBuilder();
		sb.append("select d from DemandeMaladies d inner join fetch d.etatsDemande ed ");
		sb.append("where d.idAgent = :idAgentConcerne ");
		sb.append(" and d.type.idRefTypeAbsence in ( :MALADIE , :MALADIE_ENFANT , :MALADIE_CONVALESCENCE , :MALADIE_EVASAN , :MALADIE_HOSPITALISATION ) ");
		sb.append("and d.dateDebut > :dateDebut ");
		sb.append("and ed.idEtatDemande in ( select max(ed2.idEtatDemande) from EtatDemande ed2 inner join ed2.demande d2 group by ed2.demande ) ");
		sb.append("and ed.etat in ( :PRISE, :VALIDEE )) ");

		TypedQuery<DemandeMaladies> query = absEntityManager.createQuery(sb.toString(), DemandeMaladies.class);
		query.setParameter("idAgentConcerne", idAgent);
		query.setParameter("dateDebut", dateDebut);
		
		query.setParameter("PRISE", RefEtatEnum.PRISE);
		query.setParameter("VALIDEE", RefEtatEnum.VALIDEE);

		query.setParameter("MALADIE", RefTypeAbsenceEnum.MALADIE.getValue());
		query.setParameter("MALADIE_ENFANT", RefTypeAbsenceEnum.MALADIE_ENFANT_MALADE.getValue());
		query.setParameter("MALADIE_CONVALESCENCE", RefTypeAbsenceEnum.MALADIE_CONVALESCENCE.getValue());
		query.setParameter("MALADIE_EVASAN", RefTypeAbsenceEnum.MALADIE_EVASAN.getValue());
		query.setParameter("MALADIE_HOSPITALISATION", RefTypeAbsenceEnum.MALADIE_HOSPITALISATION.getValue());

		return query.getResultList();
	}

}
