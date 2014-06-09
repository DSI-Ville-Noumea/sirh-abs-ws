package nc.noumea.mairie.abs.repository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import nc.noumea.mairie.domain.SpSold;
import nc.noumea.mairie.domain.Spadmn;
import nc.noumea.mairie.domain.Spcarr;

import org.springframework.stereotype.Repository;

@Repository
public class SirhRepository implements ISirhRepository {

	@PersistenceContext(unitName = "sirhPersistenceUnit")
	private EntityManager sirhEntityManager;

	@Override
	public SpSold getSpsold(Integer idAgent) {
		Integer nomatr = Integer.valueOf(idAgent.toString().substring(3, idAgent.toString().length()));
		return sirhEntityManager.find(SpSold.class, nomatr);
	}

	@Override
	public Spadmn getAgentCurrentPosition(Integer nomatr, Date asOfDate) {
		TypedQuery<Spadmn> qSpadmn = sirhEntityManager.createNamedQuery("getAgentSpadmnAsOfDate", Spadmn.class);
		qSpadmn.setParameter("nomatr", nomatr);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		int dateFormatMairie = Integer.valueOf(sdf.format(asOfDate));
		qSpadmn.setParameter("dateFormatMairie", dateFormatMairie);

		if (0 == qSpadmn.getResultList().size()) {
			return null;
		}

		Spadmn adm = qSpadmn.getSingleResult();

		return adm;
	}

	@Override
	public Spcarr getAgentCurrentCarriere(Integer nomatr, Date asOfDate) {

		TypedQuery<Spcarr> qCarr = sirhEntityManager.createNamedQuery("getCurrentCarriere", Spcarr.class);
		qCarr.setParameter("nomatr", nomatr);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		int dateFormatMairie = Integer.valueOf(sdf.format(asOfDate));
		qCarr.setParameter("todayFormatMairie", dateFormatMairie);

		List<Spcarr> result = qCarr.getResultList();

		if (result.size() != 1)
			return null;

		return result.get(0);
	}
}
