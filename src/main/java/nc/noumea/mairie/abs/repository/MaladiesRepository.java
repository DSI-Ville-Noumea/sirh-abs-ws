package nc.noumea.mairie.abs.repository;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import nc.noumea.mairie.abs.domain.DemandeMaladies;
import nc.noumea.mairie.abs.domain.RefDroitsMaladies;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;

import org.springframework.stereotype.Repository;

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

}