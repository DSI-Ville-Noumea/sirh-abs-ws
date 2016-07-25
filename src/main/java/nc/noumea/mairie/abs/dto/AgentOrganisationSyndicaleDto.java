package nc.noumea.mairie.abs.dto;

import nc.noumea.mairie.abs.domain.AgentA48OrganisationSyndicale;
import nc.noumea.mairie.abs.domain.AgentA54OrganisationSyndicale;
import nc.noumea.mairie.abs.domain.AgentOrganisationSyndicale;

public class AgentOrganisationSyndicaleDto {

	private Integer idAgent;
	private boolean actif;

	public AgentOrganisationSyndicaleDto() {

	}

	public AgentOrganisationSyndicaleDto(AgentOrganisationSyndicale ag) {
		this.idAgent = ag.getIdAgent();
		this.actif = ag.isActif();
	}

	public AgentOrganisationSyndicaleDto(AgentA54OrganisationSyndicale ag) {
		this.idAgent = ag.getIdAgent();
	}

	public AgentOrganisationSyndicaleDto(AgentA48OrganisationSyndicale ag) {
		this.idAgent = ag.getIdAgent();
	}

	public Integer getIdAgent() {
		return idAgent;
	}

	public void setIdAgent(Integer idAgent) {
		this.idAgent = idAgent;
	}

	public boolean isActif() {
		return actif;
	}

	public void setActif(boolean actif) {
		this.actif = actif;
	}
}
