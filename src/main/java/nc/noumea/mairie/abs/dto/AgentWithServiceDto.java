package nc.noumea.mairie.abs.dto;

public class AgentWithServiceDto extends AgentDto {

	private String service;
	private Integer idServiceADS;
	private String statut;
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

	public String getStatut() {
		return statut;
	}

	public void setStatut(String statut) {
		this.statut = statut;
	}

	public String getSigleService() {
		return sigleService;
	}

	public void setSigleService(String sigleService) {
		this.sigleService = sigleService;
	}

	public Integer getIdServiceADS() {
		return idServiceADS;
	}

	public void setIdServiceADS(Integer idServiceADS) {
		this.idServiceADS = idServiceADS;
	}

}
