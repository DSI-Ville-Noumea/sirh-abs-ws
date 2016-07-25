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
@Table(name = "ABS_A54_AGENT_ORGANISATION_SYNDICALE")
@PersistenceUnit(unitName = "absPersistenceUnit")
public class AgentA54OrganisationSyndicale {

	@Id
	@Column(name = "ID_A54_AGENT_ORGANISATION_SYNDICALE")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idA54AgentOrganisationSyndicale;

	@NotNull
	@Column(name = "ID_AGENT")
	private Integer idAgent;

	@OneToOne(optional = true)
	@JoinColumn(name = "ID_ORGANISATION_SYNDICALE")
	private OrganisationSyndicale organisationSyndicale;

	public Integer getIdA54AgentOrganisationSyndicale() {
		return idA54AgentOrganisationSyndicale;
	}

	public void setIdA54AgentOrganisationSyndicale(Integer idA54AgentOrganisationSyndicale) {
		this.idA54AgentOrganisationSyndicale = idA54AgentOrganisationSyndicale;
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
