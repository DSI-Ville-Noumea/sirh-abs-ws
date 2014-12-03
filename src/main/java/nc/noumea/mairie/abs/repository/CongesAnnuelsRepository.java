package nc.noumea.mairie.abs.repository;

import java.math.BigDecimal;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nc.noumea.mairie.abs.domain.RefEtatEnum;

import org.springframework.stereotype.Repository;

@Repository
public class CongesAnnuelsRepository implements ICongesAnnuelsRepository {

	@PersistenceContext(unitName = "absPersistenceUnit")
	private EntityManager absEntityManager;

	@Override
	public Double getSommeDureeDemandeCongeAnnuelEnCoursSaisieouViseeouAValider(Integer idAgent, Integer idDemande) {

		StringBuilder sb = new StringBuilder();
		sb.append("select sum(dce.duree) from abs_demande_conges_annuels dce ");
		sb.append("inner join abs_demande d on d.id_demande = dce.id_demande and d.id_agent = :idAgent ");
		sb.append("inner join abs_etat_demande ed on dce.id_demande = ed.id_demande ");
		sb.append("where ed.id_etat_demande in ( select max(ed2.id_etat_demande) from abs_etat_demande ed2 group by ed2.id_demande ) ");
		sb.append("and ed.id_ref_etat in ( :SAISIE, :VISEE_F, :VISEE_D) ");
		// TODO ajouter le nouveau statut "A vailder"
		// sb.append("and ed.id_ref_etat in ( :SAISIE, :VISEE_F, :VISEE_D, :A_VALIDER) ");

		BigDecimal result = (BigDecimal) absEntityManager.createNativeQuery(sb.toString())
				.setParameter("idAgent", idAgent).setParameter("SAISIE", RefEtatEnum.SAISIE.getCodeEtat())
				.setParameter("VISEE_F", RefEtatEnum.VISEE_FAVORABLE.getCodeEtat())
				.setParameter("VISEE_D", RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat()).getSingleResult();
		// TODO ajouter le nouveau statut "A vailder"
		// q.setParameter("A_VALIDER", RefEtatEnum.A_VALIDER);

		if (null != result)
			return result.doubleValue();

		return 0.0;
	}
}
