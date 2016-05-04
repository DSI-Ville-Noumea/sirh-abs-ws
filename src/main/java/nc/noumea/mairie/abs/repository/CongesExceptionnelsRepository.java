package nc.noumea.mairie.abs.repository;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.service.impl.HelperService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CongesExceptionnelsRepository implements ICongesExceptionnelsRepository {

	@PersistenceContext(unitName = "absPersistenceUnit")
    private EntityManager absEntityManager;
	
	@Autowired
	protected HelperService helperService;
	
	@Override
	public Double countDureeByPeriodeAndTypeDemande(Integer idAgentConcerne, Date fromDate,
			Date toDate, Integer idRefType, Integer idDemande) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("select sum(dce.duree) from abs_demande_conges_exceptionnels dce ");
		sb.append("inner join abs_demande d on d.id_demande = dce.id_demande and d.id_agent = :IDAGENT and d.id_type_demande = :TYPE "
				+ "and d.date_debut between :FROMDATE and :TODATE ");
		sb.append("inner join abs_etat_demande ed on d.id_demande = ed.id_demande ");
		sb.append("where ed.id_etat_demande in ( ");
			sb.append(" select max(ed2.id_etat_demande) from abs_etat_demande ed2 ");
			sb.append(" inner join abs_demande d2 on ed2.id_demande=d2.id_demande ");
			sb.append(" where d2.id_agent = :IDAGENT  and d.id_type_demande = :TYPE ");
			sb.append(" group by ed2.id_demande ");
		sb.append(" ) ");
		sb.append("and ed.id_ref_etat in ( :SAISIE, :VISEE_F, :VISEE_D, :APPROUVE, :EN_ATTENTE, :VALIDEE, :PRISE ) ");
		// #14812 SIRH - PARAMETRES - CONGES EXCEPTIONNELS
		if (null != idDemande) {
			sb.append("and dce.id_demande <> :idDemande ");
		}
		
		Query q = absEntityManager.createNativeQuery(sb.toString());
		q.setParameter("IDAGENT", idAgentConcerne);
		q.setParameter("TYPE", idRefType);
		q.setParameter("FROMDATE", fromDate);
		q.setParameter("TODATE", toDate);
		q.setParameter("SAISIE", RefEtatEnum.SAISIE.getCodeEtat());
		q.setParameter("VISEE_F", RefEtatEnum.VISEE_FAVORABLE.getCodeEtat());
		q.setParameter("VISEE_D", RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat());
		q.setParameter("APPROUVE", RefEtatEnum.APPROUVEE.getCodeEtat());
		q.setParameter("PRISE", RefEtatEnum.PRISE.getCodeEtat());
		q.setParameter("VALIDEE", RefEtatEnum.VALIDEE.getCodeEtat());
		q.setParameter("EN_ATTENTE", RefEtatEnum.EN_ATTENTE.getCodeEtat());

		if (null != idDemande) {
			q.setParameter("idDemande", idDemande);
		}
		
		BigDecimal result = (BigDecimal) q.getSingleResult();
		
		if(null != result) 
			return result.doubleValue();
		
		return 0.0;
	}

}
