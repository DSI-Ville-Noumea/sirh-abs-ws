package nc.noumea.mairie.abs.repository;

import java.util.List;

import nc.noumea.mairie.abs.domain.AgentOrganisationSyndicale;
import nc.noumea.mairie.abs.domain.OrganisationSyndicale;

public interface IOrganisationSyndicaleRepository {

	List<OrganisationSyndicale> findAllOrganisation();

	List<OrganisationSyndicale> findAllOrganisationActives();

	void persistEntity(Object obj);

	<T> T getEntity(Class<T> Tclass, Object Id);

	List<AgentOrganisationSyndicale> getListeAgentOrganisation(Integer idOrganisationSyndicale);

	List<AgentOrganisationSyndicale> getAgentOrganisation(Integer idAgent);

	AgentOrganisationSyndicale getAgentOrganisation(Integer idAgent, Integer idOrganisationSyndicale);

	List<AgentOrganisationSyndicale> getAgentOrganisationActif(Integer idAgent);
}
