package nc.noumea.mairie.abs.dto;

public class AgentWithServiceDto extends AgentDto {

	private String service;
	private String codeService;
	private String statut;
	private String direction;
	private String sigleService;

	public AgentWithServiceDto() {

	}

	public AgentWithServiceDto(AgentGeneriqueDto agent) {
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

	public String getSigleService() {
		return sigleService;
	}

	public void setSigleService(String sigleService) {
		this.sigleService = sigleService;
	}
	
}
