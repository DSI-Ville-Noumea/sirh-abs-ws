package nc.noumea.mairie.abs.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "ABS_A48_AGENT_ORGANISATION_SYNDICALE")
@PersistenceUnit(unitName = "absPersistenceUnit")
public class AgentA48OrganisationSyndicale {

	@Id
	@Column(name = "ID_A48_AGENT_ORGANISATION_SYNDICALE")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idA48AgentOrganisationSyndicale;

	@NotNull
	@Column(name = "ID_AGENT")
	private Integer idAgent;

	@OneToOne(optional = true)
	@JoinColumn(name = "ID_ORGANISATION_SYNDICALE")
	private OrganisationSyndicale organisationSyndicale;

	public Integer getIdA48AgentOrganisationSyndicale() {
		return idA48AgentOrganisationSyndicale;
	}

	public void setIdA48AgentOrganisationSyndicale(Integer idA48AgentOrganisationSyndicale) {
		this.idA48AgentOrganisationSyndicale = idA48AgentOrganisationSyndicale;
	}

	public Integer getIdAgent() {
		return idAgent;
	}

	public void setIdAgent(Integer idAgent) {
		this.idAgent = idAgent;
	}

	public OrganisationSyndicale getOrganisationSyndicale() {
		return organisationSyndicale;
	}

	public void setOrganisationSyndicale(OrganisationSyndicale organisationSyndicale) {
		this.organisationSyndicale = organisationSyndicale;
	}

}
