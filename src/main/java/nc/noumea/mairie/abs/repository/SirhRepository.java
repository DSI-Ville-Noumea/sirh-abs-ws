package nc.noumea.mairie.abs.repository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;

import nc.noumea.mairie.domain.SpSold;
import nc.noumea.mairie.domain.SpSorc;
import nc.noumea.mairie.domain.Spadmn;
import nc.noumea.mairie.domain.Spcarr;
import nc.noumea.mairie.domain.Spcc;
import nc.noumea.mairie.domain.Spmatr;

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
	public void persistEntity(Object obj) {
		sirhEntityManager.persist(obj);
	}

	@Override
	public void removeEntity(Object obj) {
		sirhEntityManager.remove(obj);
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
	public List<Spadmn> getPA50OfAgent(Integer nomatr, Date dateDerniereEmbauche) {
		
		TypedQuery<Spadmn> qSpadmn = sirhEntityManager.createNamedQuery("getPA50OfAgent", Spadmn.class);
		qSpadmn.setParameter("nomatr", nomatr);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		int dateFormatMairie = Integer.valueOf(sdf.format(dateDerniereEmbauche));
		qSpadmn.setParameter("dateFormatMairie", dateFormatMairie);

		if (qSpadmn.getResultList().isEmpty()) {
			return null;
		}

		return qSpadmn.getResultList();
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

	@Override
	public Spmatr findSpmatrForAgent(Integer nomatr) {
		return sirhEntityManager.find(Spmatr.class, nomatr);
	}

	@Override
	public Spcc getSpcc(Integer nomatr, Date asOfDate) {
		TypedQuery<Spcc> query = sirhEntityManager.createNamedQuery("getSpccByNomatrAndDate", Spcc.class);
		query.setParameter("nomatr", nomatr);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		int dateFormatMairie = Integer.valueOf(sdf.format(asOfDate));
		query.setParameter("dateFormatMairie", dateFormatMairie);

		if (0 == query.getResultList().size()) {
			return null;
		}

		return query.getSingleResult();
	}

	@Override
	public Spcc getSpcc(Integer nomatr, Date asOfDate, Integer code) {
		TypedQuery<Spcc> query = sirhEntityManager.createNamedQuery("getSpccByNomatrAndDateAndCode", Spcc.class);
		query.setParameter("nomatr", nomatr);
		query.setParameter("code", code);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		int dateFormatMairie = Integer.valueOf(sdf.format(asOfDate));
		query.setParameter("dateFormatMairie", dateFormatMairie);

		if (0 == query.getResultList().size()) {
			return null;
		}

		return query.getSingleResult();
	}

	@Override
	public SpSorc getSpsorc(Integer idAgent) {
		Integer nomatr = Integer.valueOf(idAgent.toString().substring(3, idAgent.toString().length()));
		return sirhEntityManager.find(SpSorc.class, nomatr);
	}
}
