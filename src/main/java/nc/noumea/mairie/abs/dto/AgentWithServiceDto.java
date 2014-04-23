package nc.noumea.mairie.abs.dto;

import nc.noumea.mairie.sirh.domain.Agent;

public class AgentWithServiceDto extends AgentDto {

	private String service;
	private String codeService;
	private String statut;
	private String direction;

	public AgentWithServiceDto() {

	}

	public AgentWithServiceDto(Agent agent) {
		super(agent);
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getCodeService() {
		return codeService;
	}

	public void setCodeService(String codeService) {
		this.codeService = codeService;
	}

	public String getStatut() {
		return statut;
	}

	public void setStatut(String statut) {
		this.statut = statut;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}
}
