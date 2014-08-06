package nc.noumea.mairie.abs.repository;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nc.noumea.mairie.abs.domain.RefEtatEnum;

import org.springframework.stereotype.Repository;

@Repository
public class CongesExceptionnelsRepository implements ICongesExceptionnelsRepository {

	@PersistenceContext(unitName = "absPersistenceUnit")
    private EntityManager absEntityManager;
	
	@Override
	public Double countDureeByPeriodeAndTypeDemande(Integer idAgentConcerne, Date fromDate,
			Date toDate, Integer idRefType) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("select sum(dce.duree) from abs_demande_conges_exceptionnels dce ");
		sb.append("inner join abs_demande d on d.id_demande = dce.id_demande and d.id_type_demande = :TYPE "
				+ "and d.date_debut between :FROMDATE and :TODATE ");
		sb.append("inner join abs_etat_demande ed on d.id_demande = ed.id_demande ");
		sb.append("where ed.id_etat_demande in ( select max(ed2.id_etat_demande) from abs_etat_demande ed2 group by ed2.id_demande ) ");
		sb.append("and ed.id_ref_etat in ( :SAISIE, :VISEE_F, :VISEE_D, :APPROUVE, :EN_ATTENTE, :VALIDEE, :PRISE ) ");

		BigDecimal result = (BigDecimal) absEntityManager.createNativeQuery(sb.toString())
				.setParameter("TYPE", idRefType)
				.setParameter("FROMDATE", fromDate)
				.setParameter("TODATE", toDate)
				.setParameter("SAISIE", RefEtatEnum.SAISIE.getCodeEtat())
				.setParameter("VISEE_F", RefEtatEnum.VISEE_FAVORABLE.getCodeEtat())
				.setParameter("VISEE_D", RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat())
				.setParameter("APPROUVE", RefEtatEnum.APPROUVEE.getCodeEtat())
				.setParameter("PRISE", RefEtatEnum.PRISE.getCodeEtat())
				.setParameter("VALIDEE", RefEtatEnum.VALIDEE.getCodeEtat())
				.setParameter("EN_ATTENTE", RefEtatEnum.EN_ATTENTE.getCodeEtat()).getSingleResult();
		
		if(null != result) 
			return result.doubleValue();
		
		return 0.0;
	}

}
