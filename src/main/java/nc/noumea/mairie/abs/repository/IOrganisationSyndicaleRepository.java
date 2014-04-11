package nc.noumea.mairie.abs.repository;

import java.util.List;

import nc.noumea.mairie.abs.domain.OrganisationSyndicale;

public interface IOrganisationSyndicaleRepository {

	List<OrganisationSyndicale> findAllOrganisation();

	List<OrganisationSyndicale> findAllOrganisationActives();

	void persistEntity(Object obj);

	<T> T getEntity(Class<T> Tclass, Object Id);
}
