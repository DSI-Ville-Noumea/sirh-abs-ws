package nc.noumea.mairie.abs.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nc.noumea.mairie.abs.domain.OrganisationSyndicale;

import org.springframework.stereotype.Repository;

@Repository
public class OrganisationSyndicaleRepository implements IOrganisationSyndicaleRepository {

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
	public List<OrganisationSyndicale> findAllOrganisation() {
		return absEntityManager.createQuery("SELECT o FROM OrganisationSyndicale o", OrganisationSyndicale.class)
				.getResultList();
	}

	@Override
	public List<OrganisationSyndicale> findAllOrganisationActives() {
		return absEntityManager.createQuery("SELECT o FROM OrganisationSyndicale o where o.actif = true",
				OrganisationSyndicale.class).getResultList();
	}
}
