package nc.noumea.mairie.abs.repository;

import java.util.List;

import nc.noumea.mairie.abs.domain.AgentA48OrganisationSyndicale;
import nc.noumea.mairie.abs.domain.AgentA54OrganisationSyndicale;
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

	List<OrganisationSyndicale> getListOSCounterForA52();

	List<AgentA54OrganisationSyndicale> getListeAgentA54Organisation(Integer idOrganisationSyndicale);

	List<AgentA48OrganisationSyndicale> getListeAgentA48Organisation(Integer idOrganisationSyndicale);

	List<AgentA54OrganisationSyndicale> getAgentA54Organisation(Integer idAgent);

	List<AgentA48OrganisationSyndicale> getAgentA48Organisation(Integer idAgent);
}
